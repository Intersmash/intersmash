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
package org.jboss.intersmash.testsuite.junit5;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.engine.execution.ExtensionValuesStore;
import org.junit.jupiter.engine.execution.NamespaceAwareStore;

public class EmptyExtensionContext implements ExtensionContext {
	private final Class<?> testClazz;
	private final ExtensionValuesStore valuesStore = new ExtensionValuesStore(null);

	public EmptyExtensionContext(Class<?> testClazz) {
		this.testClazz = testClazz;
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		return Optional.empty();
	}

	@Override
	public ExtensionContext getRoot() {
		return null;
	}

	@Override
	public String getUniqueId() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public Set<String> getTags() {
		return null;
	}

	@Override
	public Optional<AnnotatedElement> getElement() {
		return Optional.empty();
	}

	@Override
	public Optional<Class<?>> getTestClass() {
		return Optional.of(testClazz);
	}

	@Override
	public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
		return Optional.empty();
	}

	@Override
	public Optional<Object> getTestInstance() {
		return Optional.empty();
	}

	@Override
	public Optional<TestInstances> getTestInstances() {
		return Optional.empty();
	}

	@Override
	public Optional<Method> getTestMethod() {
		return Optional.empty();
	}

	@Override
	public Optional<Throwable> getExecutionException() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getConfigurationParameter(String s) {
		return Optional.empty();
	}

	@Override
	public <T> Optional<T> getConfigurationParameter(String s, Function<String, T> function) {
		return Optional.empty();
	}

	@Override
	public void publishReportEntry(Map<String, String> map) {

	}

	@Override
	public Store getStore(Namespace namespace) {
		return new NamespaceAwareStore(this.valuesStore, namespace);
	}
}
