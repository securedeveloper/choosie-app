package com.choozie.app.camera;

import java.io.File;

import com.choozie.app.Constants;
import com.choozie.app.L;
import com.choozie.app.R;
import com.choozie.app.Utils;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;

import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ConfirmationActivity extends Activity {

	private String path;
	private int topWrapperHeight;
	private int topHideHeight;
	private int bottomWrapperHeight;
	private int bottomHideHeight;
	private ImageView pictureImageView;
	private int screenWidth;
	private Bitmap scalledBitmapToShow;
	private Bitmap rotatedBitmap = null;
	private Bitmap framedBitmap = null;
	private boolean isBackKeyPressed = false;
	private int frameId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_confirmation);

		Intent intent = getIntent();
		path = intent.getStringExtra(Constants.IntentsCodes.path);
		frameId = intent.getIntExtra(Constants.IntentsCodes.frameId, 0);
		L.i("cameraConfirmation, getting from intent - path = " + path);

		manipulateHeightsAndSetListeners(intent);

		pictureImageView = (ImageView) findViewById(R.id.confirmation_image);

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth(); // deprecated

		scalledBitmapToShow = Bitmap
				.createScaledBitmap(BitmapFactory.decodeFile(path),
						screenWidth, screenWidth, false);

		L.i("cameraApi",
				"on confirmation, onCreate, about to show scalled bitmap width = "
						+ scalledBitmapToShow.getWidth() + " height = "
						+ scalledBitmapToShow.getWidth() + " size = "
						+ scalledBitmapToShow.getRowBytes()
						* scalledBitmapToShow.getHeight());

		if (frameId != 0) {
			scalledBitmapToShow = Utils.combine(scalledBitmapToShow,
					getResources().getDrawable(frameId));
		}
		pictureImageView.setImageBitmap(scalledBitmapToShow);
	}

	private void manipulateHeightsAndSetListeners(Intent intent) {

		Bundle bundle = intent.getExtras();

		topWrapperHeight = bundle.getInt(
				Constants.IntentsCodes.cameraTopWrapperHeight, 0);
		topHideHeight = bundle.getInt(
				Constants.IntentsCodes.cameraTopHideHeight, 0);
		bottomWrapperHeight = bundle.getInt(
				Constants.IntentsCodes.cameraBottomWrapperHeight, 0);
		bottomHideHeight = bundle.getInt(
				Constants.IntentsCodes.cameraBottomHideHeight, 0);

		L.i("orenc", "topHeight = " + topWrapperHeight + " bottomHeight = "
				+ bottomWrapperHeight + " topHideHeight = " + topHideHeight
				+ " bottomHideHeight = " + bottomHideHeight);

		RelativeLayout topLayot = (RelativeLayout) findViewById(R.id.confirmation_layout_top);
		RelativeLayout topHideLayot = (RelativeLayout) findViewById(R.id.confirmation_hide_layout_top);
		RelativeLayout bottomtLayot = (RelativeLayout) findViewById(R.id.confirmation_layout_bottom);
		RelativeLayout bottomHideLayot = (RelativeLayout) findViewById(R.id.confirmation_hide_layout_bottom);
		ImageView frameBlackImage = (ImageView) findViewById(R.id.confirmation_image_frame_black);
		ImageView framewhiteImage = (ImageView) findViewById(R.id.confirmation_image_frame_white);
		ImageView frameNoneImage = (ImageView) findViewById(R.id.confirmation_image_frame_none);
		ImageView framePatternImage = (ImageView) findViewById(R.id.confirmation_image_frame_pattern);
		ImageView framePattern2Image = (ImageView) findViewById(R.id.confirmation_image_frame_pattern2);
		ImageView framePattern3Image = (ImageView) findViewById(R.id.confirmation_image_frame_pattern3);
		ImageView framePattern4Image = (ImageView) findViewById(R.id.confirmation_image_frame_pattern4);
		ImageView framePattern5Image = (ImageView) findViewById(R.id.confirmation_image_frame_pattern5);

		Utils.setImageViewSize(topLayot, topWrapperHeight, 0);
		Utils.setImageViewSize(topHideLayot, topHideHeight, 0);

		Utils.setImageViewSize(bottomtLayot, bottomWrapperHeight, 0);
		Utils.setImageViewSize(bottomHideLayot, bottomHideHeight, 0);

		Utils.setImageViewSize(frameBlackImage, bottomWrapperHeight,
				bottomWrapperHeight);
		Utils.setImageViewSize(framewhiteImage, bottomWrapperHeight,
				bottomWrapperHeight);
		Utils.setImageViewSize(frameNoneImage, bottomWrapperHeight,
				bottomWrapperHeight);
		Utils.setImageViewSize(framePatternImage, bottomWrapperHeight,
				bottomWrapperHeight);
		Utils.setImageViewSize(framePattern2Image, bottomWrapperHeight,
				bottomWrapperHeight);
		Utils.setImageViewSize(framePattern3Image, bottomWrapperHeight,
				bottomWrapperHeight);
		Utils.setImageViewSize(framePattern4Image, bottomWrapperHeight,
				bottomWrapperHeight);
		Utils.setImageViewSize(framePattern5Image, bottomWrapperHeight,
				bottomWrapperHeight);

		ImageView rotateImageView = (ImageView) findViewById(R.id.confirmation_rotateImage1);
		Utils.setImageViewSize(rotateImageView, topWrapperHeight,
				topWrapperHeight);

		ImageView continueImageView = (ImageView) findViewById(R.id.confirmation_continueImage1);
		Utils.setImageViewSize(continueImageView, topWrapperHeight,
				topWrapperHeight);

		ImageView cancelImageView = (ImageView) findViewById(R.id.confirmation_cancelImage);
		Utils.setImageViewSize(cancelImageView, topWrapperHeight,
				topWrapperHeight);

		rotateImageView.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				handleRotate();
			}
		});

		continueImageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				handleConfirm();
			}
		});

		cancelImageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				cancelIt(false);
			}
		});

		OnClickListener frameListenere = new OnClickListener() {

			public void onClick(View v) {
				handleFrame(v);
			}
		};

		frameBlackImage.setOnClickListener(frameListenere);
		framewhiteImage.setOnClickListener(frameListenere);
		frameNoneImage.setOnClickListener(frameListenere);
		framePatternImage.setOnClickListener(frameListenere);
		framePattern2Image.setOnClickListener(frameListenere);
		framePattern3Image.setOnClickListener(frameListenere);
		framePattern4Image.setOnClickListener(frameListenere);
		framePattern5Image.setOnClickListener(frameListenere);
	}

	protected void handleFrame(View v) {

		if (rotatedBitmap == null) {
			framedBitmap = BitmapFactory.decodeFile(path);
		} else {
			framedBitmap = rotatedBitmap;
		}

		switch (v.getId()) {

		case R.id.confirmation_image_frame_none:
			if (frameId == 0)
				return;
			frameId = 0;
			break;

		case R.id.confirmation_image_frame_black:
			frameId = R.drawable.frame_black;
			break;

		case R.id.confirmation_image_frame_white:
			frameId = R.drawable.frame_white;
			break;

		case R.id.confirmation_image_frame_pattern:
			frameId = R.drawable.frame_pattern;
			break;

		case R.id.confirmation_image_frame_pattern2:
			frameId = R.drawable.frame_pattern2;
			break;

		case R.id.confirmation_image_frame_pattern3:
			frameId = R.drawable.frame_pattern3;
			break;

		case R.id.confirmation_image_frame_pattern4:
			frameId = R.drawable.frame_pattern4;
			break;

		case R.id.confirmation_image_frame_pattern5:
			frameId = R.drawable.frame_pattern5;
			break;
		}

		if (frameId != 0) {
			framedBitmap = Utils.combine(framedBitmap, getResources()
					.getDrawable(frameId));
		}

		// creating smaller size bitmap to show in imageView
		scalledBitmapToShow = Bitmap.createScaledBitmap(framedBitmap,
				screenWidth, screenWidth, false);

		pictureImageView.setImageBitmap(scalledBitmapToShow);
	}

	protected void handleRotate() {

		if (rotatedBitmap == null) {
			// need to keep the real dimension of the image, and rotate it
			rotatedBitmap = BitmapFactory.decodeFile(path);
		}
		rotatedBitmap = rotate(rotatedBitmap);

		pictureImageView.setImageBitmap(null);
		if (scalledBitmapToShow != null) {
			scalledBitmapToShow.recycle();
			scalledBitmapToShow = null;
		}

		// creating smaller size bitmap to show in imageView
		scalledBitmapToShow = Bitmap.createScaledBitmap(rotatedBitmap,
				screenWidth, screenWidth, false);

		pictureImageView.setImageBitmap(scalledBitmapToShow);
	}

	/*
	 * When the user confirmed it, if he rotated it we need to write the rotated
	 * bitmap in the file
	 */
	protected void handleConfirm() {

		if (rotatedBitmap != null) {
			Utils.writeBitmapToFile(rotatedBitmap, new File(path), 100);
		}

		Intent data = new Intent();
		data.putExtra(Constants.IntentsCodes.frameId, frameId);

		resetConfirmationActivity();
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	private void resetConfirmationActivity() {
		pictureImageView.setImageBitmap(null);
		if (rotatedBitmap != null) {
			rotatedBitmap.recycle();
			rotatedBitmap = null;
		}
		if (scalledBitmapToShow != null) {
			scalledBitmapToShow.recycle();
			scalledBitmapToShow = null;
		}
	}

	protected Bitmap rotate(Bitmap source) {

		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();
		int rotate = 0;
		switch (display.getRotation()) {
		case Surface.ROTATION_0:
			rotate = 90;
			break;
		case Surface.ROTATION_270:
			rotate = 180;
			break;
		}

		int width = source.getWidth();
		int height = source.getHeight();

		Matrix matrix = new Matrix();
		matrix.postRotate(rotate);

		Bitmap resizedBitmap = Bitmap.createBitmap(source, 0, 0, width, height,
				matrix, true);

		if (source != null) {
			source.recycle();
			source = null;
		}

		return resizedBitmap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_confirmation, menu);
		return true;
	}

	protected void cancelIt(boolean isNeedToReturnToMain) {
		if (isNeedToReturnToMain) {
			Intent intent = new Intent();
			intent.putExtra(Constants.IntentsCodes.stayOnScreen, true);
			setResult(RESULT_CANCELED, intent);
			finish();
		} else {
			setResult(RESULT_CANCELED);
			resetConfirmationActivity();
			finish();
		}
	}

	// setting the backKey to cancel the commentizatzia
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (isBackKeyPressed == false) {
				isBackKeyPressed = true;
				cancelIt(true);
			} else {
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		L.i("cameraApi", "cameraConfirmation cameraActivity onDestroy()");
		super.onDestroy();
		this.finish();
	}

	@Override
	protected void onRestart() {
		L.i("cameraApi", "cameraConfirmation cameraActivity onRestart()");
		super.onRestart();
	}

	@Override
	protected void onStop() {
		L.i("cameraApi", "cameraConfirmation cameraActivity onStop()");
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

}