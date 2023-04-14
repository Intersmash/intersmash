#!/bin/bash

#!/usr/bin/env bash
# exit immediately when a command fails
set -e
# only exit with zero if all commands of the pipeline exit successfully
set -o pipefail
# error on unset variables
set -u
# print each command before executing it
set -x

printenv KUBECONFIG
printenv KUBEADMIN_PASSWORD_FILE
oc get node
oc config view
oc whoami

export TEST_ADMIN_USERNAME=tadmin
export TEST_ADMIN_PASSWORD=adminpwd

export TEST_USER_USERNAME=tadmin
export TEST_USER_PASSWORD=userpwd

export HTPASSWD_FILE=users.htpasswd
htpasswd -c -B -b $HTPASSWD_FILE $TEST_ADMIN_USERNAME $TEST_ADMIN_PASSWORD
htpasswd -b $HTPASSWD_FILE $TEST_USER_USERNAME $TEST_USER_PASSWORD
cat $HTPASSWD_FILE
oc create secret generic htpass-secret-2 --from-file=htpasswd=$HTPASSWD_FILE -n openshift-config
oc patch OAuth cluster -p '{"spec": {"identityProviders": [{"htpasswd": {"fileData": {"name": "htpass-secret-2"}},"mappingMethod": "claim","name": "my_htpasswd_provider","type": "HTPasswd"}]}}' --type=merge
oc adm policy add-cluster-role-to-user cluster-admin $TEST_ADMIN_USERNAME

export TEST_CLUSTER_URL=$(oc whoami --show-server)
export TEST_NAMESPACE=intersmash-test

oc new-project $TEST_NAMESPACE

cat >> test.properties <<EOL
xtf.openshift.url=${TEST_CLUSTER_URL}
xtf.openshift.namespace=${TEST_NAMESPACE}-builds
xtf.bm.namespace=${TEST_NAMESPACE}
xtf.openshift.admin.username=${TEST_ADMIN_USERNAME}
xtf.openshift.admin.password=${TEST_ADMIN_PASSWORD}
xtf.openshift.master.username=${TEST_USER_USERNAME}
xtf.openshift.master.password=${TEST_USER_PASSWORD}
xtf.openshift.admin.kubeconfig=${KUBECONFIG}
EOL

cat test.properties

mkdir local-repo
mvn clean install -Dmaven.repo.local=./local-repo -DskipTests
mvn clean test -Dmaven.repo.local=./local-repo -pl testsuite/ -am
