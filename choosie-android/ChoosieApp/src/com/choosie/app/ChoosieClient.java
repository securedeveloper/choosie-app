package com.choosie.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class ChoosieClient {

	/**
	 * Gets a ChoosiePost (photo1, photo2 and a question) and posts it to the
	 * server.
	 * 
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
		AsyncTask<HttpPost, Void, HttpResponse> executeHttpPostTask = createExecuteHttpPostTask(httpClient);

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
	 * Gets the feed from the server. Calls the callback when the feed is back.
	 */
	void getFeedFromServer(Callback<List<ChoosiePostData>> callback) {
		final HttpClient client = new DefaultHttpClient();

		// Creates the GET HTTP request
		final HttpGet request = new HttpGet(
				"http://choosieapp.appspot.com/feed");

		// Executes the GET request async
		AsyncTask<HttpGet, Void, String> getStreamTask = createGetFeedTask(
				client, callback);
		getStreamTask.execute(request);
	}
    static final String ROOT_URL = "http://choosieapp.appspot.com";
	void getPictureFromServer(String pictureUrl, Callback<Bitmap> callback) {
		try {
			String urlToLoad = ROOT_URL + pictureUrl;
			Log.i(ChoosieConstants.LOG_TAG, "Loading URL: " + urlToLoad);
			URL url = new URL(urlToLoad);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			callback.handleOperationFinished(myBitmap);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Gets a NewChoosiePostData object, and creates an HTML POST request
	 * with the data.
	 * @param data
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private HttpPost createHttpPostRequest(NewChoosiePostData data)
			throws UnsupportedEncodingException {
		ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
		data.image1.compress(CompressFormat.PNG, 75, bos1);
		data.image2.compress(CompressFormat.PNG, 75, bos2);
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

	/**
	 * Creates an AsyncTask that takes care of getting the feed from the server.
	 * 
	 * @param client
	 * @param callback
	 * @return
	 */
	private AsyncTask<HttpGet, Void, String> createGetFeedTask(
			final HttpClient client,
			final Callback<List<ChoosiePostData>> callback) {
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

}
