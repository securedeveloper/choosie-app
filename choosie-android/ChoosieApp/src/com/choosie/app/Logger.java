package com.choosie.app;

import android.util.Log;

public class Logger {

	private static Logger _logger;

	private Logger() {
	}

	public static Logger getInstance() {

		if (_logger == null) {
			return new Logger();
		}
		return _logger;
	}

	public static void i(String msg) {
		Log.i(Constants.LOG_TAG, msg);
	}

	public static void e(String msg) {
		// Adding a new Exception prints out the stack trace.
		Log.e(Constants.LOG_TAG, msg, new Exception());
	}

	public static void d(String msg) {
		// Log.d(Constants.LOG_TAG, msg);
	}

	public static void v(String string, String string2) {
		Log.v(Constants.LOG_TAG, string + " " + string2);
	}

	public static void e(String string, String string2) {
		Log.e(Constants.LOG_TAG, string + " " + string2);
	}

	public static void w(String string) {
		Log.w(Constants.LOG_TAG, string);
	}

	public static void i(String string, String string2) {
		Log.i(Constants.LOG_TAG, string + " " + string2);
	}
}
