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
package org.jboss.intersmash.tools.waiting.failfast;

import java.util.Random;

public class ExponentialTimeBackoff {
	private long lastNonBlockingTime;
	private boolean blocking;
	private long maxBackoffMillis;

	private long step;
	private Random random;
	private long nonBlockingWaitMillis;

	private ExponentialTimeBackoff(Builder builder) {
		blocking = builder.blocking;
		maxBackoffMillis = builder.maxBackoffMillis;
		step = 1;
		random = new Random(System.currentTimeMillis());
		lastNonBlockingTime = System.currentTimeMillis();
		nonBlockingWaitMillis = waitMillisForCurrentStep();
	}

	public static Builder builder() {
		return new Builder();
	}

	public boolean next() {
		return blocking ? nextBlocking() : nextNonBlocking();
	}

	private boolean nextBlocking() {
		try {
			long wait = waitMillisForCurrentStep();
			Thread.sleep(wait);
			incrementStep();
			return true;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean nextNonBlocking() {
		long now = System.currentTimeMillis();
		if (lastNonBlockingTime + nonBlockingWaitMillis <= now) {
			incrementStep();
			nonBlockingWaitMillis = waitMillisForCurrentStep();
			lastNonBlockingTime = now;
			return true;
		} else {
			return false;
		}
	}

	private long waitMillisForCurrentStep() {
		if (step * 1000 >= maxBackoffMillis) {
			return maxBackoffMillis + random.nextInt(1000);
		} else {
			return step * 1000 + random.nextInt(1000);
		}
	}

	private void incrementStep() {
		if (step * 1000 < maxBackoffMillis) {
			step *= 2;
		}
	}

	public static class Builder {
		static boolean DEFAULT_BLOCKING = false;
		static long DEFAULT_MAX_BACKOFF = 1_000L;

		private boolean blocking;
		private long maxBackoffMillis;

		private Builder() {
			blocking = DEFAULT_BLOCKING;
			maxBackoffMillis = DEFAULT_MAX_BACKOFF;
		}

		public Builder blocking(boolean blocking) {
			this.blocking = blocking;
			return this;
		}

		public Builder maxBackoff(long maxBackoffMillis) {
			this.maxBackoffMillis = maxBackoffMillis;
			return this;
		}

		public ExponentialTimeBackoff build() {
			return new ExponentialTimeBackoff(this);
		}
	}
}
