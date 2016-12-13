package nl.victronenergy.util;

import nl.victronenergy.BuildConfig;
import nl.victronenergy.fragments.overviews.FragmentOverviewBmv;
import nl.victronenergy.fragments.overviews.FragmentOverviewBmvMppt;
import nl.victronenergy.fragments.overviews.FragmentOverviewBmvMulti;
import nl.victronenergy.fragments.overviews.FragmentOverviewBmvMultiMppt;
import nl.victronenergy.fragments.overviews.FragmentOverviewBmvMultiPvInverter;
import nl.victronenergy.fragments.overviews.FragmentOverviewNoConnection;
import nl.victronenergy.models.widgets.SummaryWidgetAcPowerIn;
import nl.victronenergy.models.widgets.SummaryWidgetAcPowerOut;
import nl.victronenergy.models.widgets.SummaryWidgetBatteryState;
import nl.victronenergy.models.widgets.SummaryWidgetDcPowerSystem;
import nl.victronenergy.models.widgets.SummaryWidgetPvPower;
import nl.victronenergy.models.widgets.SummaryWidgetStateOfCharge;

/**
 * @see <a href="http://juice.m2mobi.com/build/apidoc/">Victron Webservice API Specification</a>
 */
public final class Constants {

	/** SERVER_ADRESS: Used to connect with the server. */
	public static final String SERVER_URL;

	static {
		if (BuildConfig.BUILD_TYPE.equals("debug")) {
			SERVER_URL = "https://juice.victronenergy.com/";
		} else {
			SERVER_URL = "https://juice.victronenergy.com/";
		}
	}

	public static final String API_VERSION = "200.a";
	public static final String DEMO_EMAIL = "demo@victronenergy.com";
	public static final String DEMO_PASSWORD = "vrmdemo";

	// User webservice
	public static final String WEBSERVICE_USER_LOGIN = SERVER_URL + "user/login";
	public static final String WEBSERVICE_USER_LOGOUT = SERVER_URL + "user/logout";

	// Site webservice
	public static final String GET_SITES = SERVER_URL + "sites/get";
	public static final String GET_SITE = SERVER_URL + "sites/get_site";
	public static final String GET_SITE_ATTRIBUTES = SERVER_URL + "sites/get_site_attributes";
	public static final String GET_ATTRIBUTES = SERVER_URL + "sites/attributes";
	public static final String GET_IO_DATA = SERVER_URL + "sites/io_extenders";
	public static final String SET_LABEL = SERVER_URL + "sites/set_label";
	public static final String SET_GENERATOR = SERVER_URL + "sites/set_generator";
	public static final String GET_GALLERY_IMAGE = SERVER_URL + "statics/uploads/";
	public static final String DELETE_IMAGE = SERVER_URL + "image/delete";
	public static final String UPLOAD_IMAGE = SERVER_URL + "image/upload";

	public static final int TIMEOUT = 15000; // 15 seconds

	/**
	 * Possible response codes for the webservice
	 *
	 * @author M2Mobi
	 */
	public static final class RESPONSE_CODE {
		/** Unknown */
		public static final int RESPONSE_UNKNOWN = -1;
		/** OK */
		public static final int RESPONSE_OK = 200;
		/** Partially OK */
		public static final int RESPONSE_PARTIALLY_OK = 206;
		/** Info up-to-date */
		public static final int RESPONSE_UP_TO_DATE = 304;
		/** Invalid Input, "ei" contains invalid input parameter name */
		public static final int RESPONSE_INVALID_INPUT = 400;
		/** Session ID missing / wrong */
		public static final int RESPONSE_SESSION_ID = 401;
		/** User not allowed to access information */
		public static final int RESPONSE_NOT_ALLOWED = 402;
		/** Username / Password wrong */
		public static final int RESPONSE_WRONG_USER_PASS = 403;
		/** User not found */
		public static final int RESPONSE_USER_NOT_FOUND = 404;
		/** Version out-of-date */
		public static final int RESPONSE_OLD_VERSION = 412;
		/** Site does not have an IO Extender */
		public static final int RESPONSE_NO_IO_EXTENDER = 422;
		/** Internal Server Error (Unknown) */
		public static final int RESPONSE_INTERNAL = 500;
		/** Call not implemented */
		public static final int RESPONSE_CALL_UNIMPLEMENTED = 501;
		/** Wrong protocol (http vs. https) */
		public static final int RESPONSE_WRONG_PROTOCOL = 502;
		/** E-mail sending related error */
		public static final int RESPONSE_EMAIL_ERROR = 504;
		/** Maintenance mode */
		public static final int RESPONSE_MAINTANANCE = 506;

		// Everything below 400 should be fine
		public static final int RESPONSE_MAXIMUM_OK = 400;
	}

