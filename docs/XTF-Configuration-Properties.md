
### Intersmash Configuration Properties

* The following properties can be used to configure Intersmash build and test execution, see the [CI checks e2e tests
  script](../.ci/openshift-ci/build-root/e2e-test-prod.sh) as an example:


| Property                                           | Description                                                                                                                      |
|----------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| intersmash.skip.deploy                             | Skip the deployment phase, tests will be run against a prepared environment *                                                    |
| intersmash.skip.undeploy                           | Do not cleanup environment after test (development use)                                                                          |
| intersmash.deployments.repository.ref              | Manually set git repository branch of deployments                                                                                |
| intersmash.deployments.repository.url              | Manually set git repository url of deployments                                                                                   |
| intersmash.openshift.script.debug                  | Add parameter SCRIPT_DEBUG=true to DeploymentConfig/Pod                                                                          |
|                                                    |                                                                                                                                  |
| intersmash.wildfly.image                           | Wildfly/JBoss EAP 8 Builder image URL                                                                                            |
| intersmash.wildfly.runtime.image                   | Wildfly/JBoss EAP 8 Runtime image URL                                                                                            |
|                                                    |                                                                                                                                  |
| intersmash.wildfly.helm.charts.repo                | Wildfly/JBoss EAP 8 Helm Charts repository URL                                                                                   |
| intersmash.wildfly.helm.charts.branch              | Wildfly/JBoss EAP 8 Helm Charts repository branch                                                                                |	
| intersmash.wildfly.helm.charts.name                | Wildfly/JBoss EAP 8 Helm Charts repository namespaces                                                                            |
|                                                    |                                                                                                                                  |
| intersmash.wildfly.operators.catalog_source        | Wildfly/JBoss EAP custom catalog for Operator                                                                                    |
| intersmash.wildfly.operators.index_image           | Wildfly/JBoss EAP custom index image for Operator                                                                                |
| intersmash.wildfly.operators.package_manifest      | Wildfly/JBoss EAP custom package manifest for Operator                                                                           |
| intersmash.wildfly.operators.channel               | Wildfly/JBoss EAP desired channel for Operator                                                                                   |
|                                                    |                                                                                                                                  |
| intersmash.bootable.jar.image                      | Open JDK image URL that can be used as the base for an OpenShift Wildfly/JBoss EAP Bootable JAR                                  |
| intersmash.eap7.image                              | JBoss EAP 7 Builder image URL                                                                                                    |
| intersmash.eap7.runtime.image                      | JBoss EAP 7 Runtime image URL                                                                                                    |
| intersmash.eap7.templates.base.url                 | JBoss EAP 7 OpenShift Templates base URL                                                                                         |
| intersmash.eap7.templates.path                     | JBoss EAP 7 openShift Templates base path                                                                                        |
|                                                    |                                                                                                                                  |
| intersmash.infinispan.image                        | Infinispan/Red Hat DataGrid image URL                                                                                            |
| intersmash.infinispan.operators.catalog_source     | Infinispan/Red Hat DataGrid custom catalog for Operator                                                                          |
| intersmash.infinispan.operators.index_image        | Infinispan/Red Hat DataGrid custom index image for Operator                                                                      |
| intersmash.infinispan.operators.package_manifest   | Infinispan/Red Hat DataGrid custom package manifest for Operator                                                                 |
| intersmash.infinispan.operators.channel            | Infinispan/Red Hat DataGrid desired channel for Operator                                                                         |
|                                                    |                                                                                                                                  |
| intersmash.keycloak.image                          | Keycloak image URL                                                                                                               |
| intersmash.keycloak.operators.catalog_source       | Keycloak custom catalog for Operator                                                                                             |
| intersmash.keycloak.operators.index_image          | Keycloak custom index image for Operator                                                                                         |
| intersmash.keycloak.operators.package_manifest     | Keycloak custom package manifest for Operator                                                                                    |
| intersmash.keycloak.operators.channel              | Keycloak desired channel for Operator                                                                                            |
|                                                    |                                                                                                                                  |
| intersmash.rhsso.image                             | Red Hat Single Sign On 7 image URL                                                                                               |
| intersmash.rhsso.operators.catalog_source          | Red Hat Single Sign On 7 custom catalog for Operator                                                                             |
| intersmash.rhsso.operators.index_image             | Red Hat Single Sign On 7 custom index image for Operator                                                                         |
| intersmash.rhsso.operators.package_manifest        | Red Hat Single Sign On 7 custom package manifest for Operator                                                                    |
| intersmash.rhsso.operators.channel                 | Red Hat Single Sign On 7 desired channel for Operator                                                                            |
|                                                    |                                                                                                                                  |
| intersmash.kafka.operators.catalog_source          | Kafka/Streams for Apache Kafka custom catalog for Operator                                                                            |
| intersmash.kafka.operators.index_image             | Kafka/Streams for Apache Kafka custom index image for Operator                                                                        |
| intersmash.kafka.operators.package_manifest        | Kafka/Streams for Apache Kafka custom package manifest for Operator                                                                   |
| intersmash.kafka.operators.channel                 | Kafka/Streams for Apache Kafka desired channel for Operator                                                                           |
|                                                    |                                                                                                                                  |
| intersmash.activemq.image                          | Apache ActiveMQ Broker/Red Hat AMQ Broker image URL                                                                              |
| intersmash.activemq.init.image                     | ActiveMQ Broker/Red Hat AMQ Broker init image URL                                                                                |
| intersmash.activemq.operators.catalog_source       | ActiveMQ Broker/Red Hat AMQ Broker custom catalog for Operator                                                                   |
| intersmash.activemq.operators.index_image          | ActiveMQ Broker/Red Hat AMQ Broker custom index image for Operators                                                              |
| intersmash.activemq.operators.package_manifest     | ActiveMQ Broker/Red Hat AMQ Broker custom package manifest for Operators                                                         |
| intersmash.activemq.operators.channel              | ActiveMQ Broker/Red Hat AMQ Broker desired channel for Operator                                                                  |
|                                                    |                                                                                                                                  |
| intersmash.hyperfoil.operators.catalog_source      | HyperFoil custom catalog for Operator                                                                                            |
| intersmash.hyperfoil.operators.index_image         | HyperFoil custom index image for Operators                                                                                       |
| intersmash.hyperfoil.operators.package_manifest    | HyperFoil custom package manifest for Operators                                                                                  |
| intersmash.hyperfoil.operators.channel             | HyperFoil desired channel for Operator                                                                                           |
|                                                    |                                                                                                                                  |
| intersmash.mysql.image                             | MySql image URL                                                                                                                  |
| intersmash.postgresql.image                        | PostgreSql image URL                                                                                                             |
|                                                    |                                                                                                                                  |
| wildfly-maven-plugin.groupId                       | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Maven plugin `groupId`                                              |
| wildfly-maven-plugin.artifactId                    | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Maven plugin `artifactId`                                           |
| wildfly-maven-plugin.version                       | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Maven plugin `version`                                              |
| wildfly.ee-feature-pack.location                   | Used by shared configurable deployments: Wildfly/JBoss EAP 8 EE Galleon feature pack location (G:A:V)                            |
| wildfly.feature-pack.location                      | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Galleon feature pack location (G:A:V)                               |
| wildfly.cloud-feature-pack.location                | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Cloud Galleon feature pack location (G:A:V)                         |
| wildfly.datasources-feature-pack.location          | Used by shared configurable deployments: Wildfly/JBoss EAP 8 Datasources Galleon feature pack location (G:A:V)                   |
| wildfly.keycloak-saml-adapter-feature-pack.version | Used by shared configurable deployments: Keycloak SAML Adapter feature pack `version`                                            |
| wildfly.ee-channel.groupId                         | Used by shared configurable deployments: JBoss EAP 8 Channel artifact `groupId`                                                  |
| wildfly.ee-channel.artifactId                      | Used by shared configurable deployments: JBoss EAP 8 Channel artifact `artifactId`                                               |
| wildfly.ee-channel.version                         | Used by shared configurable deployments: JBoss EAP 8 Channel artifact `version`                                                  |
| bom.wildfly-ee.version                             | Used by shared configurable deployments: Wildfly/JBoss BOMs version                                                              |
|                                                    |                                                                                                                                  |
| intersmash.kubernetes.url                          | When testing on Kubernetes, this is the URL that will be used to consume the APIs                                                |
| intersmash.kubernetes.hostname                     | When testing on Kubernetes, this is the host name that Intersmash will use when computing external routes to deployed services   |
| intersmash.kubernetes.token                        | When testing on Kubernetes, the token value will be used to connect to the cluster if defined                                    |
| intersmash.kubernetes.version                      | When testing on Kubernetes, this defines the target Kubernetes version                                                           |
| intersmash.kubernetes.namespace                    | When testing on Kubernetes, the namespace where tests will be executed                                                           |
| intersmash.kubernetes.binary.path                  | When testing on Kubernetes, the path to the local `kubectl` binary                                                               |
| intersmash.kubernetes.binary.cache.enabled         | When testing on Kubernetes, whether to cache the `kubectl` binary                                                                |
| intersmash.kubernetes.binary.cache.path            | When testing on Kubernetes, the path where the `kubectl` binaries should be cached                                               |
| intersmash.kubernetes.admin.username               | When testing on Kubernetes, an administrative credentials' username                                                              |
| intersmash.kubernetes.admin.password               | When testing on Kubernetes, an administrative credentials' password                                                              |
| intersmash.kubernetes.admin.kubeconfig             | When testing on Kubernetes, the path to a `kubeconfig` file holding the configuration for an administrative client               |
| intersmash.kubernetes.admin.token                  | When testing on Kubernetes, the token value will be used to connect to the cluster with administrative rights, if defined        |
| intersmash.kubernetes.master.username              | When testing on Kubernetes, an user credentials' username                                                                        |
| intersmash.kubernetes.master.password              | When testing on Kubernetes, an user credentials' password                                                                        |
| intersmash.kubernetes.master.kubeconfig            | When testing on Kubernetes, the path to a `kubeconfig` file hoding the configuration for a user client                           |
| intersmash.kubernetes.master.token                 | When testing on Kubernetes, the token value will be used to connect to the cluster with user rights, if defined                  |
| intersmash.kubernetes.route_domain                 | When testing on Kubernetes, this is the domain name that Intersmash will use when computing external routes to deployed services |


**NOTE:** When property `intersmash.skip.deploy` is set, the prepared environment should be
configured in the user's implementation of the service interface class, (e.g. `WildflyOperatorApplication`).  It is the user's implementation class that is declared in the `@Service` annotation.
E.g.: If the user implements interface `WildflyOperatorApplication` and registers it as a service, and the user has defined the service `name`
to be "wildfly-operator-app", then a Wildfly operator application with that name should exist in the prepared environment.
This feature is useful to save debugging time during development, where the user can deploy a complex scenario and then enable the property to execute tests following runs.
