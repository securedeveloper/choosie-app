package com.choozie.app.views;

import java.util.ArrayList;
import java.util.List;

import com.choozie.app.AppSettings;
import com.choozie.app.ChoosieActivity;
import com.choozie.app.Constants;
import com.choozie.app.PostActivity;
import com.choozie.app.ProfileActivity;
import com.choozie.app.R;
import com.choozie.app.Screen;
import com.choozie.app.camera.CameraMainSuperControllerActivity;
import com.choozie.app.client.Client;
import com.choozie.app.models.User;
import com.choozie.app.models.UserManger;

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

	private List<RelativeLayout> navBarRelativeLayouts;
	private List<ImageView> navBarImageViews;

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

		navBarRelativeLayouts = new ArrayList<RelativeLayout>();
		navBarImageViews = new ArrayList<ImageView>();

		navBarRelativeLayouts
				.add((RelativeLayout) findViewById(R.id.view_navBar_layout_button_feed));
		navBarRelativeLayouts
				.add((RelativeLayout) findViewById(R.id.view_navBar_layout_button_post));
		navBarRelativeLayouts
				.add((RelativeLayout) findViewById(R.id.view_navBar_layout_button_profile));

		navBarImageViews
				.add((ImageView) findViewById(R.id.view_navBar_layout_button_image_feed));
		navBarImageViews
				.add((ImageView) findViewById(R.id.view_navBar_layout_button_image_post));
		navBarImageViews
				.add((ImageView) findViewById(R.id.view_navBar_layout_button_image_profile));

		setNavigationListeners();
	}

	private void setNavigationListeners() {
		for (RelativeLayout rl : navBarRelativeLayouts) {
			rl.setOnClickListener(navigationListener);
		}
		for (ImageView iv : navBarImageViews) {
			iv.setOnClickListener(navigationListener);
		}
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
		case R.id.view_navBar_layout_button_profile:
		case R.id.view_navBar_layout_button_image_profile:
			switchToProfileScreen();
		}

	}

	private void switchToProfileScreen() {
		if (currentScreen != Screen.USER_PROFILE) {
			UserManger um = new UserManger(activity, Client.getInstance()
					.getActiveUser());
			um.goToProfile();
		}
	}

	private void switchToPostScreen() {
		if (currentScreen != Screen.POST) {
			// changeSelectedButton((RelativeLayout)findViewById(R.id.view_navBar_layout_button_post));

			Intent intent = new Intent(activity.getApplicationContext(),
					PostActivity.class);
			activity.startActivityForResult(intent,
					Constants.RequestCodes.NEW_POST);
		}
	}

	private void switchToFeedScreen() {
		if (currentScreen != Screen.FEED) {
			// changeSelectedButton((RelativeLayout)findViewById(R.id.view_navBar_layout_button_feed));
			Intent intent = new Intent(activity.getApplicationContext(),
					ChoosieActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(intent);
		}
	}

	public void changeSelectedButton(RelativeLayout relativeLayout) {
		for (RelativeLayout rl : navBarRelativeLayouts) {
			if (rl.equals(relativeLayout))
				rl.setBackgroundResource(R.drawable.selected_button);
			else
				rl.setBackgroundResource(R.drawable.unselected_button);
		}
	}

}
