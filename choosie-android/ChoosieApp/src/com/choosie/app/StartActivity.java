package com.choosie.app;

import java.util.ArrayList;
import java.util.List;

import com.choosie.app.Models.FacebookDetails;
import com.facebook.GraphUser;
import com.facebook.LoggingBehaviors;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.android.FbDialog;
import com.facebook.SessionState;
import com.facebook.Response;
import com.facebook.Settings;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class StartActivity extends Activity{

	Button buttonLogout;
	ImageButton goToApplication;
	ImageButton buttonLogin;
	Session.StatusCallback statusCallback = new SessionStatusCallback();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_start);

		TextView welcome = (TextView) findViewById(R.id.welcome);
		welcome.setText(getResources().getString(R.string.welcome) + " "
				+ getResources().getString(R.string.app_name) + "!");

		Logger.getInstance().WriteLine("Start Application!");

		//Initialize all buttons
		InitializeComponents();
		
		Settings.addLoggingBehavior(LoggingBehaviors.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();

		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback));
			}
		}

		Logger.getInstance().WriteLine(
				"on start permissions: " + session.getPermissions().toString());
		updateView();
	}

	private void InitializeComponents() {
		Logger.getInstance().WriteLine("InitializeComponents()");
		
		// find all buttons
		buttonLogout = (Button) findViewById(R.id.fbLogoutButton);
		buttonLogin = (ImageButton)findViewById(R.id.fbLoginButton);
		goToApplication = (ImageButton)findViewById(R.id.goToApplication);
		
		// set on click listeners
		buttonLogin.setOnClickListener(loginListener);
		buttonLogout.setOnClickListener(logoutListener);
		goToApplication.setOnClickListener(goToApplicationListener);
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
		if (requestCode == Constants.RequestCodes.START_ACTIVITY){
			finish();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private void updateView() {

		Session session = Session.getActiveSession();
		Logger.getInstance().WriteLine(
				"Session: " + session.getState().toString());

		// Show "Logout" and go to application main screen
		if (session.isOpened()) {
			Logger.getInstance().WriteLine("Session is opened");
			
			goToApplication.setVisibility(View.VISIBLE);
			//buttonLogout.setVisibility(View.VISIBLE);
			buttonLogin.setVisibility(View.INVISIBLE);
			
			goToApplication();

			// Show "Login" and let user to login
		} else {
			Logger.getInstance().WriteLine("Session is closed");
			
			goToApplication.setVisibility(View.INVISIBLE);
			//buttonLogout.setVisibility(View.INVISIBLE);
			buttonLogin.setVisibility(View.VISIBLE);
		}
	}

	private void goToApplication() {

		Logger.getInstance().WriteLine("goToApplication()");

		// make request to the /me API
		Request request = Request.newMeRequest(Session.getActiveSession(),
				new Request.GraphUserCallback() {

					public void onCompleted(GraphUser user, Response response) {
						Logger.getInstance().WriteLine("onCompleted");
						if (user != null) {
							TextView welcome = (TextView) findViewById(R.id.welcome);
							welcome.setText("Hello " + user.getName() + "!");

							Logger.getInstance().WriteLine(
									"creating intent for ChoosieActivity");
							Intent intent = new Intent(StartActivity.this,
									ChoosieActivity.class);
							FacebookDetails details = new FacebookDetails(user
									.getId(), Session.getActiveSession()
									.getAccessToken(), Session
									.getActiveSession().getExpirationDate()
									.getTime());
							intent.putExtra("fb_details", details);

							Logger.getInstance().WriteLine(
									"Starting ChoosieActivity");
							startActivityForResult(intent, Constants.RequestCodes.START_ACTIVITY);
						}
					}
				}
		);
		Request.executeBatchAsync(request);
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {

			// Set read permissions
			List<String> read_permission = new ArrayList<String>();
			read_permission.add("read_stream");
			read_permission.add("read_friendlists");
			Logger.getInstance().WriteLine("Permission: " + read_permission);

			// Create the request for login
			OpenRequest req = new Session.OpenRequest(this);
			req.setPermissions(read_permission);
			req.setCallback(statusCallback);

			// Show login to Facebook screen
			session.openForRead(req);
			Logger.getInstance().WriteLine(
					"session.getPermission(): "
							+ session.getPermissions().toString());
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}
	}

	private void onClickLogout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		public void call(Session session, SessionState state,
				Exception exception) {

			// If session was opened - go to application main screen
			if (state == SessionState.OPENED) {
				Logger.getInstance().WriteLine(
						"CallBack: SessionState = " + state.toString());
				Logger.getInstance().WriteLine("Starting ChoosieActivity");

				goToApplication();

				// Else - show login screen
			} else {
				Logger.getInstance().WriteLine(
						"CallBack: SessionState = " + state.toString());
				updateView();
			}
		}
	}
	
	OnClickListener loginListener = new OnClickListener() {

		public void onClick(View v) {
			onClickLogin();
		}
	};

	OnClickListener logoutListener = new OnClickListener() {

		public void onClick(View v) {
			onClickLogout();
		}
	};

	OnClickListener goToApplicationListener = new OnClickListener() {

		public void onClick(View v) {
			goToApplication();
		}
	};
}