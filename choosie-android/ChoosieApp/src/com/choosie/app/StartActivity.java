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
import android.view.Menu;
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

		this.openSession();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_start, menu);
		return true;
	}

	@Override
	protected void onSessionStateChange(SessionState state, Exception exception) {
		// user has either logged in or not ...
		if (state.isOpened()) {
			// make request to the /me API
			Request request = Request.newMeRequest(this.getSession(),
					new Request.GraphUserCallback() {

						public void onCompleted(GraphUser user,
								Response response) {
							if (user != null) {
								TextView welcome = (TextView) findViewById(R.id.welcome);
								welcome.setText("Hello " + user.getName() + "!");

								Intent intent = new Intent(StartActivity.this,
										ChoosieActivity.class);
								FacebookDetails details = new FacebookDetails(
										user.getId(), getSession()
												.getAccessToken(), getSession()
												.getExpirationDate().getTime());
								intent.putExtra("fb_details", details);
								startActivity(intent);
							}
						}
					}

			);
			Request.executeBatchAsync(request);
		}
	}
}
