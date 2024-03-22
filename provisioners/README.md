# Intersmash - Provisioning 

Every application defined by `@Service` could be served by a different Provisioner depending on which Application 
interface it implements (see the table of mapping located below). Provisioner implementation provides the requested 
functionality while taking into account the selected platform, product, type of source, etc.

Additional set of Applications supported with relevant Provisioner classes will be implemented on demand once requested.

Example implementation of Application:

```java
public class PostgresqlApplication implements PostgreSQLTemplateOpenShiftApplication {
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

Example test scenario which deploys the `PostgresqlApplication` (more applications would be part of `@Intersmash` in real 
integration use case)

```java
@Intersmash(@Service(PostgresqlApplication.class))
@Slf4j
public class PostgresqlProvisionTest {
	@ApplicationController(PostgresqlApplication.class)
	OpenShiftProvisioner provisioner;

	@Test
	public void printPostgresqlPods() {
		provisioner.getPods().forEach(pod -> {
			log.info(pod.getMetadata().getName());
			log.info("READY: " + pod.getStatus().getContainerStatuses().get(0).getReady());
		});
	}
}
```

## Mapping of implemented provisioners:

| Service                               | Supports community project | Supports product   | Application                           | Provisioner                                 | Notes                                                                                                                                                                                                                                                                                                                                                 |
|:--------------------------------------|:---------------------------|:-------------------|:--------------------------------------|:--------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ActiveMQ Artemis & Red Hat AMQ Broker | :heavy_check_mark:         | :heavy_check_mark: | ActiveMQOperatorApplication           | ActiveMQOperatorProvisioner                 | Operator based provisioner, see details [below](#operator-based-provisioning)                                                                                                                                                                                                                                                                         |
| HyperFoil                             | :heavy_check_mark:         | :x:                | HyperfoilOperatorApplication          | HyperfoilOperatorProvisioner                | Operator based provisioner, see details [below](#operator-based-provisioning)                                                                                                                                                                                                                                                                         |
| Infinispan & Red Hat DataGrid         | :heavy_check_mark:         | :heavy_check_mark: | InfinispanOperatorApplication         | InfinispanOperatorProvisioner               | Operator based provisioner, see details [below](#operator-based-provisioning)                                                                                                                                                                                                                                                                         |
| Kafka & Red Hat AMQ Streams           | :heavy_check_mark:         | :heavy_check_mark: | KafkaOperatorApplication              | KafkaOperatorProvisioner                    | Operator based provisioner, see details [below](#operator-based-provisioning)                                                                                                                                                                                                                                                                         |
| Keycloak & Red Hat Build of Keycloak  | :heavy_check_mark:         | :heavy_check_mark: | KeycloakOperatorApplication           | KeycloakOperatorProvisioner                 | The latest Quarkus based Keycloak Operator, doesn't provide stable CRDs yet (see https://www.keycloak.org/2022/09/operator-crs); this operator offers a solution which supports the `Keycloak` and `KeycloakRealmImport` Custom Resources: these are the only supported CRs at the time of writing. See details [below](#operator-based-provisioning) |
| MySQL                                 | :heavy_check_mark:         | :x:                | MysqlImageOpenShiftApplication        | MysqlImageOpenShiftProvisioner              | Deploys a MySQL image and sets environment variables to configure the service (ports, credentials etc.)                                                                                                                                                                                                                                               |
| PostgreSQL                            | :heavy_check_mark:         | :x:                | PostgreSQLImageOpenShiftApplication   | PostgreSQLImageOpenShiftProvisioner         | Deploys a PostgreSQL image and sets environment variables to configure the service (ports, credentials etc.)                                                                                                                                                                                                                                          |
| Wildfly & Red Hat JBoss EAP XP        | :heavy_check_mark:         | :heavy_check_mark: | BootableJarOpenShiftApplication       | WildflyBootableJarImageOpenShiftProvisioner | Deploys a WildFly Bootable JAR, i.e. a runnable WildFly application                                                                                                                                                                                                                                                                                   |
| Wildfly & Red Hat JBoss EAP 8         | :heavy_check_mark:         | :heavy_check_mark: | WildflyImageOpenShiftApplication      | WildflyImageOpenShiftProvisioner            | Available both for Git sources and binary based s2i v2 build (either a pre-built deployment or a filesystem resource like a Maven project folder)                                                                                                                                                                                                     |
| Wildfly & Red Hat JBoss EAP 8         | :heavy_check_mark:         | :heavy_check_mark: | WildflyHelmChartOpenShiftApplication  | WildflyHelmChartOpenShiftProvisioner        | The `wildfly-2.3.2` tag of https://github.com/wildfly/wildfly-charts is used and the model is generated based on the https://raw.githubusercontent.com/wildfly/wildfly-charts/main/charts/wildfly/values.schema.json  value schema file                                                                                                               |
| Wildfly & Red Hat JBoss EAP 7/8       | :heavy_check_mark:         | :heavy_check_mark: | WildflyOperatorApplication            | WildflyOperatorProvisioner                  | Operator based provisioner, see details [below](#operator-based-provisioning)                                                                                                                                                                                                                                                                         |
| Red Hat JBoss EAP 7                   | :x:                        | :heavy_check_mark: | Eap7ImageOpenShiftApplication         | Eap7ImageOpenShiftProvisioner               | Available both for Git sources and binary based EAP 7 s2i (legacy) build, i.e. based on a pre-built deployment (i.e. a _WAR archive_)                                                                                                                                                                                                                 |
| Red Hat JBoss EAP 7                   | :x:                        | :heavy_check_mark: | Eap7TemplateOpenShiftApplication      | Eap7TemplateOpenShiftProvisioner            | Available Git sources and template based EAP 7 s2i (legacy) build                                                                                                                                                                                                                                                                                     |
| Red Hat JBoss EAP 7                   | :x:                        | :heavy_check_mark: | Eap7LegacyS2iBuildTemplateApplication | Eap7LegacyS2iBuildTemplateProvisioner       | Git based EAP 7 s2i (legacy) build, used to generate image streams that can be deployed by WildflyOperatorProvisioner                                                                                                                                                                                                                                 |
| Red Hat SSO 7                         | :x:                        | :heavy_check_mark: | RhSsoOperatorApplication              | RhSsoOperatorProvisioner                    | **Support for this provisioner now deprecated in Intersmash**. Based on the archived Keycloak operator project, which contains the latest Red Hat SSO 7.z CRDs definitions, see details [below](#operator-based-provisioning)                                                                                                                         |
| Red Hat SSO 7                         | :x:                        | :heavy_check_mark: | RhSsoTemplateOpenShiftApplication     | RhSsoTemplateOpenShiftProvisioner           | **Support for this provisioner now deprecated in Intersmash**. Allows provisioning Red Hat SSO 7.6.z based on latest templates, see https://raw.githubusercontent.com/jboss-container-images/redhat-sso-7-openshift-image/sso76-dev/templates/                                                                                                        | 


The only thing users have to take care of is to implement the correct `Application` (see the table above) interface and 
pass the class via `@Service` annotation, it's a task for the framework to choose a suitable Provisioner implementation 
(an exception will be thrown in case no suitable Provisioner was yet implemented). 
See [ProvisionerManager](../core/src/main/java/org/jboss/intersmash/provision/ProvisionerManager.java) and [ProvisionerFactory](../core/src/main/java/org/jboss/intersmash/provision/ProvisionerFactory.java) for implementation details regarding the Provisioner selection.

### Operator-based provisioning

The Intersmash Library provides support for operator-based provisioning, i.e. it automates the behavior needed to deploy 
a given service on cloud environments via APIs that leverage the 
[Operator Framework](https://github.com/operator-framework). 

Intersmash makes this feature available for currently supported products (see the table below), but that can be 
extended easily, since Intersmash _provisioners_ are pluggable components.

| Service                          | Supported Operator version | Channel name | Supported product version | Repository                                                | Notes                                                                                                                                                                              |
|:---------------------------------|:---------------------------|:-------------|:--------------------------|:----------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ActiveMQ Artemis                 | 1.0.11                     | upstream     | 1.0.15                    | https://github.com/artemiscloud/activemq-artemis-operator | We are using a custom index image, i.e. quay.io/jbossqe-eap/intersmash-activemq-operator-catalog:v1.0.11, built as described in https://github.com/Intersmash/intersmash/issues/32 |
| Red Hat AMQ Broker               | 7.11.2-opr-1               | 7.11.x       | 7.11.z                    | https://github.com/rh-messaging/activemq-artemis-operator | As available on the OpenShift OperatorHub                                                                                                                                          |  
| Hyperfoil                        | 0.24.2                     | alpha        | 0.24.2                    | https://github.com/Hyperfoil/hyperfoil-operator           | We force the CRs version for the used Hyperfoil runtime to be 0.24.2, see https://github.com/Hyperfoil/hyperfoil-operator/issues/18                                                |
| Infinispan                       | 2.3.4                      | 2.3.x        | 14.0.17.Final             | https://github.com/infinispan/infinispan-operator         | As available on the OpenShift OperatorHub (community-operators)                                                                                                                    |
| Red Hat DataGrid                 | 8.4.12                     | 8.4.x        | 8.4.6.GA                  | https://github.com/infinispan/infinispan-operator         | As available on the OpenShift OperatorHub                                                                                                                                          | 
| Kafka provided by Strimzi        | 0.38.0                     | stable       | 3.6.0                     | https://github.com/strimzi/strimzi-kafka-operator         |                                                                                                                                                                                    |
| Red Hat AMQ Streams              | 2.6.0-0                    | stable       | 3.6.0                     | https://github.com/strimzi/strimzi-kafka-operator         | As available on the OpenShift OperatorHub                                                                                                                                          |
| Keycloak                         | 24.0.1                     | upstream     | 24.0.1                    | https://github.com/keycloak/keycloak/tree/main/operator   | Latest Keycloak, based on Quarkus. Supports a limited number of CR (Keycloak and KeycloakRealmImport): more to come in upcoming versions                                           |
| Red Hat Build of keycloak (RHBK) | 22.0.9-opr.1               | stable-v22   | 22.0.9.redhat-00002       | https://github.com/keycloak/keycloak/tree/main/operator   | Latest Keycloak, based on Quarkus. Supports a limited number of CR (Keycloak and KeycloakRealmImport): more to come in upcoming versions                                           |
| Red Hat SSO - **Deprecated**     | 7.6.6-opr-003              | stable       | 7.6                       | https://github.com/keycloak/keycloak-operator             | Latest Red Hat SSO Operator, based on legacy Keycloak                                                                                                                              |
| WildFly                          | 0.5.6                      | alpha        | 30.0.0.Final              | https://github.com/wildfly/wildfly-operator               | As available on https://operatorhub.io/operator/wildfly                                                                                                                            |
| Red Hat JBoss EAP                | 2.4.2                      | stable       | EAP 8.0, EAP 7.4.z        | https://github.com/wildfly/wildfly-operator               | As available on the OpenShift OperatorHub                                                                                                                                          |

Intersmash operator-based provisioners implement a common contract and high level behavior which is defined by the 
[OperatorProvisioner](../core/src/main/java/org/jboss/intersmash/provision/openshift/operator/OperatorProvisioner.java)
class.

You can also learn more by going through the Intersmash 
[Operator provisioning How-to](docs/operator-howto.md), 
which describes the steps needed to create a new operator-based provisioner.

### Helm Charts based provisioning

Helm Charts based provisioning relies on Helm Charts values files, by defining the `HelmChartRelease` interface.
This means that the selected provisioner expects for the application descriptor to be able of serializing the
release data onto a file, which eventually is used to install or upgrade a given application service release.

The `HelmChartProvisioner` class holds the behavioral logic described above, while concrete implementations
provide extension logic to fit various service types' requirements, e.g. `WildflyHelmChartProvisioner` provides
logic to create Wildfly image streams during the pre-deploy phase, or to handle cleanup during the post-deployment one.

Application descriptors for test scenarios that involve Helm Based provisioning must expose a concrete
implementation of the `HelmChartRelease` interface, for the provisioner to use it during the application service
life cycle.

What above is all that is needed - together with the related Intersmash concepts - to implement the components that compose
an Helm Charts provisioned test scenario, similarly to what happens with other provisioners, see the classes and APIs in
the `org.jboss.intersmash.application.openshift.helm` and `org.jboss.intersmash.provision.helm` packages.

The process of implementing a concrete `HelmChartRelease` that serializes to a valid Helm Charts values file can be
simplified by leveraging POJOs that can be generated by external tools and wrap them into an adapter that implements
the `HelmChartRelease` interface. The adapter is responsible for exposing and manipulating the internal release
POJOs.

This avoids the need for coding a dedicated `HelmChartRelease` implementation and the related serialization logic, but it
also allows for loading data (through deserializaton) easily from static files.
Of course the release data can be manipulated through the generated Java model APIs.

### Automatic provisioning
Automatic (or application dictated) provisioning is a way to delegate the whole provisioning process to the application
descriptor, rather than depending on a provisioner.
Having a concrete instance of an application class that implements `AutoProvisioningOpenShiftApplication` will let 
you add methods that map to the provisioning process workflow, e.g.: `deploy`, `scale`, `undeploy`:

```java
public class AutoProvisioningHelloOpenShiftApplication implements AutoProvisioningOpenShiftApplication {

