package nl.victronenergy.util;

import nl.victronenergy.BuildConfig;

import android.util.Log;

public class MyLog {

	public static final boolean DEBUG = BuildConfig.DEBUG;

	public static void e(String tag, String text) {
		if (DEBUG) {
			Log.e(tag, text);
		}
	}

	public static void e(String tag, String text, Throwable e) {
		if (DEBUG) {
			Log.e(tag, text, e);
			e.printStackTrace();
		}
	}

	public static void w(String tag, String text) {
		if (DEBUG) {
			Log.w(tag, text);
		}
	}

	public static void d(String tag, String text) {
		if (DEBUG) {
			Log.d(tag, text);
		}
	}

	public static void i(String tag, String text) {
		if (DEBUG) {
			Log.i(tag, text);
		}
	}

	public static void v(String tag, String text) {
		if (DEBUG) {
			Log.v(tag, text);
		}
	}

	public static void w(String string, String string2, Throwable string3) {
		if (DEBUG) {
			Log.w(string, string2, string3);
		}
	}

	public static void printStackTrace(String tag, Throwable t) {
		if (DEBUG) {
			Log.e(tag, "########################################");
			t.printStackTrace();
		}
	}

}
