package com.choosie.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Utils {

	public static Date convertStringToDateUTC(String str_date) {
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

	public static String getFileNameForURL(String param) {
		String directory = Constants.URIs.mainDirectoryPath;

		String fileName = Integer.toString(param.hashCode());
		return directory + fileName;
	}

	public static void makeMainDirectory() {
		String directory = Constants.URIs.mainDirectoryPath;
		// create a File object for the parent directory
		File choosieDirectory = new File(directory);
		// have the object build the directory structure, if needed.
		if (choosieDirectory.exists() == false) {
			choosieDirectory.mkdirs();
		}
	}

	public static void writeByteStreamOnSD(ByteArrayOutputStream bos,
			String HashedfileName) {
		String fullPath = Constants.URIs.mainDirectoryPath + HashedfileName;
		File f = new File(fullPath);
		if (f.exists()) {
			return;
		}
		FileOutputStream fo = null;
		try {
			f.createNewFile();
			fo = new FileOutputStream(f);
			fo.write(bos.toByteArray());
			fo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isFileExists(String fileName) {
		String fullPath = Constants.URIs.mainDirectoryPath + fileName;
		File f = new File(fullPath);
		return f.exists();
	}

	public static Bitmap getBitmapFromURL(String param) {
		return BitmapFactory.decodeFile(getFileNameForURL(param));
	}

	public static void saveBitmapOnSd(String photoURL, Bitmap param) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		param.compress(CompressFormat.JPEG, 100, bos);
		writeByteStreamOnSD(bos, Integer.toString(photoURL.hashCode()));
	}
}
