## Building blocks

Intersmash provides a set of annotations, APIs and application service contracts the developer uses to describe the test scenario, within the test class.
This information is used by the framework to deploy the services and to manage their
life-cycle orchestration directly from the test class.
A curated list of service "provisioners" is provided to cover the most common use cases, for example provisioning the Kafka Operator, or a PostgreSql service via templates, or a s2i build to deploy a WildFly application.
The full list of provided services can be found [here](./Provisioner-by-Product.md).

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


* **applications**: An Intersmash provided Java interface that describes
  the configuration and specific properties of a given service.  For every supported *product component*
  there is a corresponding "application" interface class.  The framework's naming convention for these classes is *ProductComponentName*Application.java. (e.g. BootableJarOpenShiftApplication.java, ActiveMQOperatorApplication.java HelmChartOpenShiftApplication.java, etc.)
  The interface provides default values for most of the service's configuration information.
  The user is required to create an implementation class of the interface and
  provide some select information.  It is this user created implemented class that is declared in the annotations, @Service, @ServiceUrl, and @ServiceProvisioner.


* **provisioners**: Classes that take care of deploying, undeploying, managing the
  life-cycle orchestration of services, and control the specific workflow that
  needs to be executed for the test scenario. Usually no direct interaction
  with these classes is required, although it could be required in some cases.
  See [the Intersmash provisioning documentation](../provisioners/README.md)
  to learn about supported services provisioners.

### Project Modules
* **core** - core interfaces, annotations and extensions.
* **provisioners** - implementations of interfaces from `core` for selected services.
* **testsuite** - tests that verify the integration with OpenShift. (**Note:**  OpenShift is needed in order to run them.)
  * **test-deployments** - sources for shared deployments which are used by the testsuite.
    * **deployments-provider** - provides support to get path to compiled deployments
* **examples** - Test samples that illustrate the use of Intersmash.