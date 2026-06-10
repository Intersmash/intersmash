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
package org.jboss.intersmash.tools.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CLIUtils {

	private static final Pattern TOKEN_EQ = Pattern.compile("--token=\\S+");
	private static final Pattern TOKEN_SPACE = Pattern.compile("--token\\s+\\S+");
	private static final Pattern T_EQ = Pattern.compile("-t=\\S+");
	private static final Pattern T_SPACE = Pattern.compile("-t\\s+\\S+");
	private static final Pattern PASSWORD_EQ = Pattern.compile("--password=\\S+");
	private static final Pattern PASSWORD_SPACE = Pattern.compile("--password\\s+\\S+");
	private static final Pattern P_EQ = Pattern.compile("-p=\\S+");
	private static final Pattern P_SPACE = Pattern.compile("-p\\s+\\S+");

	private static final Function<String, String> DEFAULT_MASK_FUNCTION = s -> {
		s = TOKEN_EQ.matcher(s).replaceAll("--token=***");
		s = TOKEN_SPACE.matcher(s).replaceAll("--token ***");
		s = T_EQ.matcher(s).replaceAll("-t=***");
		s = T_SPACE.matcher(s).replaceAll("-t ***");
		s = PASSWORD_EQ.matcher(s).replaceAll("--password=***");
		s = PASSWORD_SPACE.matcher(s).replaceAll("--password ***");
		s = P_EQ.matcher(s).replaceAll("-p=***");
		s = P_SPACE.matcher(s).replaceAll("-p ***");
		return s;
	};

	public static String executeCommand(Map<String, String> environmentVariables, String... args) {
		return executeCommand(environmentVariables, DEFAULT_MASK_FUNCTION, args);
	}

	private static String executeCommand(Map<String, String> environmentVariables, Function<String, String> maskFunction,
			String... args) {
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.environment().putAll(environmentVariables);
		pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
		pb.redirectError(ProcessBuilder.Redirect.PIPE);

		String commandString = String.join(" ", args);
		String maskedCommandString = maskFunction.apply(commandString);

		try {
			log.debug("executing local command: {}", maskedCommandString);
			Process p = pb.start();

			ExecutorService es = Executors.newFixedThreadPool(2);

			Future<String> out = es.submit(() -> {
				try (InputStream is = p.getInputStream()) {
					return readStream(is);
				}
			});

			Future<String> err = es.submit(() -> {
				try (InputStream is = p.getErrorStream()) {
					return readStream(is);
				}
			});

			int result = p.waitFor();
			es.shutdown();

			if (result == 0) {
				String commandOutput = out.get();
				log.debug(commandOutput);
				return commandOutput;
			} else {
				log.error("Failed while executing (code {}): {}", result, maskedCommandString);
				log.error("stdout:\n{}", out.get());
				log.error("stderr:\n{}", err.get());
			}
		} catch (IOException | InterruptedException | ExecutionException e) {
			log.error("Failed while executing: " + maskedCommandString, e);
		}

		return null;
	}

	public static String executeCommand(String... args) {
		return executeCommand(Collections.emptyMap(), args);
	}

	private static String readStream(InputStream is) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString(StandardCharsets.UTF_8.name());
	}
}