	// WebApp URLs
	public static final class WEBAPP {
		public static final String BASE_URL = "https://vrm.victronenergy.com/";
		public static final String LOGIN_URL = BASE_URL + "user/login/";
		public static final String OPEN_SITE_URL = BASE_URL + "user/login?return=%2Fsite%2F";
		public static final String FORGOT_PASSWORD_URL = BASE_URL + "user/reset-password-request/email/";
	}

	/**
	 * Contains keys for the bundle values
	 */
	public static final class BUNDLE {
		public static final String SITE_LIST_DATA = "SITE_LIST_DATA";
		public static final String SITE_ARRAY_INDEX = "SITE_ARRAY_INDEX";
		public static final String SITE_OBJECT = "SITE_OBJECT";
		public static final String IO_EXTENDER_DATA = "IO_EXTENDER_DATA";
		public static final String ATTRIBUTE_DATA = "ATTRIBUTE_DATA";
		public static final String SELECTED_SITE_ID = "SELECTED_SITE_ID";
	}

	/**
	 * The state a generator can be in
	 */
	public static final class GENERATOR_STATE {
		public static final int STOPPED = 0;
		public static final int RUNNING = 1;
	}

	// IntentData
	public static final String INTENT_SITE_ID = "siteid";
	public static final String INTENT_OBJECT_IO = "object_io";
	public static final String INTENT_OBJECT_ENERGY = "object_energy";
	public static final String INTENT_OPEN_SETTINGS = "open_settings";
	public static final String INTENT_SITE_URL = "site_url";
	public static final String INTENT_ARRAY_SITELIST = "array_sitelist";
	public static final String INTENT_ARRAY_IO = "array_ios";
	public static final String INTENT_SESSION_ID = "session_id";
	public static final String INTENT_IO_CODE = "io_code";
	public static final String INTENT_IO_INDEX = "io_position";
	public static final String INTENT_IS_GENERATOR_BTN = "is_generator_btn";
	public static final String INTENT_ORIGINAL_STATE = "original_state";
	public static final String INTENT_FIRED_BY_SMS_BROADCAST = "fired_by_broadcast";
	public static final String INTENT_IO_NAME = "io_name";
	public static final String INTENT_IO_TEMP = "io_temp";
	public static final String INTENT_IO_STATUS = "io_status";
	public static final String INTENT_PHONE_NR = "phone_nr";
	public static final String INTENT_DATA_ATT_ID = "data_attribute_id";

	/**
	 * Contains default values used when no value has been assigned
	 *
	 * @author M2Mobi
	 */
	public static final class DEFAULT_VALUE {
		public static final int SITE_INDEX = 0;
		public static final int SITE_ID = -1;
		public static final String SESSION_ID = "-1";
	}

	/**
	 * Post keys, used to send parameters to the webservice
	 *
	 * @author M2Mobi
	 */
	public static final class POST {
		public static final String APIVERSION = "version";
		public static final String EMAIL = "username";
		public static final String PASSWORD = "password";
		public static final String DEVICE_TYPE = "device_type";
		public static final String SITE_ID = "siteid";
		public static final String VERIFICATION_TOKEN = "verification_token";
		public static final String SESSION_ID = "sessionid";
		public static final String ATTRIBUTE_ID = "attribute_id";
		public static final String ATTRIBUTE_IDS = "attributes";
		public static final String LABEL = "label";
		public static final String POST_HAS_GENERATOR = "has_generator";
		public static final String INSTANCE = "instance";
		public static final String URI = "uri";
		public static final String IMAGE_NAME = "image_name";
		public static final String IMAGE = "image";
	}

	/**
	 * Keys for the shared preferences
	 *
	 * @author M2Mobi
	 */
	public static final class SHARED_PREFERENCES {
		// Sp name
		public static final String VICTRON_PREFERENCES = "VictronPreferences";

		public static final String LAST_SPLASH = "last_splash";
		public static final String USERNAME = "username";
		public static final String PASSWORD = "password";
		public static final String SESSIONID = "sessionid";
		public static final String DEMO_USER = "demo_user";
		public static final String URI_PHOTO_TEMP = "uri_photo_temp";
		public static final String PATH_PHOTO_TEMP = "path_photo_temp";

		// Generator settings
		public static final String GENERATOR_OUTPUT = "gen_output";
		public static final String GENERATOR_STATE = "gen_state";
	}

	/** Loaders first bits are used to identify loadertype */
	public static final class LOADER_ID {
		public static final int LOGIN = 0x80000000;
		public static final int SITELIST = 0x40000000;
		public static final int SITE_ATTRIBUTES_SUMMARY = 0x30000000;
		public static final int SITE_ATTRIBUTES_DETAIL = 0x20000000;
		public static final int SITEDATA = 0x10000000;
		public static final int IO_NAME = 0xA0000000;
		public static final int LOADER_SET_GENERATOR = 0xB0000000;
		public static final int DELETE_IMAGE = 0xC0000000;
		public static final int UPLOAD_IMAGE = 0xD0000000;
		public static final int ATTRIBUTES = 0xE0000000;
		public static final int FILTER = 0xF0000000;
	}

