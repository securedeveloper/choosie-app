package com.choosie.app.camera;

import java.io.File;

import com.choosie.app.Constants;
import com.choosie.app.Logger;
import com.choosie.app.R;
import com.choosie.app.R.layout;
import com.choosie.app.R.menu;
import com.choosie.app.Utils;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;

public class CameraMainSuperControllerActivity extends Activity {

	private File pictureFile;
	private String imagePath1;
	private String imagePath2;
	private Bundle bundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// initializing bundle;
		bundle = new Bundle();
		// creating the first file and starting cameraActivity
		pictureFile = Utils.createImageFile(1);
		imagePath1 = pictureFile.getAbsolutePath();
		Logger.i("CameraMainActivity - onCreate - about to startNewCameraActivity, imaggePath1 = "
				+ imagePath1);
		startNewCameraActivity(Constants.RequestCodes.CAMERA_PICURE_FIRST);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case Constants.RequestCodes.CAMERA_PICURE_FIRST:
			// get back from camera with first file
			handleResultFromCamera(resultCode, data,
					Constants.RequestCodes.CAMERA_CONFIRM_FIRST);
			break;

		case Constants.RequestCodes.CAMERA_PICURE_SECOND:
			// get back from camera with first file
			handleResultFromCamera(resultCode, data,
					Constants.RequestCodes.CAMERA_CONFIRM_SECOND);
			break;

		case Constants.RequestCodes.CAMERA_CONFIRM_FIRST:
			// got back from confirmation with first image
			if (resultCode == Activity.RESULT_OK) {
				// user confirmed, save it in the gallery, and starting
				// cmaeraActivity with second image
				Utils.galleryAddPic(Uri.fromFile(pictureFile), this);
				pictureFile = Utils.createImageFile(2);
				imagePath2 = pictureFile.getAbsolutePath();
				Logger.i("CameraMainActivity - got back from first confirm, startNewCameraActivity, imaggePath2 = "
						+ imagePath2);
				startSecondPicDialog();

			} else if (resultCode == Activity.RESULT_CANCELED) {
				// user declined, let's start the CameraActivity again with
				// first image
				startNewCameraActivity(Constants.RequestCodes.CAMERA_PICURE_FIRST);
			}
			break;

		case Constants.RequestCodes.CAMERA_CONFIRM_SECOND:
			// got back from confirmation with first image
			if (resultCode == Activity.RESULT_OK) {
				// user confirmed. starting NewPostActivity
				Utils.galleryAddPic(Uri.fromFile(pictureFile), this);
				startNewPostActivity(Constants.RequestCodes.NEW_POST);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// user declined, let's start the CameraActivity again with
				// second image
				startNewCameraActivity(Constants.RequestCodes.CAMERA_PICURE_SECOND);
			}
			break;

