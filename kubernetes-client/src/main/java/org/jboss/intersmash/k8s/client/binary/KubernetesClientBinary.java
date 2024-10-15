package org.jboss.intersmash.k8s.client.binary;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import cz.xtf.core.openshift.CLIUtils;
import io.fabric8.openshift.api.model.operatorhub.lifecyclemanager.v1.PackageManifest;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KubernetesClientBinary {
	private final String path;

	@Getter
	private String configPath;

	public KubernetesClientBinary(String path) {
		this.path = path;
	}

	public KubernetesClientBinary(String path, String configPath) {
		this(path);
		this.configPath = configPath;
	}

	public void login(String url, String token) {
		this.execute("login", url, "--insecure-skip-tls-verify=true", "--token=" + token);
	}

	public void login(String url, String username, String password) {
		this.execute("login", url, "--insecure-skip-tls-verify=true", "-u", username, "-p", password);
	}

	/**
	 * Apply configuration file in the specified namespace.
	 * Delegates to `oc apply --filename='sourcepath' --namespace='namespace'`
	 *
	 * @param sourcePath path to configration file
	 * @param namespace namespace
	 */
	public void apply(String namespace, String sourcePath) {
		this.execute("apply", "--namespace=" + namespace, "--filename=" + sourcePath);
	}

	/**
	 * Apply configuration file. Delegates to `oc apply --filename='sourcepath`
	 *
	 * @param sourcePath path to configration file
	 */
	public void apply(String sourcePath) {
		this.execute("apply", "--filename=" + sourcePath);
	}

	/**
	 * Apply configuration files in the order they appear in the list
	 *
	 * @param sourcePaths list of paths to configuration files
	 */
	public void apply(List<String> sourcePaths) {
		for (String sourcePath : sourcePaths) {
			apply(sourcePath);
		}
	}

	/**
	 * Apply configuration files in the order they appear in the list, using supplied namespace.
	 *
	 * @param namespace namespace in which the configuration files should be applied
	 * @param sourcePaths list of paths to configuration files
	 */
	public void apply(String namespace, List<String> sourcePaths) {
		for (String sourcePath : sourcePaths) {
			apply(namespace, sourcePath);
		}
	}

	public void namespace(String projectName) {
		// see https://kubernetes.io/docs/reference/kubectl/cheatsheet/#kubectl-context-and-configuration
		this.execute("config", "set-context", "--current", String.format("--namespace=%s", projectName));
	}

	// TODO? check ShipWrigth
	//    public void startBuild(String buildConfig, String sourcePath) {
	//        this.execute("start-build", buildConfig, "--from-dir=" + sourcePath);
	//    }

	// Common method for any oc command call
	public String execute(String... args) {
		if (configPath == null) {
			return CLIUtils.executeCommand(ArrayUtils.addAll(new String[] { path }, args));
		} else {
			return CLIUtils.executeCommand(ArrayUtils.addAll(new String[] { path, "--kubeconfig=" + configPath }, args));
		}
	}

	public List<PackageManifest> packageManifests(final String operatorName, final String operatorNamespace) {
		Type targetClassType = new TypeToken<ArrayList<PackageManifest>>() {
		}.getType();
		return new Gson().fromJson(this.execute("get", "packagemanifest", operatorName, "-n", operatorNamespace, "-o", "json"),
				targetClassType);
	}

	public List<CatalogSource> catalogSources(final String operatorNamespace) {
		Type targetClassType = new TypeToken<ArrayList<CatalogSource>>() {
		}.getType();
		return new Gson().fromJson(this.execute("get", "catsrc", "-n", operatorNamespace, "-o", "json"), targetClassType);
	}

	public CatalogSource catalogSource(final String operatorNamespace, final String catalogSourceName) {
		io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource loaded = catalogSources(operatorNamespace).stream()
				.filter(cs -> cs.getMetadata().getName().equalsIgnoreCase(catalogSourceName))
				.findFirst().orElseThrow(
						() -> new IllegalStateException(
								"Unable to retrieve CatalogSource " + catalogSourceName));
		CatalogSource catalogSource = new CatalogSource();
		catalogSource.setMetadata(loaded.getMetadata());
		catalogSource.setSpec(loaded.getSpec());
		return catalogSource;
	}
}
