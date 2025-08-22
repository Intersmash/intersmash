/*
 * Copyright (C) 2025 Red Hat, Inc.
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
package org.jboss.intersmash.provision.util.k8s.log.collect;

import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Pod;
import lombok.Getter;

/**
 * A POJO that stores a pod logs.
 */
@Getter
public class PodLogsReport {

	private static final String POD_LOG_REPORT_BOUNDARY = "\n\n\t==========\n\n";

	private final Pod pod;
	private final String podLog;

	public PodLogsReport(Pod pod, String podLog) {
		this.pod = pod;
		this.podLog = podLog;
	}

	/**
	 * Generate a human-readable text report of a collection of pod logs.
	 * @param reports A collection of {@link PodLogsReport} instances, each holding a given pod logs
	 * @return A  human-readable text report of a collection of pod logs.
	 */
	public static String generate(List<PodLogsReport> reports) {
		if (!reports.isEmpty()) {
			return reports.stream()
					.map(podLogsReport -> String.format(
							"Pod %s failed reporting the following log traces:\n%s\n",
							podLogsReport.getPod().getMetadata().getName(),
							podLogsReport.getPodLog()))
					.collect(Collectors.joining(POD_LOG_REPORT_BOUNDARY));
		}
		return "";
	}
}
