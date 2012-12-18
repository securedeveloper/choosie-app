package com.choosie.app;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EnlargePhotoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enlarge_photo);

		Intent intent = getIntent();
		fillViewWithUserDetails(intent);
		String image1Path = intent
				.getStringExtra(Constants.IntentsCodes.photo1Path);
		String image2Path = intent
				.getStringExtra(Constants.IntentsCodes.photo2Path);
		int startingImage = intent.getIntExtra(
				Constants.IntentsCodes.startingImageToEnlarge, 3);
		CustomEnlargePagerAdapter adapter = new CustomEnlargePagerAdapter(
				image1Path, image2Path);
		ViewPager myPager = (ViewPager) findViewById(R.id.enlargePhoto_viewPager);
		myPager.setAdapter(adapter);
		myPager.setCurrentItem(startingImage);
	}

	private void fillViewWithUserDetails(Intent intent) {
		String userPhotoPath = intent
				.getStringExtra(Constants.IntentsCodes.userPhotoPath);
		String question = intent
				.getStringExtra(Constants.IntentsCodes.question);
		ImageView userPhotoImageView = (ImageView) findViewById(R.id.enlarge_activity_userPhoto);
		TextView questionTextView = (TextView) findViewById(R.id.enlarge_activity_question);
		if (userPhotoPath != null) {
//			userPhotoImageView.setImageURI(Uri
//					.fromFile(new File(userPhotoPath)));
			userPhotoImageView.setImageBitmap(BitmapFactory.decodeFile(userPhotoPath));
			questionTextView.setText(question);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_enlarge_photo, menu);
		return true;
	}

	public void backToFeed(View view) {
		setResult(RESULT_CANCELED, null);
		finish();
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

}
