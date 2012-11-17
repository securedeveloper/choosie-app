package com.choosie.app;



import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;

public class ChoosieActivity extends Activity {

	SuperController superController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choosie);

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

		superController = new SuperController(this);
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_choosie, menu);
		return true;
	}

	public void onBottomNavBarButtonClick(View view) {
		switch (view.getId()) {
		case R.id.button_feed:
			superController.switchToScreen(Screen.FEED);
			break;
		case R.id.button_post:
			superController.switchToScreen(Screen.POST);
			break;
		case R.id.button_me:
			superController.switchToScreen(Screen.ME);
			break;
		}
	}

	// think to change it!
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		superController.screenToController.get(Screen.POST).onActivityResult(
				requestCode, resultCode, data);
	}

}
