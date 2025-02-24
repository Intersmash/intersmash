package org.jboss.intersmash.provision.util.k8s.log.collect;

import java.util.List;

import cz.xtf.core.waiting.WaiterException;
import lombok.Getter;

/**
 * Represents a waiter exception which is related to the analysis of pod logs.
 * Extends {@link WaiterException} to hold a collection of {@link PodLogsReport} instances.
 */
@Getter
public class PodLogsRelatedWaiterException extends WaiterException {

	private final List<PodLogsReport> podLogsReports;

	public PodLogsRelatedWaiterException(List<PodLogsReport> podLogsReports) {
		super();
		this.podLogsReports = podLogsReports;
	}

	public PodLogsRelatedWaiterException(String message, List<PodLogsReport> podLogsReports) {
		super(message);
		this.podLogsReports = podLogsReports;
	}
}
