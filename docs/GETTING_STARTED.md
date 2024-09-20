# Getting Started

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