package com.choosie.app.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.choosie.app.R;
import com.choosie.app.R.layout;
import com.choosie.app.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
	private int photoNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_confirmation);

		Intent intent = getIntent();
		path = intent.getStringExtra("path");
		photoNumber = intent.getIntExtra("photoNumber", 0);

		manipulateHeightsAndSetListeners(intent);

		pictureImageView = (ImageView) findViewById(R.id.confirmation_image);
		// pictureImageView.getLayoutParams().height = screenWidth;
		// pictureImageView.getLayoutParams().width = screenWidth;

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth(); // deprecated
		// int height = display.getHeight(); // deprecated

		scalledBitmapToShow = Bitmap.createScaledBitmap(
				BitmapFactory.decodeFile(path), screenWidth + 20,
				screenWidth + 20, false);

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
		topWrapperHeight = intent.getIntExtra("topWrapperHeight", 0);
		topHideHeight = intent.getIntExtra("topHideHeight", 0);
		bottomWrapperHeight = intent.getIntExtra("bottomWrapperHeight", 0);
		bottomHideHeight = intent.getIntExtra("bottomHideHeight", 0);

		Log.i("orenc", "topHeight = " + topWrapperHeight + " bottomHeight = "
				+ bottomWrapperHeight + " topHideHeight = " + topHideHeight
				+ " bottomHideHeight = " + bottomHideHeight);

		RelativeLayout topLayot = (RelativeLayout) findViewById(R.id.confirmation_layout_top);
		RelativeLayout topHideLayot = (RelativeLayout) findViewById(R.id.confirmation_hide_layout_top);
		RelativeLayout bottomtLayot = (RelativeLayout) findViewById(R.id.confirmation_layout_bottom);
		RelativeLayout bottomHideLayot = (RelativeLayout) findViewById(R.id.confirmation_hide_layout_bottom);

		topLayot.getLayoutParams().height = topWrapperHeight;
		topHideLayot.getLayoutParams().height = topHideHeight;
		bottomtLayot.getLayoutParams().height = bottomWrapperHeight;
		bottomHideLayot.getLayoutParams().height = bottomHideHeight;

		ImageView rotateImageView = (ImageView) findViewById(R.id.confirmation_rotateImage1);
		rotateImageView.getLayoutParams().height = topWrapperHeight;
		rotateImageView.getLayoutParams().width = topWrapperHeight;

		ImageView continueImageView = (ImageView) findViewById(R.id.confirmation_continueImage1);
		continueImageView.getLayoutParams().height = topWrapperHeight;
		continueImageView.getLayoutParams().width = topWrapperHeight;

		ImageView cancelImageView = (ImageView) findViewById(R.id.confirmation_cancelImage);
		cancelImageView.getLayoutParams().height = topWrapperHeight;
		cancelImageView.getLayoutParams().width = topWrapperHeight;

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
			// need to save the rotated image
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			byte[] bitmapdata = bos.toByteArray();
			writeDataIntoFile(bitmapdata, new File(path));
		}

		resetConfirmationActivity();

		if (photoNumber == 1) {
			showWaitingDialog();
		} else {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

	private void showWaitingDialog() {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("");
		myAlertDialog.setMessage("Picture second photo");

		myAlertDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg1, int arg3) {
						setResult(Activity.RESULT_OK);
						finish();
					}
				});
		myAlertDialog.show();
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

	private void writeDataIntoFile(byte[] data, File pictureFile) {
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();

		} catch (FileNotFoundException e) {
			Log.d("orenc", "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d("orenc", "Error accessing file: " + e.getMessage());
		}
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
			cancelIt();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}