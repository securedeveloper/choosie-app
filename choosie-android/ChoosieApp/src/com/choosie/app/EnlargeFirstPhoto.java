package com.choosie.app;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ImageView;

//gesture
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public class EnlargeFirstPhoto extends Activity {
	String photo1Path;
	String photo2Path;
	
	private GestureDetector  myGesture ;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enlarge_first_photo);
		
		CustomGustureListener myGestureListener = new CustomGustureListener(new Callback<Void, Void, Void>() {
			
			@Override
			public void onFinish(Void param) {
				switchToSecondPhoto();
			}
			
		});
		
		myGesture = new GestureDetector(myGestureListener);
		
		Intent intent = getIntent();
		photo1Path = intent.getStringExtra(Constants.IntentsCodes.photo1Path);
		photo2Path = intent.getStringExtra(Constants.IntentsCodes.photo2Path);
		
		ImageView imageView = (ImageView)findViewById(R.id.enalrge_image1);
		imageView.setImageURI(Uri.fromFile(new File(photo1Path)));
		
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_enlarge_first_photo, menu);
		return true;
	}
	
	
	private void switchToSecondPhoto() {
		Intent intent = new Intent(this, EnlargeSecondPhoto.class);
		intent.putExtra(Constants.IntentsCodes.photo1Path, photo1Path);
		intent.putExtra(Constants.IntentsCodes.photo2Path, photo2Path);
		startActivityForResult(intent, 1);	
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResult(RESULT_CANCELED, null);
		finish();
	}
	
	
	
	@Override
    public boolean onTouchEvent(MotionEvent event){
    	return myGesture.onTouchEvent(event);
    }
	
	// setting the backKey to cancel the commentizatzia
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			setResult(RESULT_CANCELED, null);
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}


}
