package com.choosie.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSettings {

	private static SharedPreferences sp;

	public static void init(Context context) {
		sp = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static void savePrefrences(String key, String value) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getPushNotifications() {
		return sp.getString(Constants.SP.PUSH_NOTIFICATIONS, "true");
	}

}
