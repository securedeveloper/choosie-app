package com.choozie.app;

import com.choozie.app.R;
import com.choozie.app.Constants.Notifications;
import com.choozie.app.R.id;
import com.choozie.app.R.layout;
import com.choozie.app.caches.Caches;
import com.choozie.app.camera.CameraActivity;
import com.choozie.app.camera.CameraMainSuperControllerActivity;
import com.choozie.app.client.Client;
import com.choozie.app.controllers.SuperController;
import com.choozie.app.models.ChoosiePostData;
import com.choozie.app.models.FacebookDetails;
import com.choozie.app.views.BottomNavigationBarView;
import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.nullwire.trace.ExceptionHandler;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ChoosieActivity extends Activity {

	SuperController superController;
	private RelativeLayout dummyContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		L.i("ChoosieActivity: onCreate()");
		Client.getInstance().setContext(getApplicationContext());

		Intent intent = getIntent();

		ExceptionHandler.register(this, Constants.URIs.CRASH_REPORT);
		setContentView(R.layout.activity_choosie);

		Utils.makeMainDirectory();

		LinearLayout bottomView = (LinearLayout) findViewById(R.id.bottom_nav_bar);

		View customView = new BottomNavigationBarView(this, this, Screen.FEED);
		bottomView.addView(customView);

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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			showSettingsButtonIfNoHardwareKey();
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void showSettingsButtonIfNoHardwareKey() {
		if (!ViewConfiguration.get(getApplicationContext())
				.hasPermanentMenuKey()) {
			View settingsButton = findViewById(R.id.settings_button);
			settingsButton.setVisibility(View.VISIBLE);
			settingsButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					openSettingsActivity();
				}
			});
		}
	}

	private void handleNotification(PushNotification notification) {
		L.i("Start HandleNotification()");
		L.i("ChoosieActivity: GOT NOTIFICATION. NotificationType = "
				+ notification.getNotificationType() + ", text = "
				+ notification.getText() + ", postkey = "
				+ notification.getPostKey() + ", deviceID = "
				+ notification.getDeviceId());

		int notificationType = Integer.parseInt(notification
				.getNotificationType());
		Tracker tracker = GoogleAnalytics.getInstance(this).getDefaultTracker();

		switch (notificationType) {
		case Notifications.NEW_POST_NOTIFICATION_TYPE:
			tracker.trackEvent("Push Notification", "New Post", "", null);
			handleNewPostNotification(notification);
			break;
		case Notifications.NEW_COMMENT_NOTIFICATION_TYPE:
			tracker.trackEvent("Push Notification", "Comment", "", null);
			handleCommentNotification(notification);
			break;
		case Notifications.NEW_VOTE_NOTIFICATION_TYPE:
			tracker.trackEvent("Push Notification", "Vote", "", null);
			handleVoteNotification(notification);
			break;
		case Notifications.REGISTER_NOTIFICATION_TYPE:
			tracker.trackEvent("Push Notification", "Register", "", null);
			handleRegisterNotification(notification);
			break;
		}

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(Constants.Notifications.NOTIFICATION_ID);
	}

	private void handleRegisterNotification(PushNotification notification) {
		// TODO Auto-generated method stub
		L.i("handleRegisterNotification()");
		Client.getInstance().registerGCM(notification.getDeviceId());
	}

	private void handleNewPostNotification(PushNotification notification) {
		// TODO Auto-generated method stub
		L.i("HandleNewPostNotification()");

		// TODO Switch to the Post's screen

		// No need to do anything since it already goes by default
		// to Feed Screen and show latest post.
	}

	private void handleCommentNotification(PushNotification notification) {
		L.i("HandleCommentNotification()");
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
		L.i("HandleVoteNotification()");

		superController.switchToCommentScreenAndOpenVotes(notification
				.getPostKey());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_choosie, menu);
		return true;
	}

	// public void onBottomNavBarButtonClick(View view) {
	//
	// switch (view.getId()) {
	// case R.id.oren_layout_button_feed:
	// case R.id.oren_layout_button_image_feed:
	// superController.switchToScreen(Screen.FEED);
	// break;
	// case R.id.layout_button_post:
	// case R.id.layout_button_image_post:
	// if (AppSettings.isUseAdvancedCamera(this)) {
	// Intent intent = new Intent(this.getApplicationContext(),
	// CameraMainSuperControllerActivity.class);
	// startActivityForResult(intent, Constants.RequestCodes.NEW_POST);
	// } else {
	// superController.switchToScreen(Screen.POST);
	// }
	// break;
	// }
	// }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Session.getActiveSession() != null
				&& Session.getActiveSession().getPermissions() != null) {
			L.i("onActivityResult. FB Permissions: "
					+ Session.getActiveSession().getPermissions().toString());
		}

		L.i("ChoosieActivity : onActivityResult returned requestCode = "
				+ requestCode);

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
			L.i("after activity fb");
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

		case Constants.RequestCodes.NEW_POST:
			superController.getControllerForScreen(Screen.FEED).refresh();
		}
	}

	@Override
	protected void onResume() {
		L.i("ChoosieActivity: onResume()");
		super.onResume();
		// switch (superController.getCurrentScreen()) {
		// case POST:
		// superController.getControllerForScreen(Screen.POST).onResume();
		// break;
		// }
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			switch (superController.getCurrentScreen()) {
			case POST:
				superController.getControllerForScreen(Screen.POST).onKeyDown(
						keyCode, event);
				break;
			case FEED:
				L.i("onKeyDown() - calling finish() to choosieActivity");
				finish();
				break;
			}
		}

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			openSettingsActivity();
		}

		return true;
	}

	private void openSettingsActivity() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	private OnClickListener refreshClickListener = new OnClickListener() {
		public void onClick(View v) {
			L.i("Clicked refresh feed button");
			superController.getControllerForScreen(Screen.FEED).refresh();
		}
	};

	private OnClickListener settingsClickListener = new OnClickListener() {

		public void onClick(View v) {
			L.i("Clicked settings button");
			// AppSettingsWindow settingsWindow = new AppSettingsWindow(
			// superController.getActivity());
			// settingsWindow.show();
		}
	};

	@Override
	protected void onDestroy() {
		L.i("ChoosieActivity: onDestroy()");
		super.onDestroy();
		this.finish();
	}

	@Override
	protected void onStart() {
		L.i("ChoosieActivity: onStart()");
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onPause() {
		L.i("ChoosieActivity: onPause()");
		super.onPause();
	}

	@Override
	protected void onStop() {
		L.i("ChoosieActivity: onStop()");
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onRestart() {
		L.i("ChoosieActivity: onRestart()");
		super.onRestart();
	}

}
