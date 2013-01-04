package com.choosie.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class AppSettingsWindow {

	Activity activity;

	public AppSettingsWindow(Activity activity) {
		this.activity = activity;
	}

	public void show() {
		PopupWindow pw;

		// We need to get the instance of the LayoutInflater, use the
		// context of this activity
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.settings_layout,
				(ViewGroup) activity.findViewById(R.id.settings_element));

		// Initialize push notifications CheckBox
		boolean checked = (AppSettings.getPushNotifications().equals("true")) ? true
				: false;
		final CheckBox pushNotificationCb = (CheckBox) layout
				.findViewById(R.id.push_notifications_cb);
		pushNotificationCb.setChecked(checked);
		pushNotificationCb.setOnCheckedChangeListener(checkChangeListener);

		// set view and size
		pw = new PopupWindow(layout, Utils.getScreenWidth() - 30,
				Utils.getScreenHeight() / 2, true);
		pw.setAnimationStyle(R.style.PopupWindowAnimation);

		// for closing when touching outside - set the background not null
		pw.setBackgroundDrawable(new BitmapDrawable());

		// show it!
		pw.showAtLocation(layout, Gravity.CENTER, 0, 10);
	}

	private OnCheckedChangeListener checkChangeListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			String allow = (isChecked) ? "true" : "false";
			AppSettings.savePrefrences(Constants.SP.PUSH_NOTIFICATIONS, allow);
			Logger.i("SharedPreferences : Setting push_notifications = "
					+ isChecked);
		}
	};

}
