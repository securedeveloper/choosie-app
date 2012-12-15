package com.choosie.app.controllers;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

public abstract class ScreenController {
	protected View view;
	SuperController superController;

	public ScreenController(View layout, SuperController superController) {
		this.view = layout;
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
		return superController.getActivity();
	}

	public void onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void onResume() {
		// TODO Auto-generated method stub
		
	}

	public void onRequestPublishPermission() {
		// TODO Auto-generated method stub
		
	}
}
