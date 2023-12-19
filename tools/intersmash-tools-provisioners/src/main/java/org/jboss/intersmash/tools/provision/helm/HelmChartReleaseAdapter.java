package org.jboss.intersmash.tools.provision.helm;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jboss.intersmash.tools.application.openshift.helm.SerializableHelmChartRelease;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import lombok.NonNull;

/**
 * An adapter that implements a valid {@link org.jboss.intersmash.tools.application.openshift.helm.HelmChartRelease} by exposing an internal generic instance of a POJO
 * that represents the release data and that can be serialized to a values file, as an output for
 * {@link org.jboss.intersmash.tools.application.openshift.helm.HelmChartRelease#toValuesFile()}
 */
public class HelmChartReleaseAdapter<A extends Object> implements SerializableHelmChartRelease {

	protected final A adaptee;
	protected final List<Path> additionalValuesFiles;

	public HelmChartReleaseAdapter(@NonNull A release) {
		this(release, new ArrayList<>());
	}

	public HelmChartReleaseAdapter(@NonNull A release, List<Path> additionalValuesFiles) {
		this.adaptee = release;
		this.additionalValuesFiles = additionalValuesFiles;
	}

	public A getAdaptee() {
		return adaptee;
	}

	@Override
	public Path toValuesFile() {
		// Handle the values file creation
		Path temp;
		try {
			temp = Files.createTempFile("values", ".yaml");
		} catch (IOException e) {
			throw new IllegalStateException("Temporary Helm Chart values file creation failed.", e);
		}
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		try {
			mapper.writeValue(temp.toFile(), adaptee);
		} catch (IOException e) {
			throw new IllegalStateException("Helm Chart values serialization failed.", e);
		}
		return temp;
	}

	/**
	 * Implementors must generate the POJO by deserializing the data at each call and reflect the information held by
	 * the concrete instance
	 * @param valuesFileUrl Source values file URL
	 * @param typeClazz Deserialized POJO type
	 * @return A concrete instance of {@link A} generated from a source values file
	 */
	public static <A> A fromValuesFile(final URL valuesFileUrl, Class<A> typeClazz) {
		if (valuesFileUrl == null) {
			throw new IllegalStateException("No values file provided!");
		}
		try {
			Path valuePath = Path.of(valuesFileUrl.toURI());
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
			return mapper.readValue(valuePath.toFile(), typeClazz);
		} catch (Error | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<Path> getAdditionalValuesFiles() {
		return additionalValuesFiles;
	}
}
