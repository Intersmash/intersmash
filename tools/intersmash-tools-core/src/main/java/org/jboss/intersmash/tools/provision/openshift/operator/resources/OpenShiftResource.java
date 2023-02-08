package org.jboss.intersmash.tools.provision.openshift.operator.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Interface of common methods for OpenShift resources.
 */
public interface OpenShiftResource<T extends OpenShiftResource<T>> {

	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	/**
	 * Write the yaml representation of object into a file.
	 *
	 * @return a file with object definition in yaml format
	 * @throws IOException if something goes wrong when serializing the resource to a file.
	 */
	default File save() throws IOException {
		File file = File.createTempFile(this.getClass().getSimpleName() + "-", ".yaml");
		return save(file);
	}

	/**
	 * Write the yaml representation of object into a file.
	 *
	 * @param file The file to which the resource should be serialized
	 * @return a file with object definition in yaml format
	 * @throws IOException if something goes wrong when serializing the resource to a file.
	 */
	default File save(File file) throws IOException {
		mapper.writeValue(file, this);
		return file;
	}

	/**
	 * Read a yaml representation of object from a file.
	 *
	 * @param file a file with object definition in yaml format
	 * @return OpenShiftResource setup with values loaded from external source
	 * @throws IOException if something goes wrong when de-serializing the resource from a file.
	 * @see OpenShiftResource#load(OpenShiftResource) which is used to configure the current object with the values from a loaded one.
	 */
	default T load(File file) throws IOException {
		return load((T) mapper.readValue(file, this.getClass()));

	}

	/**
	 * Read a yaml representation of object from a input stream.
	 *
	 * @param inputStream a input stream with object definition in yaml format
	 * @return OpenShiftResource setup with values loaded from external source
	 * @throws IOException if something goes wrong when de-serializing the resource from a file.
	 * @see OpenShiftResource#load(OpenShiftResource) which is used to configure the current object with the values from a loaded one.
	 */
	default T load(InputStream inputStream) throws IOException {
		return load((T) mapper.readValue(inputStream, this.getClass()));
	}

	/**
	 * Read a yaml representation of object from an URL.
	 *
	 * @param url a URL with object definition in yaml format
	 * @return OpenShiftResource setup with values loaded from external source
	 * @throws IOException if something goes wrong when de-serializing the resource from a file.
	 * @see OpenShiftResource#load(OpenShiftResource) which is used to configure the current object with the values from a loaded one.
	 */
	default T load(URL url) throws IOException {
		return load((T) mapper.readValue(url, this.getClass()));
	}

	/**
	 * Setup {@code this} with values loaded from external source.
	 *
	 * @param loaded resource object loaded from external source
	 * @return OpenShiftResource setup with values loaded from external source
	 * @see #load(URL)
	 * @see #load(File)
	 * @see #load(InputStream)
	 */
	T load(T loaded);

}