	/**
	 * Constants related to pubnub
	 */
	public static final class PUBNUB {
		public static final String PUBKEY = "pub-c-bde6e083-bf8e-43d2-9ac9-b3f777d9fcb5";
		public static final String SUBKEY = "sub-c-8bb751ba-013e-11e3-91f6-02ee2ddab7fe";
		public static final String CHANNEL_STATUS = "847e40669664_LiveFeed";
		public static final String CHANNEL_KEEPALIVE = "847e40669664_Reader";

		public static final String JSON_TIMESTAMP = "ts";
		public static final String JSON_TOKEN = "token";
		public static final String JSON_USER = "user";
		public static final String JSON_OPCOMMAND = "opCommand";
		public static final String JSON_KEEPALIVE = "keepAlive";
	}

	public static final class SITE_STATUS {
		public static final int ALARM = 0;
		public static final int OK = 1;
		public static final int OLD = 2;
	}

	/**
	 * Contains tags used to identify the fragments
	 */
	public static final class FRAGMENT_TAG {
		public static final String SITE_SUMMARY = "SITE_SUMMARY";
		public static final String SITE_DETAIL = "SITE_DETAIL";
		public static final String SITE_HISTORIC_DATA = "SITE_HISTORIC_DATA";
		public static final String SITE_IO_GENERATOR_SETTINGS = "SITE_IO_GENERATOR_SETTINGS";
		public static final String SITE_IO_SETTINGS = "SITE_IO_SETTINGS";
		public static final String SITE_VIEWPAGER = "SITE_VIEWPAGER";
	}

	/**
	 * Data attribute id's
	 */
	public static class ATTRIBUTE {
		public static final String VEBUS_STATE = "S";
		public static final String DEEPEST_DISCHARGE = "H1";
		public static final String LAST_DISCHARGE = "H2";
		public static final String AVERAGE_DISCHARGE = "H3";
		public static final String CHARGE_CYCLES = "H4";
		public static final String FULL_DISCHARGE_CYCLES = "H5";
		public static final String TOTAL_AH_DRAWN = "H6";
		public static final String MINIMUM_VOLTAGE = "H7";
		public static final String MAXIMUM_VOLTAGE = "H8";
		public static final String TIME_SINCE_LAST_FULL_CHARGE = "H9";
		public static final String AUTOMATIC_SYNCS = "H10";
		public static final String LOW_VOLTAGE_ALARMS = "H11";
		public static final String HIGH_VOLTAGE_ALARMS = "H12";
		public static final String LOW_STARTER_VOLTAGE_ALARMS = "H13";
		public static final String HIGH_STARTER_VOLTAGE_ALARMS = "H14";
		public static final String MINIMUM_STARTER_VOLTAGE = "H15";
		public static final String MAXIMUM_STARTER_VOLTAGE = "H16";
		public static final String PV_AC_COUPLED_OUTPUT_L1 = "P";
		public static final String PV_AC_COUPLED_OUTPUT_L2 = "P2";
		public static final String PV_AC_COUPLED_OUTPUT_L3 = "P3";
		public static final String PV_AC_COUPLED_INPUT_L1 = "Pi";
		public static final String PV_AC_COUPLED_INPUT_L2 = "Pi2";
		public static final String PV_AC_COUPLED_INPUT_L3 = "Pi3";
		public static final String PV_DC_COUPLED = "Pdc";
		public static final String AC_CONSUMPTION_L1 = "a1";
		public static final String AC_CONSUMPTION_L2 = "a2";
		public static final String AC_CONSUMPTION_L3 = "a3";
		public static final String GRID_L1 = "g1";
		public static final String GRID_L2 = "g2";
		public static final String GRID_L3 = "g3";
		public static final String GENSET_L1 = "gs1";
		public static final String GENSET_L2 = "gs2";
		public static final String GENSET_L3 = "gs3";
		public static final String DC_SYSTEM = "dc";
		public static final String BATTERY_VOLTAGE = "bv";
		public static final String STATE_OF_CHARGE = "bs";
		public static final String CONSUMED_AMPHOURS = "ba";
		public static final String TIME_TO_GO = "bt";
		public static final String BATTERY_CURRENT = "bc";
		public static final String VEBUS_CHARGE_CURRENT = "vc";
		public static final String BATTERY_STATE = "bst";
		public static final String IO_IN1 = "IN1";
		public static final String IO_IN2 = "IN2";
		public static final String IO_IN3 = "IN3";
		public static final String IO_OUT1 = "OUT1";
		public static final String IO_OUT2 = "OUT2";
		public static final String IO_TEMPERATURE = "T1";
	}

