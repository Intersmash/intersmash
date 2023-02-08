package org.jboss.intersmash.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.xtf.core.config.XTFConfig;
import cz.xtf.core.openshift.OpenShifts;

public class IntersmashConfig {
	private static final String SKIP_DEPLOY = "intersmash.skip.deploy";
	private static final String SKIP_UNDEPLOY = "intersmash.skip.undeploy";
	private static final String SCRIPT_DEBUG = "intersmash.openshift.script.debug";
	private static final String DEPLOYMENTS_REPOSITORY_URL = "intersmash.deployments.repository.url";
	private static final String DEPLOYMENTS_REPOSITORY_REF = "intersmash.deployments.repository.ref";

	// Default Catalog for Operators
	private static final String DEFAULT_OPERATOR_CATALOG_SOURCE_NAMESPACE = "openshift-marketplace";
	private static final String REDHAT_OPERATOR_CATALOG_SOURCE_NAME = "redhat-operators";
	private static final String COMMUNITY_OPERATOR_CATALOG_SOURCE_NAME = "community-operators";
	private static final String DEFAULT_OPERATOR_CATALOG_SOURCE_NAME = REDHAT_OPERATOR_CATALOG_SOURCE_NAME;

	// Custom Catalogs for operators
	private static final String INFINISPAN_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.infinispan.operators.catalog_source";
	private static final String INFINISPAN_OPERATOR_INDEX_IMAGE = "intersmash.infinispan.operators.index_image";
	private static final String KEYCLOAK_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.keycloak.operators.catalog_source";
	private static final String KEYCLOAK_OPERATOR_INDEX_IMAGE = "intersmash.keycloak.operators.index_image";
	private static final String WILDFLY_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.wildfly.operators.catalog_source";
	private static final String WILDFLY_OPERATOR_INDEX_IMAGE = "intersmash.wildfly.operators.index_image";
	private static final String KAFKA_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.kafka.operators.catalog_source";
	private static final String KAFKA_OPERATOR_INDEX_IMAGE = "intersmash.kafka.operators.index_image";
	private static final String ACTIVEMQ_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.activemq.operators.catalog_source";
	private static final String ACTIVEMQ_OPERATOR_INDEX_IMAGE = "intersmash.activemq.operators.index_image";
	private static final String HYPERFOIL_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.hyperfoil.operators.catalog_source";
	private static final String HYPERFOIL_OPERATOR_INDEX_IMAGE = "intersmash.hyperfoil.operators.index_image";

	// Bootable Jar
	private static final String BOOTABLE_JAR_IMAGE_URL = "intersmash.bootable.jar.image";

	// WILDFLY
	private static final String WILDFLY_IMAGE_URL = "intersmash.wildfly.image";
	private static final String WILDFLY_RUNTIME_IMAGE_URL = "intersmash.wildfly.runtime.image";
	private static final String WILDFLY_EE_FEATURE_PACK_LOCATION = "wildfly.ee-feature-pack.location";
	private static final String WILDFLY_CLOUD_FEATURE_PACK_LOCATION = "wildfly.cloud-feature-pack.location";
	private static final String WILDFLY_EE_CHANNEL_LOCATION = "wildfly.ee-channel.location";
	private static final String WILDFLY_BOMS_EE_SERVER_VERSION = "bom.wildfly-ee.version";
	private static final String WILDFLY_HELM_CHARTS_REPO = "intersmash.wildfly.helm.charts.repo";
	private static final String WILDFLY_HELM_CHARTS_BRANCH = "intersmash.wildfly.helm.charts.branch";
	private static final String WILDFLY_HELM_CHARTS_NAME = "intersmash.wildfly.helm.charts.name";

	//	WildFLy Maven Plugin
	private static final String WILDFLY_MAVEN_PLUGIN_GROUPID = "wildfly-maven-plugin.groupId";
	private static final String WILDFLY_MAVEN_PLUGIN_ARTIFACTID = "wildfly-maven-plugin.artifactId";
	private static final String WILDFLY_MAVEN_PLUGIN_VERSION = "wildfly-maven-plugin.version";
	private static final String MAVEN_MIRROR_URL = "maven-mirror.url";

	// INFINISPAN
	private static final String INFINISPAN_IMAGE_URL = "intersmash.infinispan.image";
	private static final String INFINISPAN_TEMPLATES = "intersmash.infinispan.templates";

	// KEYCLOAK
	private static final String KEYCLOAK_IMAGE_URL = "intersmash.keycloak.image";
	private static final String KEYCLOAK_TEMPLATES = "intersmash.keycloak.templates";

