package com.choosie.app;

//import val.to.R.string;
//import val.to.common.AsyncTasksQueue;
//import val.to.uploader.UploadManager;
import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

public class Globals {
	public static Facebook FacebookSession;
	public static AsyncFacebookRunner AsyncRunner;
	public static String UserID;
	public static Bitmap ProfilePic;
	public static String Name;
//	public static PasswordsStore PasswordsStore = new PasswordsStore();
//	public static AsyncTasksQueue AsyncTasksQueue = new AsyncTasksQueue();

	// private static boolean initialized;

	public static void Init(Context context) {
		// if (!initialized) {
		// Facebook initialization. MUST be first
		
		//Globals.FacebookSession = new Facebook(String.valueOf(R.string.fb_app_id));
		Globals.FacebookSession = new Facebook("515799855110300");
		Globals.AsyncRunner = new AsyncFacebookRunner(Globals.FacebookSession);

		// Get existing access_token if any
		SessionStore.restore(Globals.FacebookSession, context);
	}
	
	public static void clear(){
		UserID = null;
		Name = null;
		ProfilePic = null;
	}
}
