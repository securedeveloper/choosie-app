package com.choosie.cameratry2;


import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.choosie.cameratry2.HttpHandler;
import com.choosie.cameratry2.R;

public class Cmaera extends Activity implements OnClickListener {
	private static int TAKE_FIRST_PICTURE = 1;
	private static int TAKE_SECOND_PICTURE = 2;
	private static int POST_IT = 3;
	private Uri outputFileUri;
	private Bitmap mPhotoTemp;
	private Bitmap mPhoto1;
	private Bitmap mPhoto2;
	private String mQuestion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((Button) findViewById(R.id.buttonPic1)).setOnClickListener(this);
		((Button) findViewById(R.id.buttonPic2)).setOnClickListener(this);
		((Button) findViewById(R.id.button_submit)).setOnClickListener(this);

	}

	public void onClick(View arg0) {

		if (arg0.getId() == R.id.button_submit) {
			try {
				EditText questionText = (EditText) findViewById(R.id.question_text);
				mQuestion = questionText.getText().toString();
				executeMultipartPost(mPhoto1, mPhoto2, mQuestion);
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

	public void executeMultipartPost(Bitmap photo1, Bitmap photo2, String question)
			throws Exception {
		try {
			ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
			ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
			photo1.compress(CompressFormat.JPEG, 75, bos1);
			photo2.compress(CompressFormat.JPEG, 75, bos2);
			byte[] data1 = bos1.toByteArray();
			byte[] data2 = bos2.toByteArray();
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(
					"http://choosieapp.appspot.com/upload");
			ByteArrayBody bab1 = new ByteArrayBody(data1, "photo1.jpg");
			ByteArrayBody bab2 = new ByteArrayBody(data2, "photo2.jpg");

			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);

			reqEntity.addPart("photo1", bab1);
			reqEntity.addPart("photo2", bab2);
			reqEntity.addPart("question", new StringBody(question));

			postRequest.setEntity(reqEntity);

			HttpHandler httpTask = new HttpHandler(httpClient, postRequest);
			httpTask.execute();
		} catch (Exception e) {
			Log.e(e.getClass().getName(), e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
