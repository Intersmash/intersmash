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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts;

import jakarta.xml.ws.WebServiceProvider;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.cxf.sts.operation.TokenIssueOperation;
import org.apache.cxf.sts.operation.TokenValidateOperation;
import org.apache.cxf.sts.service.ServiceMBean;
import org.apache.cxf.sts.service.StaticService;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.validator.SAMLTokenValidator;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;

@WebServiceProvider(serviceName = "SecurityTokenService", portName = "UT_Port", targetNamespace = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/", wsdlLocation = "WEB-INF/wsdl/ws-trust-1.4-service.wsdl")
@EndpointProperties(value = {
		@EndpointProperty(key = "ws-security.signature.username", value = "mystskey"),
		@EndpointProperty(key = "ws-security.signature.properties", value = "stsKeystore.properties"),
		@EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts.STSCallbackHandler"),
		@EndpointProperty(key = "ws-security.validate.token", value = "false")
})
public class SampleSTS extends SecurityTokenServiceProvider {
	public SampleSTS() throws Exception {
		super();

		StaticSTSProperties props = new StaticSTSProperties();
		props.setSignatureCryptoProperties("stsKeystore.properties");
		props.setSignatureUsername("mystskey");
		props.setCallbackHandlerClass(STSCallbackHandler.class.getName());
		props.setIssuer("DoubleItSTSIssuer");

		List<ServiceMBean> services = new LinkedList<ServiceMBean>();
		StaticService service = new StaticService();
		service.setEndpoints(Arrays.asList(System.getenv("SERVICE_ENDPOINT_URL")));
		services.add(service);

		TokenIssueOperation issueOperation = new TokenIssueOperation();
		issueOperation.setServices(services);
		issueOperation.getTokenProviders().add(new SAMLTokenProvider());
		issueOperation.setStsProperties(props);

		TokenValidateOperation validateOperation = new TokenValidateOperation();
		validateOperation.getTokenValidators().add(new SAMLTokenValidator());
		validateOperation.setStsProperties(props);

		this.setIssueOperation(issueOperation);
		this.setValidateOperation(validateOperation);
	}
}
