package com.choosie.app.camera;

import java.io.File;

import com.choosie.app.Constants;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class GalleryActivity extends Activity {

	String path;
	String mCurrentPhotoPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Intent intent = getIntent();
		path = intent.getStringExtra(Constants.IntentsCodes.path);

		startCropingStuff();
	}

	private void startCropingStuff() {
		Log.i("cameraApi", "in startCropingStuff");

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", getWindowManager().getDefaultDisplay()
				.getWidth());
		intent.putExtra("outputY", getWindowManager().getDefaultDisplay()
				.getWidth());
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(path)));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", false); // lol, negative boolean
													// noFaceDetection
		startActivityForResult(intent,
				Constants.RequestCodes.CAMERA_GALLERY_CROP);

	}

	private void setRresultOk() {
		Log.i("cameraApi", " in handleCroppedImage");
		setResult(Activity.RESULT_OK);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("cameraApi", "in onActivityResult, code = " + requestCode
				+ " result = " + resultCode);

		if (resultCode == Activity.RESULT_OK) {

			switch (requestCode) {
			case Constants.RequestCodes.CAMERA_GALLERY_CROP:
				Log.i("cameraApi", "returned!, code CAMERA_GALLERY_CROP");
				setRresultOk();
				break;
			}
		} else {
			cancelIt();
		}
	}

	private void cancelIt() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
}
