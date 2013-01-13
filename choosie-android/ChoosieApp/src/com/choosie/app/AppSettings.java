package com.choosie.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSettings {

	private static SharedPreferences sp;

	public static void init(Context context) {
		sp = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static boolean isUseAdvancedCamera() {
		return sp.getBoolean(Constants.SP.PREF_CAMERA, true);
	}
	
	public static boolean isGetAllNotifications() {
		return sp.getString(Constants.SP.PREF_NOTIFICATION, "0").equals("0");
	}

}
