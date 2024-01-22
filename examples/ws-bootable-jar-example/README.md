ws-bootable-jar-example
=======================

This module provides a simple POC webservice's jaxws application that is built as a bootable jar consisting of Wildfly and the jaxws war file, and then deployed to OpenShift, and tested.

The jaxws module builds 2 archives, the webservice's war file and a bootable jar to be run on OpenShift

This module demonstrates building a fresh (relatively current) version of Wildfly using Galleon for inclusion in the bootable jar, as compared to pulling a pre-existing Wildfly image from a public repository.  The process enables the user to build a current version of Wildfly for testing.   

Module, demos, is not declared in intersmash's root pom.xml file.  The module must be built separately and the example must run from directory, demos.  The user will also require a test.properties file with the appropriate OpenShift reference and configuration information.  The test can be run with cmd.

Built the project and then the demos module with these commands.
    mvn clean install -DskipTests
    mvn clean install -DskipTests -Pdemo

Run the test.
   mvn test -pl ws-bootable-jar-example/ -amd -Dtest=SoapWildflyBootableOpenShiftJarTest \
     -Dxtf.test_properties.path=/ABSOLUTE/PATH/TO/test.properties

or if test.properties resides in the demos directory cmd,

   mvn test -pl ws-bootable-jar-example/ -amd -Dtest=SoapWildflyBootableOpenShiftJarTest
