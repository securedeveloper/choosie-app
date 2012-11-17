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
import org.apache.http.client.methods.HttpUriRequest;
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
	 * @param progressCallback
	 */
	public void sendChoosiePostToServer(NewChoosiePostData data,
			Callback<Void, Void> progressCallback) {
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
		AsyncTask<HttpPost, Integer, HttpResponse> executeHttpPostTask = createExecuteHttpPostTask(
				httpClient, progressCallback);

		executeHttpPostTask.execute(postRequest);
	}

	/**
	 * Represents the data that is returned from the server.
	 */
	public class ChoosiePostData {
		int votes1;
		int votes2;
		String photo1URL;
		String photo2URL;
		String question;

		public String getKey() {
			// HACK: Get post key from photo1URL
			String url = photo1URL;
			String key = url.substring(url.indexOf("post_key=")
					+ "post_key=".length());
			return key;
		}

	}

	/**
	 * Gets the feed from the server. Calls the callback when the feed is back.
	 */
	void getFeedFromServer(Callback<Void, List<ChoosiePostData>> callback) {
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

	public Bitmap getPictureFromServerSync(final String pictureUrl) {
		String urlToLoad;
		urlToLoad = ROOT_URL + pictureUrl;
		Log.i(ChoosieConstants.LOG_TAG, "getPictureFromServer: Loading URL: "
				+ urlToLoad);
		URL url;
		try {
			url = new URL(urlToLoad);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(input);
			return bitmap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets a NewChoosiePostData object, and creates an HTML POST request with
	 * the data.
	 * 
	 * @param data
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private HttpPost createHttpPostRequest(NewChoosiePostData data)
			throws UnsupportedEncodingException {
		ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();

		data.image1.compress(CompressFormat.JPEG, 40, bos1);
		data.image2.compress(CompressFormat.JPEG, 40, bos2);
		byte[] data1 = bos1.toByteArray();
		byte[] data2 = bos2.toByteArray();

		HttpPost postRequest = new HttpPost(
				"http://choosieapp.appspot.com/upload");
		ByteArrayBody bab1 = new ByteArrayBody(data1, "photo1.jpg");
		ByteArrayBody bab2 = new ByteArrayBody(data2, "photo2.jpg");

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		reqEntity.addPart("photo1", bab1);
		reqEntity.addPart("photo2", bab2);
		reqEntity.addPart("question", new StringBody(data.question));

		postRequest.setEntity(reqEntity);
		return postRequest;
	}

	private AsyncTask<HttpPost, Integer, HttpResponse> createExecuteHttpPostTask(
			final HttpClient httpClient,
			final Callback<Void, Void> progressCallback) {
		AsyncTask<HttpPost, Integer, HttpResponse> executeHttpPostTask = new AsyncTask<HttpPost, Integer, HttpResponse>() {

			@Override
			protected HttpResponse doInBackground(HttpPost... arg0) {
				try {
					publishProgress();
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

			@Override
			protected void onProgressUpdate(Integer... progress) {
				progressCallback.onProgress(null);
			}

			@Override
			protected void onPostExecute(HttpResponse httpResponse) {
				progressCallback.onOperationFinished(null);

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
			final Callback<Void, List<ChoosiePostData>> callback) {
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

					callback.onOperationFinished(choosiePostsFromFeed);
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
				postData.votes1 = singleItemJsonObject.getInt("votes1");
				postData.votes2 = singleItemJsonObject.getInt("votes2");
				choosiePostsFromFeed.add(postData);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return choosiePostsFromFeed;
	}

	public void sendVoteToServer(ChoosiePostData choosiePost, int whichPhoto,
			final Callback<Void, Boolean> callback) {
		final HttpUriRequest postRequest;
		// try {
		postRequest = new HttpGet(
				"http://choosieapp.appspot.com/vote?which_photo="
						+ Integer.toString(whichPhoto) + "&post_key="
						+ choosiePost.getKey());
		// postRequest = createVoteHttpPostRequest(choosiePost, whichPhoto);
		// } catch (UnsupportedEncodingException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// return;
		// }
		final HttpClient httpClient = new DefaultHttpClient();
		AsyncTask<Void, Void, Boolean> postVoteTask = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					httpClient.execute(postRequest);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				callback.onOperationFinished(result);
			}
		};

		postVoteTask.execute();

	}

	// TODO currently we're doing 'GET' requests to do voting. Not good. 
	private HttpPost createVoteHttpPostRequest(ChoosiePostData choosiePost,
			int whichPhoto) throws UnsupportedEncodingException {
		HttpPost postRequest;
		postRequest = new HttpPost("http://choosieapp.appspot.com/vote");

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		reqEntity.addPart("which_photo",
				new StringBody(Integer.toString(whichPhoto)));
		reqEntity.addPart("post_key", new StringBody(choosiePost.getKey()));

		postRequest.setEntity(reqEntity);
		return postRequest;
	}

}
