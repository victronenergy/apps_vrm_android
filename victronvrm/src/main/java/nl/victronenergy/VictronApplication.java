package nl.victronenergy;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import io.fabric.sdk.android.Fabric;
import nl.victronenergy.util.Constants.CRASHLYTICS_KEYS;
import nl.victronenergy.util.EnvironmentUtils;
import nl.victronenergy.util.MyLog;
import nl.victronenergy.util.UserUtils;

import org.apache.commons.lang3.StringUtils;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
/**
 * This is the application class, specified in the android:name attribute in AndroidManifest.xml.
 *
 * @author M2Mobi
 */
public class VictronApplication extends Application {

	/** Tag used for logging */
	private static String LOG_TAG = "VictronApplication";
	private static GoogleAnalytics sAnalytics;
	private static Tracker sTracker;
	private static String obfuscatedEncryptionPassword = null;

	@Override
	public void onCreate() {
		super.onCreate();

		// Reset token
		UserUtils.saveToken(this, "");

		// Initialise crashlytics
		if (BuildConfig.CRASHLYTICS_ENABLED) {
			MyLog.i(LOG_TAG, "Crashlytics enabled");
			Fabric.with(this, new Crashlytics());
			MyLog.i(LOG_TAG, "Build type: " + BuildConfig.BUILD_TYPE);
			Crashlytics.setString(CRASHLYTICS_KEYS.BUILD_TYPE, BuildConfig.BUILD_TYPE);
			MyLog.i(LOG_TAG, "Build flavor: " + BuildConfig.FLAVOR);
			Crashlytics.setString(CRASHLYTICS_KEYS.BUILD_FLAVOR, StringUtils.defaultIfBlank(BuildConfig.FLAVOR, "none"));
			final String runtime = EnvironmentUtils.getAndroidRuntime(System.getProperty("java.vm.version", "0.0.0"));
			MyLog.i(LOG_TAG, "Android runtime: " + runtime);
			Crashlytics.setString(CRASHLYTICS_KEYS.ANDROID_RUNTIME, runtime);
		}

		// Initialise universal image loader
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
		ImageLoader.getInstance().init(config);

		sAnalytics = GoogleAnalytics.getInstance(this);
	}

	public static String getEncryptionMasterKey() {
		if (obfuscatedEncryptionPassword == null) {
			obfuscatedEncryptionPassword = obfuscateKey();
		}
		return obfuscatedEncryptionPassword;
	}

	private static String obfuscateKey() {
		String secret = "BW!VI".replace("!", "8QL").concat("ehChlib".replace("lib", "1ib")).concat("EcmL").concat("-+$xgtwl".replace("-+$", "+"))
				.concat("9RqYflQDY".replace("9RqYflQ", "Q")).concat("=");

		return secret;
	}

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
		if (sTracker == null) {
			sTracker = sAnalytics.newTracker(R.xml.global_tracker);
		}

		return sTracker;
	}
}
