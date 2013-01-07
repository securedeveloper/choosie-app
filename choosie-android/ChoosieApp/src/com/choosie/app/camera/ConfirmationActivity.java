package com.choosie.app.camera;

import java.io.File;
import com.choosie.app.Constants;
import com.choosie.app.Logger;
import com.choosie.app.R;
import com.choosie.app.Utils;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.util.Log;
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
	private boolean isBackKeyPressed = false;

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
		Logger.i("cameraConfirmation, getting from intent - path = " + path);

		manipulateHeightsAndSetListeners(intent);

		pictureImageView = (ImageView) findViewById(R.id.confirmation_image);
		// pictureImageView.getLayoutParams().height = screenWidth;
		// pictureImageView.getLayoutParams().width = screenWidth;

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth(); // deprecated
		// int height = display.getHeight(); // deprecated

		scalledBitmapToShow = Bitmap.createScaledBitmap(
				BitmapFactory.decodeFile(path), screenWidth,
				screenWidth, false);

		Log.i("cameraApi",
				"on confirmation, onCreate, about to show scalled bitmap width = "
						+ scalledBitmapToShow.getWidth() + " height = "
						+ scalledBitmapToShow.getWidth() + " size = "
						+ scalledBitmapToShow.getRowBytes()
						* scalledBitmapToShow.getHeight());

		pictureImageView.setImageBitmap(scalledBitmapToShow);
	}

	private void manipulateHeightsAndSetListeners(Intent intent) {
		float density = getApplicationContext().getResources()
				.getDisplayMetrics().density;

		Bundle bundle = intent.getExtras();

		topWrapperHeight = bundle.getInt(
				Constants.IntentsCodes.cameraTopWrapperHeight, 0);
		topHideHeight = bundle.getInt(
				Constants.IntentsCodes.cameraTopHideHeight, 0);
		bottomWrapperHeight = bundle.getInt(
				Constants.IntentsCodes.cameraBottomWrapperHeight, 0);
		bottomHideHeight = bundle.getInt(
				Constants.IntentsCodes.cameraBottomHideHeight, 0);

		Log.i("orenc", "topHeight = " + topWrapperHeight + " bottomHeight = "
				+ bottomWrapperHeight + " topHideHeight = " + topHideHeight
				+ " bottomHideHeight = " + bottomHideHeight);

		RelativeLayout topLayot = (RelativeLayout) findViewById(R.id.confirmation_layout_top);
		RelativeLayout topHideLayot = (RelativeLayout) findViewById(R.id.confirmation_hide_layout_top);
		RelativeLayout bottomtLayot = (RelativeLayout) findViewById(R.id.confirmation_layout_bottom);
		RelativeLayout bottomHideLayot = (RelativeLayout) findViewById(R.id.confirmation_hide_layout_bottom);

		Utils.setImageViewSize(topLayot, topWrapperHeight, 0);
		Utils.setImageViewSize(topHideLayot, topHideHeight, 0);

		Utils.setImageViewSize(bottomtLayot, bottomWrapperHeight, 0);
		Utils.setImageViewSize(bottomHideLayot, bottomHideHeight, 0);

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
				cancelIt();
			}
		});
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

		resetConfirmationActivity();
		setResult(Activity.RESULT_OK);
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

	protected void cancelIt() {
		setResult(RESULT_CANCELED, null);
		resetConfirmationActivity();
		finish();
	}

	// setting the backKey to cancel the commentizatzia
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (isBackKeyPressed == false) {
				isBackKeyPressed = true;
				cancelIt();
			} else {
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		Log.i("cameraApi", "cameraConfirmation cameraActivity onDestroy()");
		super.onDestroy();
		this.finish();
	}

	@Override
	protected void onRestart() {
		Log.i("cameraApi", "cameraConfirmation cameraActivity onRestart()");
		super.onRestart();
	}

	@Override
	protected void onStop() {
		Log.i("cameraApi", "cameraConfirmation cameraActivity onStop()");
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

}