package com.choosie.app;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public abstract class ScreenController {
	protected ChoosieClient client = new ChoosieClient();
	protected View view;
	Activity activity;

	public ScreenController(View layout, Activity activity) {
		this.view = layout;
		this.activity = activity;
	}
	
	protected abstract void onCreate();

	protected abstract void onShow();

	protected abstract void onHide();

	protected void hideScreen() {
		view.setVisibility(View.GONE);
		onHide();
	}

	protected void showScreen() {
		view.setVisibility(View.VISIBLE);
		onShow();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
}
