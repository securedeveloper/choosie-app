package com.choosie.app;

import java.io.File;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
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

	public PostScreenController(View layout, Activity activity) {
		super(layout, activity);
	}

	@Override
	protected void onCreate() {
		ImageView image1 = (ImageView) view.findViewById(R.id.image_photo1);
		ImageView image2 = (ImageView) view.findViewById(R.id.image_photo2);
		Button buttonSubmit = (Button) view.findViewById(R.id.button_submit);
		OnClickListener listener = new OnClickListener() {
			public void onClick(View arg0) {
				onImageClick(arg0);
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

	private void onImageClick(View arg0) {
		if (arg0.getId() == R.id.button_submit) {
			EditText questionText = (EditText) view
					.findViewById(R.id.editText_question);
			mQuestion = questionText.getText().toString();
			client.sendChoosiePostToServer(new NewChoosiePostData(mImage1, mImage2, mQuestion));
		} else {
			TakePhoto(arg0);
		}
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
