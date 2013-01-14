package com.choozie.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSettings {

	public static boolean isUseAdvancedCamera(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(Constants.SP.PREF_CAMERA, true);
	}

	public static boolean isGetAllNotifications(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(Constants.SP.PREF_NOTIFICATION, "0").equals("0");
	}

}
