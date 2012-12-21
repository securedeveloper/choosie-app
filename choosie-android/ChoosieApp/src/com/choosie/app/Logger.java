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

	public void WriteLine(String msg) {
		Log.i(Constants.LOG_TAG, msg);
	}
}
