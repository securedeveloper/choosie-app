package com.choosie.app;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CommentScreen extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_screen);

		Button buttonSendComment = (Button) findViewById(R.id.button_send_comment);

		buttonSendComment.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				String text = ((EditText) findViewById(R.id.editText_comment))
						.getText().toString();

				Intent i = getIntent();
				i.putExtra("text", text);

				setResult(RESULT_OK, i);

				finish();

				// superController.CommentFor(choosiePost, text);
			}
		});
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, null);
		finish();
		return;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			setResult(RESULT_CANCELED, null);
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_comment_screen, menu);
		return true;
	}
}
