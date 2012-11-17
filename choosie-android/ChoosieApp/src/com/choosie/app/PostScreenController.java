package com.choosie.app;

import java.io.File;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class PostScreenController extends ScreenController {
	private Bitmap mPhotoTemp;
	private Bitmap mImage1;
	private Bitmap mImage2;
	private String mQuestion;
	private Uri outputFileUri;
	private final int TAKE_FIRST_PICTURE = 1;
	private final int TAKE_SECOND_PICTURE = 2;
	private ImageView image1;
	private ImageView image2;
	private EditText questionText;

	public PostScreenController(View layout, Activity activity,
			SuperController superController) {
		super(layout, activity, superController);
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
			activity.startActivityForResult(intent, TAKE_FIRST_PICTURE);
		}
		if (arg0.getId() == R.id.image_photo2) {
			activity.startActivityForResult(intent, TAKE_SECOND_PICTURE);
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
			Toast toast = Toast.makeText(activity, "Please add two photos",
					Toast.LENGTH_SHORT);
			toast.show();
		} else {
			mQuestion = questionText.getText().toString();

			superController.getClient().sendChoosiePostToServer(
					new NewChoosiePostData(mImage1, mImage2, mQuestion),
					new Callback<Void, Void>() {

						@Override
						void onOperationFinished(Void param) {
							Toast toast = Toast.makeText(
									superController.screenToController
											.get(Screen.FEED).activity,
									"Loaded!!", Toast.LENGTH_SHORT);
							toast.show();
						}

						@Override
						public void onProgress(Void Param) {
							Toast toast = Toast.makeText(
									superController.screenToController
											.get(Screen.FEED).activity,
									"Loading!!", Toast.LENGTH_SHORT);
							toast.show();
						}

					});

			// clear images and text
			resetPost();

			// switch back to feed screen
			superController.screenToController.get(Screen.FEED).showScreen();
			superController.screenToController.get(Screen.POST).hideScreen();
		}
	}

	private void resetPost() {
		mImage1 = null;
		mImage2 = null;
		mQuestion = null;
		image1.setImageResource(android.R.drawable.ic_menu_crop);
		image2.setImageResource(android.R.drawable.ic_menu_crop);
		questionText.setText("");

	}

	// when the camera returns
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			activity.getContentResolver().notifyChange(outputFileUri, null);
			ContentResolver cr = activity.getContentResolver();
			try {
				mPhotoTemp = android.provider.MediaStore.Images.Media
						.getBitmap(cr, outputFileUri);
			} catch (Exception e) {
				// Toast.makeText(this, e.getMessage(),
				// Toast.LENGTH_SHORT).show();
			}

			if (requestCode == TAKE_FIRST_PICTURE) {
				((ImageView) view.findViewById(R.id.image_photo1))
						.setImageBitmap(mPhotoTemp);
				mImage1 = mPhotoTemp;
			}
			if (requestCode == TAKE_SECOND_PICTURE) {
				((ImageView) view.findViewById(R.id.image_photo2))
						.setImageBitmap(mPhotoTemp);
				mImage2 = mPhotoTemp;
			}
		}
	}

}
