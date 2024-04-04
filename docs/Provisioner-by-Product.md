### Supported Product Provisioners
The following table lists the available Intersmash _provisioners_, based on the product component type.


| Product Component              | Provisioner Name                        | Supports Community project | Supports product    |
|:-------------------------------|:----------------------------------------|:--------------------------|:--------------------|
| **Application Server Images**  |                                         |                           |                     |
| BootableJarOpenShift           | BootableJarImageOpenShiftProvisioner    | :heavy_check_mark:        | :heavy_check_mark:  |
| WildflyImageOpenShift          | WildflyImageOpenShiftProvisioner        | :heavy_check_mark: | :heavy_check_mark: |
| Eap7ImageOpenShift             | Eap7ImageOpenShiftProvisioner           | :x:                       | :heavy_check_mark: |
| **Database Images**            |                                         |||
| MysqlImageOpenShift            | MysqlImageOpenShiftProvisioner          | :heavy_check_mark: | :x:                       |
| PostgreSQLImageOpenShift       | PostgreSQLImageOpenShiftProvisioner     | :heavy_check_mark: | :x:                       |
| **Operator services**          |                                         |||
| ActiveMQ Operator              | ActiveMQOperatorProvisioner             | :heavy_check_mark:        | :heavy_check_mark:  |
| Hyperfoil Operator             | HyperfoilOperatorProvisioner            | :heavy_check_mark:        | :x:                 |
| Infinispan Operator            | InfinispanOperatorProvisioner           | :heavy_check_mark:        | :heavy_check_mark:  |
| Kafka Operator                 | KafkaOperatorProvisioner                | :heavy_check_mark:        | :heavy_check_mark:  |
| Keycloak Operator              | KeycloakOperatorProvisioner             | :heavy_check_mark:        | :heavy_check_mark:  |
| Red Hat SSO Operator           | RhSsoOperatorProvisioner                | :x:                       | :heavy_check_mark: |
| Wildfly Operator               | WildflyOperatorProvisioner              | :heavy_check_mark:        | :heavy_check_mark:  |
| **Templated services**         |                                         |||
| Eap7LegacyS2iBuildTemplate     | Eap7LegacyS2iBuildTemplateProvisioner   | :x:                       | :heavy_check_mark: |
| Eap7TemplateOpenShift          | Eap7TemplateOpenShiftProvisioner        | :x:                       | :heavy_check_mark: |
| PostgreSQLTemplateOpenShift    | PostgreSQLTemplateOpenShiftProvisioner  | :heavy_check_mark:        | :heavy_check_mark:  |
| RhSsoTemplateOpenShift         | RhSsoTemplateOpenShiftProvisioner       | :x:                       | :heavy_check_mark: |
| **Helm Charts**                |                                         |||
| HelmChartOpenShift             | HelmChartOpenShiftProvisioner           | :heavy_check_mark:        | :heavy_check_mark:  |
| WildflyHelmChartOpenShift      | WildflyHelmChartOpenShiftProvisioner    | :heavy_check_mark:        | :heavy_check_mark:  |
