package com.choozie.app.models;

import com.choozie.app.Constants;
import com.choozie.app.ProfileActivity;

import android.app.Activity;
import android.content.Intent;

public class UserManger {

	private Activity activity;
	private User user;

	public UserManger(Activity activity, User user) {
		this.activity = activity;
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void goToProfile() {
		Intent intent = new Intent(activity.getApplication(),
				ProfileActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

		intent.putExtra(Constants.IntentsCodes.user, user);
		activity.startActivity(intent);
	}
}
