package com.choosie.app.camera;

import java.io.File;
import java.util.List;

import com.choosie.app.Constants;
import com.choosie.app.Logger;
import com.choosie.app.R;
import com.choosie.app.Utils;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CameraMainSuperControllerActivity extends Activity {

	private enum MODE {
		TOT, YAA_NAA
	}

	// private File pictureFile;
	private String imagePath1;
	private String imagePath2;
	private Bundle bundle;

	private MODE currentMode = MODE.TOT;

	private ImageView totImageView;
	private ImageView yaanaaImageView;

	private RelativeLayout topLayout;
	private EditText mQuestion;
	private ImageView mImage1;
	private ImageView mImage2;
	private TextView mTvFacebook;
	private ToggleButton mTbFacebook;
	private Session session;
	// private StatusCallback statusCallback = new SessionStatusCallback();
	private ImageButton mBtnSubmit;

	private File imageFile1;
	private File imageFile2;

	private Bitmap image1BitmapTot;
	private Bitmap image2BitmapTot;
	private Bitmap image1BitmapYaanaa;
	private Bitmap image2BitmapYaanaa;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_main);

		InitializeComponents();

		// initializing bundle;
		bundle = new Bundle();
		// creating the first file and starting cameraActivity
		imageFile1 = Utils.createImageFile(1);
		imageFile2 = Utils.createImageFile(2);
		imagePath1 = imageFile1.getAbsolutePath();
		imagePath2 = imageFile2.getAbsolutePath();
		Logger.i("CameraMainActivity - onCreate - about to startNewCameraActivity, imaggePath1 = "
				+ imagePath1 + " imagePath2 = " + imagePath2);
		startNewCameraActivity(Constants.RequestCodes.CAMERA_PICURE_FIRST,
				imagePath1);
	}

	private void InitializeComponents() {

		// initialize all components
		this.topLayout = (RelativeLayout) findViewById(R.id.post_layout_top);
		this.mQuestion = (EditText) findViewById(R.id.post_tvQuestion);
		this.mImage1 = (ImageView) findViewById(R.id.image_photo1);
		this.mImage2 = (ImageView) findViewById(R.id.image_photo2);
		this.mTvFacebook = (TextView) findViewById(R.id.tvFacebook);
		this.mTbFacebook = (ToggleButton) findViewById(R.id.tbFacebook);
		this.mBtnSubmit = (ImageButton) findViewById(R.id.post_btnSubmit);
		this.session = Session.getActiveSession();
		this.yaanaaImageView = (ImageView) findViewById(R.id.post_yaanaaButton_image);
		this.totImageView = (ImageView) findViewById(R.id.post_totButton_image);

		float density = getApplicationContext().getResources()
				.getDisplayMetrics().density;
		int topHeight = Math.round(50 * density);

		// set heights and shit
		Utils.setImageViewSize(mImage1, Utils.getScreenWidth() / 2,
				Utils.getScreenWidth() / 2);
		Utils.setImageViewSize(mImage2, Utils.getScreenWidth() / 2,
				Utils.getScreenWidth() / 2);
		Utils.setImageViewSize(topLayout, topHeight, 0);
		Utils.setImageViewSize(mBtnSubmit, topHeight, topHeight);
		Utils.setImageViewSize(findViewById(R.id.post_imagesLayout),
				(Utils.getScreenWidth() / 2) + 10, 0);

		yaanaaImageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				switchToYaanaaMode();
			}
		});

		yaanaaImageView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {

				if (currentMode == MODE.YAA_NAA) {
					return true;
				}

				int maxX = arg0.getWidth();
				int maxY = arg0.getHeight();

				float x = arg1.getX();
				float y = arg1.getY();

				// if moved outside the button - make the focus image gone
				if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
					if ((x < 0) || (y < 0) || (x > maxX) || (y > maxY)) {
						yaanaaImageView.setImageResource(R.drawable.yaa_naa);

						return true;
					}
				}

				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
					yaanaaImageView
							.setImageResource(R.drawable.yaa_naa_pressed);
				}

				if (arg1.getAction() == MotionEvent.ACTION_UP) {

					if ((x > 0) && (y > 0) && (x < maxX) && (y < maxY)) {
						yaanaaImageView.performClick();
					}
				}

				return true;

			}
		});

		totImageView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {

				if (currentMode == MODE.TOT) {
					return true;
				}

				int maxX = arg0.getWidth();
				int maxY = arg0.getHeight();

				float x = arg1.getX();
				float y = arg1.getY();

				// if moved outside the button - make the focus image gone
				if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
					if ((x < 0) || (y < 0) || (x > maxX) || (y > maxY)) {
						totImageView.setImageResource(R.drawable.tot);

						return true;
					}
				}

				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
					totImageView.setImageResource(R.drawable.tot_pressed);
				}

				if (arg1.getAction() == MotionEvent.ACTION_UP) {

					if ((x > 0) && (y > 0) && (x < maxX) && (y < maxY)) {
						totImageView.performClick();
					}
				}

				return true;

			}
		});

		totImageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				switchToTotMode();
			}
		});

		mImage1.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				handleImage1Click();
			}
		});

		mImage2.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				handleImage2Click();

			}
		});
	}

	protected void handleImage1Click() {

		startConfirmActivity(Constants.RequestCodes.CAMERA_CONFIRM_FIRST,
				imagePath1);
	}

	protected void handleImage2Click() {
		if (currentMode == MODE.TOT) {

			if (image2BitmapTot != null) {
				startConfirmActivity(
						Constants.RequestCodes.CAMERA_CONFIRM_SECOND,
						imagePath2);
			} else {
				startNewCameraActivity(
						Constants.RequestCodes.CAMERA_PICURE_SECOND, imagePath2);
			}
		} else if (currentMode == MODE.YAA_NAA) {
			startConfirmActivity(Constants.RequestCodes.CAMERA_CONFIRM_FIRST,
					imagePath1);
		}
	}

	protected void switchToTotMode() {

		if (currentMode == MODE.TOT) {
			return;
		}

		yaanaaImageView.setImageResource(R.drawable.yaa_naa);
		totImageView.setImageResource(R.drawable.tot_pressed);

		mImage1.setImageBitmap(image1BitmapTot);

		if (image2BitmapTot != null) {
			mImage2.setImageBitmap(image2BitmapTot);
		} else {
			mImage2.setImageDrawable(getResources()
					.getDrawable(R.drawable.plus));
		}

		currentMode = MODE.TOT;

	}

	protected void switchToYaanaaMode() {

		if (currentMode == MODE.YAA_NAA) {
			return;
		}
		yaanaaImageView.setImageResource(R.drawable.yaa_naa_pressed);
		totImageView.setImageResource(R.drawable.tot);

		mImage1.setImageBitmap(image1BitmapYaanaa);
		mImage2.setImageBitmap(image2BitmapYaanaa);

		// Utils.writeBitmapToFile(bluredBitmap, pictureFile, 100);

		// makeNo();

		// startNewPostActivity(Constants.RequestCodes.NEW_POST);

		// set image2 as the blured image

		currentMode = MODE.YAA_NAA;
	}

	protected boolean isUserHasPublishPermissions() {
		boolean userHasPublishPermissions = false;
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			List<String> perms = session.getPermissions();
			userHasPublishPermissions = perms.contains("publish_stream");
		} else {
			Logger.i("isUserHasPublishPermissions(): session is not opened!");
		}
		return userHasPublishPermissions;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case Constants.RequestCodes.CAMERA_PICURE_FIRST:
			// get back from camera with first file
			handleResultFromCamera(resultCode, data,
					Constants.RequestCodes.CAMERA_CONFIRM_FIRST, imagePath1);
			break;

		case Constants.RequestCodes.CAMERA_PICURE_SECOND:
			// get back from camera with first file
			handleResultFromCamera(resultCode, data,
					Constants.RequestCodes.CAMERA_CONFIRM_SECOND, imagePath2);
			break;

		case Constants.RequestCodes.CAMERA_CONFIRM_FIRST:
			// got back from confirmation with first image
			if (resultCode == Activity.RESULT_OK) {
				// user confirmed, save it in the gallery, and starting
				// cmaeraActivity with second image
				Utils.galleryAddPic(Uri.fromFile(imageFile1), this);
				Logger.i("CameraMainActivity - got back from first confirm, startNewCameraActivity, imaggePath2 = "
						+ imagePath2);
				saveImage1Bitmap();
				setImages();

				// startSecondPicDialog();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// user declined, let's start the CameraActivity again with
				// first image
				startNewCameraActivity(
						Constants.RequestCodes.CAMERA_PICURE_FIRST, imagePath1);
			}
			break;

		case Constants.RequestCodes.CAMERA_CONFIRM_SECOND:
			// got back from confirmation with first image
			if (resultCode == Activity.RESULT_OK) {
				// user confirmed. starting NewPostActivity
				Utils.galleryAddPic(Uri.fromFile(imageFile2), this);

				saveImage2Bitmap();
				mImage2.setBackgroundDrawable(null);
				mImage2.setImageBitmap(image2BitmapTot);

			} else if (resultCode == Activity.RESULT_CANCELED) {
				// user declined, let's start the CameraActivity again with
				// second image
				startNewCameraActivity(
						Constants.RequestCodes.CAMERA_PICURE_SECOND, imagePath2);
			}
			break;

		case Constants.RequestCodes.NEW_POST:
			// got beck from postActivity
			if (resultCode == Activity.RESULT_OK) {
				// good, we can go back to choosieActivity!!
				goBackToChoosieActivity(Activity.RESULT_OK);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// mmm... so the user is not sure about his image, hey?

			}
			break;
		}
	}

	private void setImages() {
		if (currentMode == MODE.TOT) {
			mImage1.setImageBitmap(image1BitmapTot);
		} else if (currentMode == MODE.YAA_NAA) {
			mImage1.setImageBitmap(image1BitmapYaanaa);
			mImage2.setImageBitmap(image2BitmapYaanaa);
		}
	}

	private void saveImage2Bitmap() {

		mImage2.setImageBitmap(null);

		if (image2BitmapTot != null) {
			image2BitmapTot.recycle();
			image2BitmapTot = null;
		}

		// get images bitmaps
		image2BitmapTot = Utils.getBitmapFromFileByViewSize(imagePath2,
				Utils.getScreenWidth() / 2, Utils.getScreenWidth() / 2);

	}

	private void saveImage1Bitmap() {

		mImage1.setImageBitmap(null);

		if (image1BitmapTot != null) {
			image1BitmapTot.recycle();
			image1BitmapTot = null;
		}

		if (image1BitmapYaanaa != null) {
			image1BitmapYaanaa.recycle();
			image1BitmapYaanaa = null;
		}

		if (image2BitmapYaanaa != null) {
			image2BitmapYaanaa.recycle();
			image2BitmapYaanaa = null;
		}

		// get images bitmaps
		image1BitmapTot = Utils.getBitmapFromFileByViewSize(imagePath1,
				Utils.getScreenWidth() / 2, Utils.getScreenWidth() / 2);

		// set image 1 and 2 yaanaa

		image1BitmapYaanaa = combine(image1BitmapTot, getResources()
				.getDrawable(R.drawable.yaa));
		image2BitmapYaanaa = combine(fastblur(image1BitmapTot, 30),
				getResources().getDrawable(R.drawable.naa));
	}

	private void handleResultFromCamera(int resultCode, Intent data,
			int requestCode, String path) {
		if (resultCode == Activity.RESULT_OK) {
			// starting ConfirmationActivity
			bundle = data.getExtras();
			startConfirmActivity(requestCode, path);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			goBackToChoosieActivity(Activity.RESULT_CANCELED);
		}
	}

	private void goBackToChoosieActivity(int result) {
		setResult(result);
		finish();
	}

	private void startConfirmActivity(int requestCode, String path) {
		Intent intent = new Intent(this.getApplication(),
				ConfirmationActivity.class);
		bundle.remove(Constants.IntentsCodes.path);
		Logger.i("CameraMain - inserting to intent path = " + path);
		intent.putExtra(Constants.IntentsCodes.path, path);
		intent.putExtras(bundle);
		startActivityForResult(intent, requestCode);
	}

	private void startNewCameraActivity(int requestCode, String path) {
		Logger.i("CameraMainActivity - startNewCameraActivity, path = " + path);
		Intent cameraIntent = new Intent(this.getApplication(),
				CameraActivity.class);

		cameraIntent.putExtra(Constants.IntentsCodes.path, path);

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

	private Bitmap combine(Bitmap bitmap1, Drawable drawable2) {

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
			// Drawable drawable2 = new BitmapDrawable(bitmap2);

			drawable1.setBounds(0, 0, 1224, 1224);
			drawable2.setBounds(0, 0, 1224, 1224);
			drawable1.draw(c);
			drawable2.draw(c);

		} catch (Exception e) {

		}
		return bitmap;
	}

}
