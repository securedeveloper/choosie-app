package com.choosie.app.controllers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.choosie.app.Callback;
import com.choosie.app.Constants;
import com.choosie.app.NewChoosiePostData;
import com.choosie.app.R;
import com.choosie.app.Screen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.Session;
import com.facebook.Session.ReauthorizeRequest;

public class PostScreenController extends ScreenController {
	private Bitmap mImage1;
	private Bitmap mImage2;
	private String mQuestion;
	private ImageView image1;
	private ImageView image2;
	private Button buttonSubmit;
	private ToggleButton shareOnFacebookTb;
	private String mCurrentPhotoPath;
	private Boolean isNeedToSave;

	public PostScreenController(View layout, SuperController superController) {
		super(layout, superController);
	}

	@Override
	protected void onCreate() {
		image1 = (ImageView) view.findViewById(R.id.image_photo1);
		image2 = (ImageView) view.findViewById(R.id.image_photo2);
		buttonSubmit = (Button) view.findViewById(R.id.button_submit1);
		EditText questionText = (EditText) view
				.findViewById(R.id.editText_question);
		questionText.setFocusable(false);
//		questionText.setInputType(EditorInfo.TYPE_NULL);
		shareOnFacebookTb = (ToggleButton) view
				.findViewById(R.id.shareOnFacebookToggleButton);

		OnClickListener listener = new OnClickListener() {
			public void onClick(View arg0) {
				onItemClick(arg0);

			}
		};

		image1.setOnClickListener(listener);
		image2.setOnClickListener(listener);
		buttonSubmit.setOnClickListener(listener);
	}

	@Override
	protected void onShow() {
		superController.setCurrentScreen(Screen.POST);
//		EditText questionText = (EditText) view
//				.findViewById(R.id.editText_question);
//		questionText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
		((RelativeLayout) getActivity().findViewById(R.id.layout_button_post))
				.setBackgroundDrawable(getActivity().getResources()
						.getDrawable(R.drawable.selected_button));
	}

	@Override
	protected void onHide() {
//		EditText questionText = (EditText) view
//				.findViewById(R.id.editText_question);
//		questionText.setInputType(EditorInfo.TYPE_NULL);
		((RelativeLayout) getActivity().findViewById(R.id.layout_button_post))
				.setBackgroundDrawable(getActivity().getResources()
						.getDrawable(R.drawable.unselected_button));
	}

	private void onItemClick(View arg0) {
		if (arg0.getId() == R.id.button_submit1) {
			if (shareOnFacebookTb.isChecked()) {
				Log.i(Constants.LOG_TAG, "Share on facebook is checked!");
				Session session = Session.getActiveSession();
				if (session.isOpened()) {
					Log.i(Constants.LOG_TAG, "session permissions: " + session.getPermissions().toString());
					if (!session.getPermissions().contains("publish_stream")) {
						
						Log.i(Constants.LOG_TAG, "requesting publish_stream permissions");

						List<String> write_permissions = new ArrayList<String>();
						write_permissions.add("publish_stream");

						ReauthorizeRequest openRequest = new ReauthorizeRequest(
								getActivity(), write_permissions);
						try {
							session.reauthorizeForPublish(openRequest);
						} catch (Exception ex) {
							Log.i(Constants.LOG_TAG,
									"EXCEPTION!!! : " + ex.toString());
						}
					}
				}
			}
			submitChoosiePost();
		} else {
			startDialog(arg0);
		}
	}

	private void startDialog(final View arg0) {
		final File tempFile = createImageFile(arg0.getId());

		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
				getActivity());
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

		if (arg0.getId() == R.id.image_photo1) {
			getActivity().startActivityForResult(intent,
					Constants.RequestCodes.TAKE_FIRST_PICTURE_FROM_CAMERA);
		}
		if (arg0.getId() == R.id.image_photo2) {
			intent.putExtra("return-data", true);
			getActivity().startActivityForResult(intent,
					Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_CAMERA);
		}
	}

	private void takeImageFromGallery(View arg0) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);

		if (arg0.getId() == R.id.image_photo1) {
			getActivity().startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
					Constants.RequestCodes.TAKE_FIRST_PICTURE_FROM_GALLERY);
		}

		if (arg0.getId() == R.id.image_photo2) {
			getActivity().startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
					Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_GALLERY);
		}
	}

	// when the camera or gallery return
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

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
		}

	}

	private Bitmap setImageFromData(Intent data, ImageView imageView) {
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
			imageView.setBackgroundDrawable(getActivity().getResources()
					.getDrawable(R.drawable.image_frame_post_filled));
		}

		InputMethodManager mgr = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(imageView, InputMethodManager.SHOW_IMPLICIT);
		return imageBitmapToReturn;
	}

	private void setAndStartCropIntent(int code, Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("outputX", 350);
		intent.putExtra("outputY", 350);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		getActivity().startActivityForResult(intent, code);
	}

	private void submitChoosiePost() {
		if ((mImage1 == null) || (mImage2 == null)) {
			Toast toast = Toast.makeText(getActivity(),
					"Please add two photos", Toast.LENGTH_SHORT);
			toast.show();
		} else {
			EditText questionText = (EditText) view
					.findViewById(R.id.editText_question);
			mQuestion = questionText.getText().toString();
			final ProgressBar progressBar = (ProgressBar) getActivity()
					.findViewById(R.id.progressBarPost);

			superController.getClient().sendChoosiePostToServer(
					new NewChoosiePostData(mImage1, mImage2, mQuestion,
							shareOnFacebookTb.isChecked()),
					new Callback<Void, Integer, Void>() {

						@Override
						public void onPre(Void param) {
							progressBar.setProgress(0);
							progressBar.setMax(100);
							progressBar.setVisibility(View.VISIBLE);
							progressBar.bringToFront();
						}

						@Override
						public void onProgress(Integer param) {
							progressBar.setProgress(param);
						}

						@Override
						public void onFinish(Void param) {
							progressBar.setVisibility(View.GONE);
							superController.screenToController.get(Screen.FEED)
									.refresh();
							resetPost();
						}
					});

			// switch back to feed screen
			superController.screenToController.get(Screen.FEED).showScreen();
			superController.screenToController.get(Screen.POST).hideScreen();
		}
	}

	private void resetPost() {
		image1.setImageResource(R.drawable.camera);
		image2.setImageResource(R.drawable.camera);
		image1.setBackgroundDrawable(getActivity().getResources().getDrawable(
				R.drawable.image_frame_post));
		image2.setBackgroundDrawable(getActivity().getResources().getDrawable(
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
		EditText questionText = (EditText) view
				.findViewById(R.id.editText_question);
		questionText.setText("");

	}

	private File createImageFile(Integer prefix) {
		File dir = getAlbumDir();
		if (dir.exists() == false) {
			dir.mkdir();
		}
		// Create an image file name
		String timeStamp = new SimpleDateFormat("_yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "image" + prefix.toString() + timeStamp + "_";
		File image = null;
		try {
			image = File.createTempFile(imageFileName, ".jpg", getAlbumDir());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	private File getAlbumDir() {
		File storageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				Constants.URIs.APPLICATION_NAME);
		return storageDir;
	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		getActivity().sendBroadcast(mediaScanIntent);
	}

	@Override
	public void onKeyDown(int keyCode, KeyEvent event) {
		superController.switchToScreen(Screen.FEED);
	}

	@Override
	public void onResume() {
		EditText questionText = (EditText) view
				.findViewById(R.id.editText_question);
		questionText.setFocusable(true);   
		questionText.setFocusableInTouchMode(true);
		questionText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
	}

}
