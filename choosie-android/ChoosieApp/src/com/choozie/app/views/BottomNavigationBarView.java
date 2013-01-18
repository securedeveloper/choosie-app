package com.choozie.app.views;

import com.choozie.app.AppSettings;
import com.choozie.app.ChoosieActivity;
import com.choozie.app.Constants;
import com.choozie.app.R;
import com.choozie.app.Screen;
import com.choozie.app.camera.CameraMainSuperControllerActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class BottomNavigationBarView extends LinearLayout {

	// private SuperController superController;
	private Activity activity;
	private Screen currentScreen;

	public BottomNavigationBarView(Context context, Activity activity,
			Screen currentScreen) {
		super(activity);
		this.activity = activity;
		this.currentScreen = currentScreen;
		// this.superController = superController;
		initialize();
	}

	private void initialize() {

		LinearLayout.LayoutParams lp = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		this.setLayoutParams(lp);

		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_bottom_nav_bar, this);

		((RelativeLayout) findViewById(R.id.view_navBar_layout_button_feed))
				.setOnClickListener(navigationListener);

		((ImageView) findViewById(R.id.view_navBar_layout_button_image_feed))
				.setOnClickListener(navigationListener);

		((RelativeLayout) findViewById(R.id.view_navBar_layout_button_post))
				.setOnClickListener(navigationListener);

		((ImageView) findViewById(R.id.view_navBar_layout_button_image_post))
				.setOnClickListener(navigationListener);

	}

	OnClickListener navigationListener = new OnClickListener() {

		public void onClick(View v) {
			onBottomNavBarButtonClick(v);

		}
	};

	public void onBottomNavBarButtonClick(View view) {
		switch (view.getId()) {
		case R.id.view_navBar_layout_button_feed:
		case R.id.view_navBar_layout_button_image_feed:
			switchToFeedScreen();
			break;
		case R.id.view_navBar_layout_button_post:
		case R.id.view_navBar_layout_button_image_post:
			if (AppSettings.isUseAdvancedCamera(activity)) {
				Intent intent = new Intent(activity.getApplicationContext(),
						CameraMainSuperControllerActivity.class);
				activity.startActivityForResult(intent,
						Constants.RequestCodes.NEW_POST);
			} else {
				switchToPostScreen();
			}
			break;
		}

	}

	private void switchToPostScreen() {
		if (currentScreen != Screen.FEED) {
			Intent intent = new Intent(activity.getApplicationContext(),
					ChoosieActivity.class);
			activity.startActivity(intent);
		}

	}

	private void switchToFeedScreen() {
		if (currentScreen != Screen.FEED) {
			Intent intent = new Intent(activity.getApplicationContext(),
					ChoosieActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(intent);
		}
	}

}
