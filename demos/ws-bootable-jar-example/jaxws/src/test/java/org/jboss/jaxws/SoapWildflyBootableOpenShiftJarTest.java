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

package org.jboss.jaxws;

import jakarta.xml.ws.BindingProvider;

import java.net.URL;

import javax.xml.namespace.QName;

import org.assertj.core.api.Assertions;
import org.jboss.intersmash.tools.annotations.Intersmash;
import org.jboss.intersmash.tools.annotations.Service;
import org.jboss.intersmash.tools.annotations.ServiceUrl;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Intersmash({
		@Service(SoapWildflyBootableOpenShiftJarApplication.class)
})
public class SoapWildflyBootableOpenShiftJarTest {
	@ServiceUrl(SoapWildflyBootableOpenShiftJarApplication.class)
	private String appOpenShiftUrl;

	@Test
	public void testPing() throws Exception {
		URL baseURL = new URL(appOpenShiftUrl + "/EndpointServiceSERVICE");
		QName serviceName = new QName("http://org.jboss.ws/cxf/container", "EndpointServiceSERVICE");
		URL wsdlURL = new URL(baseURL + "?wsdl");
		jakarta.xml.ws.Service service = jakarta.xml.ws.Service.create(wsdlURL, serviceName);

		Endpoint proxy = service.getPort(Endpoint.class);

		BindingProvider bp = (BindingProvider) proxy;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toExternalForm());

		String greeting = proxy.ping();
		Assertions.assertThat(greeting).contains("pong");
	}

	@Test
	public void testGreet() throws Exception {
		URL baseURL = new URL(appOpenShiftUrl + "/EndpointServiceSERVICE");
		QName serviceName = new QName("http://org.jboss.ws/cxf/container", "EndpointServiceSERVICE");
		URL wsdlURL = new URL(baseURL + "?wsdl");
		jakarta.xml.ws.Service service = jakarta.xml.ws.Service.create(wsdlURL, serviceName);

		Endpoint proxy = service.getPort(Endpoint.class);

		BindingProvider bp = (BindingProvider) proxy;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toExternalForm());

		String greeting = proxy.greetings("Bob");
		Assertions.assertThat(greeting).contains("Bob, hello from WildFly");
	}
}
