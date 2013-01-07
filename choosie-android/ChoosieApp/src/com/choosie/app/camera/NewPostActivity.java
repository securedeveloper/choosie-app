package com.choosie.app.camera;

import java.util.ArrayList;
import java.util.List;

import com.choosie.app.Constants;
import com.choosie.app.Logger;
import com.choosie.app.NewChoosiePostData;
import com.choosie.app.R;
import com.choosie.app.Utils;
import com.choosie.app.Constants.IntentsCodes;
import com.choosie.app.R.drawable;
import com.choosie.app.R.id;
import com.choosie.app.R.layout;
import com.choosie.app.R.menu;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class NewPostActivity extends Activity {

	private int topWrapperHeight;
	private RelativeLayout topLayout;
	private EditText mQuestion;
	private ImageView mImage1;
	private ImageView mImage2;
	private TextView mTvFacebook;
	private ToggleButton mTbFacebook;
	private Session session;
	private StatusCallback statusCallback = new SessionStatusCallback();
	private ImageButton mBtnSubmit;
	private String imagePath1;
	private String imagePath2;
	private Bitmap bmp1;
	private Bitmap bmp2;
	private Bitmap photoBitmap1;
	private Bitmap photoBitmap2;
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_post);

		// Get the intent from the Camera Activity
		intent = getIntent();
		if (intent == null) {
			Logger.e("An Error has occurred! Intent is NULL");
		}

		InitializeComponents(intent);
	}

	private void InitializeComponents(Intent intent) {

		// initialize all components
		this.topLayout = (RelativeLayout) findViewById(R.id.post_layout_top);
		this.mQuestion = (EditText) findViewById(R.id.post_tvQuestion);
		this.mImage1 = (ImageView) findViewById(R.id.image_photo1);
		this.mImage2 = (ImageView) findViewById(R.id.image_photo2);
		this.mTvFacebook = (TextView) findViewById(R.id.tvFacebook);
		this.mTbFacebook = (ToggleButton) findViewById(R.id.tbFacebook);
		this.mBtnSubmit = (ImageButton) findViewById(R.id.post_btnSubmit);
		this.session = Session.getActiveSession();

		this.imagePath1 = intent
				.getStringExtra(Constants.IntentsCodes.photo1Path);
		this.imagePath2 = intent
				.getStringExtra(Constants.IntentsCodes.photo2Path);

		topWrapperHeight = intent.getIntExtra(
				Constants.IntentsCodes.cameraTopWrapperHeight, 0);

		// set heights and shit
		Utils.setImageViewSize(topLayout, topWrapperHeight, 0);
		Utils.setImageViewSize(mBtnSubmit, topWrapperHeight, topWrapperHeight);
		Utils.setImageViewSize(mImage1, Utils.getScreenWidth() / 2,
				Utils.getScreenWidth() / 2);
		Utils.setImageViewSize(mImage2, Utils.getScreenWidth() / 2,
				Utils.getScreenWidth() / 2);

		// set all listeners
		this.mImage1.setOnClickListener(imageClickListener);
		this.mImage2.setOnClickListener(imageClickListener);
		this.mBtnSubmit.setOnClickListener(submitClickListener);
		this.mTbFacebook.setOnCheckedChangeListener(fbCheckChangedListener);

		// get images bitmaps
		photoBitmap1 = Utils.getBitmapFromFileByViewSize(imagePath1,
				Utils.getScreenWidth() / 2, Utils.getScreenWidth() / 2);
		photoBitmap2 = Utils.getBitmapFromFileByViewSize(imagePath2,
				Utils.getScreenWidth() / 2, Utils.getScreenWidth() / 2);

		// set imaged in views
		mImage1.setImageBitmap(photoBitmap1);
		mImage2.setImageBitmap(photoBitmap2);
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

//		// create a new post with all details
//		NewChoosiePostData ncpd = new NewChoosiePostData(bmp1, bmp2, mQuestion
//				.getText().toString());
//
//		// insert it to a bundle
//		Bundle bundle = new Bundle();
//		bundle.putSerializable("post", ncpd);
//
//		// add the bundle to the intent
//		Intent intent = new Intent();
//		intent.putExtras(bundle);
//
//		startActivity(intent);
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
				sendToConfirmation(1);
			} else {
				sendToConfirmation(2);
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

	protected void sendToConfirmation(int photoNumber) {
		Intent intent = new Intent();
		intent.putExtra(Constants.IntentsCodes.photoNumber, photoNumber);
		setResult(Activity.RESULT_CANCELED, intent);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			sendToConfirmation(2);
		}
		return super.onKeyDown(keyCode, event);
	}

}
