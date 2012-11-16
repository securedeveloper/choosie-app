package com.choosie.app;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ChoosieActivity extends Activity {

	Map<Screen, ScreenController> screenToController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choosie);

		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(this.LAYOUT_INFLATER_SERVICE);

		// Inflate FEED xml and add it to layout_feed
		RelativeLayout layoutFeed = (RelativeLayout) findViewById(R.id.layout_feed);
		layoutFeed.addView(layoutInflater.inflate(R.layout.screen_feed, null));

		// Inflate FEED xml and add it to layout_post
		RelativeLayout layoutPost = (RelativeLayout) findViewById(R.id.layout_post);
		layoutPost.addView(layoutInflater.inflate(R.layout.screen_post, null));

		// Inflate FEED xml and add it to layout_post
		RelativeLayout layoutMe = (RelativeLayout) findViewById(R.id.layout_me);
		layoutMe.addView(layoutInflater.inflate(R.layout.screen_me, null));
		
		screenToController = new HashMap<Screen, ScreenController>();
		screenToController.put(Screen.FEED, new FeedScreenController(
				findViewById(R.id.layout_feed), this));
		screenToController.put(Screen.POST, new PostScreenController(
				findViewById(R.id.layout_post), this));
		screenToController.put(Screen.ME, new MeScreenController(
				findViewById(R.id.layout_me), this));

		for (ScreenController screen : screenToController.values()) {
			screen.onCreate();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_choosie, menu);
		return true;
	}

	public void switchToScreen(Screen screenToShow) {
		// Hide all screens except 'screen'
		for (Screen screen : screenToController.keySet()) {
			if (screen == screenToShow) {
				screenToController.get(screen).showScreen();
			} else {
				screenToController.get(screen).hideScreen();
			}
		}
	}
	
	public void onBottomNavBarButtonClick(View view) {
		switch (view.getId()) {
		case R.id.button_feed:
			switchToScreen(Screen.FEED);
			break;
		case R.id.button_post:
			switchToScreen(Screen.POST);
			break;
		case R.id.button_me:
			switchToScreen(Screen.ME);
			break;
		}
	}
	
	
	//think to change it!	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		screenToController.get(Screen.POST).onActivityResult(requestCode, resultCode, data);
	}

}
