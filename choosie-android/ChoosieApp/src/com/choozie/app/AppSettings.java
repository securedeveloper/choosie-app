package com.choozie.app;

import com.choozie.app.models.FacebookDetails;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AppSettings {

	private static final String PREF_FB_LAST_NAME = "FB_LAST_NAME";
	private static final String FB_FIRST_NAME = "FB_FIRST_NAME";
	private static final String PREF_FB_ACCESS_TOKEN_EXPDATE = "FB_ACCESS_TOKEN_EXPDATE";
	private static final String PREF_FB_ACCESS_TOKEN = "FB_ACCESS_TOKEN";
	private static final String PREF_FB_UID = "FB_UID";

	public static boolean isUseAdvancedCamera(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(Constants.SP.PREF_CAMERA, true);
	}

	public static boolean isGetAllNotifications(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(Constants.SP.PREF_NOTIFICATION, "0").equals("0");
	}

	public static FacebookDetails getFacebookDetailsOfLoggedInUser(
			Context context) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);

		String fb_uid = p.getString(PREF_FB_UID, "");
		String access_token = p.getString(PREF_FB_ACCESS_TOKEN, "");
		long access_token_expdate = p.getLong(PREF_FB_ACCESS_TOKEN_EXPDATE, -1);
		String firstName = p.getString(FB_FIRST_NAME, "");
		String lastName = p.getString(PREF_FB_LAST_NAME, "");
		FacebookDetails fbDetails = new FacebookDetails(fb_uid, access_token,
				access_token_expdate, firstName, lastName);
		return fbDetails;
	}

	public static void saveFacebookDetailsOfLoggedInUser(Context context,
			FacebookDetails fbDetails) {
		Editor e = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		e.putString(PREF_FB_UID, fbDetails.getFb_uid());
		e.putString(PREF_FB_ACCESS_TOKEN, fbDetails.getAccess_token());
		e.putString(FB_FIRST_NAME, fbDetails.getFirstName());
		e.putString(PREF_FB_LAST_NAME, fbDetails.getLastName());
		e.putLong(PREF_FB_ACCESS_TOKEN_EXPDATE,
				fbDetails.getAccess_token_expdate());
		e.apply();
	}

}
