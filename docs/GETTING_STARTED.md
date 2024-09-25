# Getting Started

## Get an OpenShift cluster (in case you don't have one)

If you've an OpenShift cluster availabe, then you can skip this section.
In case you don't have an OpenShift cluster available to experiment on, then the best choice - 
at least according to our experience - would be to use a local instance by installing [Red Hat OpenShift
Local](https://developers.redhat.com/products/openshift-local/getting-started), formerly known as
CodeReady Containers (or CRC).

Once you've followed the steps in the 
[installation guide](https://docs.redhat.com/en/documentation/red_hat_openshift_local/2.5/html/getting_started_guide/installation_gsg), 
and you have a local OpenShift 4 cluster available on your local machine, you can move to next sectiton, 
in order to configure Intersmash for connecting to such instance.

## Set up Intersmash configuration

While interacting with an OpenShft cluster, Intersmash uses two different logical users, namely `admin` and `master`.
Such logical users are meant to represent two OpenSHift user accounts, with the former holding administrative permissins,
e.g.: Intersmash would use the administrative account when creating a namespace for the tests to be run, while it would use
the regular account when installing an Operator in the current namespace.

Along with the above mentioned data, like the access token or the credentials the user accounts would use, additional information 
is needed in order toconnect to the cluster, e.g.: the API URL.

Intersmash uses [XTF](https://github.com/xtf-cz/xtf/?tab=readme-ov-file#configuration)
in order to connect to an OpenShift cluster. This mainly translates into the fact that it can rely on an existing 
`.kube/config` file, or it can use some configuration properties alternatively.

Following is a minimal list of properties that should be configured in order to provide Intersmash with details to connect
to your cluster with the right users accounts:

```
xtf.openshift.namespace=intersmash-test
xtf.bm.namespace=intersmash-test-bm

xtf.openshift.url=https://api.my-ocp-cluster:6443
#xtf.openshift.admin.username=admin
#xtf.openshift.admin.password=admin
xtf.openshift.admin.token=sha256~PMKGVnVjkqhvfFtvUnZcX5Nj6jlJ6MUNnomXfFk7kOU
#xtf.openshift.master.username=xpaasqe
#xtf.openshift.master.password=xpaasqe
xtf.openshift.master.token=sha256~VLz5aYr9x-YFSVOlTXvBk4CmfxLzSAg6cjT-WDFvJh8
```

The user credentials properties are commented in the above example, as a token is provided.
See the [XTF configuration guide](https://github.com/xtf-cz/xtf/?tab=readme-ov-file#configuration) for more details.

## Build Intersmash, and run an existing test on your cluster

```shell
mvn clean install -DskipTests ; mvn test -pl testsuite -Dtest=KafkaOperatorProvisionerTest
```
And keep an eye on what's going on on the cluster in the meantime:

```shell
oc get pods
```
Once you've verified that everything works, you'll be ready to add your first Intersmash test to your project.

## Create an Intersmash test

### Add Intersmash dependencies

The following dependencies must be added to your project POM:

```
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

### Create _application descriptor_(s) and a test class

The following example outlines a simple test scenario in which PostgresSql and Wildfly are used.

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
   enable the test code to scale the deployment up and down, for example.

An example implementation of `PostgreSQLTemplateOpenShiftApplication`, which leverages templates to describe a PostgreSql service, would look like this. 

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
For this service the user
needs to identify the template to use; POSTGRESQL_EPHEMERAL in this case and
provides the name of the service.

An example implementation of `WildflyImageOpenShiftApplication`, which leverages s2i build to deploy a WildFly application service, would look like this.

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
The application's name is declared
and the build of the Wildfly image to deploy is retrieved and provided to the framework.
