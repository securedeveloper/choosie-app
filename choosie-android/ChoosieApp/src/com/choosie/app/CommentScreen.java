package com.choosie.app;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.choosie.app.R;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class CommentScreen extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(Constants.LOG_TAG, "in comment screen");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_screen);

		final ImageView imageViewPhoto1 = (ImageView) findViewById(R.id.photo1_comment_screen);
		final ImageView imageViewPhoto2 = (ImageView) findViewById(R.id.photo2_comment_screen);

		// get the images Strings from the intent
		final Intent i = getIntent();
		String photo1String = i.getStringExtra("photo1");
		String photo2String = i.getStringExtra("photo2");

		parseToUriAndSetInImageView(photo1String, imageViewPhoto1);
		parseToUriAndSetInImageView(photo2String, imageViewPhoto2);

		Button buttonSendComment = (Button) findViewById(R.id.button_send_comment);

		buttonSendComment.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				String text = ((EditText) findViewById(R.id.editText_comment))
						.getText().toString();

				imageViewPhoto1.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_action_search));
				imageViewPhoto2.setImageDrawable(getResources().getDrawable(
						R.drawable.ic_action_search));

				// get the intent and add the comment
				i.putExtra("text", text);

				// activate the 'onActivityResult'
				setResult(RESULT_OK, i);

				finish();
			}
		});
	}

	private void parseToUriAndSetInImageView(String photoString,
			ImageView imageViewPhoto) {
		Bitmap imageBitmap = null;
		if (photoString != null) {
			Uri photo1Uri = Uri.parse(photoString);
			try {
				imageBitmap = Media.getBitmap(getContentResolver(), photo1Uri);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			imageViewPhoto.setImageBitmap(imageBitmap);
		}

	}

	// TODO: check if can delete this function
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, null);
		finish();
		return;
	}

	// setting the backKey to cancel the commentizatzia
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			setResult(RESULT_CANCELED, null);
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_comment_screen, menu);
		return true;
	}
}
