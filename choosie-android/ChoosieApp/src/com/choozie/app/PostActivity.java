package com.choozie.app;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.choozie.app.client.Client;
import com.choozie.app.views.BottomNavigationBarView;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Session.ReauthorizeRequest;
import com.facebook.Session.StatusCallback;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PostActivity extends Activity {

	private ImageView image1;
	private ImageView image2;
	private Button buttonSubmit;
	private ToggleButton shareOnFacebookTb;
	private Session session;
	private StatusCallback statusCallback = new SessionStatusCallback();
	private Bitmap mImage1;
	private Bitmap mImage2;
	private boolean isNeedToSave;
	private String mCurrentPhotoPath;
	private String mQuestion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);

		Intent intent = getIntent();

		LinearLayout bottomView = (LinearLayout) findViewById(R.id.post_buttom_nav_bar);

		BottomNavigationBarView customView = new BottomNavigationBarView(this,
				this, Screen.POST);
		bottomView.addView(customView);
		customView
				.changeSelectedButton((RelativeLayout) findViewById(R.id.view_navBar_layout_button_post));

		image1 = (ImageView) findViewById(R.id.post_image_photo1);
		image2 = (ImageView) findViewById(R.id.post_image_photo2);
		buttonSubmit = (Button) findViewById(R.id.post_button_submit);
		EditText questionText = (EditText) findViewById(R.id.post_question);
		questionText.setFocusable(true);
		// questionText.setInputType(EditorInfo.TYPE_NULL);
		shareOnFacebookTb = (ToggleButton) findViewById(R.id.post_shareOnFacebookToggleButton);

		OnCheckedChangeListener checkChangedListener = new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					// check if user has publish_stream permissions
					boolean userHasPublishPermissions = isUserHasPublishPermissions();
					shareOnFacebookTb
							.setBackgroundResource(R.drawable.facebook_square_blue);

					// if so, just show button as checked
					if (userHasPublishPermissions) {
						L.i("Already have publish permissions: "
								+ session.getPermissions().toString());
						shareOnFacebookTb.setChecked(true);

					} else {
						// else - pop up Facebook screen and ask for
						// publish_stream permissions
						askForPublishPermissions();
					}
				} else {
					shareOnFacebookTb
							.setBackgroundResource(R.drawable.facebook_square_bw);
				}
			}
		};

		OnClickListener listener = new OnClickListener() {
			public void onClick(View arg0) {
				onItemClick(arg0);

			}
		};

		shareOnFacebookTb.setOnCheckedChangeListener(checkChangedListener);
		image1.setOnClickListener(listener);
		image2.setOnClickListener(listener);
		buttonSubmit.setOnClickListener(listener);
		session = Session.getActiveSession();
	}

	protected void askForPublishPermissions() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			List<String> permissions = new ArrayList<String>();
			permissions.add("publish_stream");

			ReauthorizeRequest request = new ReauthorizeRequest(this,
					permissions);
			request.setCallback(statusCallback);

			try {
				session.reauthorizeForPublish(request);
			} catch (Exception ex) {
				L.e("Exception in reauthorizeForPublish() : " + ex.toString());
			}
			L.i("on set active session permissions: "
					+ session.getPermissions().toString());
		}
	}

	public void refresh() {
		// super.refresh();

		// NOTE: Special handling: In case user presses on 'Share on Facebook'
		// and
		// then, after we launch askForPublicPermissions(), the user cancels, we
		// identify this situation and set it back to 'false'.
		// This happens by:
		// ChoosieActivity gets onActivityResult with
		// requestCode == FB_REQUEST_PUBLISH_PERMISSION, it calls this refresh
		// method.
		if (this.shareOnFacebookTb.isChecked()
				&& !isUserHasPublishPermissions()) {
			this.shareOnFacebookTb.setChecked(false);
		}
	}

	protected boolean isUserHasPublishPermissions() {
		boolean userHasPublishPermissions = false;
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			List<String> perms = session.getPermissions();
			userHasPublishPermissions = perms.contains("publish_stream");
		} else {
			L.i("isUserHasPublishPermissions(): session is not opened!");
		}
		return userHasPublishPermissions;
	}

	private void onItemClick(View arg0) {
		if (arg0.getId() == R.id.post_button_submit) {
			if (shareOnFacebookTb.isChecked()) {
				L.i("Share on facebook is checked!");
				Session session = Session.getActiveSession();
				if (session.isOpened()) {
					L.i("session permissions: "
							+ session.getPermissions().toString());
					if (!session.getPermissions().contains("publish_stream")) {

						L.i("requesting publish_stream permissions");

						List<String> write_permissions = new ArrayList<String>();
						write_permissions.add("publish_stream");

						L.i("Opening new ReauthorizeRequest");
						ReauthorizeRequest openRequest = new ReauthorizeRequest(
								this, write_permissions);
						try {
							L.i("Opening new ReauthorizeRequest");
							session.reauthorizeForPublish(openRequest);
						} catch (Exception ex) {
							L.e("EXCEPTION!!! : " + ex.toString());
						}
					}
				}
			}
			L.i("executing submitChoosiePost()");
			submitChoosiePost();
		} else {
			startDialog(arg0);
		}
	}

	private void startDialog(final View arg0) {
		L.i("PostScreenController - enter startdialog");
		final File tempFile = createImageFile(arg0.getId());

		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Upload Pictures Option");
		myAlertDialog.setMessage("How do you want to set your picture?");

		myAlertDialog.setPositiveButton("Camera",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg1, int arg3) {
						TakePhoto(arg0, Uri.fromFile(tempFile));
					}
				});

		myAlertDialog.setNegativeButton("Gallery",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg2, int arg1) {
						takeImageFromGallery(arg0);
					}
				});

		myAlertDialog.show();
	}

	private void TakePhoto(View arg0, Uri uri) {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

		if (arg0.getId() == R.id.post_image_photo1) {
			L.i("PostScreenController - enter TakePhoto - first pic");
			startActivityForResult(intent,
					Constants.RequestCodes.TAKE_FIRST_PICTURE_FROM_CAMERA);
		}
		if (arg0.getId() == R.id.post_image_photo2) {
			L.i("PostScreenController - enter TakePhoto - second pic");
			intent.putExtra("return-data", true);
			startActivityForResult(intent,
					Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_CAMERA);
		}
	}

	private void takeImageFromGallery(View arg0) {

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);

		if (arg0.getId() == R.id.post_image_photo1) {
			L.i("PostScreenController - enter takeImageFromGallery - first pic");
			startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
					Constants.RequestCodes.TAKE_FIRST_PICTURE_FROM_GALLERY);
		}

		if (arg0.getId() == R.id.post_image_photo2) {
			L.i("PostScreenController - enter takeImageFromGallery - second pic");
			startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
					Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_GALLERY);
		}
	}

	// when the camera or gallery return
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		L.i("PostScreenController - onActivityResult - request code = "
				+ requestCode + " result code = " + resultCode);

		if (resultCode == Activity.RESULT_CANCELED
				&& requestCode != Constants.RequestCodes.FB_REQUEST_PUBLISH_PERMISSION) {
			return;
		}

		switch (requestCode) {
		case Constants.RequestCodes.CROP_FIRST:

			// Wysie_Soh: After a picture is taken, it will go to
			// PICK_FROM_CAMERA, which will then come here
			// after the image is cropped.

			if (mImage1 != null) {
				mImage1.recycle();
			}
			mImage1 = setImageFromData(data, image1);
			break;

		case Constants.RequestCodes.CROP_SECOND:

			// Wysie_Soh: After a picture is taken, it will go to
			// PICK_FROM_CAMERA, which will then come here
			// after the image is cropped.

			if (mImage2 != null) {
				mImage2.recycle();
			}
			mImage2 = setImageFromData(data, image2);
			break;

		case Constants.RequestCodes.TAKE_FIRST_PICTURE_FROM_CAMERA:
			isNeedToSave = true;
			setAndStartCropIntent(Constants.RequestCodes.CROP_FIRST,
					Uri.fromFile(new File(mCurrentPhotoPath)));
			break;

		case Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_CAMERA:
			isNeedToSave = true;
			setAndStartCropIntent(Constants.RequestCodes.CROP_SECOND,
					Uri.fromFile(new File(mCurrentPhotoPath)));
			break;

		case Constants.RequestCodes.TAKE_FIRST_PICTURE_FROM_GALLERY:
			isNeedToSave = false;
			setAndStartCropIntent(Constants.RequestCodes.CROP_FIRST,
					data.getData());
			break;

		case Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_GALLERY:
			isNeedToSave = false;
			setAndStartCropIntent(Constants.RequestCodes.CROP_SECOND,
					data.getData());
			break;
			
		case Constants.RequestCodes.FB_REQUEST_PUBLISH_PERMISSION:
			L.i("after activity fb");
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
			refresh();
		}

	}

	private Bitmap setImageFromData(Intent data, ImageView imageView) {
		L.i("PostScreenController - enter setImageFromData");
		if (isNeedToSave == true) {
			galleryAddPic();
		}
		final Bundle extras = data.getExtras();

		Bitmap imageBitmapToReturn = null;

		if (extras != null) {
			imageView.setImageBitmap(null);
			imageBitmapToReturn = extras.getParcelable("data");
			int w = imageBitmapToReturn.getHeight();
			int h = imageBitmapToReturn.getWidth();
			// imageView.getLayoutParams().height = w;
			// imageView.getLayoutParams().width = h;
			imageView.setImageBitmap(imageBitmapToReturn);
			imageView.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.image_frame_post_filled));
		}

		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(imageView, InputMethodManager.SHOW_IMPLICIT);
		return imageBitmapToReturn;
	}

	private void setAndStartCropIntent(int code, Uri uri) {
		L.i("PostScreenController - enter setAndStartCropIntent");
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("outputX", 350);
		intent.putExtra("outputY", 350);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, code);
	}

	private void submitChoosiePost() {

		if ((mImage1 == null) || (mImage2 == null)) {
			Toast toast = Toast.makeText(this, "Please add two photos",
					Toast.LENGTH_SHORT);
			toast.show();
		} else {
			EditText questionText = (EditText) findViewById(R.id.post_question);
			mQuestion = questionText.getText().toString();
			if (mQuestion.equals("")) {
				Toast toast = Toast.makeText(this, "Please add a question",
						Toast.LENGTH_SHORT);
				toast.show();
			} else {
				Tracker tracker = GoogleAnalytics.getInstance(this)
						.getDefaultTracker();
				tracker.trackEvent("Ui action", "Post Screen", "Share", null);

				Client.getInstance().sendChoosiePostToServer(
						new NewChoosiePostData(mImage1, mImage2, mQuestion,
								shareOnFacebookTb.isChecked()),
						new Callback<Void, Integer, Void>() {

							@SuppressWarnings("deprecation")
							@Override
							public void onPre(Void param) {
								showDialog(Constants.DialogId.WAIT_LOADING);
							}

							@Override
							public void onProgress(Integer param) {
							}

							@Override
							public void onFinish(Void param) {
								goBackToChoosieActivity(Activity.RESULT_OK);
							}
						});
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Constants.DialogId.EXIT_ALERT_DIALOG:
			// Create out AlterDialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage("This will discard the pictures you have taken.");
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new OkOnClickListener());
			builder.setNegativeButton("Back to camera",
					new CancelOnClickListener());
			AlertDialog dialog = builder.create();
			dialog.show();
			break;

		case Constants.DialogId.WAIT_LOADING:
			ProgressDialog dialog1 = new ProgressDialog(this);
			dialog1.setMessage("Please wait while uploading...");
			dialog1.setIndeterminate(true);
			dialog1.setCancelable(false);
			return dialog1;

		case Constants.DialogId.ERROR:
			AlertDialog.Builder builderError = new AlertDialog.Builder(this);
			builderError.setMessage("Camera is having issues");
			builderError.setCancelable(false);
			builderError.setPositiveButton("OK", new OkOnClickListener());
			AlertDialog dialogError = builderError.create();
			dialogError.show();
			break;
		}

		return super.onCreateDialog(id);
	}

	private final class CancelOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {

		}
	}

	private final class OkOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

	private void goBackToChoosieActivity(int result) {
		setResult(result);
		finish();
		resetPost();
	}

	private void resetPost() {
		image1.setImageResource(R.drawable.camera);
		image2.setImageResource(R.drawable.camera);
		image1.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.image_frame_post));
		image2.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.image_frame_post));
		if (mImage1 != null) {
			mImage1.recycle();
		}
		if (mImage2 != null) {
			mImage2.recycle();
		}
		mImage1 = null;
		mImage2 = null;
		mQuestion = null;
		EditText questionText = (EditText) findViewById(R.id.post_question);
		questionText.setText("");
		shareOnFacebookTb.setChecked(false);

	}

	private File createImageFile(Integer prefix) {
		L.i("PostScreenController - enter createImageFile");
		File dir = getAlbumDir();
		if (dir.exists() == false) {
			L.i("PostScreenController - createImageFile: the dir is not exist, path = "
					+ dir.getAbsolutePath());
			boolean dirCreated = dir.mkdirs();
			L.i("PostScreenController, dirCreated = " + dirCreated);
		} else {
			L.i("PostScreenController - createImageFile: dir exists, path = "
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
		mCurrentPhotoPath = imageFile.getAbsolutePath();
		return imageFile;
	}

	private File getAlbumDir() {
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

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		L.i("PostScreenController - adding to gallery - " + mCurrentPhotoPath);
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		sendBroadcast(mediaScanIntent);
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		public void call(Session session, SessionState state,
				Exception exception) {
			L.i("Entered SessionStatusCallback()");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_post, menu);
		return true;
	}

}
