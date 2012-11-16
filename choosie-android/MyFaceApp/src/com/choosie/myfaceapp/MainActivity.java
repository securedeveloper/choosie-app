package com.choosie.myfaceapp;

import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.Session;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	Facebook fb;
	ImageView button, pic;
	SharedPreferences sp;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        String APP_ID = getString(R.string.APP_ID);
        fb = new Facebook(APP_ID);
        
        sp = getPreferences(MODE_PRIVATE);
        String access_token = sp.getString("access_token", null);
        long expires = sp.getLong("access_exipres", 0);
        
        if (access_token != null){
        	fb.setAccessToken(access_token);
        }
        if (expires != 0){
        	fb.setAccessExpires(expires);
        }


        button = (ImageView)findViewById(R.id.login);
        pic = (ImageView)findViewById(R.id.picture_pic);
        button.setOnClickListener(this);
        
        updateButtonImage();
    }

	private void updateButtonImage() {
		// TODO Auto-generated method stub
		if (fb.isSessionValid()) {
			button.setImageResource(R.drawable.logout_button);
		} else {
			button.setImageResource(R.drawable.login_button);
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (fb.isSessionValid()) {
			// button will close our session - logout facebook
			try {
				fb.logout(getApplicationContext());
				updateButtonImage();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// login facebook
			String[] permissions = {"publish_stream"};
			fb.authorize(this, permissions ,new DialogListener() {
				
				@Override
				public void onFacebookError(FacebookError e) {
					// TODO Auto-generated method stub
					Toast.makeText(MainActivity.this, "Facebook Error", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onError(DialogError e) {
					// TODO Auto-generated method stub
					Toast.makeText(MainActivity.this, "onError", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onComplete(Bundle values) {
					// TODO Auto-generated method stub
					Editor editor = sp.edit();
					editor.putString("access_token", fb.getAccessToken());
					editor.putLong("access_exipres", fb.getAccessExpires());
					editor.commit();
					
					updateButtonImage(); 
				}
				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		fb.authorizeCallback(requestCode, resultCode, data);
	}
}
