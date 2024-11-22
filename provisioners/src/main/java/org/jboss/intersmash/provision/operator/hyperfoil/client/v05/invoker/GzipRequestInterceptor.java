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
package org.jboss.intersmash.provision.operator.hyperfoil.client.v05.invoker;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * Encodes request bodies using gzip.
 *
 * Taken from https://github.com/square/okhttp/issues/350
 */
class GzipRequestInterceptor implements Interceptor {
	@Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();
		if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
			return chain.proceed(originalRequest);
		}

		Request compressedRequest = originalRequest.newBuilder()
				.header("Content-Encoding", "gzip")
				.method(originalRequest.method(), forceContentLength(gzip(originalRequest.body())))
				.build();
		return chain.proceed(compressedRequest);
	}

	private RequestBody forceContentLength(final RequestBody requestBody) throws IOException {
		final Buffer buffer = new Buffer();
		requestBody.writeTo(buffer);
		return new RequestBody() {
			@Override
			public MediaType contentType() {
				return requestBody.contentType();
			}

			@Override
			public long contentLength() {
				return buffer.size();
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				sink.write(buffer.snapshot());
			}
		};
	}

	private RequestBody gzip(final RequestBody body) {
		return new RequestBody() {
			@Override
			public MediaType contentType() {
				return body.contentType();
			}

			@Override
			public long contentLength() {
				return -1; // We don't know the compressed length in advance!
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
				body.writeTo(gzipSink);
				gzipSink.close();
			}
		};
	}
}
