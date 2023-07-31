#!/bin/bash

#!/usr/bin/env bash
# exit immediately when a command fails
set -e
# avoid exiting with non-zero if any of the pipeline commands fail because we need retries for oc login
#set -o pipefail
# error on unset variables
set -u
# print each command before executing it
set -x

printenv KUBECONFIG
printenv KUBEADMIN_PASSWORD_FILE

oc get node
oc config view

export TEST_CLUSTER_URL=$(oc whoami --show-server)

export SYSADMIN_USERNAME=kubeadmin
export SYSADMIN_PASSWORD=$(cat "${KUBEADMIN_PASSWORD_FILE}")

# note that for some reason it takes a few minutes for this to be loaded by OCP and authentication start working
export TEST_ADMIN_USERNAME=tadmin
export TEST_ADMIN_PASSWORD=adminpwd
export TEST_USER_USERNAME=tuser
export TEST_USER_PASSWORD=userpwd
export HTPASSWD_FILE=users.htpasswd

htpasswd -c -B -b $HTPASSWD_FILE $TEST_ADMIN_USERNAME $TEST_ADMIN_PASSWORD
htpasswd -b $HTPASSWD_FILE $TEST_USER_USERNAME $TEST_USER_PASSWORD

cat $HTPASSWD_FILE

oc create secret generic htpass-secret --from-file=htpasswd=$HTPASSWD_FILE -n openshift-config
oc patch OAuth cluster -p '{"spec": {"identityProviders": [{"htpasswd": {"fileData": {"name": "htpass-secret"}},"mappingMethod": "claim","name": "my_htpasswd_provider","type": "HTPasswd"}]}}' --type=merge

# wait until authentication operator updates auth
sleep 45 # wait until operator notices changes
counter=0
while [ "$(oc get clusteroperator authentication -o custom-columns=STATUS:.status.conditions[1].status | tail -1)" == "True" ]; do
  sleep 5
  echo Waiting for authentication operator to finish processing
  ((counter=counter+1))
  if [ "$counter" == "200" ]; then
    echo "Timeout waiting for authentication operator."
    exit 1
  fi
done

counter=0
until [[ "$(oc login --insecure-skip-tls-verify ${TEST_CLUSTER_URL} -u ${TEST_ADMIN_USERNAME} -p ${TEST_ADMIN_PASSWORD})" =~ "Login successful" ]] || [[ counter++ -ge 80 ]]
do
  sleep 5
done
export ADMIN_TOKEN=$(oc whoami -t)

counter=0
until [[ "$(oc login --insecure-skip-tls-verify ${TEST_CLUSTER_URL} -u ${TEST_USER_USERNAME} -p ${TEST_USER_PASSWORD})" =~ "Login successful" ]] || [[ counter++ -ge 80 ]]
do
  sleep 5
done
export USER_TOKEN=$(oc whoami -t)

oc login --insecure-skip-tls-verify "${TEST_CLUSTER_URL}" -u ${SYSADMIN_USERNAME} -p "${SYSADMIN_PASSWORD}"
oc adm policy add-cluster-role-to-user cluster-admin ${TEST_ADMIN_USERNAME}
# We need to do this since InfinispanOperatorProvisionerTest would fail unless the master account is made
# cluster-admin as well, see https://github.com/Intersmash/intersmash/issues/48
oc adm policy add-cluster-role-to-user cluster-admin ${TEST_USER_USERNAME}

export TEST_NAMESPACE=intersmash-test

export PULL_SECRET_PATH_REDHAT_REGISTRY_IO=/var/run/registry-redhat-io-pull-secret
export PULL_SECRET_FILE_REDHAT_REGISTRY_IO=${PULL_SECRET_PATH_REDHAT_REGISTRY_IO}/pull-secret
export PULL_SECRET_REDHAT_REGISTRY_IO=$(cat "${PULL_SECRET_FILE_REDHAT_REGISTRY_IO}")

cat >> test.properties <<EOL
xtf.openshift.url=${TEST_CLUSTER_URL}
xtf.openshift.namespace=${TEST_NAMESPACE}
xtf.bm.namespace=${TEST_NAMESPACE}-builds
xtf.openshift.admin.username=${TEST_ADMIN_USERNAME}
xtf.openshift.admin.password=${TEST_ADMIN_PASSWORD}
xtf.openshift.admin.token=${ADMIN_TOKEN}
xtf.openshift.master.username=${TEST_USER_USERNAME}
xtf.openshift.master.password=${TEST_USER_PASSWORD}
xtf.openshift.master.token=${USER_TOKEN}
xtf.openshift.admin.kubeconfig=${KUBECONFIG}
xtf.openshift.master.kubeconfig=${KUBECONFIG}
xtf.openshift.pullsecret=${PULL_SECRET_REDHAT_REGISTRY_IO}

EOL

cat test.properties

# start tests
mkdir local-repo
mvn clean install -Dmaven.repo.local=./local-repo -DskipTests -Pwildfly-deployments-build.eap
mvn test -Dmaven.repo.local=./local-repo -pl testsuite/ \
 -Dintersmash.wildfly.image=quay.io/wildfly/wildfly-s2i-jdk17:latest
 -Dintersmash.wildfly.runtime.image=quay.io/wildfly/wildfly-runtime-jdk17:latest \
 -Dintersmash.wildfly.operators.catalog_source=community-operators-wildfly-operator \
 -Dintersmash.wildfly.operators.index_image=quay.io/operatorhubio/catalog:latest \
 -Dintersmash.wildfly.operators.package_manifest=wildfly \
 -Dintersmash.wildfly.operators.channel=alpha \
 -Dintersmash.wildfly.helm.charts.repo=https://github.com/wildfly/wildfly-charts.git \
 -Dintersmash.wildfly.helm.charts.branch=wildfly-2.3.2 \
 -Dintersmash.wildfly.helm.charts.name=wildfly \
 -Dintersmash.activemq.operators.catalog_source=intersmash-activemq-operator-index \
 -Dintersmash.activemq.operators.index_image=quay.io/jbossqe-eap/intersmash-activemq-operator-catalog:v1.0.11 \
 -Dintersmash.activemq.operators.package_manifest=activemq-artemis-operator \
 -Dintersmash.activemq.operators.channel=upstream \
 -Dintersmash.kafka.operators.channel=strimzi-0.29.x \
 -Dintersmash.keycloak.realm_import.image=quay.io/keycloak/keycloak:21.1.1 \
 -Dintersmash.keycloak.realm_import.operators.catalog_source=community-operators \
 -Dintersmash.keycloak.realm_import.operators.index_image=registry.redhat.io/redhat/community-operator-index:v4.12 \
 -Dintersmash.keycloak.realm_import.operators.channel=fast
