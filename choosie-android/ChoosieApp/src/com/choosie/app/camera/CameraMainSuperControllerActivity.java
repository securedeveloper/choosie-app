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
import android.content.Intent;
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
				startNewCameraActivity(Constants.RequestCodes.CAMERA_PICURE_SECOND);

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

}
