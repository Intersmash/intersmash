# Intersmash - Cloud-native testing with Java

![Simple build workflow](https://github.com/Intersmash/intersmash/actions/workflows/simple-build.yml/badge.svg)
![Kubernetes E2E Tests](https://github.com/Intersmash/intersmash/actions/workflows/kubernetes-e2e.yml/badge.svg)

Intersmash is a Java library that makes it easy to automate the
provisioning and execution of tests in cloud-native environments.
It helps the user prototype and test complex interoperability scenarios on
kubernetes compliant cloud-native environments and platforms, most notably OpenShift.
Kubernetes support is being introduced. Support for other platforms (Bare-metal etc.) could be added
later on demand.

Intersmash is designed with these principles.  
* It is integrated with JUnit.
* It handles all the plumbing (i.e. provisioning) of the cloud container. It deploys the test
scenario *product components*, manages their life-cycle orchestration, and
undeploys and cleans up the environment on test completion.  *(The term services and product components will be used interchangeably.)*
* Intersmash is designed to be extensible.  If there is a cloud service not yet implemented,
the user can easily create an implementation and integrate it into the framework.
* The framework is cloud implementation agnostic.  It will be possible *(in the future)*
to test on different Kubernetes compliant cloud implementations.  This will
ensure application portability.

## Supported services

Intersmash aims at providing tools for provisioning up to the latest stable release generally
available.
The following table is a best-effort tracker for the version that each supported service has been tested with.
That being said, since each service could be provisioned via different means - like templates and operators -
having their own release cadence - it _could_ happen that a service version is provisioned which is not the one listed.

Feel free to submit an issue in such a case, Intersmash welcomes community contributions to keep the tooling up to date.

| Service                          | Supported version            | Notes                                                                                                                                                                          |
|:---------------------------------|:-----------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ActiveMQ Artemis                 | 2.38.0                       | The one provided by the custom index image, i.e. quay.io/jbossqe-eap/intersmash-activemq-operator-index:1.2.9, which lists to images in https://quay.io/organization/arkmq-org |
| Red Hat AMQ Broker               | 7.12.z                       | Or _latest_ in the `:7.12` tag image stream, see registry.redhat.io/amq7/amq-broker-init-rhel8                                                                                 |
|                                  |                              |                                                                                                                                                                                |
| Infinispan                       | 15.1.3.Final                 | Or _default_ provided by the default Infinispan Operator `stable` channel                                                                                                      |
| Red Hat Data Grid                | 8.5.2.GA                     | Or _default_ provided by the Red Hat DataGrid Operator  `stable` channel                                                                                                       |
|                                  |                              |                                                                                                                                                                                |
| Kafka provided by Strimzi        | 3.9.0                        | Provided by the Strimzi Operator `stable` channel                                                                                                                              |
| Streams for Apache Kafka         | 3.8.0.redhat-00007           | Or _default_, as provided by the Red Hat Streams for Apache Kafka Operator `stable` channel                                                                                    |
|                                  |                              |                                                                                                                                                                                |
| Keycloak                         | 26.0.8                       | Or _default_, as provided by default by the Keycloak Operator `fast` channel                                                                                                   |
| Red Hat Build of keycloak (RHBK) | 26.0.8.redhat-00001          | Or _latest_ in the `:26.0` tag image stream, see registry.redhat.io/rhbk/keycloak-rhel9                                                                                        |
| Red Hat SSO - **DEPRECATED**     | 7.6.z                        | The _latest_ in the `:7.6` tag image stream, see registry.redhat.io/rh-sso-7/sso76-openshift-rhel8:7.6                                                                         |
|                                  |                              |                                                                                                                                                                                |
| WildFly                          | 32.0.0.Final                 |                                                                                                                                                                                |                                                      
| Red Hat JBoss EAP 8              | JBoss EAP 8.0.x (and XP 5.x) |                                                                                                                                                                                |
| Red Hat JBoss EAP 7              | JBoss EAP 7.4.z (and XP 4.z) |                                                                                                                                                                                |
|                                  |                              |                                                                                                                                                                                |
| Hyperfoil                        | 0.24.2                       | Supports provisioning via the Operator, both on **Kubernetes** and **OpenShift**                                                                                               | 
|                                  |                              |                                                                                                                                                                                |
| Open Data Hub                    | 2.22.0                       | Supports provisioning on OpenShift via the Operator                                                                                                                            |

Since multiple deliverables can be bound to a given service version, e.g.: container images, operator CRs, or Helm Charts,
more information can be found in [the provisioners' documentation](./provisioners/README.md), or in the resources there linked.

## Next steps
- See how to [configure Intersmash for running tests on OpenShift and Kubernetes, or both](./docs/CLOUD_TARGET.md).
- For usage examples, see [Getting Started](./docs/GETTING_STARTED.md).
- More usage and technical details in [Building blocks](./docs/BUILDING_BLOCKS.md)


## Configuration
[XTF](https://github.com/xtf-cz/xtf) is used by Intersmash to communicate
with OpenShift.  This tool provides a means to pass configuration properties
from Intersmash to OpenShift.
Intersmash also uses class, [XTFConfig](https://github.com/xtf-cz/xtf/blob/master/core/src/main/java/cz/xtf/core/config/XTFConfig.java),
internally.
XTF supports four ways to provide properties. In priority order they are,

- **System properties**
- **Environment variables**
- **test.properties file**: residing by default in the root of the project with the goal to contain
  user specific setup.
- **global-test.properties file**: residing by default in the root of the project with the goal to contain
  a shared setup.

A table of properties Intersmash uses to configure and execute tests can be
found [here](./docs/XTF-Configuration-Properties.md).


## Maven Dependencies
Two maven archives are needed to build and run with Intersmash.
```xml
<dependencies>
  <!-- contains the Intersmash core annotations, contracts and APIs -->
  <dependency>
    <groupId>org.jboss.intersmash</groupId>
    <artifactId>intersmash-core</artifactId>
  </dependency>
  <!-- provisioning implementations and components-->
  <dependency>
      <groupId>org.jboss.intersmash</groupId>
      <artifactId>intersmash-provisioners</artifactId>
  </dependency>
</dependencies>
```

## Building Intersmash

### JDK version
JDK-11 is the current version this project supports.

Build the project without running the tests
```shell
mvn clean install -DskipTests
```

### Running the Intersmash testsuite

**Note:** to run the Intersmash testsuite Openshift and XTF properties files with corresponding configuration information are needed. See the Framework Design section above.

* Run the testsuite against community deliverables, e.g.: the Keycloak operator, or WildFly images:
```shell
mvn test -pl testsuite/ -Pts.execution-profile.community
```
This is usually executed to test community images and deliverables. 
Regarding the _deployments_ used by the testsuite, by default those are built out of community (i.e. WildFly) bits.

* Run the testsuite against the product variant deliverables of a community project, e.g.: the Red Hat Build of Keycloak 
operator, or JBoss EAP images:
```shell
mvn test -pl testsuite/ -Pts.execution-profile.prod
```
This is usually executed to test product based image and deliverables.
In such a case the EAP or EAP XP _deployment applications_ used by the test suite should be built out of product grade 
artifacts, which is done by enabling the `ts.wildfly.target-distribution.eap` or 
`ts.wildfly.target-distribution.eapxp` profiles, respectively, e.g.:

```shell
mvn test -pl testsuite/ -Pts.execution-profile.prod -Pts.wildfly.target-distribution.eap
```

Regarding EAP _minor_ version, Intersmash supports provisioning both EAP 8.0 and EAP 8.1, and the deployments used by the
Intersmash testsuite can be built on related artifacts and feature packs by enabling either the `ts.eap-stream.eap80`,
or the `ts.eap-stream.eap81` profile, e.g.:

```shell
mvn test -pl testsuite/ -Pts.execution-profile.prod -Pts.wildfly.target-distribution.eap -Pts.eap-stream.eap80
```

Regarding EAP XP _major_ version, Intersmash supports provisioning both XP 5 and XP 6, and the deployments used by the 
Intersmash testsuite can be built on related artifacts and feature packs by enabling either the `ts.eapxp-stream.xp5`, 
or the `ts.eapxp-stream.xp6` profile, e.g.:

```shell
mvn test -pl testsuite/ -Pts.execution-profile.prod -Pts.wildfly.target-distribution.eapxp -Pts.eapxp-stream.xp5
```

* Run Kubernetes integration tests:
```shell
mvn test -pl testsuite/ -Pts.execution-profile.community -Pts.k8s 
```
This is useful when running the testsuite against a Kubernetes cluster, so that only Kubernetes integration test will be 
executed, while OpenShift ones will be skipped.

* Run OpenShift integration tests:
```shell
mvn test -pl testsuite/ -Pts.execution-profile.community -Pts.openshift 
```
This is useful when running the testsuite against an OpenShift cluster, so that only OpenShift integration test will be
executed, while Kubernetes ones will be skipped.

## Platforms

Intersmash is designed to allow executions on different Kubernetes compliant platforms.
At the moment we are fully focused on supporting provisioning implementation for OpenShift, running on Linux support. 
We welcome community contributions to other Kubernetes implementations.


## Future goals
* Full support for OpenShift AI provisioning
* Documentation and examples enhancements

see the [current milestone](https://github.com/Intersmash/intersmash/milestone/2) for a complete list of issues.

## Issue tracking
To tack or report an issue see [GitHub issue](https://github.com/Intersmash/intersmash/issues).

When reporting an issue please choose the proper template and fill it with the required information.

## Contributing
Intersmash is an open source community project.  We welcome participation and contributions.
See [Intersmash contributing guidelines](CONTRIBUTING.md).
