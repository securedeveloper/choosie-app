package com.choosie.app;

import com.choosie.app.R.string;
import com.facebook.FacebookActivity;
import com.facebook.GraphUser;
import com.facebook.Request;
import com.facebook.SessionState;
import com.facebook.Response;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.sax.TextElementListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StartActivity extends FacebookActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		TextView welcome = (TextView) findViewById(R.id.welcome);
		welcome.setText(getResources().getString(R.string.welcome) + " "
				+ getResources().getString(R.string.app_name) + "!");

		Button fbLoginButton = (Button)findViewById(R.id.fbLoginButton);
		fbLoginButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Log.i(Constants.LOG_TAG, "on click open session");
				openSession();
				
			}
		});
		
		openSession();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_start, menu);
		return true;
	}

	@Override
	protected void onSessionStateChange(SessionState state, Exception exception) {
		// user has either logged in or not ...
		Log.i(Constants.LOG_TAG, "onSessionStateChange");
		if (state.isOpened()) {
			// make request to the /me API
			Request request = Request.newMeRequest(this.getSession(),
					new Request.GraphUserCallback() {

						public void onCompleted(GraphUser user,
								Response response) {
							Log.i(Constants.LOG_TAG, "onCompleted");
							if (user != null) {
								TextView welcome = (TextView) findViewById(R.id.welcome);
								welcome.setText("Hello " + user.getName() + "!");

								Log.i(Constants.LOG_TAG, "creating intent for ChoosieActivity");	
								Intent intent = new Intent(StartActivity.this,
										ChoosieActivity.class);
								FacebookDetails details = new FacebookDetails(
										user.getId(), getSession()
												.getAccessToken(), getSession()
												.getExpirationDate().getTime());
								intent.putExtra("fb_details", details);
								
								Log.i(Constants.LOG_TAG, "Starting ChoosieActivity");
								startActivity(intent);
							}
						}
					}

			);
			Request.executeBatchAsync(request);
		}
	}
}
