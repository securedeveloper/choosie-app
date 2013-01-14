package com.choozie.app;

import com.choozie.app.R;
import com.nullwire.trace.ExceptionHandler;

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
		ExceptionHandler.register(this, Constants.URIs.CRASH_REPORT);

		//get the intent
		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		IntentData intentData = data.getParcelable(Constants.IntentsCodes.intentData);
		
		//set the constant views at this activity
		fillViewWithUserDetails(intentData);
		
		//get from the intent the data to pass to the pager adapter
		String image1Path = intentData.getphoto1Path();
				
		String image2Path = intentData.getphoto2Path();
				
		int votes1 = intentData.getVotes1();
		int votes2 = intentData.getVotes2();	
		boolean isAlreadyVoted = intentData.checkIfVotedAlready();

//		EnlargeDetails details = new EnlargeDetails(image1Path, image2Path,
//				votes1, votes2, isAlreadyVoted);

		CustomEnlargePagerAdapter adapter = new CustomEnlargePagerAdapter(
				intentData, getWindowManager().getDefaultDisplay());
		ViewPager myPager = (ViewPager) findViewById(R.id.enlargePhoto_viewPager);
		myPager.setAdapter(adapter);
		
		//start the view pager by the current photo
		int startingImage = intentData.getStartingImage();
		myPager.setCurrentItem(startingImage);
	}

	private void fillViewWithUserDetails(IntentData intentData) {
		
		String userPhotoPath = intentData.getUserPhotoPath();				
		String question = intentData.getQuestion();				
		String userName = intentData.getUserName();
				
		ImageView userPhotoImageView = (ImageView) findViewById(R.id.enlarge_activity_userPhoto);
		TextView userNameTextView = (TextView) findViewById(R.id.enlarge_activity_user_name);
		TextView questionTextView = (TextView) findViewById(R.id.enlarge_activity_question);
		if (userPhotoPath != null) {
			// userPhotoImageView.setImageURI(Uri
			// .fromFile(new File(userPhotoPath)));
			userPhotoImageView.setImageBitmap(BitmapFactory
					.decodeFile(userPhotoPath));
			questionTextView.setText(question);
		}
		userNameTextView.setText(userName);
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
