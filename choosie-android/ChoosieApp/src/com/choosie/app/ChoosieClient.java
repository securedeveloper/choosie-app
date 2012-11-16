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
import android.os.Message;

public class ChoosieClient {

	/**
	 * Gets a ChoosiePost (photo1, photo2 and a question) and posts it to the server.
	 * @param data
	 */
	public void sendChoosiePostToServer(NewChoosiePostData data) {
		final HttpClient httpClient = new DefaultHttpClient();
		
		HttpPost postRequest = null;
		try {
			// Creates the POST request
			postRequest = createHttpPostRequest(data);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (postRequest == null) {
			return;
		}
		
		// Executes the POST request, async
		AsyncTask<HttpPost, Void, HttpResponse> executeHttpPostTask = getExecuteHttpPostTask(httpClient);

		executeHttpPostTask.execute(postRequest);
	}
	
	/**
	 * Represents the data that is returned from the server.
	 */
	public class ChoosiePostData {
		String photo1URL;
		String photo2URL;
		String question;
	}

	/**
	 * Gets the feed from the server.
	 * Calls the callback when the feed is back.
	 */
	void getFeedFromServer(Callback<List<ChoosiePostData>> callback) {
		final HttpClient client = new DefaultHttpClient();

		// Creates the GET HTTP request
		final HttpGet request = new HttpGet(
				"http://choosieapp.appspot.com/feed");

		// Executes the GET request async
		AsyncTask<HttpGet, Void, String> getStreamTask = createGetStreamTask(client, callback);
		getStreamTask.execute(request);
	}

	private AsyncTask<HttpGet, Void, String> createGetStreamTask(
			final HttpClient client, final Callback<List<ChoosiePostData>> callback) {
		AsyncTask<HttpGet, Void, String> getStreamTask = new AsyncTask<HttpGet, Void, String>() {

			@Override
			protected String doInBackground(HttpGet... params) {
				HttpResponse response;
				try {
					response = client.execute(params[0]);
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
					List<ChoosiePostData> choosiePostsFromFeed = convertJsonToChoosiePosts(jsonString);

					callback.handleOperationFinished(choosiePostsFromFeed);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		};
		return getStreamTask;
	}

	void getPictureFromServer() {

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

	/**
	 * Takes a JSON string, and builds ChoosiePostData objet from it.
	 * 
	 * @param jsonString
	 * @return
	 * @throws JSONException
	 */
	private List<ChoosiePostData> convertJsonToChoosiePosts(String jsonString)
			throws JSONException {
		JSONObject jsonObject = new JSONObject(jsonString);
		JSONArray jsonArray = jsonObject.getJSONArray("feed");
		List<ChoosiePostData> choosiePostsFromFeed = new ArrayList<ChoosieClient.ChoosiePostData>();
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
