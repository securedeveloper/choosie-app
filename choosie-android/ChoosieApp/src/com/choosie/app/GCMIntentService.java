package com.choosie.app;

import com.choosie.app.controllers.SuperController;
import com.google.android.gcm.GCMBaseIntentService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class GCMIntentService extends GCMBaseIntentService {
	
	private static SuperController superController;
	
	public static void setSuperController(SuperController superController1){
		superController = superController1;
	}

	@Override
    protected boolean onRecoverableError(Context context, String errorId){
        Log.d("GCM", "recovable error" + errorId);
        return false;
    }

    @Override
    protected void onMessage(Context arg0, Intent arg1) {
        Log.d("GCM", "message " + String.valueOf(arg1));
        String message = arg1.getStringExtra("text");
        NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.image_selected_v)
		        .setContentTitle("Choozie notification!!!")
		        .setContentText(message);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, StartActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		
		
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(StartActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notif = mBuilder.build();
		notif.defaults |= Notification.DEFAULT_SOUND;
		// mId allows you to update the notification later on.
		mNotificationManager.notify(123, notif);		
		
    } 

    @Override
    protected void onRegistered(Context arg0, String arg1) {
        Log.d("GCM","registerd " + arg1);
        superController.getClient().registerGCM(arg1);        
    }

    @Override
    protected void onUnregistered(Context arg0, String arg1) {
        Log.d("GCM", "unregisetr " + arg1);
    }

	@Override
	protected void onError(Context arg0, String arg1) {
		  Log.d("GCM", "error " +  arg1);
		
	}
}

