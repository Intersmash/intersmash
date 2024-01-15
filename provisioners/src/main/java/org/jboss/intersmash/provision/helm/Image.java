package org.jboss.intersmash.provision.helm;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Image {

	private final From from;
	private final List<Path> paths;

	@Getter
	@Setter
	@AllArgsConstructor
	public static class From {
		private final String kind;
		private final String namespace;
		private final String name;

	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Path {
		private final String sourcePath;
		private final String destinationDir;
	}
}
