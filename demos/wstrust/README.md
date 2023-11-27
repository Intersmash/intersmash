Web Service Trust - A 2 Pods WildFly test case
=======

This module is an example of testing a jaxws application that requires 2 communicating OpenShift pods.
One pod is a Security Token Service (STS).  The other is the web service provider.  The test itself
is the web service requester.  Modules sts and service build a bootable jar that is deployed to OpenShift.

# Test Overview

This example is a basic WS-Trust scenario.
The service provider requires a SAML 2.0 token issued from a designed STS to be presented by the
service requester using asymmetric binding. These communication requirements are declared in the
service provider’s WSDL. The STS requires service requester credentials be provided in a WSS
UsernameToken format request using symmetric binding. The STS’s response is provided containing
a SAML 2.0 token. These communication requirements are declared in the STS’s WSDL.

 1.   A service requester contacts the service provider and consumes its WSDL. Upon finding the security token issuer requirement, it creates and configures a STSClient with the information it requires to generate a proper request.

 2.   The STSClient contacts the STS and consumes its WSDL. The security policies are discovered. The STSClient creates and sends an authentication request, with appropriate credentials.

 3.   The STS verifies the credentials.

 4.   In response, the STS issues a security token that provides proof that the service requester has authenticated with the STS.

 5.   The STSClient presents a message with the security token to the service provider.

 6.   The service provider verifies the token was issued by the STS, thus proving the service requester has successfully authenticated with the STS.

 7.   The service provider executes the requested service and returns the results to the service requester.


# Example layout

This example consists of 4 modules and 2 reference directories.

- modules
    *   _shared:_ contains class files that are shared among modules, sts, service and test.

    *   _sts:_ a mock STS implementation.

    *   _service:_ the service implementation.

    *   _test:_ the test code.

- directories
    *   _shared-cli-scripts:_ contains a Wildfly CLI script and corresponding properties file.  The script
        configures Wildfly's `standalone.xml` file providing the needed security information.
        Modules sts and service are both configured with the same information.

    *   _shared-extra-content:_ contains the keystore and property files provided to Wildfly.



# Test Configuration

In Wildfly a deployable archive named **ROOT** (e.g ROOT.war) has special meaning.
Wildfly automatically deploys an archive with this name into its root directory,
"/", and this forces the application's context-root to be empty. [1]
The format for the url of a webservices application is
~~~~
http://<hostname>:<port number>/<context-root>/<other>
~~~~
  When the context-root
is not explicitly defined in a `web.xml` or `jboss-web.xml` file in the archive,
the archive's name is used. For example the context-root of a webservice
archive named `MyWebservice.war` in which no context-root has been explicitly
defined would be MyWebservice.  The url would be,
~~~~
http://<hostname>:<port number>/MyWebservice/<other>
~~~~

When a webservice archive is named **ROOT** the application's context-root must be
explicitly defined to be empty so that the url can properly be resolved. This can be achieved
by adding a `jboss-web.xml` file to the archive that contains the following,

    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE jboss-web PUBLIC "-//JBoss//DTD Web Application 2.4//EN" "http://www.jboss.org/j2ee/dtd/j
    boss-web_4_0.dtd">
    <jboss-web>
        <context-root></context-root>
    </jboss-web>

In troubleshooting a webservice's url,
a method to determine the expected url is to startup Wildfly and look for
the following information in the server.log or the terminal window.

~~~~
  ... JBWS024061: Adding service endpoint metadata: id=org.jboss.jaxws.EndpointImpl
   address=http://jbossws.undefined.host:8080/EndpointServiceSERVICE
   implementor=org.jboss.jaxws.EndpointImpl
   serviceName={http://org.jboss.ws/cxf/container}EndpointServiceSERVICE
   portName={http://org.jboss.ws/cxf/container}EndpointService
   annotationWsdlLocation=null
   wsdlLocationOverride=null
   mtomEnabled=false
~~~~


The `address` above displays the url information of the service.

__Note:__

Be aware that `wildfly-jar-maven-plugin` by default places any deployed archive
into Wildfly's root directory; the archive does not need to be named ROOT. [2]
This behavior can be changed to use the WAR file name as the context-root
by specifying the `<context-root>false<context-root>` element in the plugin's
configuration section. [3]

The wstrust example demonstrates both context-root scenarios.  Module, sts, builds an archive
named `sts-ROOT.war`, that is deployed by the `wildfly-jar-maven-plugin` into Wildfly's
root directory.  
The archive name is declared by the `<warName>` element in the `maven-war-plugin`.
A `jboss-web.xml` file as described above is provided in the `sts/src/main/webapp/WEB-INF/`
directory.  No `<context-root>` element is declared in the `wildfly-jar-maven-plugin`'s
configuration section to change its default behavior of deploying the archive into
Wildfly's root directory.  In `WstrustOpenShiftJarTest` the URL to the STS does not reference
the war filename because an empty context-root is being used.  Module, service, builds an
archive named `service-ROOT.war`, that is deployed by the `wildfly-jar-maven-plugin` into Wildfly
but uses the archive name as the service's context-root.  The archive name is declared in the
`<warName>` element in the `maven-war-plugin`.  In the `wildfly-jar-maven-plugin`'s configuration
section, element `<context-root>` is declared to be *false*.  This causes the archive name
to be used as the context-root.  In `WstrustOpenShiftJarTest` the URL to the service contain
`service-ROOT`.

__Mock STS Configuration__

The mock STS provider needs to be configured with the service provider's url.
Intersmash uses a fabric8 kubernetes `EvnVar` object to pass the provider's url string from
the test setup code to the sts provider.  Class `STSWstrustOpenShiftJarApplication`
provides the information via its `getEnvVars` method.

__Build and Run__

The test can be built and run with the following commands.

~~~~
    mvn clean install -DskipTests -Pdemo
    mvn test  -pl demos/wstrust/test    -Dtest=WstrustOpenShiftJarTest -Pdemo \
       -Dxtf.test_properties.path=/ABSOLUTE/PATH/TO/test.properties
~~~~


__References__

[1] https://www.mastertheboss.com/web/jboss-web-server/how-to-deploy-a-web-application-on-the-root-context-on-jboss-as-7/

[2] https://docs.wildfly.org/bootablejar/#wildfly_jar_url_context

[3] https://docs.wildfly.org/bootablejar/#contextRoot
