package com.choosie.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.choosie.app.caches.Caches;
import com.choosie.app.camera.GalleryActivity;
import com.choosie.app.controllers.SuperController;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.util.DisplayMetrics;

import android.view.View;
import android.widget.ImageView;

public class Utils {

	private static int screenWidth = -1;
	private static int screenHeight = -1;

	public static Date convertStringToDateUTC(String str_date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date();

		try {
			date = df.parse(str_date);
		} catch (ParseException e) {
			L.e("convertStringToDateUTC", "failed parsing SimpleDateFormat");
			e.printStackTrace();
			return null;
		}

		return date;
	}

	public static CharSequence getTimeDifferenceTextFromNow(Date createdAt) {
		Date now = new Date();
		long milliseconds = now.getTime() - createdAt.getTime();

		if (milliseconds < 0) {
			// In case the time difference is negative: probably an error.
			L.w("Got a picture from the future.");
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

	public static void writeBitmapToFile(Bitmap bitmap, File file, int quality) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
		byte[] bitmapdata = bos.toByteArray();
		writeBytesIntoFile(bitmapdata, file);
	}

	public static void writeBytesIntoFile(byte[] data, File pictureFile) {
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			L.i("writeBytesIntoFile: path = " + pictureFile.getAbsolutePath());

		} catch (FileNotFoundException e) {
			L.e("File not found: " + e.getMessage());
		} catch (IOException e) {
			L.e("Error accessing file: " + e.getMessage());
		}
	}

