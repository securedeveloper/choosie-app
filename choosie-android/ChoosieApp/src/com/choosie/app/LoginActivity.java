package com.choosie.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.choosie.app.Globals;
import com.choosie.app.LoginButton;
import com.choosie.app.LoginActivity;
import com.choosie.app.R;
import com.choosie.app.SessionEvents;
import com.choosie.app.ChoosieActivity;
//import com.choosie.app.SuccessActivity;
//import com.choosie.app.LoginActivity.FacebookAuthListener;
import com.choosie.app.ErrorHelper;
import com.choosie.app.FacebookDetails;
import com.choosie.app.SessionEvents.AuthListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class LoginActivity extends Activity {

	private Handler mHandler;
	private LoginButton mLoginButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Globals.Init(this);

		mHandler = new Handler();
		String[] mPermissions = new String[] { "publish_stream", "user_photos",
				"email" };

		mLoginButton = (LoginButton) findViewById(R.id.login);
		mLoginButton.setVisibility(View.INVISIBLE);
		mLoginButton.init(this, 102, Globals.FacebookSession, mPermissions);

		SessionEvents.addAuthListener(new FacebookAuthListener());

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (Globals.FacebookSession.isSessionValid()) {
					mHandler.post(new Runnable() {

						public void run() {
							finish();

							Intent intent = new Intent().setClass(
									LoginActivity.this, ChoosieActivity.class);
							startActivity(intent);
						}
					});
				}

				else {
					// show login button
					mHandler.post(new Runnable() {

						public void run() {
							mLoginButton.setVisibility(View.VISIBLE);

						}
					});
				}

			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 2);
	}

	@SuppressWarnings("deprecation")
	private String getUserId() {
		JSONObject me = null;
		try {
			me = new JSONObject(Globals.FacebookSession.request("me"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String id = null;
		try {
			id = me.getString("id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return id;
	}

	public class FacebookAuthListener implements AuthListener {

		public void onAuthSucceed() {
			Log.i(ChoosieConstants.LOG_TAG, "onAuthSucceeded");
			FacebookDetails facebookDetails = new FacebookDetails(getUserId(),
					Globals.FacebookSession.getAccessToken(),
					Globals.FacebookSession.getAccessExpires());

			sendChoosiePostToServer(facebookDetails);

			Intent intent = new Intent().setClass(LoginActivity.this,
					ChoosieActivity.class);
			startActivity(intent);
			finish();
		}

		public void onAuthFail(String error) {
			Log.i(ChoosieConstants.LOG_TAG, "onAuthFail2");
			ErrorHelper.showErrorAlert(LoginActivity.this, "Login Failed",
					error).show();
		}

	}

	public void sendChoosiePostToServer(FacebookDetails facebookDetails) {
		final HttpClient httpClient = new DefaultHttpClient();

		HttpPost postRequest = null;
		try {
			// Creates the POST request
			postRequest = createHttpPostRequest(facebookDetails);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (postRequest == null) {
			return;
		}

		// Executes the POST request, async
		AsyncTask<HttpPost, Void, HttpResponse> executeHttpPostTask = createExecuteHttpPostTask(httpClient);

		executeHttpPostTask.execute(postRequest);
	}

	private HttpPost createHttpPostRequest(FacebookDetails facebookDetails)
			throws UnsupportedEncodingException {

		HttpPost postRequest = new HttpPost(
				"http://dev.choosieapp.appspot.com/login");

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		reqEntity
				.addPart("fb_uid", new StringBody(facebookDetails.getFb_uid()));
		reqEntity.addPart("fb_access_token",
				new StringBody(facebookDetails.getAccess_token()));
		reqEntity.addPart(
				"fb_access_token_expdate",
				new StringBody(String.valueOf(facebookDetails
						.getAccess_token_expdate())));

		postRequest.setEntity(reqEntity);
		return postRequest;
	}

	private AsyncTask<HttpPost, Void, HttpResponse> createExecuteHttpPostTask(
			final HttpClient httpClient) {
		AsyncTask<HttpPost, Void, HttpResponse> executeHttpPostTask = new AsyncTask<HttpPost, Void, HttpResponse>() {

			@Override
			protected HttpResponse doInBackground(HttpPost... arg0) {
				try {
					return httpClient.execute(arg0[0]);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
		return executeHttpPostTask;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}
}
