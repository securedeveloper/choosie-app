package com.choosie.app;

import java.util.ArrayList;
import java.util.List;

import com.choosie.app.Models.FacebookDetails;
import com.facebook.FacebookActivity;
import com.facebook.GraphUser;
import com.facebook.Request;
import com.facebook.SessionState;
import com.facebook.Response;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends FacebookActivity {

	private String APP_ID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		APP_ID = getResources().getString(R.string.app_id);

		TextView welcome = (TextView) findViewById(R.id.welcome);
		welcome.setText(getResources().getString(R.string.welcome) + " "
				+ getResources().getString(R.string.app_name) + "!");

		Button fbLoginButton = (Button) findViewById(R.id.fbLoginButton);

		final List<String> permissions = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;
			{
				add("publish_stream");
				add("email");
			}
		};

		fbLoginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Log.i(Constants.LOG_TAG, "on click open session");
				// closeSession();

				openSessionForPublish(APP_ID, permissions);

			}
		});

		openSessionForPublish(APP_ID, permissions);
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

								Log.i(Constants.LOG_TAG,
										"creating intent for ChoosieActivity");
								Intent intent = new Intent(StartActivity.this,
										ChoosieActivity.class);
								FacebookDetails details = new FacebookDetails(
										user.getId(), getSession()
												.getAccessToken(), getSession()
												.getExpirationDate().getTime());
								intent.putExtra("fb_details", details);

								Log.i(Constants.LOG_TAG,
										"Starting ChoosieActivity");
								startActivity(intent);
							}
						}
					}

			);
			Request.executeBatchAsync(request);
		}
	}
}
