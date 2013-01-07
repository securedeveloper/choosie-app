package com.choosie.app;

import com.choosie.app.R;
import com.choosie.app.caches.Caches;
import com.choosie.app.camera.CameraActivity;
import com.choosie.app.camera.CameraMainSuperControllerActivity;
import com.choosie.app.client.Client;
import com.choosie.app.controllers.SuperController;
import com.choosie.app.Models.ChoosiePostData;
import com.choosie.app.Models.FacebookDetails;
import com.choosie.app.Constants.Notifications;
import com.choosie.app.R.id;
import com.choosie.app.R.layout;
import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.nullwire.trace.ExceptionHandler;

import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class ChoosieActivity extends Activity {

	SuperController superController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.i("ChoosieActivity: onCreate()");

		Intent intent = getIntent();

		ExceptionHandler.register(this, Constants.URIs.CRASH_REPORT);
		setContentView(R.layout.activity_choosie);

		Utils.makeMainDirectory();

		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Inflate FEED xml and add it to layout_feed
		RelativeLayout layoutFeed = (RelativeLayout) findViewById(R.id.layout_feed);
		layoutFeed.addView(layoutInflater.inflate(R.layout.screen_feed, null));

		// Inflate FEED xml and add it to layout_post
		RelativeLayout layoutPost = (RelativeLayout) findViewById(R.id.layout_post);
		layoutPost.addView(layoutInflater.inflate(R.layout.screen_post, null));

		// Inflate FEED xml and add it to layout_post
		RelativeLayout layoutMe = (RelativeLayout) findViewById(R.id.layout_me);
		layoutMe.addView(layoutInflater.inflate(R.layout.screen_me, null));

		ImageButton refreshButton = (ImageButton) findViewById(R.id.refresh_button);
		refreshButton.setOnClickListener(refreshClickListener);

		ImageButton settingsButton = (ImageButton) findViewById(R.id.settings_button);
		settingsButton.setOnClickListener(settingsClickListener);

		PushNotification notification = (PushNotification) intent
				.getParcelableExtra("notification");

		Utils.setScreenWidth(this);

		superController = new SuperController(this);  

		if (notification != null) {
			handleNotification(notification);
		}
	}

	private void handleNotification(PushNotification notification) {
		Logger.i("Start HandleNotification()");

		int notificationType = Integer.parseInt(notification
				.getNotificationType());

		switch (notificationType) {
		case Notifications.NEW_POST_NOTIFICATION_TYPE:
			handleNewPostNotification(notification);
			break;
		case Notifications.NEW_COMMENT_NOTIFICATION_TYPE:
			handleCommentNotification(notification);
			break;
		case Notifications.NEW_VOTE_NOTIFICATION_TYPE:
			handleVoteNotification(notification);
			break;
		case Notifications.REGISTER_NOTIFICATION_TYPE:
			handleRegisterNotification(notification);
			break;
		}

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(Constants.Notifications.NOTIFICATION_ID);
	}

	private void handleRegisterNotification(PushNotification notification) {
		// TODO Auto-generated method stub
		Logger.i("handleRegisterNotification()");
		Client.getInstance().registerGCM(notification.getDeviceId());
	}

	private void handleNewPostNotification(PushNotification notification) {
		// TODO Auto-generated method stub
		Logger.i("HandleNewPostNotification()");

		// TODO Switch to the Post's screen

		// No need to do anything since it already goes by default
		// to Feed Screen and show latest post.
	}

	private void handleCommentNotification(PushNotification notification) {
		Logger.i("HandleCommentNotification()");
		// Invalidate the post in cache, so that next time it is asked for
		// we'll get the updated one with the new comment / vote.
		Caches.getInstance().getPostsCache()
				.invalidateKey(notification.getPostKey());
		superController.switchToCommentScreen(notification.getPostKey());
	}

	private void handleVoteNotification(PushNotification notification) {
		// Invalidate the post in cache, so that next time it is asked for
		// we'll get the updated one with the new comment / vote.
		Caches.getInstance().getPostsCache()
				.invalidateKey(notification.getPostKey());
		Logger.i("HandleVoteNotification()");

		superController.switchToCommentScreenAndOpenVotes(notification
				.getPostKey());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_choosie, menu);
		return true;
	}

	public void onBottomNavBarButtonClick(View view) {
		switch (view.getId()) {
		case R.id.layout_button_feed:
		case R.id.layout_button_image_feed:
			superController.switchToScreen(Screen.FEED);
			break;
		case R.id.layout_button_post:
		case R.id.layout_button_image_post:
			if (AppSettings.useChoozieCamera() == true) {
				Intent intent = new Intent(this.getApplicationContext(),
						CameraMainSuperControllerActivity.class);
				startActivity(intent);
			} else {
				superController.switchToScreen(Screen.POST);
			}
			break;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Session.getActiveSession() != null
				&& Session.getActiveSession().getPermissions() != null) {
			Log.i(Constants.LOG_TAG, "onActivityResult. FB Permissions: "
					+ Session.getActiveSession().getPermissions().toString());
		}

		switch (requestCode) {

		case Constants.RequestCodes.TAKE_FIRST_PICTURE_FROM_CAMERA:
		case Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_CAMERA:
		case Constants.RequestCodes.TAKE_FIRST_PICTURE_FROM_GALLERY:
		case Constants.RequestCodes.TAKE_SECOND_PICTURE_FROM_GALLERY:
		case Constants.RequestCodes.CROP_FIRST:
		case Constants.RequestCodes.CROP_SECOND:
			superController.getControllerForScreen(Screen.POST)
					.onActivityResult(requestCode, resultCode, data);
			break;

		case Constants.RequestCodes.COMMENT:
			superController.onActivityResult(resultCode, data);
			break;

		case Constants.RequestCodes.FB_REQUEST_PUBLISH_PERMISSION:
			Log.i(Constants.LOG_TAG, "after activity fb");
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
			// NOTE: Special handling: In case user presses on 'Share on
			// Facebook'
			// and then, after we launch askForPublicPermissions(), the user
			// cancels, we identify this situation and set it back to 'false'.
			// This happens by:
			// ChoosieActivity gets onActivityResult with
			// requestCode == FB_REQUEST_PUBLISH_PERMISSION, it calls this
			// refresh
			// method.
			superController.getControllerForScreen(Screen.POST).refresh();
		}
	}

	@Override
	protected void onResume() {
		Logger.i("ChoosieActivity: onResume()");
		super.onResume();
		switch (superController.getCurrentScreen()) {
		case POST:
			superController.getControllerForScreen(Screen.POST).onResume();
			break;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			switch (superController.getCurrentScreen()) {
			case POST:
				superController.getControllerForScreen(Screen.POST).onKeyDown(
						keyCode, event);
				break;
			case FEED:
				Logger.i("onKeyDown() - calling finish() to choosieActivity");
				finish();
				break;
			}
		}

		return true;
	}

	private OnClickListener refreshClickListener = new OnClickListener() {
		public void onClick(View v) {
			Logger.i("Clicked refresh feed button");
			superController.getControllerForScreen(Screen.FEED).refresh();
		}
	};

	private OnClickListener settingsClickListener = new OnClickListener() {

		public void onClick(View v) {
			Logger.i("Clicked settings button");
			AppSettingsWindow settingsWindow = new AppSettingsWindow(
					superController.getActivity());
			settingsWindow.show();
		}
	};

	@Override
	protected void onDestroy() {
		Logger.i("ChoosieActivity: onDestroy()");
		super.onDestroy();
		this.finish();
	}

	@Override
	protected void onStart() {
		Logger.i("ChoosieActivity: onStart()");
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onPause() {
		Logger.i("ChoosieActivity: onPause()");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Logger.i("ChoosieActivity: onStop()");
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onRestart() {
		Logger.i("ChoosieActivity: onRestart()");
		super.onRestart();
	}

}
