/*
 * Copyright (c) 2012-2015 Victron Energy.
 */

package nl.victronenergy.util;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Class containing utility methods related to system environment values.
 * <p/>
 * User: Victron Energy<br/>
 * Date: 18/09/14<br/>
 * Time: 15:55
 */
public class EnvironmentUtils {

	/** String tag used for logging */
	private static final String LOG_TAG = "EnvironmentUtils";

	/** DefaultArtifactVersion representing an unknown runtime */
	private static final DefaultArtifactVersion UNKNOWN_RUNTIME = new DefaultArtifactVersion("0.0.0");

	/** DefaultArtifactVersion representing the lowest VM version used by ART */
	private static final DefaultArtifactVersion DALVIK_ART_THRESHOLD = new DefaultArtifactVersion("2.0.0");

	/** String used to describe unknown runtimes */
	public static final String UNKNOWN_RUNTIME_NAME = "Unknown";

	/** String used to describe Dalvik runtimes */
	public static final String DALVIK_NAME = "Dalvik";

	/** String used to describe ART runtimes */
	public static final String ART_NAME = "ART";

	/**
	 * Constructor
	 */
	private EnvironmentUtils() {
		// private, empty constructor to discourage creating an instance of this class
	}

	/**
	 * Returns a human-readable name for the runtime on which the app is running, based on known version thresholds.
	 * <p/>
	 * More info <a href="https://developer.android.com/guide/practices/verifying-apps-art.html">here</a>.
	 *
	 * @param pVmVersion
	 *        String versionname of the vm as contained in the environment variable java.vm.version
	 * @return String containing human-readable VM name
	 */
	public static String getAndroidRuntime(final String pVmVersion) {
		final DefaultArtifactVersion virtualMachineVersion = new DefaultArtifactVersion(pVmVersion);
		final String runtimeName;

		// if version is 0.0.0, or somehow lower, return unknown
		if (virtualMachineVersion.compareTo(UNKNOWN_RUNTIME) <= 0) {
			runtimeName = UNKNOWN_RUNTIME_NAME;
		}
		// if version is below 2.0.0, assume Dalvik
		else if (virtualMachineVersion.compareTo(DALVIK_ART_THRESHOLD) == -1) {
			runtimeName = DALVIK_NAME;
		}
		// anything above is ART
		else {
			runtimeName = ART_NAME;
		}

		return runtimeName;
	}
}
