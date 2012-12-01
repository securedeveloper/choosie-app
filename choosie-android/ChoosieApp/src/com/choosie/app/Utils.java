package com.choosie.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;

public class Utils {
	
	private static Utils s_instance;
	
	private Utils() {
	}
	
	public static Utils getInstance() {
		if (s_instance == null)
			s_instance = new Utils();
		return s_instance;
	}
	
	public Date ConvertStringToDateUTC(String str_date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date();
		
		try {
			date = df.parse(str_date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return date;
	}

	public static CharSequence getTimeDifferenceTextFromNow(Date createdAt) {
		Date now = new Date();
		long milliseconds = now.getTime() - createdAt.getTime();

		if (milliseconds < 0) {
			// In case the time difference is negative: probably an error.
			Log.w(Constants.LOG_TAG, "Got a picture from the future.");
			return "";
		}
		long seconds = milliseconds / 1000;
		if (seconds < 60) {
			return seconds + "s";
		}
		long minutes = seconds / 60;
		if (minutes < 60) {
			return minutes + "m";
		}
		long hours = minutes / 60;
		if (hours < 24) {
			return hours + "h";
		}
		long days = hours / 24;
		if (days < 7) {
			return days + "d";
		}
		long weeks = days / 7;
		return weeks + "w";
	}
}
