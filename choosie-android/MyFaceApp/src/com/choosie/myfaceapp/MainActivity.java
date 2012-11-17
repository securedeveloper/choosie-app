package com.choosie.myfaceapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Session;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap.CompressFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity<MultipartEntity> extends Activity implements OnClickListener {
	
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
	
	private String getUserId()
	{
		JSONObject me = null;
		try {
			me = new JSONObject(fb.request("me"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String id = null;
		try {
			id = me.getString("id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return id;
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
					
					
					sendDetailsToServer(getUserId(), fb.getAccessToken(), fb.getAccessExpires());
					
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
	
	private void sendDetailsToServer(String fb_uid, String access_token, long access_token_expdate)
	{
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = null;
		
		postRequest = createDetailsPost(fb_uid, access_token, access_token_expdate);

		AsyncTask<HttpPost, Void, HttpResponse> executeHttpPostTask = createExecuteHttpPostTask(httpClient);
		executeHttpPostTask.execute(postRequest);
	}
	
	private HttpPost createDetailsPost(String fb_uid, String access_token, long access_token_expdate) {
		
		HttpPost postRequest = new HttpPost(
				"http://choosieapp.appspot.com/login");

		org.apache.http.entity.mime.MultipartEntity reqEntity = new org.apache.http.entity.mime.MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			reqEntity.addPart("fb_uid", new StringBody(fb_uid));
			reqEntity.addPart("fb_access_token", new StringBody(access_token));
			reqEntity.addPart("fb_access_token_expdate", new StringBody(String.valueOf(access_token_expdate)));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		postRequest.setEntity(reqEntity);
		return postRequest;
	}

	private AsyncTask<HttpPost, Void, HttpResponse> createExecuteHttpPostTask(
			final HttpClient httpClient) {
		AsyncTask<HttpPost, Void, HttpResponse> executeHttpPostTask = new AsyncTask<HttpPost, Void, HttpResponse>() {

			@Override
			protected HttpResponse doInBackground(HttpPost... arg0) {
				try {
					return httpClient.execute(arg0[0]);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

		};
		return executeHttpPostTask;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		fb.authorizeCallback(requestCode, resultCode, data);
	}
}
