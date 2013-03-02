package com.choozie.app;

import com.choozie.app.client.Client;
import com.choozie.app.models.User;
import com.choozie.app.models.UserDetails;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class ProfileEditActivity extends Activity {

	private UserDetails userDetails;
	private EditText etNickname;
	private ImageButton ibUserPhoto;
	private ImageButton ibSaveChanges;
	private EditText etInfo;
	private TextView tvFullName;
	private Spinner spGender;
	private TextView tvInfoLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_edit);

		Intent intent = getIntent();
		// user = intent.getParcelableExtra(Constants.IntentsCodes.user);
		userDetails = intent
				.getParcelableExtra(Constants.IntentsCodes.userDetails);

		// initialize all components in the activity
		initializeComponents();

		fillUserDetails();

		initializeListeners();
	}

	private void initializeListeners() {
		// initialize all listeners
		ibSaveChanges.setOnClickListener(saveChangesClickListener);
		spGender.setOnItemSelectedListener(genderSelectedListener);
		etInfo.setOnFocusChangeListener(infoFocusChangedListeners);
		etInfo.addTextChangedListener(infoTextChangeListner);
	}

	private void fillUserDetails() {
		// photo
		String userImagePath = Utils.getFileNameForURL(userDetails.getUser()
				.getPhotoURL());
		Utils.setImageFromPath(userImagePath, ibUserPhoto);

		// full name + nickname + info
		etNickname.setText(userDetails.getNickname());
		etInfo.setText(userDetails.getInfo());
		userDetails.getInfo().length();
		tvFullName.setText(userDetails.getUser().getUserName());
	}

	private void initializeComponents() {
		// initialize all views
		ibSaveChanges = (ImageButton) findViewById(R.id.edit_profile_save_changes_image_button);
		ibUserPhoto = (ImageButton) findViewById(R.id.edit_profile_user_photo);
		etNickname = (EditText) findViewById(R.id.edit_profile_nickname_text);
		etInfo = (EditText) findViewById(R.id.edit_profile_info_text);
		tvInfoLabel = (TextView) findViewById(R.id.edit_profile_info_label);
		tvFullName = (TextView) findViewById(R.id.edit_profile_full_name);
		spGender = (Spinner) findViewById(R.id.edit_profile_gender_spinner);
	}

	protected void saveChanges() {
		UserDetails ud = getDetailsFromEditForm();
		Client.getInstance().setActiveUserDetails(ud);
		Client.getInstance().updateUserDetailsInfo(ud,
				new Callback<Void, Void, Void>() {
					@Override
					public void onPre(Void param) {
						L.i("showing WAIT_SAVING dialog");
						showDialog(Constants.DialogId.WAIT_SAVING);
					}

					@Override
					public void onProgress(Void param) {
						L.i("onProgress");
					}

					@Override
					public void onFinish(Void param) {
						L.i("returning to previous activity");
						returnToPreviousActivity(Activity.RESULT_OK);
					}
				});
	}

	private UserDetails getDetailsFromEditForm() {
		userDetails.setNickname(etNickname.getText().toString());
		userDetails.setInfo(etInfo.getText().toString());
		
//		UserDetails ud = new UserDetails(userDetails.getUser());
//
//		ud.setNickname(etNickname.getText().toString());
//		ud.setInfo(etInfo.getText().toString());

		L.i("User Details from the edit form: " + userDetails.toString());
		return userDetails;
	}

	protected void returnToPreviousActivity(int result) {
		setResult(result);
		finish();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Constants.DialogId.EXIT_ALERT_DIALOG:
			// Create out AlterDialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage("This will discard all your changes.");
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new OkOnClickListener());
			builder.setNegativeButton("Cancel", new CancelOnClickListener());
			AlertDialog dialog = builder.create();
			dialog.show();
			break;

		case Constants.DialogId.WAIT_SAVING:
			ProgressDialog dialog1 = new ProgressDialog(this);
			dialog1.setMessage("Saving changes...");
			dialog1.setIndeterminate(true);
			dialog1.setCancelable(false);
			return dialog1;

		case Constants.DialogId.ERROR:
			AlertDialog.Builder builderError = new AlertDialog.Builder(this);
			// TODO: change this to an ERROR MSG
			builderError.setMessage("Operation is not implemented yet.");
			builderError.setCancelable(false);
			builderError.setPositiveButton("OK", new OkOnClickListener());
			AlertDialog dialogError = builderError.create();
			dialogError.show();
			break;
		}

		return super.onCreateDialog(id);
	}

	private final class CancelOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {

		}
	}

	private final class OkOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			setResult(Activity.RESULT_OK);
			ProfileEditActivity.this.finish();
		}
	}

	private final class ErrorOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			setResult(Activity.RESULT_CANCELED);
			ProfileEditActivity.this.finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_profile_edit, menu);
		return true;
	}

	private OnClickListener saveChangesClickListener = new OnClickListener() {

		public void onClick(View v) {
			saveChanges();
		}
	};

	private OnItemSelectedListener genderSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			switch (pos) {
			case 1:
				userDetails.setGender(Constants.Gender.MALE);
				break;
			case 2:
				userDetails.setGender(Constants.Gender.FEMALE);
				break;
			default:
				userDetails.setGender("");
				break;
			}
			L.i("Set userDetails.gender: " + userDetails.getGender());
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	};

	private TextWatcher infoTextChangeListner = new TextWatcher() {

		public void afterTextChanged(Editable s) {
			tvInfoLabel.setText(Constants.USER_INFO_MAX_LEN - s.length()
					+ " more");
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}
	};

	private OnFocusChangeListener infoFocusChangedListeners = new OnFocusChangeListener() {

		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				tvInfoLabel.setText(getResources().getString(R.string.info));
			} else {
				etInfo.addTextChangedListener(infoTextChangeListner);
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showDialog(Constants.DialogId.EXIT_ALERT_DIALOG);
		}
		return true;
	}

}
