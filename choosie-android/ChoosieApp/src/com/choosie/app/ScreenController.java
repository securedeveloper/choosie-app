package com.choosie.app;

import android.view.View;

public abstract class ScreenController {
	protected View view;
	
	public ScreenController(View layout) {
		this.view = layout;
	}

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
}