	// IO Codes
	public static final String OUTPUT_CODE_PREFIX = "OUT";
	public static final String INPUT_CODE_PREFIX = "IN";

	// SMS Data
	public static final String SMS_COMMAND_ON = "on";
	public static final String SMS_COMMAND_OFF = "off";
	public static final String SMS_COMMAND_OUTPUT = "output";
	public static final String SMS_COMMAND_SENT = "nl.victronenergy.SMS_COMMAND_SENT";
	public static final String SMS_COMMAND_DELIVERED = "nl.victronenergy.SMS_COMMAND_DELIVERED";
	public static final String BROADCAST_TOGGLE_BUTTON = "nl.victronenergy.BROADCAST_TOGGLE_BUTTON";
	public static final String BROADCAST_TOGGLE_EXTRA = "nl.victronenergy.BROADCAST_TOGGLE_EXTRA";

	/**
	 * Constants used in the IO Picture settings
	 */
	public static final class IO_PICTURE {
		public static final String PREFIX = "thumbnail_";
		public static final String EXTENSION = ".jpg";
	}

	/**
	 * Custom keys for providing Crashlytics with environment/state information
	 */
	public static final class CRASHLYTICS_KEYS {

		/** String key used to communicate the build type of this build */
		public static final String BUILD_TYPE = "build_type";

		/** String key used to communicate the build flavor of this build */
		public static final String BUILD_FLAVOR = "build_flavor";

		/** String key used to communicate whether device has Play Services installed */
		public static final String HAS_PLAY_SERVICES = "has_play_services";

		/** String key used to communicate whether device has registered with GCM */
		public static final String HAS_REGISTERED_WITH_GCM = "has_registered_with_gcm";

		/** String key used to communicate this device's GCM registration ID */
		public static final String GCM_REGISTRATION_ID = "gcm_registration_id";

		/** String key used to communicate which Android Runtime the device is using (Dalvik vs ART) */
		public static final String ANDROID_RUNTIME = "android_runtime";
	}

	// VE.Bus State
	public static final int VEBUS_OFF = 0;
	public static final int VEBUS_LOW_POWER = 1;
	public static final int VEBUS_INVERTING = 9;

	/** Attribute unit formatting */
	public static enum AttributeUnit {
		WATTS, VOLT, AMPS, TIME, PERCENTAGE, AMPHOUR, COUNT
	}

	/** Available overviews */
	public static enum Overview {
		BMV_MULTI_PVINVERTER(FragmentOverviewBmvMultiPvInverter.class, "BMV_MULTI_PVINVERTER"), // --
		BMV_MULTI_MPPT(FragmentOverviewBmvMultiMppt.class, "BMV_MULTI_MPPT"), // --
		BMV_MPPT(FragmentOverviewBmvMppt.class, "BMV_MPPT"), // --
		BMV_MULTI(FragmentOverviewBmvMulti.class, "BMV_MULTI"), // --
		MULTI(FragmentOverviewBmv.class, "MULTI"), // --
		BMV(FragmentOverviewBmv.class, "BMV"), // --
		NO_CONNECTION(FragmentOverviewNoConnection.class, "NO_CONNECTION");

		private Class mClassOverviewFragment;
		private String mTitle;

		private Overview(Class pClassOverviewFragment, String pTitle) {
			mClassOverviewFragment = pClassOverviewFragment;
			mTitle = pTitle;
		}

		public Class getClassOverviewFragment() {
			return mClassOverviewFragment;
		}

		public String getTitle() {
			return mTitle;
		}
	};

	public static enum DownloadStatus {
		DOWNLOAD_NEEDED, DOWNLOAD_NOT_NEEDED, DOWNLOAD_IN_PROGRESS, DOWNLOAD_ERROR, DOWNLOAD_SESSION_EXPIRED, DOWNLOAD_FINISHED
	}

	/**
	 * Enum that contains all available widgets
	 */
	public static enum Widget {
		SOC(SummaryWidgetStateOfCharge.class), // --
		BATTERY_STATE(SummaryWidgetBatteryState.class), // --
		PV_POWER(SummaryWidgetPvPower.class), // --
		AC_OUT(SummaryWidgetAcPowerOut.class), // --
		AC_IN(SummaryWidgetAcPowerIn.class), // --
		DC_POWER(SummaryWidgetDcPowerSystem.class);

		private Class mClassWidget;

		private Widget(Class pClassWidget) {
			mClassWidget = pClassWidget;
		}

		public Class getClassWidget() {
			return mClassWidget;
		}
	}
}
