package com.choosie.app;

import java.util.ArrayList;
import java.util.List;

import com.choosie.app.camera.ConfirmationActivity;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Session.ReauthorizeRequest;
import com.facebook.Session.StatusCallback;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class NewPostActivity extends Activity {

	private EditText mQuestion;
	private ImageView mImage1;
	private ImageView mImage2;
	private TextView mTvFacebook;
	private ToggleButton mTbFacebook;
	private Session session;
	private StatusCallback statusCallback = new SessionStatusCallback();
	private ImageButton mBtnSubmit;
	private Bitmap bmp1;
	private Bitmap bmp2;
	private byte[] imgByteArray1;
	private byte[] imgByteArray2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_post);

		// Get the intent from the Camera Activity
		Intent intent = getIntent();
		if (intent == null) {
			Logger.e("An Error has occurred! Intent is NULL");
		}

		InitializeComponents(intent);
	}

	private void InitializeComponents(Intent intent) {
		
		// initialize all components
		this.mQuestion = (EditText) findViewById(R.id.tvQuestion);
		this.mImage1 = (ImageView) findViewById(R.id.image_photo1);
		this.mImage2 = (ImageView) findViewById(R.id.image_photo2);
		this.mTvFacebook = (TextView) findViewById(R.id.tvFacebook);
		this.mTbFacebook = (ToggleButton) findViewById(R.id.tbFacebook);
		this.mBtnSubmit = (ImageButton) findViewById(R.id.btnSubmit);
		this.session = Session.getActiveSession();

		// Place images taken in the ImageViews
		this.imgByteArray1 = intent.getByteArrayExtra("image1");
		this.imgByteArray2 = intent.getByteArrayExtra("image2");
		
		this.bmp1 = BitmapFactory.decodeByteArray(imgByteArray1, 0, imgByteArray1.length);
		this.bmp2 = BitmapFactory.decodeByteArray(imgByteArray2, 0, imgByteArray2.length);

		this.mImage1.setImageBitmap(bmp1);
		this.mImage2.setImageBitmap(bmp2);
		
		// set all listeners
		this.mImage1.setOnClickListener(imageClickListener);
		this.mImage2.setOnClickListener(imageClickListener);
		this.mBtnSubmit.setOnClickListener(submitClickListener);
		this.mTbFacebook.setOnCheckedChangeListener(fbCheckChangedListener);
	}

	protected boolean isUserHasPublishPermissions() {
		boolean userHasPublishPermissions = false;
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			List<String> perms = session.getPermissions();
			userHasPublishPermissions = perms.contains("publish_stream");
		} else {
			Logger.i("isUserHasPublishPermissions(): session is not opened!");
		}
		return userHasPublishPermissions;
	}

	protected void askForPublishPermissions() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			List<String> permissions = new ArrayList<String>();
			permissions.add("publish_stream");

			ReauthorizeRequest request = new ReauthorizeRequest(this,
					permissions);
			request.setCallback(statusCallback);

			try {
				session.reauthorizeForPublish(request);
			} catch (Exception ex) {
				Log.e(Constants.LOG_TAG,
						"Exception in reauthorizeForPublish() : "
								+ ex.toString());
			}
			Logger.i("on set active session permissions: "
					+ session.getPermissions().toString());
		}
	}

	protected void sendPostData() {

		// create a new post with all details
		NewChoosiePostData ncpd = new NewChoosiePostData(bmp1, bmp2, mQuestion
				.getText().toString());

		// insert it to a bundle
		Bundle bundle = new Bundle();
		bundle.putSerializable("post", ncpd);

		// add the bundle to the intent
		Intent intent = new Intent();
		intent.putExtras(bundle);

		startActivity(intent);
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		public void call(Session session, SessionState state,
				Exception exception) {
			Logger.i("Entered SessionStatusCallback()");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_new_post, menu);
		return true;
	}
	
	private OnClickListener imageClickListener = new OnClickListener() {
		public void onClick(View v) {
			
			if (v.equals(mImage1)) {
				sendToConfirmation(imgByteArray1);	
			} else {
				sendToConfirmation(imgByteArray2);	
			}
			
		}
	};

	private OnClickListener submitClickListener = new OnClickListener() {
		public void onClick(View arg0) {
			sendPostData();
		}
	};

	private OnCheckedChangeListener fbCheckChangedListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				// check if user has publish_stream permissions
				boolean userHasPublishPermissions = isUserHasPublishPermissions();
				mTbFacebook
						.setBackgroundResource(R.drawable.facebook_square_blue);

				// if so, just show button as checked
				if (userHasPublishPermissions) {
					Logger.i("Already have publish permissions: "
							+ session.getPermissions().toString());
					mTbFacebook.setChecked(true);

				} else {
					// else - pop up Facebook screen and ask for
					// publish_stream permissions
					askForPublishPermissions();
				}
			} else {
				mTbFacebook
						.setBackgroundResource(R.drawable.facebook_square_bw);
			}
		}
	};

	protected void sendToConfirmation(byte[] imgByteArray) {
		
		Bundle bundle = new Bundle();
		bundle.putByteArray("image", imgByteArray);
		
		Intent intent = new Intent(this, ConfirmationActivity.class);
		intent.putExtras(bundle);
		
		startActivity(intent);
	}

}
