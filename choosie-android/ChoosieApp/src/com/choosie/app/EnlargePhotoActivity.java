package com.choosie.app;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class EnlargePhotoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enlarge_photo);
		Intent intent = getIntent();
		String image1Path = intent
				.getStringExtra(Constants.IntentsCodes.photo1Path);
		String image2Path = intent
				.getStringExtra(Constants.IntentsCodes.photo2Path);
		int startingImage = intent.getIntExtra(Constants.IntentsCodes.startingImageToEnlarge, 3);
		CustomPagerAdapter adapter = new CustomPagerAdapter(image1Path, image2Path);
		ViewPager myPager = (ViewPager) findViewById(R.id.enlargePhoto_viewPager);
		myPager.setAdapter(adapter);
		myPager.setCurrentItem(startingImage);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_enlarge_photo, menu);
		return true;
	}

}
