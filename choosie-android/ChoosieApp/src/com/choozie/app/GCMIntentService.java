package com.choozie.app;

import java.util.List;
import java.util.Random;

import com.choozie.app.R;
import com.choozie.app.Constants.Notifications;
import com.choozie.app.client.Client;
import com.choozie.app.controllers.SuperController;
import com.google.android.gcm.GCMBaseIntentService;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class GCMIntentService extends GCMBaseIntentService {
	@SuppressWarnings("hiding")
	private static final String TAG = "GCMIntentService";

	public GCMIntentService() {
		super(Constants.Notifications.SENDER_ID);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		L.i(TAG + " Received message. Extras: " + intent.getExtras());

		generateNotification(context, intent);
	}

	private void generateNotification(Context context, Intent intent) {
		if (isApplicationRunningInForeground()) {
			// TODO: show +1 in notification manager inside the app
			L.i("Application is running in Foreground");
			L.i("No need to send the Push Notification");
		} else {
			L.i("Application is not running in Background");
			/*
			 * Handle Push Notification /* 1 = New Post /* 2 = New Comment /* 3
			 * = New Vote
			 */

			String notificationType = intent.getStringExtra("type");
			String text = intent.getStringExtra("text");
			String postKey = intent.getStringExtra("post_key");
			String deviceId = intent.getStringExtra("device_id");
			L.i("GCMINTENTSERVICE: GOT NOTIFICATION. NotificationType = "
					+ notificationType + ", text = " + text + ", postkey = "
					+ postKey + ", deviceID = " + deviceId);

			PushNotification notification = new PushNotification(
					notificationType, text, postKey, deviceId);

			if (notification.getNotificationType().equals("1")) {
				if (!AppSettings.isGetAllNotifications(context)) {
					// TODO: change this bad hook

					// randomly decide if send notification or not
					Random r = new Random();
					int i = r.nextInt(2);
					if (i != 0)
						return;
				}
			}

			notifyStartActivity(context, notification);
		}
	}

	private boolean isApplicationRunningInForeground() {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		// get the info from the currently running task
		List<ActivityManager.RunningTaskInfo> runningTasks = am
				.getRunningTasks(1);
		if (runningTasks.size() == 0) {
			return false;
		}
		ActivityManager.RunningTaskInfo taskInfo = runningTasks.get(0);
		L.i("current task: " + taskInfo.topActivity.getClass().getSimpleName());

		return taskInfo.topActivity.getPackageName().equalsIgnoreCase(
				"com.choozie.app");
	}

	private boolean isAppRunning() {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		List<RunningAppProcessInfo> procList = am.getRunningAppProcesses();
		for (RunningAppProcessInfo proc : procList) {
			if (proc.processName.equals("com.choozie.app")) {
				return true;
			}
		}
		return false;
	}

	private void notifyStartActivity(Context context,
			PushNotification notificationData) {
		L.i("NotifyStartActivity()");

		PendingIntent resultPendingIntent = buildPendingIntent(context,
				notificationData);
		Notification notification = buildNotification(notificationData,
				resultPendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		notificationManager.notify(Constants.Notifications.NOTIFICATION_ID,
				notification);
	}

	private PendingIntent buildPendingIntent(Context context,
			PushNotification notificationData) {
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, StartActivity.class);
		resultIntent.putExtra("notification", notificationData);
		PendingIntent pendingIntent = null;
		L.i("-------------------- Application is running!!");

		// set intent so it does not start a new activity
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		pendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		return pendingIntent;
	}

	private Notification buildNotification(PushNotification notificationData,
			PendingIntent resultPendingIntent) {
		// Build the notification, the actual line in the status bar.
		String contentTitle = notificationData.getContentTitle();
		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.logo_purple_less_large)
				.setContentTitle(contentTitle)
				.setContentText(notificationData.getText())
				.setContentIntent(resultPendingIntent).build();

		// notification.defaults |= Notification.DEFAULT_SOUND;
		notification.sound = Uri.parse("android.resource://com.choozie.app/"
				+ R.raw.notification_sound);
		return notification;
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		L.i(TAG + " Device registered: regId = " + registrationId);
		Client.getInstance().setContext(context);
		Client.getInstance().registerGCM(registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		L.i(TAG + " Device unregistered");
		Client.getInstance().setContext(context);
		Client.getInstance().unregisterGCM(registrationId);
	}

	@Override
	protected void onError(Context context, String errorId) {
		L.i(TAG + " Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		L.i(TAG + " Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}
}
