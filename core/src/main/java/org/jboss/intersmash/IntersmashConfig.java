/*
 * Copyright (C) 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.intersmash;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.xtf.core.config.XTFConfig;
import cz.xtf.core.openshift.OpenShift;

public class IntersmashConfig {
	private static final String SKIP_DEPLOY = "intersmash.skip.deploy";
	private static final String SKIP_UNDEPLOY = "intersmash.skip.undeploy";
	private static final String SCRIPT_DEBUG = "intersmash.openshift.script.debug";
	private static final String DEPLOYMENTS_REPOSITORY_URL = "intersmash.deployments.repository.url";
	private static final String DEPLOYMENTS_REPOSITORY_REF = "intersmash.deployments.repository.ref";

	// Default Catalog for Operators
	private static final String KUBERNETES_OPERATOR_CATALOG_SOURCE_NAMESPACE = "olm";
	private static final String OPENSHIFT_OPERATOR_CATALOG_SOURCE_NAMESPACE = "openshift-marketplace";
	private static final String DEFAULT_OPERATOR_CATALOG_SOURCE_NAMESPACE = KUBERNETES_OPERATOR_CATALOG_SOURCE_NAMESPACE;
	private static final String REDHAT_OPERATOR_CATALOG_SOURCE_NAME = "redhat-operators";
	private static final String COMMUNITY_OPERATOR_CATALOG_SOURCE_NAME = "community-operators";
	private static final String OPERATORHUB_IO_OPERATOR_CATALOG_SOURCE_NAME = "operatorhubio-catalog";
	private static final String DEFAULT_OPERATOR_CATALOG_SOURCE_NAME = COMMUNITY_OPERATOR_CATALOG_SOURCE_NAME;
	private static final String OLM_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.olm.operators.catalog_source";
	private static final String OLM_OPERATOR_CATALOG_SOURCE_NAMESPACE = "intersmash.olm.operators.namespace";

	// Custom Catalogs for operators
	private static final String INFINISPAN_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.infinispan.operators.catalog_source";
	private static final String INFINISPAN_OPERATOR_INDEX_IMAGE = "intersmash.infinispan.operators.index_image";
	private static final String INFINISPAN_OPERATOR_CHANNEL = "intersmash.infinispan.operators.channel";
	private static final String INFINISPAN_OPERATOR_PACKAGE_MANIFEST = "intersmash.infinispan.operators.package_manifest";
	private static final String COMMUNITY_INFINISPAN_OPERATOR_PACKAGE_MANIFEST = "infinispan";
	private static final String PRODUCT_INFINISPAN_OPERATOR_PACKAGE_MANIFEST = "datagrid";
	private static final String DEFAULT_INFINISPAN_OPERATOR_PACKAGE_MANIFEST = COMMUNITY_INFINISPAN_OPERATOR_PACKAGE_MANIFEST;
	private static final String RHSSO_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.rhsso.operators.catalog_source";
	private static final String KEYCLOAK_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.keycloak.operators.catalog_source";
	private static final String RHSSO_OPERATOR_INDEX_IMAGE = "intersmash.rhsso.operators.index_image";
	private static final String KEYCLOAK_OPERATOR_INDEX_IMAGE = "intersmash.keycloak.operators.index_image";
	private static final String RHSSO_OPERATOR_CHANNEL = "intersmash.rhsso.operators.channel";
	private static final String KEYCLOAK_OPERATOR_CHANNEL = "intersmash.keycloak.operators.channel";
	private static final String RHSSO_OPERATOR_PACKAGE_MANIFEST = "intersmash.rhsso.operators.package_manifest";
	private static final String KEYCLOAK_OPERATOR_PACKAGE_MANIFEST = "intersmash.keycloak.operators.package_manifest";
	private static final String COMMUNITY_KEYCLOAK_OPERATOR_PACKAGE_MANIFEST = "keycloak-operator";
	private static final String PRODUCT_KEYCLOAK_OPERATOR_PACKAGE_MANIFEST = "rhsso-operator";
	private static final String DEFAULT_KEYCLOAK_OPERATOR_PACKAGE_MANIFEST = COMMUNITY_KEYCLOAK_OPERATOR_PACKAGE_MANIFEST;
	private static final String WILDFLY_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.wildfly.operators.catalog_source";
	private static final String WILDFLY_OPERATOR_INDEX_IMAGE = "intersmash.wildfly.operators.index_image";
	private static final String WILDFLY_OPERATOR_CHANNEL = "intersmash.wildfly.operators.channel";
	private static final String WILDFLY_OPERATOR_PACKAGE_MANIFEST = "intersmash.wildfly.operators.package_manifest";
	private static final String COMMUNITY_WILDFLY_OPERATOR_PACKAGE_MANIFEST = "wildfly";
	private static final String PRODUCT_WILDFLY_OPERATOR_PACKAGE_MANIFEST = "eap";
	private static final String DEFAULT_WILDFLY_OPERATOR_PACKAGE_MANIFEST = COMMUNITY_WILDFLY_OPERATOR_PACKAGE_MANIFEST;
	private static final String KAFKA_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.kafka.operators.catalog_source";
	private static final String KAFKA_OPERATOR_INDEX_IMAGE = "intersmash.kafka.operators.index_image";
	private static final String KAFKA_OPERATOR_CHANNEL = "intersmash.kafka.operators.channel";
	private static final String KAFKA_OPERATOR_PACKAGE_MANIFEST = "intersmash.kafka.operators.package_manifest";
	private static final String COMMUNITY_KAFKA_OPERATOR_PACKAGE_MANIFEST = "strimzi-kafka-operator";
	private static final String PRODUCT_KAFKA_OPERATOR_PACKAGE_MANIFEST = "amq-streams";
	private static final String DEFAULT_KAFKA_OPERATOR_PACKAGE_MANIFEST = COMMUNITY_KAFKA_OPERATOR_PACKAGE_MANIFEST;
	private static final String ACTIVEMQ_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.activemq.operators.catalog_source";
	private static final String ACTIVEMQ_OPERATOR_INDEX_IMAGE = "intersmash.activemq.operators.index_image";
	private static final String ACTIVEMQ_OPERATOR_CHANNEL = "intersmash.activemq.operators.channel";
	private static final String ACTIVEMQ_OPERATOR_PACKAGE_MANIFEST = "intersmash.activemq.operators.package_manifest";
	private static final String COMMUNITY_ACTIVEMQ_OPERATOR_PACKAGE_MANIFEST = "artemis";
	private static final String PRODUCT_ACTIVEMQ_OPERATOR_PACKAGE_MANIFEST = "amq-broker-rhel8";
	private static final String DEFAULT_ACTIVEMQ_OPERATOR_PACKAGE_MANIFEST = COMMUNITY_ACTIVEMQ_OPERATOR_PACKAGE_MANIFEST;
	private static final String HYPERFOIL_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.hyperfoil.operators.catalog_source";
	private static final String HYPERFOIL_OPERATOR_INDEX_IMAGE = "intersmash.hyperfoil.operators.index_image";
	private static final String HYPERFOIL_OPERATOR_CHANNEL = "intersmash.hyperfoil.operators.channel";
	private static final String HYPERFOIL_OPERATOR_PACKAGE_MANIFEST = "intersmash.hyperfoil.operators.package_manifest";
	private static final String COMMUNITY_HYPERFOIL_OPERATOR_PACKAGE_MANIFEST = "hyperfoil-bundle";
	private static final String DEFAULT_HYPERFOIL_OPERATOR_PACKAGE_MANIFEST = COMMUNITY_HYPERFOIL_OPERATOR_PACKAGE_MANIFEST;
	private static final String OPEN_DATA_HUB_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.odh.operators.catalog_source";
	private static final String OPEN_DATA_HUB_OPERATOR_INDEX_IMAGE = "intersmash.odh.operators.index_image";
	private static final String OPEN_DATA_HUB_OPERATOR_CHANNEL = "intersmash.odh.operators.channel";
	private static final String OPEN_DATA_HUB_OPERATOR_PACKAGE_MANIFEST = "intersmash.odh.operators.package_manifest";
	private static final String COMMUNITY_OPEN_DATA_HUB_OPERATOR_PACKAGE_MANIFEST = "opendatahub-operator";
	private static final String DEFAULT_OPEN_DATA_HUB_OPERATOR_PACKAGE_MANIFEST = COMMUNITY_OPEN_DATA_HUB_OPERATOR_PACKAGE_MANIFEST;

	private static final String OPENSHIFT_AI_OPERATOR_CATALOG_SOURCE_NAME = "intersmash.rhods.operators.catalog_source";
	private static final String OPENSHIFT_AI_OPERATOR_INDEX_IMAGE = "intersmash.rhods.operators.index_image";
	private static final String OPENSHIFT_AI_OPERATOR_CHANNEL = "intersmash.rhods.operators.channel";
	private static final String OPENSHIFT_AI_OPERATOR_PACKAGE_MANIFEST = "intersmash.rhods.operators.package_manifest";
	private static final String PRODUCT_OPENSHIFT_AI_OPERATOR_PACKAGE_MANIFEST = "rhods-operator";
	private static final String DEFAULT_OPENSHIFT_AI_OPERATOR_PACKAGE_MANIFEST = PRODUCT_OPENSHIFT_AI_OPERATOR_PACKAGE_MANIFEST;

	// Bootable Jar
	private static final String BOOTABLE_JAR_IMAGE_URL = "intersmash.bootable.jar.image";

	// WILDFLY
	private static final String WILDFLY_IMAGE_URL = "intersmash.wildfly.image";
	private static final String WILDFLY_RUNTIME_IMAGE_URL = "intersmash.wildfly.runtime.image";
	private static final String WILDFLY_HELM_CHARTS_REPO = "intersmash.wildfly.helm.charts.repo";
	private static final String WILDFLY_HELM_CHARTS_BRANCH = "intersmash.wildfly.helm.charts.branch";
	private static final String WILDFLY_HELM_CHARTS_NAME = "intersmash.wildfly.helm.charts.name";

	// EAP 7.z (i.e. Jakarta EE 8 based WildFly)
	private static final String EAP7_IMAGE_URL = "intersmash.eap7.image";
	private static final String EAP7_RUNTIME_IMAGE_URL = "intersmash.eap7.runtime.image";
	private static final String EAP7_TEMPLATES_BASE_URL = "intersmash.eap7.templates.base.url";
	private static final String EAP7_TEMPLATES_PATH = "intersmash.eap7.templates.path";

	// INFINISPAN
	private static final String INFINISPAN_IMAGE_URL = "intersmash.infinispan.image";

	// KEYCLOAK/RHSSO
	private static final String KEYCLOAK_IMAGE_URL = "intersmash.keycloak.image";
	private static final String RHSSO_IMAGE_URL = "intersmash.rhsso.image";
	private static final String RHSSO_TEMPLATES = "intersmash.rhsso.templates";

	// ACTIVEMQ
	private static final String ACTIVEMQ_IMAGE_URL = "intersmash.activemq.image";
	private static final String ACTIVEMQ_INIT_IMAGE_URL = "intersmash.activemq.init.image";

	// DB
	private static final String MYSQL_IMAGE_URL = "intersmash.mysql.image";
	private static final String PGSQL_IMAGE_URL = "intersmash.postgresql.image";

	public static boolean skipDeploy() {
		return XTFConfig.get(SKIP_DEPLOY, "false").equals("true");
	}

	public static boolean skipUndeploy() {
		return skipDeploy() || XTFConfig.get(SKIP_UNDEPLOY, "false").equals("true");
	}

	public static String[] getKnownCatalogSources() {
		return new String[] { COMMUNITY_OPERATOR_CATALOG_SOURCE_NAME, REDHAT_OPERATOR_CATALOG_SOURCE_NAME };
	}

	public static String defaultOperatorCatalogSourceName() {
		return XTFConfig.get(OLM_OPERATOR_CATALOG_SOURCE_NAME, DEFAULT_OPERATOR_CATALOG_SOURCE_NAME);
	}

	public static String defaultOperatorCatalogSourceNamespace() {
		return XTFConfig.get(OLM_OPERATOR_CATALOG_SOURCE_NAMESPACE, DEFAULT_OPERATOR_CATALOG_SOURCE_NAMESPACE);
	}

	public static String infinispanOperatorCatalogSource() {
		return XTFConfig.get(INFINISPAN_OPERATOR_CATALOG_SOURCE_NAME, defaultOperatorCatalogSourceName());
	}

	public static String infinispanOperatorIndexImage() {
		return XTFConfig.get(INFINISPAN_OPERATOR_INDEX_IMAGE);
	}

	public static String infinispanOperatorChannel() {
		return XTFConfig.get(INFINISPAN_OPERATOR_CHANNEL);
	}

	public static String infinispanOperatorPackageManifest() {
		return XTFConfig.get(INFINISPAN_OPERATOR_PACKAGE_MANIFEST, DEFAULT_INFINISPAN_OPERATOR_PACKAGE_MANIFEST);
	}

	public static String rhSsoOperatorCatalogSource() {
		return XTFConfig.get(RHSSO_OPERATOR_CATALOG_SOURCE_NAME, defaultOperatorCatalogSourceName());
	}

	public static String rhSsoOperatorIndexImage() {
		return XTFConfig.get(RHSSO_OPERATOR_INDEX_IMAGE);
	}

	public static String rhSsoOperatorChannel() {
		return XTFConfig.get(RHSSO_OPERATOR_CHANNEL);
	}

	public static String rhSsoOperatorPackageManifest() {
		return XTFConfig.get(RHSSO_OPERATOR_PACKAGE_MANIFEST, PRODUCT_KEYCLOAK_OPERATOR_PACKAGE_MANIFEST);
	}

	public static String wildflyOperatorCatalogSource() {
		return XTFConfig.get(WILDFLY_OPERATOR_CATALOG_SOURCE_NAME, defaultOperatorCatalogSourceName());
	}

	public static String wildflyOperatorIndexImage() {
		return XTFConfig.get(WILDFLY_OPERATOR_INDEX_IMAGE);
	}

	public static String wildflyOperatorChannel() {
		return XTFConfig.get(WILDFLY_OPERATOR_CHANNEL);
	}

	public static String wildflyOperatorPackageManifest() {
		return XTFConfig.get(WILDFLY_OPERATOR_PACKAGE_MANIFEST, DEFAULT_WILDFLY_OPERATOR_PACKAGE_MANIFEST);
	}

	public static String kafkaOperatorCatalogSource() {
		return XTFConfig.get(KAFKA_OPERATOR_CATALOG_SOURCE_NAME, defaultOperatorCatalogSourceName());
	}

	public static String kafkaOperatorIndexImage() {
		return XTFConfig.get(KAFKA_OPERATOR_INDEX_IMAGE);
	}

	public static String kafkaOperatorChannel() {
		return XTFConfig.get(KAFKA_OPERATOR_CHANNEL);
	}

	public static String kafkaOperatorPackageManifest() {
		return XTFConfig.get(KAFKA_OPERATOR_PACKAGE_MANIFEST, DEFAULT_KAFKA_OPERATOR_PACKAGE_MANIFEST);
	}

	public static String activeMQOperatorCatalogSource() {
		return XTFConfig.get(ACTIVEMQ_OPERATOR_CATALOG_SOURCE_NAME, defaultOperatorCatalogSourceName());
	}

	public static String activeMQOperatorIndexImage() {
		return XTFConfig.get(ACTIVEMQ_OPERATOR_INDEX_IMAGE);
	}

	public static String activeMQOperatorChannel() {
		return XTFConfig.get(ACTIVEMQ_OPERATOR_CHANNEL);
	}

	public static String activeMQOperatorPackageManifest() {
		return XTFConfig.get(ACTIVEMQ_OPERATOR_PACKAGE_MANIFEST, DEFAULT_ACTIVEMQ_OPERATOR_PACKAGE_MANIFEST);
	}

	public static String hyperfoilOperatorCatalogSource() {
		return XTFConfig.get(HYPERFOIL_OPERATOR_CATALOG_SOURCE_NAME, defaultOperatorCatalogSourceName());
	}

	public static String hyperfoilOperatorIndexImage() {
		return XTFConfig.get(HYPERFOIL_OPERATOR_INDEX_IMAGE);
	}

	public static String hyperfoilOperatorChannel() {
		return XTFConfig.get(HYPERFOIL_OPERATOR_CHANNEL);
	}

	public static String hyperfoilOperatorPackageManifest() {
		return XTFConfig.get(HYPERFOIL_OPERATOR_PACKAGE_MANIFEST, DEFAULT_HYPERFOIL_OPERATOR_PACKAGE_MANIFEST);
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

	public static String eap7ImageURL() {
		return XTFConfig.get(EAP7_IMAGE_URL);
	}

	public static String eap7RuntimeImageUrl() {
		return XTFConfig.get(EAP7_RUNTIME_IMAGE_URL);
	}

	public static String eap7ProductCode() {
		final String image = eap7ImageURL();
		if (image.matches(".*eap-xp\\d+.*")) {
			return image.replaceFirst(".*eap-xp(\\d+).*", "eap-xp$1");
		} else if (image.matches(".*eap\\d\\d.*")) {
			return image.replaceFirst(".*eap(\\d\\d?).*", "eap$1");
		} else {
			return IntersmashConfig.getProductCode(image);
		}
	}

	public static String eap7Templates() {
		return XTFConfig.get(EAP7_TEMPLATES_BASE_URL) + XTFConfig.get(EAP7_TEMPLATES_PATH);
	}

	public static String eap7ImageStreams() {
		return XTFConfig.get(EAP7_TEMPLATES_BASE_URL);
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

	public static String infinispanProductCode() {
		return getProductCode(infinispanImageURL());
	}

	public static String rhSsoImageURL() {
		return XTFConfig.get(RHSSO_IMAGE_URL);
	}

	public static String keycloakImageURL() {
		return XTFConfig.get(KEYCLOAK_IMAGE_URL);
	}

	public static String rhSsoProductCode() {
		return getProductCode(rhSsoImageURL());
	}

	public static String rhSsoTemplates() {
		return XTFConfig.get(RHSSO_TEMPLATES);
	}

	public static String activeMQImageUrl() {
		return XTFConfig.get(ACTIVEMQ_IMAGE_URL);
	}

	public static String activeMQInitImageUrl() {
		return XTFConfig.get(ACTIVEMQ_INIT_IMAGE_URL);
	}

	public static String getPostgreSQLImage() {
		return XTFConfig.get(PGSQL_IMAGE_URL);
	}

	public static String rhSsoImageName() {
		return getImageName(rhSsoImageURL());
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

	public static String getOcpVersion(OpenShift openShift) {
		return String.format("%s.%s", openShift.getVersion().getMajor(), openShift.getVersion().getMinor());
	}

	public static boolean isOcp3x(OpenShift openShift) {
		return openShift.getVersion().getMajor().startsWith("3");
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

	public static String getWildflyHelmChartsName() {
		return XTFConfig.get(WILDFLY_HELM_CHARTS_NAME);
	}

	public static String getWildflyHelmChartsRepo() {
		return XTFConfig.get(WILDFLY_HELM_CHARTS_REPO);
	}

	public static String getWildflyHelmChartsBranch() {
		return XTFConfig.get(WILDFLY_HELM_CHARTS_BRANCH);
	}

	public static String keycloakOperatorCatalogSource() {
		return XTFConfig.get(KEYCLOAK_OPERATOR_CATALOG_SOURCE_NAME, defaultOperatorCatalogSourceName());
	}

	public static String keycloakOperatorIndexImage() {
		return XTFConfig.get(KEYCLOAK_OPERATOR_INDEX_IMAGE);
	}

	public static String keycloakOperatorChannel() {
		return XTFConfig.get(KEYCLOAK_OPERATOR_CHANNEL);
	}

	public static String keycloakOperatorPackageManifest() {
		return XTFConfig.get(KEYCLOAK_OPERATOR_PACKAGE_MANIFEST, DEFAULT_KEYCLOAK_OPERATOR_PACKAGE_MANIFEST);
	}

	/**
	 * Read the configuration property for the Open Data Hub Operator catalog source, i.e. {@code intersmash.odh.operators.catalog_source}.
	 *
	 * @return The value for the {@code intersmash.odh.operators.catalog_source} property or the default catalog source,
	 * i.e. the value of {@code intersmash.olm.operators.catalog_source} property.
	 */
	public static String openDataHubOperatorCatalogSource() {
		return XTFConfig.get(OPEN_DATA_HUB_OPERATOR_CATALOG_SOURCE_NAME, defaultOperatorCatalogSourceName());
	}

	/**
	 * Read the configuration property for the Open Data Hub Operator index image, i.e. {@code intersmash.odh.operators.index_image}.
	 *
	 * @return The value for the {@code intersmash.odh.operators.index_image} property, representing a custom index image.
	 */
	public static String openDataHubOperatorIndexImage() {
		return XTFConfig.get(OPEN_DATA_HUB_OPERATOR_INDEX_IMAGE);
	}

	/**
	 * Read the configuration property for the Open Data Hub Operator channel to be used, i.e. {@code intersmash.odh.operators.channel}.
	 *
	 * @return The value for the {@code intersmash.odh.operators.channel} property. If not provided the default operator
	 * channel is used.
	 */
	public static String openDataHubOperatorChannel() {
		return XTFConfig.get(OPEN_DATA_HUB_OPERATOR_CHANNEL);
	}

	/**
	 * Read the configuration property for the Open Data Hub Operator channel to be used, i.e. {@code intersmash.odh.operators.package_manifest}.
	 *
	 * @return The value for the {@code intersmash.odh.operators.package_manifest} property or the default package manifest
	 * that should be used for this operator, i.e. {@code opendatahub-operator} property.
	 */
	public static String openDataHubOperatorPackageManifest() {
		return XTFConfig.get(OPEN_DATA_HUB_OPERATOR_PACKAGE_MANIFEST, DEFAULT_OPEN_DATA_HUB_OPERATOR_PACKAGE_MANIFEST);
	}

	/**
	 * Read the configuration property for the OpenShift AI Operator catalog source, i.e. {@code intersmash.rhods.operators.catalog_source}.
	 *
	 * @return The value for the {@code intersmash.rhods.operators.catalog_source} property or the default catalog source,
	 * i.e. the value of {@code intersmash.olm.operators.catalog_source} property.
	 */
	public static String openShiftAIOperatorCatalogSource() {
		return XTFConfig.get(OPENSHIFT_AI_OPERATOR_CATALOG_SOURCE_NAME, defaultOperatorCatalogSourceName());
	}

	/**
	 * Read the configuration property for the OpenShift AI Operator index image, i.e. {@code intersmash.rhods.operators.index_image}.
	 *
	 * @return The value for the {@code intersmash.rhods.operators.index_image} property, representing a custom index image.
	 */
	public static String openShiftAIOperatorIndexImage() {
		return XTFConfig.get(OPENSHIFT_AI_OPERATOR_INDEX_IMAGE);
	}

	/**
	 * Read the configuration property for the OpenShift AI Operator channel to be used, i.e. {@code intersmash.rhods.operators.channel}.
	 *
	 * @return The value for the {@code intersmash.rhods.operators.channel} property. If not provided the default operator
	 * channel is used.
	 */
	public static String openShiftAIOperatorChannel() {
		return XTFConfig.get(OPENSHIFT_AI_OPERATOR_CHANNEL);
	}

	/**
	 * Read the configuration property for the OpenShift AI Operator channel to be used, i.e. {@code intersmash.rhods.operators.package_manifest}.
	 *
	 * @return The value for the {@code intersmash.rhods.operators.package_manifest} property or the default package manifest
	 * that should be used for this operator, i.e. {@code rhods-operator} property.
	 */
	public static String openShiftAIOperatorPackageManifest() {
		return XTFConfig.get(OPENSHIFT_AI_OPERATOR_PACKAGE_MANIFEST, DEFAULT_OPENSHIFT_AI_OPERATOR_PACKAGE_MANIFEST);
	}
}
