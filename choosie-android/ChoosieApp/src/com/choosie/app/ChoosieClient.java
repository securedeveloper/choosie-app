package com.choosie.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;

public class ChoosieClient {

	void sendChoosiePostToServer(NewChoosiePostData data) {
		final HttpClient httpClient = new DefaultHttpClient();
		AsyncTask<HttpPost, Void, HttpResponse> executeHttpPostTask = getExecuteHttpPostTask(httpClient);

		HttpPost postRequest = null;
		try {
			postRequest = createHttpPostRequest(data);
			executeHttpPostTask.execute(postRequest);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private AsyncTask<HttpPost, Void, HttpResponse> getExecuteHttpPostTask(
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

	public class ChoosiePostData {
		String photo1URL;
		String photo2URL;
		String question;
	}
	
	void getFeedFromServer() {
		final HttpClient client = new DefaultHttpClient();
		final HttpGet request = new HttpGet("http://choosieapp.appspot.com/feed");
		
		AsyncTask<Context, Void, String> getStreamTask =
				new AsyncTask<Context, Void, String>() {
			
			@Override
			protected String doInBackground(Context... params) {
				HttpResponse response;
				try {
					response = client.execute(request);
					return EntityUtils.toString(response.getEntity());
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(String jsonString) {
				if (jsonString == null) {
					// TODO Handle error
					return;
				}
				try {
					List<ChoosiePostData> choosiePostsFromFeed =
							convertJsonToChoosiePosts(jsonString);
					
					choosiePostsFromFeed = choosiePostsFromFeed;
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			/**
			 * Takes a JSON string, and builds ChoosiePostData objet from it.
			 * @param jsonString
			 * @return
			 * @throws JSONException
			 */
			private List<ChoosiePostData> convertJsonToChoosiePosts(String jsonString)
					throws JSONException {
				JSONObject jsonObject = new JSONObject(jsonString);
				JSONArray jsonArray = jsonObject.getJSONArray("feed");
				List<ChoosiePostData> choosiePostsFromFeed = 
						new ArrayList<ChoosieClient.ChoosiePostData>();
				for (int i = 0; i < jsonArray.length(); i++) {
					try {
						JSONObject singleItemJsonObject = jsonArray.getJSONObject(i);
						ChoosiePostData postData = new ChoosiePostData();
						postData.photo1URL = singleItemJsonObject.getString("photo1"); 
						postData.photo2URL = singleItemJsonObject.getString("photo2");
						postData.question = singleItemJsonObject.getString("question");
						choosiePostsFromFeed.add(postData);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return choosiePostsFromFeed;
			}
		};
		getStreamTask.execute();
	}

	void getPictureFromServer() {

	}

	
	private HttpPost createHttpPostRequest(NewChoosiePostData data)
			throws UnsupportedEncodingException {
		ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
		data.photo1.compress(CompressFormat.PNG, 75, bos1);
		data.photo2.compress(CompressFormat.PNG, 75, bos2);
		byte[] data1 = bos1.toByteArray();
		byte[] data2 = bos2.toByteArray();

		HttpPost postRequest = new HttpPost(
				"http://choosieapp.appspot.com/upload");
		ByteArrayBody bab1 = new ByteArrayBody(data1, "photo1.png");
		ByteArrayBody bab2 = new ByteArrayBody(data2, "photo2.png");

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		reqEntity.addPart("photo1", bab1);
		reqEntity.addPart("photo2", bab2);
		reqEntity.addPart("question", new StringBody(data.question));

		postRequest.setEntity(reqEntity);
		return postRequest;
	}


}
