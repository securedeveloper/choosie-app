package com.choosie.app.controllers;

import java.io.File;
import java.io.IOException;

import com.choosie.app.Callback;
import com.choosie.app.Constants;
import com.choosie.app.NewChoosiePostData;
import com.choosie.app.R;
import com.choosie.app.Screen;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

	}

	@Override
	protected void onHide() {
		// TODO Auto-generated method stub

	}

	private void TakePhoto(View arg0) {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File f = new File(Environment.getExternalStorageDirectory(),
				"photo.jpg");

		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		outputFileUri = Uri.fromFile(f);
		if (arg0.getId() == R.id.image_photo1) {
			getActivity().startActivityForResult(intent,
					Constants.RequestCodes.TAKE_FIRST_PICTURE);
		}
		if (arg0.getId() == R.id.image_photo2) {
			getActivity().startActivityForResult(intent,
					Constants.RequestCodes.TAKE_SECOND_PICTURE);
		}
	}

	private void onItemClick(View arg0) {
		if (arg0.getId() == R.id.button_submit) {
			submitChoosiePost();
		} else {
			TakePhoto(arg0);
		}
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

			// clear images and text

			// switch back to feed screen
			superController.screenToController.get(Screen.FEED).showScreen();
			superController.screenToController.get(Screen.POST).hideScreen();
		}
	}

	private void resetPost() {
		image1.setImageResource(android.R.drawable.ic_menu_crop);
		image2.setImageResource(android.R.drawable.ic_menu_crop);
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

	// when the camera returns
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bitmap photoTemp = null;
		Bitmap resizedPhoto = null;

		if (resultCode == Activity.RESULT_OK) {
			getActivity().getContentResolver()
					.notifyChange(outputFileUri, null);
			ContentResolver cr = getActivity().getContentResolver();
			try {
				photoTemp = android.provider.MediaStore.Images.Media.getBitmap(
						cr, outputFileUri);
				int w = photoTemp.getWidth();
				int h = photoTemp.getHeight();
				resizedPhoto = Bitmap.createScaledBitmap(photoTemp, w / 10,
						h / 10, true);
				photoTemp.recycle();
				photoTemp = null;
			} catch (Exception e) {
				Toast.makeText(getActivity(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}

			if (requestCode == Constants.RequestCodes.TAKE_FIRST_PICTURE) {
				mImage1 = null;
				mImage1 = rotateBitmap(resizedPhoto, outputFileUri);
				resizedPhoto = rotateAndSetImage(image1, mImage1, resizedPhoto,
						R.id.image_photo1);
			}
			if (requestCode == Constants.RequestCodes.TAKE_SECOND_PICTURE) {
				mImage2 = null;
				mImage2 = rotateBitmap(resizedPhoto, outputFileUri);
				resizedPhoto = rotateAndSetImage(image2, mImage2, resizedPhoto,
						R.id.image_photo2);

			}
		}
	}

	private Bitmap rotateAndSetImage(ImageView imageView, Bitmap image,
			Bitmap resizedPhoto, int id) {
		imageView.setImageResource(android.R.drawable.ic_menu_crop);
		((ImageView) view.findViewById(id)).setImageBitmap(image);
		resizedPhoto.recycle();
		resizedPhoto = null;
		return resizedPhoto;
	}

	// for later use
	private Bitmap rotateBitmap(Bitmap sour, Uri uriOutputFile) {

		Bitmap source = sour;

		ExifInterface exif;
		int orientation = 0;
		try {
			exif = new ExifInterface(uriOutputFile.getPath());
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
		} catch (IOException e1) {
			Toast.makeText(getActivity(), e1.getMessage(), Toast.LENGTH_SHORT)
					.show();
			e1.printStackTrace();
		} // Since API Level 5

		int rotate = 0;
		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotate += 90;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotate += 90;
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotate += 90;
		}

		/*
		 * Display d = activity.getWindowManager().getDefaultDisplay(); int x =
		 * source.getWidth(); int y = source.getHeight(); Bitmap scaledBitmap =
		 * Bitmap.createScaledBitmap(source, x, y, true);
		 * 
		 * // create a matrix object Matrix matrix = new Matrix();
		 * matrix.postRotate(rotate); // anti-clockwise by 90 degrees
		 * 
		 * // create a new bitmap from the original using the matrix to
		 * transform // the result Bitmap rotatedBitmap = Bitmap
		 * .createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(),
		 * scaledBitmap.getHeight(), matrix, true);
		 * 
		 * //return rotatedBitmap;
		 * 
		 * return source;
		 */

		int width = source.getWidth();

		int height = source.getHeight();

		Matrix matrix = new Matrix();

		// matrix.postScale(scaleWidth, scaleHeight);
		matrix.postRotate(rotate);

		Bitmap resizedBitmap = Bitmap.createBitmap(source, 0, 0, width, height,
				matrix, true);

		return resizedBitmap;

	}

}
