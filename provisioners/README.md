# Intersmash - Provisioning 

### Contents
- [Overview](#Overview)
- [Provisioner Categories](#Provisioner-Categories)
- [Adding a new Service](#Adding-a-new-Service)
  - [Provisioning Example](#Provisioning-Example)
    - [Step 1 - Creating a new Application Interface](#Step-1-Creating-a-new-Application-Interface)
    - [Step 2 - Creating a new Provisioner class](#Step-2-Creating-a-new-Provisioner-class)
    - [Step 3 - Creating a new ProvisionerFactory class](#Step-3-Creating-a-new-ProvisionerFactory-class)
    - [Step 4 - Register the ProvisionerFactory as a service](#Step-4-Register-the-ProvisionerFactory-as-a-service)
    - [Step 5 - Provide a demonstration application](#Step-5-Provide-a-demonstration-application)
- [Provisioning by Category](#Provisioning-by-Category)
  - [Application server image Provisioning](#Application-server-image-Provisioning)
  - [Database image Provisioning ](#Database-image-Provisioning)
  - [Operator-based Provisioning](#Operator-based-Provisioning)
  - [Template Provisioning](#Template-Provisioning)
  - [Helm Chart Provisioning](#Helm-Chart-Provisioning)
  - [Automatic Provisioning](#Automatic-Provisioning)

## Overview
Provisioners are the engine of this framework. They are the components
that do the work of configuring the cloud containers, deploying, undeploying, and
managing a service's life-cycle orchestration.
There are two other components that have a one-to-one relationship with a provisioner,
the _Application_ and the _ProvisionerFactory_.  The Application is an externally facing
(service) interface class the user must implement and register with Intersmash via the
@Service annotation.  The ProvisionerFactory
is an internal service specific concrete class that evaluates the _Application_ instance and
returns the corresponding _Provisioner_ class for the service or throws an exception if none is found.

Intersmash uses a class naming convention that shows a clear
relationship between these three class types.

- ${ProductComponentName}**Application**
- ${ProductComponentName}**Provisioner**
- ${ProductComponentName}**ProvisionerFactory**

## Provisioner Categories
There are several categories of service provisioners in Intersmash.
Each category has some unique provision requirements.
(The full list of provided services can be found [here](../docs/Provisioner-by-Product.md).)
- **Application server images**: Application server images require the identification
  of the image file to deploy and any server specific configuration data.


- **Database images**: Database provisioning requires the image and items like
  user credentials, database name and mount path.


- **Operator services**: Operator services automates the behavior needed to deploy
  a given service on cloud environments via APIs that leverage the
  [Operator Framework](https://github.com/operator-framework).


- **Templated services**:  Some _product components_ have named templates that are executed to generate 
  a specific service version or configuration.  The user must declare the template name in 
  their _Application_ implementation. 
 
 
- **Helm Chart**: Helm Chart based provisioning relies on Helm Chart value files.


- **Automatic**: Automatic (or application dictated) provisioning is a way to delegate the whole provisioning process to the Application class, rather than depending on a provisioner.



## Adding a new Service
Here are the steps in adding a new service to Intersmash.

1. Create a new Application interface class which extends from an existing Application interface.
2. Create a new Provisioner class which implements or extends an existing Provisioner interface or class respectively.
3. Create a new ProvisionerFactory class which implements the _ProvisionerFactory_ interface.
4. Add your new ProvisionerFactory class to the service file, _./provisioners/src/main/resources/META-INF/services/org.jboss.intersmash.provision.ProvisionerFactory_ 
5. Provide a demonstration application in the examples directory.

## Provisioning Example
Here is a simple example that adds a "spy" service to the framework.

### Step 1 - Creating a new Application Interface
```java
package org.secret.agents;

import java.util.Collections;
import java.util.List;

import org.jboss.intersmash.application.openshift.HasEnvVars;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.EnvVar;

public interface SpyApplication extends OpenShiftApplication, HasSecrets,
    HasEnvVars {

	// OpenShift configuration
	@Override
	default List<Secret> getSecrets() {
		return Collections.emptyList();
	}

        @Override
	default List<EnvVar> getEnvVars() {
		return Collections.emptyList();
	}
}
```

### Step 2 - Creating a new Provisioner class
```java
package org.secret.agents;

// a long list of imports here

public class SpyProvisioner implements OpenShiftProvisioner<SpyApplication> {

	private final SpyApplication spyApp;
	private FailFastCheck ffCheck = () -> false;

	public SpyProvisioner(@NonNull SpyApplication spyApp) {
		this.spyApp = spyApp;
	}

	@Override
	public SpyApplication getApplication() {
		return spyApp;
	}


	@Override
	public void deploy() {
		deployImage();
	}

	@Override
	public void undeploy() {
		// implement the logic for provisioning a "Spy" service instance on OpenShift
	}
    
	@Override
	public void scale(int replicas, boolean wait) {
		openShift.scale(spyApp.getName(), replicas);
		if (wait) {
			waitForReplicas(replicas);
		}
	}

	public void waitForReplicas(int replicas) {
		OpenShiftWaiters.get(openShift, ffCheck).areExactlyNPodsReady(replicas, spyApp.getName()).level(Level.DEBUG)
				.waitFor();
		WaitersUtil.serviceEndpointsAreReady(openShift, getApplication().getName(), replicas, 8080)
				.level(Level.DEBUG)
				.waitFor();
		if (replicas > 0) {
			WaitersUtil.routeIsUp(getUrl(spyApp.getName(), false))
					.level(Level.DEBUG)
					.waitFor();
		}
	}

	@Override
	public List<Pod> getPods() {
		return openShift.getPods(getApplication().getName());
	}

	private void deployImage() {
            // This is an long task of many steps and thus will not be
            // listed here.  Look at existing provisioners for examples.
        }
}
```

### Step 3 - Creating a new ProvisionerFactory class
```java
package org.secret.agents;

import org.jboss.intersmash.application.Application;
import org.jboss.intersmash.provision.ProvisionerFactory;

public class SpyProvisionerFactory implements ProvisionerFactory<SpyProvisioner> {

	@Override
	public SpyProvisioner getProvisioner(Application application) {
		if (SpyApplication.class.isAssignableFrom(application.getClass()))
			return new SpyProvisioner((SpyApplication) application);
		return null;
	}
}
```

### Step 4 - Register the ProvisionerFactory as a service
Edit service file, _./provisioners/src/main/resources/META-INF/services/org.jboss.intersmash.provision.ProvisionerFactory_ and add _org.secret.agents.SpyProvisionerFactory_ and rebuild Intersmash.


### Step 5 - Provide a demonstration application
The last task is to write and add a simple application that demonstrates the service working in the examples directory.
Provide a README file that describes the service, how to configure and run it.


## Provisioning by Category

### Application server image Provisioning
Application server images require the identification
of the image file to deploy and any server specific configuration data.  A
provisioner should implement the _OpenShiftProvisioner_ interface.
[WildflyImageOpenShiftProvisionerFactory](src/main/java/org/jboss/intersmash/provision/openshift/WildflyImageOpenShiftProvisionerFactory.java)
provides a reference to a _Provisioner_ and _Application_ implementing this feature.

### Database image Provisioning
Database provisioning requires the image and items like
user credentials, database name and mount path.
Abstract class, _DBImageOpenShiftProvisioner_ provides basic configuration.
A provisioner's concrete class provides the extension to these types of services.
[MysqlImageOpenShiftProvisionerFactory](src/main/java/org/jboss/intersmash/provision/openshift/MysqlImageOpenShiftProvisionerFactory.java)
provides a reference to a _Provisioner_ and _Application_ implementing this feature.

### Operator-based Provisioning
Operator services automates the behavior needed to deploy
a given service on cloud environments via APIs that leverage the
[Operator Framework](https://github.com/operator-framework).  Operator-based provisioners implement a common contract and high level behavior which is defined by Intersmash's, abstract class, _OperatorProvisioner_.
[Operator provisioning How-to](docs/operator-howto.md),
describes the steps needed to create a new operator-based provisioner.
[KeycloakOperatorProvisionerFactory](src/main/java/org/jboss/intersmash/provision/openshift/KeycloakOperatorProvisionerFactory.java) provides a reference to a _Provisioner_ and _Application_ implementing this feature.

A list of operator based provisioners [here](../docs/Operator-Based-Provisioning.md)

### Template Provisioning
Some _product components_ have named templates that are executed to generate
a specific service version or configuration.  The user must declare the template name in
their _Application_ implementation. The parent interface class for these provisioners is _OpenShiftProvisioner_. [RhSsoTemplateOpenShiftProvisionerFactory](src/main/java/org/jboss/intersmash/provision/openshift/RhSsoTemplateOpenShiftProvisionerFactory.java)
provides a reference to a _Provisioner_ and _Application_ implementing this feature.


### Helm Chart Provisioning
Helm Chart based provisioning relies on Helm Chart value files.
It requires the implementation of interface, _HelmChartRelease_, which
defines the contract for implementing classes to represent a Helm Chart release.
The provisioner expects the application descriptor to be able to serialize the
release data onto a file, which eventually is used to install or upgrade a
given application service release.
Abstract class, _HelmChartOpenShiftProvisioner_, holds the behavioral logic described.
A concrete implementation of this class provides extension logic to fit various service types.
[WildflyHelmChartOpenShiftProvisionerFactory](src/main/java/org/jboss/intersmash/provision/helm/wildfly/WildflyHelmChartOpenShiftProvisionerFactory.java) provides a reference to a _Provisioner_ and _Application_ implementing this feature.

### Automatic Provisioning
Automatic (or application dictated) provisioning is a way to delegate the whole provisioning process to the Application descriptor, rather than depending on a provisioner.
Having a concrete instance of an Application class that implements interface
_AutoProvisioningOpenShiftApplication_ will let the user add methods that map to the
provisioning process workflow, e.g.: `deploy`, `scale`, `undeploy`.
[OpenShiftAutoProvisionerFactory](../core/src/main/java/org/jboss/intersmash/provision/openshift/auto/OpenShiftAutoProvisionerFactory.java)
provides a reference to a _Provisioner_ and _Application_ implementing this feature.

A list of provisioners by product can be found [here](../docs/Provisioner-by-Product.md).