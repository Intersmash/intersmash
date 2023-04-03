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
package org.jboss.intersmash.tools.provision.openshift;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cz.xtf.core.http.Https;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.Waiter;
import io.fabric8.kubernetes.api.model.EndpointPort;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;

public class WaitersUtil {
	public static Waiter serviceEndpointsAreReady(OpenShift openShift, String serviceName, int numOfPods, Integer... ports) {
		return new SimpleWaiter(() -> {
			List<Endpoints> endpointsList = openShift.getEndpoints()
					.stream()
					.filter(endpoints1 -> endpoints1.getMetadata().getName().equals(serviceName))
					.collect(Collectors.toList());
			if (endpointsList.size() < 1) {
				return false;
			}
			List<EndpointSubset> subsets = endpointsList.get(0).getSubsets();
			if (subsets.size() < 1) {
				return numOfPods == 0;
			} else {
				// can there be more subsets?
				// according to https://docs.openshift.com/container-platform/3.7/rest_api/api/v1.Endpoints.html yes
				// but what usecase it is ??
				EndpointSubset subset = subsets.get(0);
				if (subset.getNotReadyAddresses().size() > 0) {
					return false;
				}
				long endpointPodsCount = subset.getAddresses().stream()
						.filter(endpointAddress -> endpointAddress.getTargetRef().getKind().equals("Pod"))
						.count();
				if (endpointPodsCount < numOfPods) {
					return false;
				}
				Set<Integer> portSet = subset.getPorts().stream()
						.map(EndpointPort::getPort)
						.collect(Collectors.toSet());

				return portSet.containsAll(Arrays.asList(ports));
			}
		})
				.reason("Wait until the service has all endpoints.");
	}

	public static Waiter routeIsUp(String routeURL) {
		return new SimpleWaiter(
				() -> Https.getCode(routeURL) != 503)
				.reason("Wait until the route is ready to serve.");
	}
}
