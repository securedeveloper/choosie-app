package com.choozie.app;

import android.util.Log;

public class L {

	private static boolean useLogger = false;

	private L() {
	}

	public static void i(String msg) {
		if (!useLogger)
			return;
		Log.i(Constants.LOG_TAG, msg);
	}

	public static void e(String msg) {
		if (!useLogger)
			return;
		// Adding a new Exception prints out the stack trace.
		Log.e(Constants.LOG_TAG, msg, new Exception());
	}

	public static void d(String msg) {
		if (!useLogger)
			return;
		// Log.d(Constants.LOG_TAG, msg);
	}

	public static void v(String string, String string2) {
		if (!useLogger)
			return;
		Log.v(Constants.LOG_TAG, string + " " + string2);
	}

	public static void e(String string, String string2) {
		if (!useLogger)
			return;
		Log.e(Constants.LOG_TAG, string + " " + string2);
	}

	public static void w(String string) {
		if (!useLogger)
			return;
		Log.w(Constants.LOG_TAG, string);
	}

	public static void i(String string, String string2) {
		if (!useLogger)
			return;
		Log.i(Constants.LOG_TAG, string + " " + string2);
	}
}
