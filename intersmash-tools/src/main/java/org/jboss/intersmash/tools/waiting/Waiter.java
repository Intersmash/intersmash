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
package org.jboss.intersmash.tools.waiting;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jboss.intersmash.tools.config.WaitingConfig;
import org.jboss.intersmash.tools.waiting.failfast.FailFastCheck;
import org.slf4j.event.Level;

import lombok.extern.slf4j.Slf4j;

public interface Waiter {
	long DEFAULT_INTERVAL = 1_000L;

	Waiter timeout(long millis);

	Waiter timeout(TimeUnit timeUnit, long t);

	Waiter interval(long millis);

	Waiter interval(TimeUnit timeUnit, long t);

	Waiter reason(String reason);

	Waiter logPoint(LogPoint logPoint);

	default Waiter level(Level level) {
		throw new UnsupportedOperationException("Method level hasn't been implemented.");
	}

	default Waiter onIteration(Runnable runnable) {
		throw new UnsupportedOperationException("Method onIteration hasn't been implemented.");
	}

	default Waiter onSuccess(Runnable runnable) {
		throw new UnsupportedOperationException("Method onSuccess hasn't been implemented.");
	}

	default Waiter onFailure(Runnable runnable) {
		throw new UnsupportedOperationException("Method onFailure hasn't been implemented.");
	}

	default Waiter onTimeout(Runnable runnable) {
		throw new UnsupportedOperationException("Method onTimeout hasn't been implemented.");
	}

	default Waiter failFast(FailFastCheck failFast) {
		throw new UnsupportedOperationException("Method failFast hasn't been implemented.");
	}

	boolean waitFor();

	@Slf4j
	enum LogPoint {
		NONE,
		START,
		END,
		BOTH;

		public void logStart(String reason, long millis) {
			logStart(reason, millis, WaitingConfig.level());
		}

		public void logStart(String reason, long millis, Level level) {
			if (this.equals(START) || this.equals(BOTH))
				logMessage(level, String.format("Waiting up to %s. Reason: %s",
						DurationFormatUtils.formatDurationWords(millis, true, true), reason));
		}

		public void logEnd(String reason, long millis) {
			logEnd(reason, millis, WaitingConfig.level());
		}

		public void logEnd(String reason, long millis, Level level) {
			if (this.equals(END) || this.equals(BOTH))
				logMessage(level, String.format("Finished waiting after %s. Reason: %s",
						DurationFormatUtils.formatDurationWords(millis, true, true), reason));
		}

		private void logMessage(Level level, String message) {
			switch (level) {
				case TRACE:
					log.trace(message);
					break;
				case DEBUG:
					log.debug(message);
					break;
				case INFO:
					log.info(message);
					break;
				case WARN:
					log.warn(message);
					break;
				case ERROR:
					log.error(message);
					break;
			}
		}
	}
}
