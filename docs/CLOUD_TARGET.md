# Conditional test execution based on test platforms

## Target test platform
Each test that is annotated by the `@Intersmash` annotation will be managed by the `Intersmash` extension,
which relies on JUnit5 Jupiter.

The `Intersmash` extension will load the relevant provisioner dynamically, based on the
_[Application](../core/src/main/java/org/jboss/intersmash/application/Application.java) descriptor_ type.

Specifically, application descriptors that leverage Operator based provisioning, i.e. those implementing of
[OperatorApplication](../core/src/main/java/org/jboss/intersmash/application/operator/OperatorApplication.java), can either
implement the [OpenShiftApplication](../core/src/main/java/org/jboss/intersmash/application/openshift/OpenShiftApplication.java)
or the [KubernetesApplication](../core/src/main/java/org/jboss/intersmash/application/openshift/OpenShiftApplication.java) interface additionally, to let the `Intersmash` extension select the related OpenShift or Kubernetes provisioner.

**IMPORTANT**: _Intersmash support for provisioning on Kubernetes is currently limited to the following provisioners_:

- Hyperfoil


Intersmash tests should be executed conditionally, (e.g.: via the Maven Surefire plugin exclusions) in order to
be aligned with the targeted environment, see for instance [the OpenShiftTest annotation](../testsuite/integration-tests/src/main/java/org/jboss/intersmash/testsuite/junit5/categories/OpenShiftTest.java), which
is used to enable the Intersmash testsuite integration tests selectively.
