package com.choosie.cameratry2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.graphics.Matrix;
import android.widget.Toast;
import com.choosie.cameratry2.R;

public class Cmaera extends Activity implements OnClickListener {
	private static int TAKE_FIRST_PICTURE = 1;
	private static int TAKE_SECOND_PICTURE = 2;
	private static int POST_IT = 3;
	private Uri outputFileUri;
	private Bitmap mPhotoTemp;
	private Bitmap mPhoto1;
	private Bitmap mPhoto2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		// Button buttonPic = (Button) findViewById(R.id.buttonPic);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((Button) findViewById(R.id.buttonPic1)).setOnClickListener(this);
		((Button) findViewById(R.id.buttonPic2)).setOnClickListener(this);
		((Button) findViewById(R.id.button_submit)).setOnClickListener(this);

	}

	public void onClick(View arg0) {

		if (arg0.getId() == R.id.button_submit) {
			try {
				executeMultipartPost(mPhoto1, mPhoto2);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.getMessage();
				e.printStackTrace();
			}
		} else {
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			File f = new File(Environment.getExternalStorageDirectory(),
					"photo.jpg");
			// intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
			// ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			outputFileUri = Uri.fromFile(f);
			if (arg0.getId() == R.id.buttonPic1) {
				startActivityForResult(intent, TAKE_FIRST_PICTURE);
			}
			if (arg0.getId() == R.id.buttonPic2) {
				startActivityForResult(intent, TAKE_SECOND_PICTURE);
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			getContentResolver().notifyChange(outputFileUri, null);
			ContentResolver cr = getContentResolver();
			try {
				mPhotoTemp = android.provider.MediaStore.Images.Media
						.getBitmap(cr, outputFileUri);
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}

			if (requestCode == TAKE_FIRST_PICTURE) {
				((ImageView) findViewById(R.id.photoHolder1))
						.setImageBitmap(mPhotoTemp);
				mPhoto1 = mPhotoTemp;
			}
			if (requestCode == TAKE_SECOND_PICTURE) {
				((ImageView) findViewById(R.id.photoHolder2))
						.setImageBitmap(mPhotoTemp);
				mPhoto2 = mPhotoTemp;
			}
		}
	}

	public void executeMultipartPost(Bitmap photo1, Bitmap photo2)
			throws Exception {
		try {
			ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
			ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
			photo1.compress(CompressFormat.JPEG, 75, bos1);
			// photo2.compress(CompressFormat.JPEG, 75, bos2);
			byte[] data1 = bos1.toByteArray();
			// byte[] data2 = bos2.toByteArray();
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(
					"http://choosieapp.appspot.com/upload");
			// ByteArrayBody bab1 = new ByteArrayBody(data1, "forest.jpg");
			// ByteArrayBody bab2 = new ByteArrayBody(data1, "forest.jpg");
			// File file= new File("/mnt/sdcard/forest.png");
			// FileBody bin = new FileBody(file);

			// MultipartEntity reqEntity1 = new
			// MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			// reqEntity1.addPart("photo1", bab1);
			// reqEntity1.addPart("photoCaption", new StringBody("sfsdfsdf"));
			// postRequest.setEntity(reqEntity1);
			/*
			 * MultipartEntity reqEntity2 = new MultipartEntity(
			 * HttpMultipartMode.BROWSER_COMPATIBLE);
			 * reqEntity2.addPart("photo2", bab2);
			 * reqEntity2.addPart("photoCaption", new StringBody("sfsdfsdf"));
			 * postRequest.setEntity(reqEntity2);
			 */

			String data_string = Base64.encodeToString(data1, Base64.DEFAULT);

			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("photo1", new String(
					data1)));
			nameValuePairs.add(new BasicNameValuePair("photo2", ""));
			nameValuePairs
					.add(new BasicNameValuePair("question", "ohhhh yeah"));
			postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// StrictMode.ThreadPolicy policy = new
			// StrictMode.ThreadPolicy.Builder().permitAll().build();
			// StrictMode.setThreadPolicy(policy);

			HttpHandler httpTask = new HttpHandler(httpClient, postRequest);
			httpTask.execute(null);
			// HttpResponse response = httpClient.execute(postRequest);
			/*
			 * BufferedReader reader = new BufferedReader(new InputStreamReader(
			 * response.getEntity().getContent(), "UTF-8")); String sResponse;
			 * StringBuilder s = new StringBuilder();
			 * 
			 * while ((sResponse = reader.readLine()) != null) { s =
			 * s.append(sResponse); } // System.out.println("Response: " + s);
			 */
		} catch (Exception e) {
			// handle exception here
			Log.e(e.getClass().getName(), e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