	// ACTIVEMQ
	private static final String ACTIVEMQ_IMAGE_URL = "intersmash.activemq.image";

	// DB
	private static final String MYSQL_IMAGE_URL = "intersmash.mysql.image";
	private static final String PGSQL_IMAGE_URL = "intersmash.postgresql.image";

	public static boolean skipDeploy() {
		return XTFConfig.get(SKIP_DEPLOY, "false").equals("true");
	}

	public static boolean skipUndeploy() {
		return skipDeploy() || XTFConfig.get(SKIP_UNDEPLOY, "false").equals("true");
	}

	public static String defaultOperatorCatalogSourceName() {
		return DEFAULT_OPERATOR_CATALOG_SOURCE_NAME;
	}

	public static String defaultOperatorCatalogSourceNamespace() {
		return DEFAULT_OPERATOR_CATALOG_SOURCE_NAMESPACE;
	}

	public static String infinispanOperatorCatalogSource() {
		return XTFConfig.get(INFINISPAN_OPERATOR_CATALOG_SOURCE_NAME, DEFAULT_OPERATOR_CATALOG_SOURCE_NAME);
	}

	public static String infinispanOperatorIndexImage() {
		return XTFConfig.get(INFINISPAN_OPERATOR_INDEX_IMAGE);
	}

	public static String keycloakOperatorCatalogSource() {
		return XTFConfig.get(KEYCLOAK_OPERATOR_CATALOG_SOURCE_NAME, DEFAULT_OPERATOR_CATALOG_SOURCE_NAME);
	}

	public static String keycloakOperatorIndexImage() {
		return XTFConfig.get(KEYCLOAK_OPERATOR_INDEX_IMAGE);
	}

	public static String wildflyOperatorCatalogSource() {
		return XTFConfig.get(WILDFLY_OPERATOR_CATALOG_SOURCE_NAME, DEFAULT_OPERATOR_CATALOG_SOURCE_NAME);
	}

	public static String wildflyOperatorIndexImage() {
		return XTFConfig.get(WILDFLY_OPERATOR_INDEX_IMAGE);
	}

	public static String kafkaOperatorCatalogSource() {
		return XTFConfig.get(KAFKA_OPERATOR_CATALOG_SOURCE_NAME, DEFAULT_OPERATOR_CATALOG_SOURCE_NAME);
	}

	public static String kafkaOperatorIndexImage() {
		return XTFConfig.get(KAFKA_OPERATOR_INDEX_IMAGE);
	}

	public static String activeMQOperatorCatalogSource() {
		return XTFConfig.get(ACTIVEMQ_OPERATOR_CATALOG_SOURCE_NAME, DEFAULT_OPERATOR_CATALOG_SOURCE_NAME);
	}

	public static String activeMQOperatorIndexImage() {
		return XTFConfig.get(ACTIVEMQ_OPERATOR_INDEX_IMAGE);
	}

	public static String bootableJarImageURL() {
		return XTFConfig.get(BOOTABLE_JAR_IMAGE_URL);
	}

	public static String wildflyImageURL() {
		return XTFConfig.get(WILDFLY_IMAGE_URL);
	}

	public static String wildflyRuntimeImageURL() {
		return XTFConfig.get(WILDFLY_RUNTIME_IMAGE_URL);
	}

	public static String getProductCode(final String image) {
		// truncates "-"
		// e.g. ..fspolti/processserver64-eap70-openshift:1.3 ->  processserver64
		String dashRegexp = ".*/([a-z0-9]+)-.*";
		Pattern dashPattern = Pattern.compile(dashRegexp);
		Matcher dashMatcher = dashPattern.matcher(image);
		if (dashMatcher.matches()) {
			return dashMatcher.replaceAll("$1");
		}

		// truncate also image names without "-"
		// e.g. docker-registry.engineering.redhat.com/ochaloup/wildfly17:JBEAP-xxxxx -> wildfly17
		String noDashRegexp = ".*/([a-z0-9]+):?+.*";
		Pattern noDashPattern = Pattern.compile(noDashRegexp);
		Matcher noDashMatcher = noDashPattern.matcher(image);
		return noDashMatcher.replaceAll("$1");
	}

	public static String getMysqlImage() {
		return XTFConfig.get(MYSQL_IMAGE_URL);
	}

	public static String infinispanImageURL() {
		return XTFConfig.get(INFINISPAN_IMAGE_URL);
	}

	public static String infinispanTemplates() {
		return XTFConfig.get(INFINISPAN_TEMPLATES);
	}

	public static String infinispanProductCode() {
		return getProductCode(infinispanImageURL());
	}