  private static final String HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION = "target/test-classes/org/jboss/intersmash/provision/openshift/auto/hello-openshift-deployment.yaml";
  private static final String HELLO_OPENSHIFT_SERVICE_DEFINITION = "target/test-classes/org/jboss/intersmash/provision/openshift/auto/hello-openshift-service.yaml";
  private static final String HELLO_OPENSHIFT_ROUTE_DEFINITION = "target/test-classes/org/jboss/intersmash/provision/openshift/auto/hello-openshift-route.yaml";
  private static final String HELLO_OPENSHIFT_APP_NAME = "hello-openshift";
  private static final String HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER = "autoprovisioning-hello-openshift-app-replicas-changeme";

  @Override
  public void preDeploy() {
    // on preDeploy, we've got nothing to do... (yet, maybe we could create the secret for securing the route)
    /// TODO
  }

  @Override
  public void deploy() throws AutoProvisioningException {
    final int replicas = 1;
    // set initial replicas
    try {
      adjustConfiguration(HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION,
              String.format("replicas: %s", HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER),
              String.format("replicas: %d", replicas));
    } catch (IOException e) {
      throw new AutoProvisioningException(e);
    }
    try {
      // on deploy, we'll provision the app pod as per the JSON definition in HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION
      OpenShifts.masterBinary().execute(
              "apply", "-f", HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION);
    } finally {
      try {
        adjustConfiguration(HELLO_OPENSHIFT_DEPLOYMENT_DEFINITION,
                String.format("replicas: %d", replicas),
                String.format("replicas: %s", HELLO_OPENSHIFT_APP_REPLICAS_PLACEHOLDER));
      } catch (IOException e) {
        throw new AutoProvisioningException(e);
      }
    }
    // let's wait for the exact number of pods
    OpenShiftWaiters.get(OpenShifts.master(), () -> false).areExactlyNPodsReady(
            1, "app", getName()).waitFor();
    // then create a service for the hello-openshift app
    OpenShifts.masterBinary().execute(
            "apply", "-f", HELLO_OPENSHIFT_SERVICE_DEFINITION);
    // and let's expose the service through a route so that it is available externally
    OpenShifts.masterBinary().execute(
            "apply", "-f", HELLO_OPENSHIFT_ROUTE_DEFINITION);
  }
  // ...
}
```

These classes can implement their own provisioning process by using the above-mentioned annotations in order to 
accomplish a declarative configuration, instead that requiring a specific provisioner that would instead leverage 
a client API.

Application instances implementing the `AutoProvisioningOpenShiftApplication` interface can be used the same ways as the 
other `Application` types, i.e. in a testing scenario defined by a test class and its `@Intersmash` annotations.

For an example of automatic provisioning process, just run the following commands: 
```text
$ mvn clean package -DskipTests
...
$ mvn test -pl core -Dtest=AutoProvisioningTests
```
