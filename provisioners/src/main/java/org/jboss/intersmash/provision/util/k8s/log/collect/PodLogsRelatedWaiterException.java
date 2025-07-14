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