	public static String keycloakImageURL() {
		return XTFConfig.get(KEYCLOAK_IMAGE_URL);
	}

	public static String keycloakTemplates() {
		return XTFConfig.get(KEYCLOAK_TEMPLATES);
	}

	public static String keycloakProductCode() {
		return getProductCode(keycloakImageURL());
	}

	public static String activeMQImageUrl() {
		return XTFConfig.get(ACTIVEMQ_IMAGE_URL);
	}

	public static String getPostgreSQLImage() {
		return XTFConfig.get(PGSQL_IMAGE_URL);
	}

	public static String keycloakImageName() {
		return getImageName(keycloakImageURL());
	}

	public static String scriptDebug() {
		return XTFConfig.get(SCRIPT_DEBUG);
	}

	private static String getImageName(String image) {
		// extract anything after last '/'
		// e.g. ..fspolti/processserver64-eap70-openshift:1.3 ->  processserver64-eap70-openshift:1.3
		String slashRegexp = ".+/(.+)$";
		Pattern slashPattern = Pattern.compile(slashRegexp);
		Matcher slashMatcher = slashPattern.matcher(image);
		if (slashMatcher.matches()) {
			return slashMatcher.group(1);
		}
		return image;
	}

	public static String getOcpVersion() {
		return OpenShifts.getVersion();
	}

	public static boolean isOcp3x() {
		return OpenShifts.getVersion().startsWith("3");
	}

	/**
	 * Git repository URL to deployments repository. Look for the value of {@link #DEPLOYMENTS_REPOSITORY_URL}
	 * in intersmash config. In case the one is not set, try to look into the git config and find a value there. Fallback
	 * to "https://github.com/Intersmash/intersmash.git" if none of above works.
	 *
	 * @return intersmash-deployments git repository url
	 */
	public static String deploymentsRepositoryUrl() {
		return XTFConfig.get(DEPLOYMENTS_REPOSITORY_URL, IntersmashDeploymentsGitHelper.repositoryUrl());
	}

	/**
	 * Git repository REF to deployments repository. Look for the value of {@link #DEPLOYMENTS_REPOSITORY_REF}
	 * in intersmash config. In case the one is not set, try to look into the git config and find a value there. Fallback
	 * to "master" if none of above works.
	 *
	 * @return deployments git repository ref
	 */
	public static String deploymentsRepositoryRef() {
		return XTFConfig.get(DEPLOYMENTS_REPOSITORY_REF, IntersmashDeploymentsGitHelper.repositoryReference());
	}

	public static String hyperfoilOperatorIndexImage() {
		return XTFConfig.get(HYPERFOIL_OPERATOR_INDEX_IMAGE);
	}

	public static String hyperfoilOperatorCatalogSource() {
		return XTFConfig.get(HYPERFOIL_OPERATOR_CATALOG_SOURCE_NAME, COMMUNITY_OPERATOR_CATALOG_SOURCE_NAME);
	}

	public static String getWildflyMavenPluginGroupId() {
		return XTFConfig.get(WILDFLY_MAVEN_PLUGIN_GROUPID);
	}

	public static String getWildflyMavenPluginArtifactId() {
		return XTFConfig.get(WILDFLY_MAVEN_PLUGIN_ARTIFACTID);
	}

	public static String getWildflyMavenPluginVersion() {
		return XTFConfig.get(WILDFLY_MAVEN_PLUGIN_VERSION);
	}

	public static String getWildflyEeFeaturePackLocation() {
		return XTFConfig.get(WILDFLY_EE_FEATURE_PACK_LOCATION);
	}

	public static String getWildflyCloudFeaturePackLocation() {
		return XTFConfig.get(WILDFLY_CLOUD_FEATURE_PACK_LOCATION);
	}

	public static String getWildflyEeChannelLocation() {
		return XTFConfig.get(WILDFLY_EE_CHANNEL_LOCATION);
	}

	public static String getWildflyBomsEeServerVersion() {
		return XTFConfig.get(WILDFLY_BOMS_EE_SERVER_VERSION);
	}

	public static String getMavenMirrorUrl() {
		return XTFConfig.get(MAVEN_MIRROR_URL);
	}

	public static String getWildflyHelmChartsName() {
		return XTFConfig.get(WILDFLY_HELM_CHARTS_NAME);
	}

	public static String getWildflyHelmChartsRepo() {
		return XTFConfig.get(WILDFLY_HELM_CHARTS_REPO);
	}

	public static String getWildflyHelmChartsBranch() {
		return XTFConfig.get(WILDFLY_HELM_CHARTS_BRANCH);
	}
}
