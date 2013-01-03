package com.choosie.app;

import java.util.List;

import com.choosie.app.controllers.SuperController;
import com.choosie.app.PushNotification;
import com.google.android.gcm.GCMBaseIntentService;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		Log.d("GCM", "message " + String.valueOf(arg1));

		if (isApplicationRunningInForeground()) {
			// TODO: show +1 in notification manager inside the app
			Logger.i("Application is running in Foreground");
			Logger.i("No need to send the Push Notification");
		} else {
			Logger.i("Application is running in Background");
			/*
			 * Handle Push Notification /* 1 = New Post /* 2 = New Comment /* 3
			 * = New Vote
			 */

			String notificationType = arg1.getStringExtra("type");
			String text = arg1.getStringExtra("text");
			String postKey = arg1.getStringExtra("post_key");
			String deviceId = arg1.getStringExtra("device_id");

			PushNotification notification = new PushNotification(
					notificationType, text, postKey, deviceId);
			NotifyStartActivity(notification);
		}
	}

	private boolean isApplicationRunningInForeground() {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		// get the info from the currently running task
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		Logger.i("current task: "
				+ taskInfo.get(0).topActivity.getClass().getSimpleName());

		ComponentName componentInfo = taskInfo.get(0).topActivity;

		return componentInfo.getPackageName().equalsIgnoreCase(
				"com.choosie.app");
	}

	private boolean isAppRunning() {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		List<RunningAppProcessInfo> procList = am.getRunningAppProcesses();
		for (RunningAppProcessInfo proc : procList) {
			if (proc.processName.equals("com.choosie.app"))
				return true;
		}
		return false;
	}

	private void NotifyStartActivity(PushNotification notification) {
		Logger.i("NotifyStartActivity()");

		String contentTitle = notification.getContentTitle();

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(contentTitle)
				.setContentText(notification.getText());

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, StartActivity.class);
		resultIntent.putExtra("notification", notification);
		if (isAppRunning()) {
			Logger.i("-------------------- Application is running!!");
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_ACTIVITY_NEW_TASK);
		} else {
			Logger.i("---------------------------------- Application is NOT running!!");
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		// The stack builder object will contain an artificial back stack for
		// the started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(StartActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT
						| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notif = mBuilder.build();
		notif.defaults |= Notification.DEFAULT_SOUND;

		// mId allows you to update the notification later on.
		mNotificationManager.notify(Constants.Notifications.NOTIFICATION_ID,
				notif);
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.d("GCM", "registerd " + arg1);

		SuperController.getInstance(null, null).getClient().registerGCM(arg1);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.d("GCM", "unregisetr " + arg1);
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		Log.d("GCM", "error " + arg1);

	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		Log.d("GCM", "recovable error" + errorId);
		return false;
	}
}
