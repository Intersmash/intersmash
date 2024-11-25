### Supported Product Provisioners
The following table lists the available Intersmash _provisioners_, based on the product component type.
The provisioner is selected by a factory, based on the _Application class_ type that a given service implements.

| Product Component                                                                     | Provisioner class                           | Community support  | Product support    |
|:--------------------------------------------------------------------------------------|:--------------------------------------------|:-------------------|:-------------------|
| **Application Server Images**                                                         |                                             |                    |                    |
| **WildFly & JBoss EAP 8 Bootable Jar** (see BootableJarOpenShiftApplication)          | WildflyBootableJarImageOpenShiftProvisioner | :heavy_check_mark: | :heavy_check_mark: |
| **WildFly & JBoss EAP 8 s2i** (see WildflyImageOpenShiftApplication)                  | WildflyImageOpenShiftProvisioner            | :heavy_check_mark: | :heavy_check_mark: |
| **JBoss EAP 7 s2i** (see Eap7ImageOpenShiftApplication)                               | Eap7ImageOpenShiftProvisioner               | :x:                | :heavy_check_mark: |
| **Database Images**                                                                   |                                             |                    |                    |
| **MySQL Image** (see MysqlImageOpenShiftApplication)                                  | MysqlImageOpenShiftProvisioner              | :heavy_check_mark: | :x:                |
| **PostgreSQL Image** (see PostgreSQLImageOpenShiftApplication)                        | PostgreSQLImageOpenShiftProvisioner         | :heavy_check_mark: | :x:                |
| **Operator based services**                                                           |                                             |                    |                    |
| **ActiveMQ Artemis & Red Hat AMQ Broker Operator** (see ActiveMQOperatorApplication)  | ActiveMQOperatorProvisioner                 | :heavy_check_mark: | :heavy_check_mark: |
| **Hyperfoil Operator** - Kubernetes (see HyperfoilOperatorApplication)                | HyperfoilKubernetesOperatorProvisioner      | :heavy_check_mark: | :x:                |
| **Hyperfoil Operator** - OpenShift (see HyperfoilOperatorApplication)                 | HyperfoilOpenShiftOperatorProvisioner       | :heavy_check_mark: | :x:                |
| **Infinispan & Red Hat DataGrid Operator** (see InfinispanOperatorApplication)        | InfinispanOperatorProvisioner               | :heavy_check_mark: | :heavy_check_mark: |
| **Kafka/Red Hat AMQ Streams Operator** (see KafkaOperatorApplication)                 | KafkaOperatorProvisioner                    | :heavy_check_mark: | :heavy_check_mark: |
| **Keycloak & Red Hat Build of Keycloak Operator** (see KeycloakOperatorApplication)   | KeycloakOperatorProvisioner                 | :heavy_check_mark: | :heavy_check_mark: |
| **Red Hat SSO Operator** (see RhSsoOperatorApplication) - **DEPRECATED**              | RhSsoOperatorProvisioner                    | :x:                | :heavy_check_mark: |
| **WildFly & JBoss EAP 8 Operator** (see WildflyOperatorApplication)                   | WildflyOperatorProvisioner                  | :heavy_check_mark: | :heavy_check_mark: |
| **Template based services**                                                           |                                             |                    |                    |
| **JBoss EAP 7 Legacy s2i Build Template** (see Eap7LegacyS2iBuildTemplateApplication) | Eap7LegacyS2iBuildTemplateProvisioner       | :x:                | :heavy_check_mark: |
| **JBoss EAP 7 Legacy s2i Deployment Template** (see Eap7TemplateOpenShiftApplication) | Eap7TemplateOpenShiftProvisioner            | :x:                | :heavy_check_mark: |
| **PostgreSQL Template** (see PostgreSQLTemplateOpenShiftApplication)                  | PostgreSQLTemplateOpenShiftProvisioner      | :heavy_check_mark: | :heavy_check_mark: |
| **Red Hat SSO Template** (see RhSsoTemplateOpenShiftApplication) - **DEPRECATED**     | RhSsoTemplateOpenShiftProvisioner           | :x:                | :heavy_check_mark: |
| **Helm Charts**                                                                       |                                             |                    |                    |
| **WildFly & JBoss EAP 8 Helm Charts** (see WildflyHelmChartOpenShiftApplication)      | WildflyHelmChartOpenShiftProvisioner        | :heavy_check_mark: | :heavy_check_mark: |
