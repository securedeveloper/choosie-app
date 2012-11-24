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
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class Client {

	private FacebookDetails fbDetails;

	public Client(FacebookDetails fbDetails) {
		this.fbDetails = fbDetails;
	}

	/**
	 * Gets a ChoosiePost (photo1, photo2 and a question) and posts it to the
	 * server.
	 * 
	 * @param data
	 * @param progressCallback
	 */
	public void sendChoosiePostToServer(NewChoosiePostData data,
			Callback<Void, Integer, Void> progressCallback) {
		final HttpClient httpClient = new DefaultHttpClient();

		AsyncTask<HttpPost, Integer, HttpResponse> executePostTask = createExecuteHttpPostTask(
				data, httpClient, progressCallback);

		executePostTask.execute((HttpPost) null);
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
		public String userName;
		public String userPhotoURL;

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
	void getFeedFromServer(Callback<Void, Void, List<ChoosiePostData>> callback) {
		final HttpClient client = new DefaultHttpClient();

		// Creates the GET HTTP request
		final HttpGet request = new HttpGet(Constants.URIs.FEED_URI);

		// Executes the GET request async
		AsyncTask<HttpGet, Void, String> getStreamTask = createGetFeedTask(
				client, callback);
		getStreamTask.execute(request);
	}

	public Bitmap getPictureFromServerSync(final String pictureUrl,
			Callback<Void, Object, Void> progressCallback) {
		String urlToLoad = pictureUrl;
		Log.i(Constants.LOG_TAG, "getPictureFromServer: Loading URL: "
				+ urlToLoad);
		URL url;
		try {
			url = new URL(urlToLoad);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		progressCallback.onProgress(Integer.valueOf(1));
		try {
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(true);
			connection.connect();
			byte[] buffer = downloadFile(progressCallback, connection);
			progressCallback.onProgress(Integer.valueOf(99));
			Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0,
					buffer.length);
			return bitmap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void login(final Callback<Void, Void, Void> onLoginComplete) {
		final HttpClient httpClient = new DefaultHttpClient();
		final HttpPost postRequest = new HttpPost(
				Constants.URIs.ROOT_URL + "/login");

		try {
			createLoginPostRequest(postRequest);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		AsyncTask<Void, Void, Void> loginTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					httpClient.execute(postRequest);
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
			protected void onPostExecute(Void result) {
				onLoginComplete.onFinish(null);
			}
		};
		loginTask.execute();
	}

	private void createLoginPostRequest(HttpPost postRequest)
			throws UnsupportedEncodingException {
		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		reqEntity.addPart("fb_uid", new StringBody(this.fbDetails.getFb_uid()));
		reqEntity.addPart("fb_access_token",
				new StringBody(this.fbDetails.getAccess_token()));
		reqEntity.addPart(
				"fb_access_token_expdate",
				new StringBody(String.valueOf(this.fbDetails
						.getAccess_token_expdate())));
		postRequest.setEntity(reqEntity);
	}

	private byte[] downloadFile(Callback<Void, Object, Void> progressCallback,
			HttpURLConnection connection) throws IOException {
		int imageSize = connection.getContentLength();
		// Sometimes the size isn't known: just read the stream.
		if (imageSize <= 0) {
			ByteArrayBuffer buf = new ByteArrayBuffer(1024);
			int b;
			while ((b = connection.getInputStream().read()) > -1) {
				buf.append(b);
			}
			return buf.toByteArray();
		}
		// When it is known, read the stream and public progress.
		progressCallback.onProgress(Integer.valueOf(2));
		InputStream input = connection.getInputStream();
		final int kBufferSize = 1024;
		byte[] buffer = new byte[imageSize];
		int downloaded = 0;
		int bytesRead = 0;
		while (bytesRead > -1) {
			int remaining = imageSize - downloaded;
			int toReadThisIteration = Math.min(remaining, kBufferSize);
			bytesRead = input.read(buffer, downloaded, toReadThisIteration);
			if (bytesRead >= 0) {
				downloaded += bytesRead;
				progressCallback.onProgress(100 * downloaded / imageSize);
			}
		}
		return buffer;
	}

	/**
	 * Gets a NewChoosiePostData object, and creates an HTML POST request with
	 * the data.
	 * 
	 * @param data
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public CustomMultiPartEntity createMultipartContent(
			NewChoosiePostData data,
			final Callback<Void, Integer, Void> progressCallback)
			throws UnsupportedEncodingException {
		ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();

		data.image1.compress(CompressFormat.JPEG, 20, bos1);
		data.image2.compress(CompressFormat.JPEG, 20, bos2);
		byte[] data1 = bos1.toByteArray();
		byte[] data2 = bos2.toByteArray();

		ByteArrayBody bab1 = new ByteArrayBody(data1, "photo1.jpg");
		ByteArrayBody bab2 = new ByteArrayBody(data2, "photo2.jpg");

		CustomMultiPartEntity multipartContent = new CustomMultiPartEntity(
				new Callback<Void, Integer, Void>() {
					@Override
					void onProgress(Integer param) {
						progressCallback.onProgress(param);
					}
				});
		// new ProgressListener() {
		// public void transferred(long param) {
		// progressCallback
		// .onProgress((int)param);//(int) (param / (float) 100) * 100);
		// }
		// });
		multipartContent.addPart("photo1", bab1);
		multipartContent.addPart("photo2", bab2);
		multipartContent.addPart("question", new StringBody(data.question));
		multipartContent.addPart("fb_uid",
				new StringBody(this.fbDetails.getFb_uid()));

		return multipartContent;
	}

	private AsyncTask<HttpPost, Integer, HttpResponse> createExecuteHttpPostTask(
			final NewChoosiePostData data, final HttpClient httpClient,
			final Callback<Void, Integer, Void> callback) {
		AsyncTask<HttpPost, Integer, HttpResponse> executeHttpPostTask = new AsyncTask<HttpPost, Integer, HttpResponse>() {
			Long totalSize;

			@Override
			protected void onPreExecute() {
				callback.onPre(null);
			}

			@Override
			protected HttpResponse doInBackground(HttpPost... arg0) {
				CustomMultiPartEntity multipartContent = null;
				HttpPost httpPost = new HttpPost(
						Constants.URIs.NEW_POSTS_URI);
				try {
					multipartContent = createMultipartContent(data,
							new Callback<Void, Integer, Void>() {
								@Override
								void onProgress(Integer param) {
									publishProgress((int) ((param / (float) totalSize) * 100));
								}
							});
					totalSize = multipartContent.getContentLength();
					httpPost.setEntity(multipartContent);
					return httpClient.execute(httpPost);
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
				callback.onProgress(progress[0]);
			}

			@Override
			protected void onPostExecute(HttpResponse httpResponse) {
				// TODO: check response
				callback.onFinish(null);
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
			final Callback<Void, Void, List<ChoosiePostData>> callback) {
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

					callback.onFinish(choosiePostsFromFeed);
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
		JSONObject feedJsonObject = new JSONObject(jsonString);
		JSONArray jsonPostsArray = feedJsonObject.getJSONArray("feed");
		List<ChoosiePostData> choosiePostsFromFeed = new ArrayList<Client.ChoosiePostData>();
		for (int i = 0; i < jsonPostsArray.length(); i++) {
			try {
				JSONObject singleItemJsonObject = jsonPostsArray
						.getJSONObject(i);
				ChoosiePostData postData = new ChoosiePostData();
				postData.photo1URL = Constants.URIs.ROOT_URL
						+ singleItemJsonObject.getString("photo1");
				postData.photo2URL = Constants.URIs.ROOT_URL
						+ singleItemJsonObject.getString("photo2");
				postData.question = singleItemJsonObject.getString("question");
				postData.votes1 = singleItemJsonObject.getInt("votes1");
				postData.votes2 = singleItemJsonObject.getInt("votes2");
				JSONObject userJsonObject = singleItemJsonObject
						.getJSONObject("user");
				postData.userName = userJsonObject.getString("first_name")
						+ " " + userJsonObject.getString("last_name");
				postData.userPhotoURL = userJsonObject.getString("avatar");
				choosiePostsFromFeed.add(postData);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return choosiePostsFromFeed;
	}

	public void sendVoteToServer(ChoosiePostData choosiePost, int whichPhoto,
			final Callback<Void, Void, Boolean> callback) {
		final HttpUriRequest postRequest;
		// try {
		postRequest = new HttpGet(Constants.URIs.NEW_VOTE_URI
				+ "?which_photo=" + Integer.toString(whichPhoto) + "&post_key="
				+ choosiePost.getKey() + "&fb_uid="
				+ this.fbDetails.getFb_uid());
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
				callback.onFinish(result);
			}
		};

		postVoteTask.execute();

	}

	// TODO currently we're doing 'GET' requests to do voting. Not good.
	private HttpPost createVoteHttpPostRequest(ChoosiePostData choosiePost,
			int whichPhoto) throws UnsupportedEncodingException {
		HttpPost postRequest;
		postRequest = new HttpPost(Constants.URIs.NEW_VOTE_URI);

		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		reqEntity.addPart("which_photo",
				new StringBody(Integer.toString(whichPhoto)));
		reqEntity.addPart("post_key", new StringBody(choosiePost.getKey()));

		postRequest.setEntity(reqEntity);
		return postRequest;
	}

}
