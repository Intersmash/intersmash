# Intersmash deployments
This module contains applications used for interoperability testing.
Also, it provides utilities and APIs which can be leveraged by external project when implementing their specific 
deployment modules.

## Provider
The `intersmash-deployments-provider` module provides support for getting installed archives from the local repository 
and utilities (e.g.: Maven settings ones) which you can leverage when implementing deployment modules which are 
specific to a project that would depend on the Intersmash library.

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
