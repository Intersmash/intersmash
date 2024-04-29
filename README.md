# Intersmash - Cloud-native testing with Java

Intersmash is a Java library that makes it easy to automate the
provisioning and execution of tests in cloud-native environments.
It helps the user prototype and test complex interoperability scenarios on
kubernetes compliant cloud-native environments and platforms, most notably OpenShift.  (*Other Kubernetes implementations will be supported in the future.*)

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


## Usage Example

The following outlines a simple test scenario in which PostgresSql and Wildfly are used.


```java
@Intersmash({ // 1
        @Service(PostgresqlApp.class),
        @Service(WildflyOpenShiftApp.class)
    }
)
public class SampleTest {
    @ServiceUrl(WildflyOpenShiftApp.class) // 2
    private String wildflyRouteUrl;
  
    @ServiceProvisioner(WildflyOpenShiftApp.class) // 3
    private OpenShiftProvisioner wildflyOpenShiftProvisioner;
    
    @ServiceProvisioner(PostgresqlApp.class) 
    private OpenShiftProvisioner postgresqlProvisioner;

    @Test
    public void test() {
        // Do your thing.
    }
}
```
1. By decorating the class with the Intersmash annotation, the user registers their interface implementation
   classes of the services to be used in this test.
2. Injects into the field a string containing the URL to communicate with Wildfly.
3. Injects into the field the provisioner for Wildfly and PostgreSQL.  These classes
   enable the test code to, scale the deployment up and down, for example.


An example implementation of PostgreSQLTemplateOpenShiftApplication, which leverages templates to describe a PostgreSql service, would look like this. For this service the user
needs to identify the template to use; POSTGRESQL_EPHEMERAL in this case and
provides the name of the service.

```java
import org.jboss.intersmash.application.openshift.PostgreSQLTemplateOpenShiftApplication;
import org.jboss.intersmash.application.openshift.template.PostgreSQLTemplate;

public class PostgresqlApp implements PostgreSQLTemplateOpenShiftApplication {
	static String NAME = "postgresql";

	@Override
	public PostgreSQLTemplate getTemplate() {
		return PostgreSQLTemplate.POSTGRESQL_EPHEMERAL;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
```


An example implementation of WildflyImageOpenShiftApplication, which leverages s2i build to deploy a WildFly application service, would look like this.  The application's name is declared
and the build of the Wildfly image to deploy is retrieved and provided to the framework.

```java
import org.jboss.intersmash.application.openshift.WildflyImageOpenShiftApplication;

public class WildflyOpenShiftApp implements WildflyImageOpenShiftApplication {
  @Override
  public String getName() {
    return "wildfly-app";
  }

  @Override
  public BuildInput getBuildInput() {
    return new BuildInputBuilder().archive(app).build();
  }
}
```


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


## Platforms

Intersmash is designed to allow executions on different Kubernetes compliant platforms, at the moment we are fully focused on OpenShift running on Linux support.  We welcome community contributions to other Kubernetes implementations.


## Future goals
* K8s support and Quarkus provisioning
* documentation and examples enhancements

see the [current milestone](https://github.com/Intersmash/intersmash/milestone/2) for a complete list of issues.

## Issue tracking
To tack or report an issue see [GitHub issue](https://github.com/Intersmash/intersmash/issues).

When reporting an issue please choose the proper template and fill it with the required information.

## Contributing
Intersmash is an open source community project.  We welcome participation and contributions.
See [Intersmash contributing guidelines](CONTRIBUTING.md).
