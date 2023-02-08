package org.jboss.intersmash.deployments.util.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

import com.google.common.collect.Lists;

/**
 * Maven utilities APIs, see e.g.:
 * https://github.com/shillner/MavenPP/blob/master/de.itemis.mpp.utils.aether/src/main/java/de/itemis/mpp/aether/internal/util/MavenSettingsUtil.java
 */
public class MavenSettingsUtil {
	private static final String DEFAULT_REPOSITORY_LOCAL = System.getProperty("user.home") + "/.m2/repository";
	private static Settings settings;

	private MavenSettingsUtil() {
	}

	public static File getGlobalSettings() {
		return new File(System.getenv("M2_HOME"), "conf/settings.xml");
	}

	public static File getUserSettings() {
		return new File(System.getProperty("user.home"), ".m2/settings.xml");
	}

	public static Settings loadSettings() throws SettingsBuildingException {
		File globalSettings = getGlobalSettings();
		File userSettings = getUserSettings();
		return loadSettings(globalSettings, userSettings);
	}

	public static Settings loadSettings(File globalSettings, File userSettings) throws SettingsBuildingException {
		if (settings == null) {
			SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
			request.setGlobalSettingsFile(globalSettings);
			request.setUserSettingsFile(userSettings);

			DefaultSettingsBuilderFactory factory = new DefaultSettingsBuilderFactory();
			SettingsBuilder builder = factory.newInstance();
			SettingsBuildingResult result = builder.build(request);
			settings = result.getEffectiveSettings();
		}

		return settings;
	}

	public static LocalRepository getLocalRepository(Settings settings) {
		String repoPath;
		// sys prop localRepository
		if (System.getProperty("localRepository") != null) {
			repoPath = System.getProperty("localRepository");
		} else if (settings.getLocalRepository() != null) {
			repoPath = settings.getLocalRepository();
		} else {
			repoPath = DEFAULT_REPOSITORY_LOCAL;
		}
		return new LocalRepository(repoPath);
	}

	public static List<RemoteRepository> getRemoteRepositories(Settings settings) {
		List<RemoteRepository> list = Lists.newArrayList();
		if (settings.getLocalRepository() != null) {
			list.add(new RemoteRepository.Builder("localRepoFromSettings", "default", "file:" + settings.getLocalRepository())
					.build());
		} else {
			list.add(new RemoteRepository.Builder("localRepoFromSettings", "default", "file:" + DEFAULT_REPOSITORY_LOCAL)
					.build());
		}
		if (System.getProperty("localRepository") != null) {
			list.add(new RemoteRepository.Builder("localRepoFromSettings", "default",
					"file:" + System.getProperty("localRepository")).build());
		}
		return list;
	}
}
