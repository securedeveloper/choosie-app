package com.choosie.app;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;

public class ChoosieActivity extends Activity {

	Map<Screen, View> screenToView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choosie);

		screenToView = new HashMap<Screen, View>();
		screenToView.put(Screen.FEED, findViewById(R.id.layout_feed));
		screenToView.put(Screen.POST, findViewById(R.id.layout_post));
		screenToView.put(Screen.ME, findViewById(R.id.layout_me));

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_choosie, menu);
		return true;
	}

	public void switchToScreen(Screen screenToShow) {
		// Hide all screens except 'screen'
		for (Screen screen : screenToView.keySet()) {
			screenToView.get(screen).setVisibility(
					screen == screenToShow ? View.VISIBLE : View.GONE);
		}
	}
}