		case Constants.RequestCodes.NEW_POST:
			// got beck from postActivity
			if (resultCode == Activity.RESULT_OK) {
				// good, we can go back to choosieActivity!!
				goBackToChoosieActivity(Activity.RESULT_OK);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// mmm... so the user is not sure about his image, hey?
				int retakeImageNumber = data.getIntExtra(
						Constants.IntentsCodes.photoNumber, 0);
				retakeImage(retakeImageNumber);
			}
			break;
		}
	}

	private void startSecondPicDialog() {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("");

		myAlertDialog.setPositiveButton("Take second photo",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg1, int arg3) {
						startNewCameraActivity(Constants.RequestCodes.CAMERA_PICURE_SECOND);
					}
				});

		myAlertDialog.setNegativeButton("Create YES/NO question",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg1, int arg3) {
						BlurImageAndSendToPost();
					}
				});

		myAlertDialog.show();

	}

	protected void BlurImageAndSendToPost() {

		BitmapFactory.Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath1, options);
		options.inSampleSize = Utils.calculateInSampleSize(options,
				Utils.getScreenWidth(), Utils.getScreenWidth());
		options.inJustDecodeBounds = false;
		Bitmap original = BitmapFactory.decodeFile(imagePath1, options);

		Bitmap bluredBitmap = fastblur(original, 30);

		original.recycle();
		original = null;

		Utils.writeBitmapToFile(bluredBitmap, pictureFile, 100);

		makeYes();
		makeNo();

		startNewPostActivity(Constants.RequestCodes.NEW_POST);
	}

	private void makeNo() {

		BitmapFactory.Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath2, options);
		options.inSampleSize = Utils.calculateInSampleSize(options,
				Utils.getScreenWidth(), Utils.getScreenWidth());
		options.inJustDecodeBounds = false;
		Bitmap original = BitmapFactory.decodeFile(imagePath2, options);

		Bitmap noBitmap = combine(original,
				BitmapFactory.decodeResource(getResources(), R.drawable.naa));
		Utils.writeBitmapToFile(noBitmap, new File(imagePath2), 100);

		original.recycle();
		original = null;

		noBitmap.recycle();
		noBitmap = null;

	}

	private void makeYes() {

		BitmapFactory.Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath1, options);
		options.inSampleSize = Utils.calculateInSampleSize(options,
				Utils.getScreenWidth(), Utils.getScreenWidth());
		options.inJustDecodeBounds = false;
		Bitmap original = BitmapFactory.decodeFile(imagePath1, options);

		Bitmap yesBitmap = combine(original,
				BitmapFactory.decodeResource(getResources(), R.drawable.yaa));
		Utils.writeBitmapToFile(yesBitmap, new File(imagePath1), 100);

		original.recycle();
		original = null;

		yesBitmap.recycle();
		yesBitmap = null;
	}

	private void retakeImage(int retakeImageNumber) {
		if (retakeImageNumber == 1) {
			pictureFile = new File(imagePath1);
		} else if (retakeImageNumber == 2) {
			pictureFile = new File(imagePath2);
		}
		startConfirmActivity(Constants.RequestCodes.CAMERA_CONFIRM_SECOND);

	}

	private void handleResultFromCamera(int resultCode, Intent data,
			int requestCode) {
		if (resultCode == Activity.RESULT_OK) {
			// starting ConfirmationActivity
			bundle = data.getExtras();
			startConfirmActivity(requestCode);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			goBackToChoosieActivity(Activity.RESULT_CANCELED);
		}
	}

	private void goBackToChoosieActivity(int result) {
		setResult(result);
		finish();
	}

	private void startNewPostActivity(int requestCode) {
		Intent newPostIntent = new Intent(this.getApplication(),
				NewPostActivity.class);

		newPostIntent.putExtra(Constants.IntentsCodes.photo1Path, imagePath1);
		newPostIntent.putExtra(Constants.IntentsCodes.photo2Path, imagePath2);
		startActivityForResult(newPostIntent, requestCode);
	}

	private void startConfirmActivity(int requestCode) {
		Intent intent = new Intent(this.getApplication(),
				ConfirmationActivity.class);
		bundle.remove(Constants.IntentsCodes.path);
		Logger.i("CameraMain - inserting to intent path = "
				+ pictureFile.getAbsolutePath());
		intent.putExtra(Constants.IntentsCodes.path,
				pictureFile.getAbsolutePath());
		intent.putExtras(bundle);
		startActivityForResult(intent, requestCode);
	}

	private void startNewCameraActivity(int requestCode) {
		Logger.i("CameraMainActivity - startNewCameraActivity, path = "
				+ pictureFile.getAbsolutePath());
		Intent cameraIntent = new Intent(this.getApplication(),
				CameraActivity.class);

		cameraIntent.putExtra(Constants.IntentsCodes.path,
				pictureFile.getAbsolutePath());

		startActivityForResult(cameraIntent, requestCode);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(Activity.RESULT_CANCELED);
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public Bitmap fastblur(Bitmap sentBitmap, int radius) {

		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		Log.e("pix", w + " " + h + " " + pix.length);
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

		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return (bitmap);
	}

	private Bitmap combine(Bitmap bitmap1, Bitmap bitmap2) {

		Bitmap bitmap = null;
		try {
			bitmap = Bitmap.createBitmap(1224, 1224, Config.ARGB_8888);
			Canvas c = new Canvas(bitmap);
			// Resources res = getResources();

			// Bitmap bitmap1 = BitmapFactory
			// .decodeResource(res, R.drawable.test1); // blue

			// Bitmap bitmap2 = BitmapFactory
			// .decodeResource(res, R.drawable.test2); // green
			Drawable drawable1 = new BitmapDrawable(bitmap1);
			Drawable drawable2 = new BitmapDrawable(bitmap2);

			drawable1.setBounds(0, 0, 1224, 1224);
			drawable2.setBounds(0, 0, 1224, 1224);
			drawable1.draw(c);
			drawable2.draw(c);

		} catch (Exception e) {

			int i = 2;
			i = 90;
		}
		return bitmap;
	}

}
