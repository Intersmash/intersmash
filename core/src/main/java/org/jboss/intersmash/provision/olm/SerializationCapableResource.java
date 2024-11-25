/**
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
package org.jboss.intersmash.provision.olm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.fabric8.kubernetes.client.CustomResource;

/**
 * Interface of common methods for OpenShift resources.
 */
public interface SerializationCapableResource<T extends SerializationCapableResource<T>> {

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

	static <T> File save(T data) throws IOException {
		return save(File.createTempFile(data.getClass().getSimpleName() + "-", ".yaml"), data);
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

	static <O> File save(File file, O data) throws IOException {
		mapper.writeValue(file, data);
		return file;
	}

	/**
	 * Read a yaml representation of object from a file.
	 *
	 * @param file a file with object definition in yaml format
	 * @return OpenShiftResource setup with values loaded from external source
	 * @throws IOException if something goes wrong when de-serializing the resource from a file.
	 * @see SerializationCapableResource#load(SerializationCapableResource) which is used to configure the current object with the values from a loaded one.
	 */
	default T load(File file) throws IOException {
		return load((T) mapper.readValue(file, this.getClass()));

	}

	static <CR extends CustomResource> CR load(File file, Class<CR> clazz, CR target) throws IOException {
		CR loaded = mapper.readValue(file, clazz);
		target.setMetadata(loaded.getMetadata());
		target.setSpec(loaded.getSpec());
		return target;
	}

	/**
	 * Read a yaml representation of object from a input stream.
	 *
	 * @param inputStream a input stream with object definition in yaml format
	 * @return OpenShiftResource setup with values loaded from external source
	 * @throws IOException if something goes wrong when de-serializing the resource from a file.
	 * @see SerializationCapableResource#load(SerializationCapableResource) which is used to configure the current object with the values from a loaded one.
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
	 * @see SerializationCapableResource#load(SerializationCapableResource) which is used to configure the current object with the values from a loaded one.
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
