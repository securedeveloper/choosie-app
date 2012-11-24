package com.choosie.app;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public abstract class ScreenController {
	protected View view;
	Activity activity;
	SuperController superController;

	public ScreenController(View layout, Activity activity,
			SuperController superController) {
		this.view = layout;
		this.activity = activity;
		this.superController = superController;
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

	public void refresh() {

	}
}
