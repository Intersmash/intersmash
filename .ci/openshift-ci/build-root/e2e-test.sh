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

# For some reason the creation of a cluster admin and a user authentication mechanism does not allow for immediate
# login, even though the oc commands don't report any error.
# This doesn't let us get a token, and XTF won't take the xtf.openshift.admin.kubeconfig and
# xtf.openshift.master.kubeconfig properties into account when the token is not present.
# Eventually this turns out to be the condition that prevents the oc binary commands to succeed, because they would
# use a tmp oc.config file which is not found on the build cluster filesystem.
# This is why we login with kubeadmin, get a token and use its full set of credentials (username, password and token)
# both for xtf.openshift.admin.* and xtf.openshift.master.* properties
export SYSADMIN_USERNAME=kubeadmin
export SYSADMIN_PASSWORD=$(cat "${KUBEADMIN_PASSWORD_FILE}")
export TEST_CLUSTER_URL=$(oc whoami --show-server)
oc login "${TEST_CLUSTER_URL}" -u "${SYSADMIN_USERNAME}" -p "${SYSADMIN_PASSWORD}" --insecure-skip-tls-verify=true
export SYSADMIN_TOKEN=$(oc whoami -t)

export TEST_NAMESPACE=intersmash-test
oc new-project $TEST_NAMESPACE

cat >> test.properties <<EOL
xtf.openshift.url=${TEST_CLUSTER_URL}
xtf.openshift.namespace=${TEST_NAMESPACE}
xtf.bm.namespace=${TEST_NAMESPACE}-builds
xtf.openshift.admin.username=${SYSADMIN_USERNAME}
xtf.openshift.admin.password=${SYSADMIN_PASSWORD}
xtf.openshift.admin.token=${SYSADMIN_TOKEN}
xtf.openshift.master.username=${SYSADMIN_USERNAME}
xtf.openshift.master.password=${SYSADMIN_PASSWORD}
xtf.openshift.master.token=${SYSADMIN_TOKEN}
xtf.openshift.admin.kubeconfig=${KUBECONFIG}
xtf.openshift.master.kubeconfig=${KUBECONFIG}

# WildFly community operator settings
intersmash.wildfly.operators.catalog_source=community-operators-wildfly-operator
intersmash.wildfly.operators.index_image=quay.io/operatorhubio/catalog:latest
intersmash.wildfly.operators.package_manifest=wildfly
intersmash.wildfly.operators.channel=alpha
EOL

cat test.properties

mkdir local-repo
mvn clean install -Dmaven.repo.local=./local-repo -DskipTests
mvn clean test -Dmaven.repo.local=./local-repo -pl testsuite/ -am
