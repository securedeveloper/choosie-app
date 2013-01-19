package com.choozie.app;

import com.choozie.app.models.User;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

public class ProfileEditActivity extends Activity {

	private User user;
	private EditText etNickname;
	private ImageButton ibUserPhoto;
	private ImageButton ibSaveChanges;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_edit);

		Intent intent = getIntent();
		user = intent.getParcelableExtra(Constants.IntentsCodes.user);

		ibSaveChanges = (ImageButton) findViewById(R.id.edit_profile_save_changes_image_button);
		etNickname = (EditText) findViewById(R.id.edit_profile_nickname_text);
		ibUserPhoto = (ImageButton) findViewById(R.id.edit_profile_user_photo);
		String userImagePath = Utils.getFileNameForURL(user.getPhotoURL());
		Utils.setImageFromPath(userImagePath, ibUserPhoto);
		ibSaveChanges.setOnClickListener(saveChangesClickListener);
	}

	protected void saveChanges() {
		// TODO Auto-generated method stub
		// TODO: send changes to server in order to SAVE
		showDialog(Constants.DialogId.ERROR);
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
			builder.setNegativeButton("Cancel",
					new CancelOnClickListener());
			AlertDialog dialog = builder.create();
			dialog.show();
			break;

		case Constants.DialogId.WAIT_LOADING:
			ProgressDialog dialog1 = new ProgressDialog(this);
			dialog1.setMessage("Saving changes...");
			dialog1.setIndeterminate(true);
			dialog1.setCancelable(false);
			return dialog1;

		case Constants.DialogId.ERROR:
			AlertDialog.Builder builderError = new AlertDialog.Builder(this);
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

}
