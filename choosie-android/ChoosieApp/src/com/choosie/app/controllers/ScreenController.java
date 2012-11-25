package com.choosie.app.controllers;


import android.app.Activity;
import android.content.Intent;
import android.view.View;

public abstract class ScreenController {
	protected View view;
	private Activity activity;
	SuperController superController;

	public ScreenController(View layout, Activity activity,
			SuperController superController) {
		this.view = layout;
		this.setActivity(activity);
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

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
