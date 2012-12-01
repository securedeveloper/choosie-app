package com.choosie.app.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class PostScreenController extends ScreenController {
	private Bitmap mImage1;
	private Bitmap mImage2;
	private String mQuestion;
	private Uri outputFileUri;
	private ImageView image1;
	private ImageView image2;
	private EditText questionText;

	public PostScreenController(View layout, SuperController superController) {
		super(layout, superController);
	}

	@Override
	protected void onCreate() {
		image1 = (ImageView) view.findViewById(R.id.image_photo1);
		image2 = (ImageView) view.findViewById(R.id.image_photo2);
		Button buttonSubmit = (Button) view.findViewById(R.id.button_submit);
		questionText = (EditText) view.findViewById(R.id.editText_question);
		questionText.setFocusable(false);

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
		questionText.setFocusableInTouchMode(true);
		questionText.setFocusable(true);
		((RelativeLayout) getActivity().findViewById(R.id.layout_button_post))
				.setBackgroundDrawable(getActivity().getResources()
						.getDrawable(R.drawable.selected_button));
	}

	@Override
	protected void onHide() {
		((RelativeLayout) getActivity().findViewById(R.id.layout_button_post))
				.setBackgroundDrawable(getActivity().getResources()
						.getDrawable(R.drawable.unselected_button));
	}

	private void onItemClick(View arg0) {
		if (arg0.getId() == R.id.button_submit) {
			submitChoosiePost();
		} else {
			startDialog(arg0);
		}
	}

	private void startDialog(final View arg0) {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
				getActivity());
		myAlertDialog.setTitle("Upload Pictures Option");
		myAlertDialog.setMessage("How do you want to set your picture?");

		myAlertDialog.setPositiveButton("Camera",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg1, int arg3) {
						TakePhoto(arg0);
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

	private void TakePhoto(View arg0) {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File f = new File(Environment.getExternalStorageDirectory(),
				"photo.jpg");
		outputFileUri = Uri.fromFile(f);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		outputFileUri = Uri.fromFile(f);
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
			setAndStartCropIntent(Constants.RequestCodes.CROP_FIRST);
			break;

		case Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_CAMERA:
			setAndStartCropIntent(Constants.RequestCodes.CROP_SECOND);
			break;

		case Constants.RequestCodes.TAKE_FIRST_PICTURE_FROM_GALLERY:
			outputFileUri = data.getData();
			setAndStartCropIntent(Constants.RequestCodes.CROP_FIRST);
			break;

		case Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_GALLERY:
			outputFileUri = data.getData();
			setAndStartCropIntent(Constants.RequestCodes.CROP_SECOND);
			break;
		}

	}

	private Bitmap setImageFromData(Intent data, ImageView imageView) {
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

		// Wysie_Soh: Delete the temporary file
		File f = new File(outputFileUri.getPath());
		if (f.exists()) {
			f.delete();
		}

		InputMethodManager mgr = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(imageView, InputMethodManager.SHOW_IMPLICIT);
		return imageBitmapToReturn;
	}

	private void setAndStartCropIntent(int code) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		// intent.setClassName("com.android.camera",
		// "com.android.camera.CropImage");
		/*
		 * Bitmap im= null; try { im =
		 * Media.getBitmap(getActivity().getContentResolver(), outputFileUri); }
		 * catch (FileNotFoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */

		intent.setDataAndType(outputFileUri, "image/*");
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200 * 6 / 5);
		intent.putExtra("aspectX", 5);
		intent.putExtra("aspectY", 6);
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
			mQuestion = questionText.getText().toString();
			final ProgressBar progressBar = (ProgressBar) getActivity()
					.findViewById(R.id.progressBarPost);

			superController.getClient().sendChoosiePostToServer(
					new NewChoosiePostData(mImage1, mImage2, mQuestion),
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
		image1.setImageResource(android.R.drawable.ic_menu_crop);
		image2.setImageResource(android.R.drawable.ic_menu_crop);
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

		questionText.setText("");

	}
}
