# Intersmash test deployments
This module contains applications used for testing in integration with OpenShift.
Deployments are grouped in categories, in order to share common configuration that is easier to maintain.
Standalone deployments instead have their own configuration and are consequently harder to maintain, but represent 
a nicer option for OpenShift builds.

## Provider
The `deployments-provider` module exposes APIs to access such deployments from test classes, where they're needed. 
It can get installed archives from the local Maven repository, or filesystem based ones, like pure maven projects.

## Notes

#### Wildfly Bootable JAR
The `intersmash-deployments-shared-wildfly` POM includes some Wildfly deployments and profiles to provide minimalistic 
configuration for:
* `wildfly-jar-maven-plugin`
* `wildfly-maven-plugin`
* `maven-install-plugin`

The Bootable JAR related profiles are described in details in the following sections.

###### Bootable-jar-baremetal
Creates ${project.build.finalName}-bootable-baremetal.jar and install it into local repository

`wildfly-jar-maven-plugin` configuration: 
* Output name.

To activate profile create `maven/bootable-jar-baremetal` file in the submodule

###### Bootable-jar-openshift
Creates ${project.build.finalName}-bootable-openshift.jar and install it into local repository

`wildfly-jar-maven-plugin` configuration: 
* Output name.
* Defines `<cloud/>` in the configuration - Bootable JAR supposed to be run in OpenShift environment.

To activate profile create `maven/bootable-jar-openshift` file in the submodule
