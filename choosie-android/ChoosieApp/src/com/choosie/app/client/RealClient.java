package com.choosie.app.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
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

import com.choosie.app.Callback;
import com.choosie.app.Comment;
import com.choosie.app.Constants;
import com.choosie.app.CustomMultiPartEntity;
import com.choosie.app.FacebookDetails;
import com.choosie.app.NewChoosiePostData;
import com.choosie.app.Utils;
import com.choosie.app.Vote;
import com.choosie.app.Models.ChoosiePostData;
import com.choosie.app.controllers.FeedCacheKey;

import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;

public class RealClient extends ClientBase {

	public RealClient(FacebookDetails fbDetails) {
		super(fbDetails);
	}

	/**
	 * Gets a ChoosiePost (photo1, photo2 and a question) and posts it to the
	 * server.
	 * 
	 * @param data
	 * @param progressCallback
	 */
	@Override
	public void sendChoosiePostToServer(NewChoosiePostData data,
			Callback<Void, Integer, Void> progressCallback) {
		final HttpClient httpClient = new DefaultHttpClient();

		AsyncTask<HttpPost, Integer, HttpResponse> executePostTask = createExecuteHttpPostTask(
				data, httpClient, progressCallback);

		executePostTask.execute((HttpPost) null);
	}

	@Override
	public FeedResponse getFeedByCursor(FeedCacheKey feedRequest,
			Callback<Void, Object, Void> progressCallback) {
		// Creates the GET HTTP request
		String feedUri = Constants.URIs.FEED_URI + "?limit=8";
		if (feedRequest.getCursor() != null && feedRequest.isAppend()) {
			feedUri += "&cursor=" + feedRequest.getCursor();
		}

		Log.i(Constants.LOG_TAG, "Getting feed from URI: " + feedUri);
		final HttpClient client = new DefaultHttpClient();
		final HttpGet request = new HttpGet(feedUri);
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		String jsonString;
		try {
			jsonString = EntityUtils.toString(response.getEntity());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		FeedResponse choosiePostsFromFeed;
		try {
			choosiePostsFromFeed = convertJsonToChoosiePosts(jsonString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		Log.i(Constants.LOG_TAG, "Feed converted to posts, and got cursor.");
		choosiePostsFromFeed.setAppend(feedRequest.isAppend());
		return choosiePostsFromFeed;
	}

		@Override
	public void login(final Callback<Void, Void, Void> onLoginComplete) {
		final HttpClient httpClient = new DefaultHttpClient();
		final HttpPost postRequest = new HttpPost(Constants.URIs.ROOT_URL
				+ "/login");

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

		data.getImage1().compress(CompressFormat.JPEG, 20, bos1);
		data.getImage2().compress(CompressFormat.JPEG, 20, bos2);
		byte[] data1 = bos1.toByteArray();
		byte[] data2 = bos2.toByteArray();

		ByteArrayBody bab1 = new ByteArrayBody(data1, "photo1.jpg");
		ByteArrayBody bab2 = new ByteArrayBody(data2, "photo2.jpg");

		CustomMultiPartEntity multipartContent = new CustomMultiPartEntity(
				new Callback<Void, Integer, Void>() {
					public void onProgress(Integer param) {
						progressCallback.onProgress(param);
					}
				});
		multipartContent.addPart("photo1", bab1);
		multipartContent.addPart("photo2", bab2);
		multipartContent
				.addPart("question", new StringBody(data.getQuestion()));
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
				HttpPost httpPost = new HttpPost(Constants.URIs.NEW_POSTS_URI);
				try {
					multipartContent = createMultipartContent(data,
							new Callback<Void, Integer, Void>() {
								@Override
								public void onProgress(Integer param) {
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
	 * Takes a JSON string, and builds ChoosiePostData object from it.
	 * 
	 * @param jsonString
	 * @return
	 * @throws JSONException
	 */
	private FeedResponse convertJsonToChoosiePosts(String jsonString)
			throws JSONException {
		JSONObject feedJsonObject = new JSONObject(jsonString);
		JSONArray jsonPostsArray = feedJsonObject.getJSONArray("feed");
		String cursor = (feedJsonObject.getString("cursor"));
		List<ChoosiePostData> choosiePostsFromFeed = new ArrayList<ChoosiePostData>();
		for (int i = 0; i < jsonPostsArray.length(); i++) {
			try {
				JSONObject singleItemJsonObject = jsonPostsArray
						.getJSONObject(i);
				ChoosiePostData postData = new ChoosiePostData(this.fbDetails);
				postData.setPhoto1URL(Constants.URIs.ROOT_URL
						+ singleItemJsonObject.getString("photo1"));
				postData.setPhoto2URL(Constants.URIs.ROOT_URL
						+ singleItemJsonObject.getString("photo2"));
				postData.setQuestion(singleItemJsonObject.getString("question"));
				postData.setVotes1(singleItemJsonObject.getInt("votes1"));
				postData.setVotes2(singleItemJsonObject.getInt("votes2"));

				JSONObject userJsonObject = singleItemJsonObject
						.getJSONObject("user");
				postData.setUserName(userJsonObject.getString("first_name")
						+ " " + userJsonObject.getString("last_name"));
				postData.setUserPhotoURL(userJsonObject.getString("avatar"));
				postData.setUser_fb_uid(userJsonObject.getString("fb_uid"));

				JSONArray allVotes = singleItemJsonObject.getJSONArray("votes");
				for (int j = 0; j < allVotes.length(); j++) {
					JSONObject jsonVoteObject = allVotes.getJSONObject(j);

					String fb_uid = jsonVoteObject.getString("fb_uid");
					String date = jsonVoteObject.getString("created_at");
					Date created_at = Utils.getInstance().ConvertStringToDate(
							date);
					int vote_for = jsonVoteObject.getInt("vote_for");

					Vote vote = new Vote(fb_uid, created_at, vote_for);
					postData.getLstVotes().add(vote);
				}
				
				JSONArray allComments = singleItemJsonObject.getJSONArray("comments");
				for (int j = 0; j < allComments.length(); j++) {
					JSONObject jsonCommentObject = allComments.getJSONObject(j);

					String fb_uid = jsonCommentObject.getString("fb_uid");
					String date = jsonCommentObject.getString("created_at");
					Date created_at = Utils.getInstance().ConvertStringToDate(
							date);
					String text = jsonCommentObject.getString("text");

					Comment comment = new Comment(fb_uid, created_at, text, postData.getKey());
					postData.getLstComment().add(comment);
				}


				choosiePostsFromFeed.add(postData);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FeedResponse result = new FeedResponse(true, cursor, choosiePostsFromFeed);
		return result;
	}

	@Override
	public void sendVoteToServer(ChoosiePostData choosiePost, int whichPhoto,
			final Callback<Void, Void, Boolean> callback) {
		final HttpUriRequest postRequest;
		// try {
		postRequest = new HttpGet(Constants.URIs.NEW_VOTE_URI + "?which_photo="
				+ Integer.toString(whichPhoto) + "&post_key="
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

	@Override
	public void sendCommentToServer(String post_key, String text,
			final Callback<Void, Void, Boolean> callback) {

		Comment commentToSend = new Comment(this.fbDetails.getFb_uid(), null,
				text, post_key);
		final HttpPost postRequest;
		postRequest = createNewCommentPostRequest(commentToSend);

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

	private HttpPost createNewCommentPostRequest(Comment comment) {
		HttpPost commentPostRequest = new HttpPost(
				Constants.URIs.NEW_COMMENT_URI);

		MultipartEntity multipartContent = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			multipartContent.addPart("fb_uid",
					new StringBody(this.fbDetails.getFb_uid()));
			multipartContent.addPart("text", new StringBody(comment.getText().toString()));
			multipartContent.addPart("post_key",
					new StringBody(comment.getPost_key()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		commentPostRequest.setEntity(multipartContent);

		return commentPostRequest;
	}
}
