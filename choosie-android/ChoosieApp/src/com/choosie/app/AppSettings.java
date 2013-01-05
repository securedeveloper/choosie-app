package com.choosie.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSettings {

	private static SharedPreferences sp;

	public static void init(Context context) {
		sp = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static void setPushNotifications(boolean value) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(Constants.SP.PUSH_NOTIFICATIONS, value);
		editor.commit();
	}

	public static boolean getPushNotifications() {
		return sp.getBoolean(Constants.SP.PUSH_NOTIFICATIONS, true);
	}

}
