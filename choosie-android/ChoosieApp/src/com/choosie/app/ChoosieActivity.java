package com.choosie.app;

import com.choosie.app.controllers.SuperController;
import com.choosie.app.Models.FacebookDetails;
import com.facebook.Session;
import com.google.android.gcm.GCMRegistrar;
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
	private final String SENDER_ID = "101212394485";
	SuperController superController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		FacebookDetails fbDetails = (FacebookDetails) getIntent()
				.getSerializableExtra("fb_details");

		superController = new SuperController(this, fbDetails);
		
		Utils.setScreenWidth(this);
		
		//temporary until superController will be a single instance
		GCMIntentService.setSuperController(superController);

		ImageButton refreshButton = (ImageButton) findViewById(R.id.refresh_button);
		refreshButton.setOnClickListener(refreshClickListener);

		handleGCMRegister();
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(123);
	}

	private void handleGCMRegister() {
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		GCMRegistrar.unregister(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
			Log.v("GCM", "succeeded registering!!!");
		} else {
			Log.v("GCM", "Already registered");
		}
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
			superController.switchToScreen(Screen.POST);
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

}
