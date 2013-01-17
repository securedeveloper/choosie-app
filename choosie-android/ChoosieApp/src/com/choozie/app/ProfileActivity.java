package com.choozie.app;

import com.choozie.app.caches.CacheCallback;
import com.choozie.app.caches.Caches;
import com.choozie.app.client.Client;
import com.choozie.app.models.FacebookDetails;
import com.facebook.android.FbDialog;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.TextView;

public class ProfileActivity extends Activity {

	private TextView tvFullName;
	private ImageButton ibUserPicture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		Intent intent = getIntent();
		
		tvFullName = (TextView)findViewById(R.id.profile_user_name);
		ibUserPicture = (ImageButton)findViewById(R.id.profile_user_picture);
		
		FacebookDetails fb_details = intent.getParcelableExtra("fb_details");
		tvFullName.setText(fb_details.getFirstName() + " " + fb_details.getLastName());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_profile, menu);
		return true;
	}

}