	/*
	 * writing Bitmap on sd, from URL!!
	 */
	public static void saveBitmapOnSdFrom(String photoURL, Bitmap param) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		param.compress(CompressFormat.JPEG, 100, bos);
		writeByteStreamOnSD(bos, Integer.toString(photoURL.hashCode()));
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
			L.e("writeByteStreamOnSD", "failed to wirte on file: " + fullPath);
			e.printStackTrace();
		}
	}

	public static boolean isFileExists(String fileName) {
		String fullPath = Constants.URIs.mainDirectoryPath + fileName;
		File f = new File(fullPath);
		return f.exists();
	}

	@SuppressLint("NewApi")
	public static Bitmap getBitmapFromURL(String param,
			Callback<Void, Integer, Void> progressCallback) {

		String fullPath = getFileNameForURL(param);

		progressCallback.onProgress(5);
		Bitmap toRet = BitmapFactory.decodeFile(fullPath);

		L.d("getBitmapFromURL - URL = " + param + " fullPath = " + fullPath
				+ " toRet = " + toRet);
		//toRet = shrinkBitmapToImageViewSizeIfNeeded(toRet);
		// Log.i("mem", "insert toRet WR" + toRet.getRowBytes() + " H - " +
		// toRet.getHeight() + " BC: " +toRet.getByteCount());
		progressCallback.onProgress(100);
		return toRet;

		/*
		 * note: code below for showing large images, we will keep it for now
		 */

		// // **this function is for showing the images in the feed, so we will
		// // bring smaller version**/
		//
		// String fullPath = getFileNameForURL(param);
		//
		// // First decode with inJustDecodeBounds=true to check dimensions
		// final BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inJustDecodeBounds = true;
		// BitmapFactory.decodeFile(fullPath, options);
		//
		// // Calculate inSampleSize
		// options.inSampleSize = calculateInSampleSize(options, screenWidth /
		// 2,
		// screenWidth / 2);
		//
		// // Decode bitmap with inSampleSize set
		// options.inJustDecodeBounds = false;
		// return BitmapFactory.decodeFile(fullPath, options);
		//
		// // return BitmapFactory.decodeFile(getFileNameForURL(param));
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	@SuppressLint("DefaultLocale")
	public static void dumpMemoryInfoToLog() {
		Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
		Debug.getMemoryInfo(memoryInfo);

		String memMessage = String.format(
				"Memory: Pss=%.2f MB, Private=%.2f MB, Shared=%.2f MB",
				memoryInfo.getTotalPss() / 1024.0,
				memoryInfo.getTotalPrivateDirty() / 1024.0,
				memoryInfo.getTotalSharedDirty() / 1024.0);
		L.d("Utils: " + memMessage);
	}

	public static Bitmap shrinkBitmapToImageViewSizeIfNeeded(
			Bitmap inputBitmap, boolean fullScreen) {
		L.d("enterd shrinkBitmapToImageViewSizeIfNeeded, inputBitmap = "
				+ inputBitmap + screenWidth);
		int newMaxSize = fullScreen ? screenWidth : screenWidth / 2;
		if (newMaxSize < inputBitmap.getWidth()) {
			Bitmap shrinkedBitmap = Bitmap.createScaledBitmap(inputBitmap,
					newMaxSize, newMaxSize, false);

			inputBitmap.recycle();
			return shrinkedBitmap;
		}
		return inputBitmap;
	}

	public static int getScreenWidth() {
		return screenWidth;
	}

	public static int getScreenHeight() {
		return screenHeight;
	}

	public static void setScreenWidth(ChoosieActivity choosieActivity) {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		choosieActivity.getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);

		screenWidth = displaymetrics.widthPixels;
		screenHeight = displaymetrics.heightPixels;
	}

	public static void setImageViewSize(View imageView, int height, int width) {
		imageView.getLayoutParams().height = height;
		if (width > 0) {
			imageView.getLayoutParams().width = width;
		}
	}

	public static Bitmap getBitmapFromFileByViewSize(String path, int height,
			int width) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = calculateInSampleSize(options, width, height);

		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}

	public static File getAlbumDir() {
		L.i("PostScreenController - enter getAlbumDir,path = "
				+ Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
				+ " " + Constants.URIs.APPLICATION_NAME);
		File storageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				Constants.URIs.APPLICATION_NAME);
		return storageDir;
	}

	public static File createImageFile(Integer prefix) {
		File dir = getAlbumDir();
		if (dir.exists() == false) {
			L.i("Utils - createImageFile: the dir is not exist, path = "
					+ dir.getAbsolutePath());
			boolean dirCreated = dir.mkdirs();
			L.i("Utils, dirCreated = " + dirCreated);
		} else {
			L.i("Utils - createImageFile: dir exists, path = "
					+ dir.getAbsolutePath());
		}
		// Create an image file name
		String timeStamp = new SimpleDateFormat("_yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "image" + prefix.toString() + timeStamp + "_";
		File imageFile = null;
		try {
			imageFile = File.createTempFile(imageFileName, ".jpg", dir);
		} catch (IOException e) {
			L.e("createImageFile", "failed to create temp image file: "
					+ imageFileName);
			e.printStackTrace();
		}
		return imageFile;
	}

	public static void galleryAddPic(Uri uri, Context context) {
		L.i("Utils: adding to gallery, path = " + uri.getPath());
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(uri);
		context.sendBroadcast(mediaScanIntent);
	}

	public static Bitmap combine(Bitmap bitmap1, Drawable drawable2) {
		int width = bitmap1.getWidth();
		int height = bitmap1.getHeight();

		Bitmap bitmap = null;
		try {
			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas c = new Canvas(bitmap);
			// Resources res = getResources();

			// Bitmap bitmap1 = BitmapFactory
			// .decodeResource(res, R.drawable.test1); // blue

			// Bitmap bitmap2 = BitmapFactory
			// .decodeResource(res, R.drawable.test2); // green
			Drawable drawable1 = new BitmapDrawable(bitmap1);
			// Drawable drawable2 = new BitmapDrawable(bitmap2);

			drawable1.setBounds(0, 0, width, height);
			drawable2.setBounds(0, 0, width, height);
			drawable1.draw(c);
			drawable2.draw(c);

		} catch (Exception e) {

		}
		return bitmap;
	}

	public static Bitmap fastblur(Bitmap sentBitmap, int radius) {
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		L.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		L.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return (bitmap);
	}

	public static void executeTaskOnThreadPoolExecutor(AsyncTask<?, ?, ?> task) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			executeOnThreadPoolExecutor(task);
		} else {
			task.execute();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void executeOnThreadPoolExecutor(AsyncTask<?, ?, ?> task) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

}
