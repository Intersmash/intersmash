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
