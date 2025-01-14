# Intersmash - Cloud-native testing with Java

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

## Next steps
- See how to [configure Intersmash for running tests on OpenShift and Kubernetes, or both](./docs/CLOUD_TARGET.md).
- For usage examples, see [Getting Started](./docs/GETTING_STARTED.md).

## Framework Design

Intersmash provides a set of annotations, APIs and application service contracts the developer uses to describe the test scenario, within the test class.
This information is used by the framework to deploy the services and to manage their
life-cycle orchestration directly from the test class.
A curated list of service "provisioners" is provided to cover the most common use cases, for example provisioning the Kafka Operator, or a PostgreSql service via templates, or a s2i build and deploy a WildFly application.
The full list of provided services can be found [here](./docs/Provisioner-by-Product.md).

### Components

There are three main components that make up the framework:

* **annotations**: Intersmash annotations are used to declare the
  *product components* that make up the test scenario.
  These statements can be applied to the test class and its fields. 

  There are four annotations
  - **@Intersmash**: applied to the test class level definition, contains a list of identified services that make up the test scenario.
  - **@Service**:  the class reference to an individual service
  - **@ServiceUrl**: applied to a given test class field, injects into the test class as a String or URL the address of a service
  - **@ServiceProvisioner**: applied to a given test class field, injects into the test class a provisioner that enables control over platform-specific actions or example, scaling the deployment up and down.


* **applications**: An Intersmash provided interface that describes
  the configuration and specific properties of a given service.  For every supported *product component*
  there is a corresponding "application" interface class.  The Framework's naming convention for these classes is *ProductComponentName*Application.java. (e.g. BootableJarOpenShiftApplication.java, ActiveMQOperatorApplication.java HelmChartOpenShiftApplication.java, ... etc)
  The interface provides default values for most of the service's configuration information.
  The user is required to create an implementation class of the interface and
  provide some select information.  It is this user created implemented class that is declared in the annotations, @Service, @ServiceUrl, and @ServiceProvisioner.


* **provisioners**: Classes that take care of deploying, undeploying, managing the
  life-cycle orchestration of services, and control the specific workflow that
  needs to be executed for the test scenario.  The user has no direct interaction
  with these classes.
  See [the Intersmash provisioning documentation](./provisioners/README.md)
  to learn about supported services provisioners.

### Properties
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

* Run the testsuite against community deliverables, e.g.: Keycloak operator or WildFly images:
```shell
mvn test -pl testsuite/ -Pts.execution-profile.community
```
This is usually executed to test community images, deliverables and for application servers like WildFly - deployments built from community artifacts.

* Run the testsuite against productised deliverables, e.g.: Red Hat Single Sign On operator or JBoss EAP images:
```shell
mvn test -pl testsuite/ -Pts.execution-profile.prod
```
This is usually executed to test productised images, deliverables and - for application servers like JBoss EAP - deployments built from community artifacts.


### Project Modules
* **core** - core interfaces, annotations and extensions.
* **provisioners** - implementations of interfaces from `core` for selected services.
* **testsuite** - tests that verify the integration with OpenShift. (**Note:**  OpenShift is needed in order to run them.)
  * **test-deployments** - sources for shared deployments which are used by the testsuite.
    * **deployments-provider** - provides support to get path to compiled deployments
* **examples** - Test samples that illustrate the use of Intersmash.

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
| Infinispan                       | 15.1.1.Final                 | Or _default_ provided by the default Infinispan Operator `stable` channel                                                                                                      |
| Red Hat Data Grid                | 8.5.2.GA                     | Or _default_ provided by the Red Hat DataGrid Operator  `stable` channel                                                                                                       |
|                                  |                              |                                                                                                                                                                                |
| Kafka provided by Strimzi        | 3.9.0                        | Provided by the Strimzi Operator `stable` channel                                                                                                                              |
| Streams for Apache Kafka         | 3.8.0.redhat-00007           | Or _default_, as provided by the Red Hat Streams for Apache Kafka Operator `stable` channel                                                                                    |
|                                  |                              |                                                                                                                                                                                |
| Keycloak                         | 26.0.7                       | Or _default_, as provided by default by the Keycloak Operator `fast` channel                                                                                                   |
| Red Hat Build of keycloak (RHBK) | 26.0.7.redhat-00001          | Or _latest_ in the `:26.0` tag image stream, see registry.redhat.io/rhbk/keycloak-rhel9                                                                                        |
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
