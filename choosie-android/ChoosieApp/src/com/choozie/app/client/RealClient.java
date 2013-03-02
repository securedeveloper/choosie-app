package com.choozie.app.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.choozie.app.Callback;
import com.choozie.app.Constants;
import com.choozie.app.CustomMultiPartEntity;
import com.choozie.app.L;
import com.choozie.app.NewChoosiePostData;
import com.choozie.app.Utils;
import com.choozie.app.NewChoosiePostData.PostType;
import com.choozie.app.controllers.FeedCacheKey;
import com.choozie.app.models.ChoosiePostData;
import com.choozie.app.models.Comment;
import com.choozie.app.models.User;
import com.choozie.app.models.UserDetails;
import com.choozie.app.models.Vote;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;

public class RealClient extends Client {

	protected RealClient() {
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
			Callback<Void, Integer, Void> progressCallback) {
		// Creates the GET HTTP request
		String feedUri = Constants.URIs.FEED_URI + "?limit=8";
		if (feedRequest.getCursor() != null && feedRequest.isAppend()) {
			feedUri += "&cursor=" + feedRequest.getCursor();
		}

		if (feedRequest.getFbUid() != null) {
			feedUri += "&fb_uid=" + feedRequest.getFbUid();
		}

		L.i("Getting feed from URI: " + feedUri);
		final HttpClient client = new DefaultHttpClient();
		final HttpGet request = new HttpGet(feedUri);

		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			L.e("getFeedByCursor",
					"ClientProtocolException failed to get response");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			L.e("getFeedByCursor", "IOException failed to get response");
			e.printStackTrace();
			return null;
		}
		String jsonString;
		try {
			jsonString = EntityUtils.toString(response.getEntity());
		} catch (ParseException e) {
			L.e("getFeedByCursor", "ParseException - failed");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			L.e("getFeedByCursor", "IOException - failed");
			e.printStackTrace();
			return null;
		}

		FeedResponse choosiePostsFromFeed;
		try {
			choosiePostsFromFeed = convertJsonToChoosiePosts(jsonString);
		} catch (JSONException e) {
			L.e("choosiePostsFromFeed", "convertJsonToChoosiePosts failed");
			e.printStackTrace();
			return null;
		}

		L.i("Feed converted to posts, and got cursor.");
		choosiePostsFromFeed.setAppend(feedRequest.isAppend());
		return choosiePostsFromFeed;
	}

	@Override
	public ChoosiePostData getPostByKey(String postKey,
			Callback<Void, Integer, Void> progressCallback) {
		// Creates the GET HTTP request
		String postUri = Constants.URIs.POSTS_URI + "/" + postKey;

		L.i("Getting post from URI: " + postUri);

		// enable on server hebrew sync
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		params.setBooleanParameter("http.protocol.expect-continue", false);

		final HttpClient client = new DefaultHttpClient(params);
		final HttpGet request = new HttpGet(postUri);
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			L.e("getPostByKey",
					"ClientProtocolException - failed to get response");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			L.e("getPostByKey", "IOException - failed to get response");
			e.printStackTrace();
			return null;
		}
		String jsonString;
		try {
			jsonString = EntityUtils.toString(response.getEntity());
		} catch (ParseException e) {
			L.e("getPostByKey", "ParseException - failed toString Json");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			L.e("getPostByKey", "IOException - failed toString Json");
			e.printStackTrace();
			return null;
		}

		ChoosiePostData choosiePostFromResponse;
		try {
			choosiePostFromResponse = convertJsonToChoosiePost(jsonString);
		} catch (JSONException e) {
			L.e("getPostByKey",
					"JSONException - failed convertJsonToChoosiePost");
			e.printStackTrace();
			return null;
		}

		L.i("Response converted to post.");
		return choosiePostFromResponse;
	}

	@Override
	public void login(final Callback<Void, Void, Void> onLoginComplete) {
		final HttpClient httpClient = new DefaultHttpClient();
		final HttpPost postRequest = new HttpPost(Constants.URIs.ROOT_URL
				+ "/login");

		try {
			createLoginPostRequest(postRequest);
		} catch (UnsupportedEncodingException e) {
			L.e("login",
					"UnsupportedEncodingException - failed createLoginPostRequest");
			e.printStackTrace();
			return;
		}

		AsyncTask<Void, Void, Void> loginTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					httpClient.execute(postRequest);
				} catch (ClientProtocolException e) {
					L.e("login",
							"ClientProtocolException - failed execute postRequest");
					e.printStackTrace();
				} catch (IOException e) {
					L.e("login", "IOException - failed execute postRequest");
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

		reqEntity.addPart("fb_uid", new StringBody(this.getFacebookDetailsOfLoogedInUser()
				.getFb_uid()));
		reqEntity.addPart("fb_access_token", new StringBody(this
				.getFacebookDetailsOfLoogedInUser().getAccess_token()));
		reqEntity.addPart(
				"fb_access_token_expdate",
				new StringBody(String.valueOf(this.getFacebookDetailsOfLoogedInUser()
						.getAccess_token_expdate())));
		postRequest.setEntity(reqEntity);
	}

	private void createRegisterRequest(HttpPost postRequest, String deviceId)
			throws UnsupportedEncodingException {
		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		reqEntity.addPart("fb_uid", new StringBody(this.getFacebookDetailsOfLoogedInUser()
				.getFb_uid()));
		reqEntity.addPart("device_id", new StringBody(deviceId));
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
		CustomMultiPartEntity multipartContent = new CustomMultiPartEntity(
				new Callback<Void, Integer, Void>() {
					public void onProgress(Integer param) {
						progressCallback.onProgress(param);
					}
				});
		addImageToMultiPartEntity(multipartContent, data.getImage1(), "photo1");
		if (data.getPostType() == PostType.TOT) {
			addImageToMultiPartEntity(multipartContent, data.getImage2(),
					"photo2");
		}
		multipartContent.addPart("question", new StringBody(data.getQuestion(),
				Charset.forName("UTF-8")));
		multipartContent.addPart("fb_uid", new StringBody(this
				.getFacebookDetailsOfLoogedInUser().getFb_uid()));
		multipartContent.addPart("post_type_id",
				new StringBody(data.getPostTypeAsString()));

		// Add share on facebook details to server HTTP request
		if (data.isShareOnFacebook()) {
			multipartContent.addPart("share_to_fb", new StringBody("on"));
			multipartContent.addPart("fb_access_token", new StringBody(this
					.getFacebookDetailsOfLoogedInUser().getAccess_token()));
			multipartContent.addPart(
					"fb_access_token_expdate",
					new StringBody(String.valueOf(this.getFacebookDetailsOfLoogedInUser()
							.getAccess_token_expdate())));

			L.i("share_to_fb = on");
			L.i("fb_access_token = "
					+ this.getFacebookDetailsOfLoogedInUser().getAccess_token());
			L.i("fb_access_token_expdate = "
					+ String.valueOf(this.getFacebookDetailsOfLoogedInUser()
							.getAccess_token_expdate()));
		}

		L.i("finished building multipart content");

		return multipartContent;
	}

	private void addImageToMultiPartEntity(
			CustomMultiPartEntity multipartContent, Bitmap img, String partName) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		img.compress(CompressFormat.JPEG, 80, bos);
		byte[] data = bos.toByteArray();
		ByteArrayBody bab = new ByteArrayBody(data, partName + ".jpg");
		multipartContent.addPart(partName, bab);
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
					L.i("doInBackground(): Executing HTTP Request!");
					return httpClient.execute(httpPost);
				} catch (ClientProtocolException e) {
					L.e("createExecuteHttpPostTask",
							"ClientProtocolException failed execute postRequest");
					e.printStackTrace();
				} catch (IOException e) {
					L.e("createExecuteHttpPostTask",
							"IOException - failed execute postRequest");
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

				ChoosiePostData postData = buildChoosiePostFromJsonObject(singleItemJsonObject);

				choosiePostsFromFeed.add(postData);

			} catch (JSONException e) {
				L.e("convertJsonToChoosiePosts", "JSONException failed");
				e.printStackTrace();
			}
		}
		FeedResponse result = new FeedResponse(true, cursor,
				choosiePostsFromFeed);
		return result;
	}

	private ChoosiePostData convertJsonToChoosiePost(String jsonString)
			throws JSONException {
		JSONObject postJsonObject = new JSONObject(jsonString);
		return buildChoosiePostFromJsonObject(postJsonObject);
	}

	private ChoosiePostData buildChoosiePostFromJsonObject(JSONObject jsonObject)
			throws JSONException {
		String photo1URL = Constants.URIs.ROOT_URL
				+ jsonObject.getString("photo1");
		String photo2URL = Constants.URIs.ROOT_URL
				+ jsonObject.getString("photo2");
		String question = jsonObject.getString("question");
		String postKey = jsonObject.getString("key");
		PostType postType;
		if (jsonObject.has("post_type")) {
			int postTypeId = jsonObject.getInt("post_type");
			postType = postTypeId == 1 ? PostType.TOT : PostType.YesNo;
		} else {
			postType = PostType.TOT;
		}
		Date createdAtUTC = Utils.convertStringToDateUTC(jsonObject
				.getString("created_at"));

		JSONObject userJsonObject = jsonObject.getJSONObject("user");
		User author = buildUserFromJson(userJsonObject);

		JSONArray jsonVotes = jsonObject.getJSONArray("votes");
		List<Vote> votes = buildVotesFromJson(jsonVotes);

		JSONArray allComments = jsonObject.getJSONArray("comments");
		List<Comment> comments = buildCommentsFromJson(postKey, allComments);

		return new ChoosiePostData(postKey, photo1URL, photo2URL, question,
				author, createdAtUTC, votes, comments, postType);
	}

	private User buildUserFromJson(JSONObject userJsonObject)
			throws JSONException {
		String authorName = userJsonObject.getString("first_name") + " "
				+ userJsonObject.getString("last_name");
		String authorPhotoURL = userJsonObject.getString("avatar");
		String authorFBUid = userJsonObject.getString("fb_uid");
		User author = new User(authorName, authorPhotoURL, authorFBUid);
		return author;
	}

	private List<Comment> buildCommentsFromJson(String postKey,
			JSONArray allComments) throws JSONException {
		List<Comment> comments = new ArrayList<Comment>();
		for (int j = 0; j < allComments.length(); j++) {
			JSONObject jsonCommentObject = allComments.getJSONObject(j);

			String date = jsonCommentObject.getString("created_at");
			Date createdAtUTC = Utils.convertStringToDateUTC(date);
			String text = jsonCommentObject.getString("text");

			JSONObject userJsonObject = jsonCommentObject.getJSONObject("user");
			User user = buildUserFromJson(userJsonObject);

			Comment comment = new Comment(createdAtUTC, text, postKey, user);
			comments.add(comment);
		}
		return comments;
	}

	private List<Vote> buildVotesFromJson(JSONArray jsonVotes)
			throws JSONException {
		List<Vote> votes = new ArrayList<Vote>();
		for (int j = 0; j < jsonVotes.length(); j++) {
			JSONObject jsonVoteObject = jsonVotes.getJSONObject(j);

			String date = jsonVoteObject.getString("created_at");
			Date createdAtUTC = Utils.convertStringToDateUTC(date);
			int vote_for = jsonVoteObject.getInt("vote_for");
			JSONObject userJsonObject = jsonVoteObject.getJSONObject("user");
			User user = buildUserFromJson(userJsonObject);

			Vote vote = new Vote(createdAtUTC, vote_for, user);
			votes.add(vote);
		}
		return votes;
	}

	@Override
	public void sendVoteToServer(String postKey, int whichPhoto,
			final Callback<Void, Void, Boolean> callback) {
		// TODO: Change to POST request
		final HttpUriRequest getRequest;
		// try {
		getRequest = new HttpGet(Constants.URIs.NEW_VOTE_URI + "?which_photo="
				+ Integer.toString(whichPhoto) + "&post_key=" + postKey
				+ "&fb_uid=" + this.getFacebookDetailsOfLoogedInUser().getFb_uid());
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
					HttpResponse response = httpClient.execute(getRequest);
					int i = 9;

				} catch (ClientProtocolException e) {
					L.e("sendVoteToServer",
							"ClientProtocolException failed execute");
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					L.e("sendVoteToServer", "IOException failed execute");
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
		reqEntity.addPart("post_key", new StringBody(choosiePost.getPostKey()));

		postRequest.setEntity(reqEntity);
		return postRequest;
	}

	@Override
	public void sendCommentToServer(String post_key, String text,
			final Callback<Void, Void, Boolean> callback) {

		Comment commentToSend = new Comment(null, text, post_key, null);
		final HttpPost postRequest;
		postRequest = createNewCommentPostRequest(commentToSend);

		// enable on server hebrew sync
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		params.setBooleanParameter("http.protocol.expect-continue", false);

		final HttpClient httpClient = new DefaultHttpClient(params);
		AsyncTask<Void, Void, Boolean> postVoteTask = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					httpClient.execute(postRequest);
				} catch (ClientProtocolException e) {
					L.e("sendCommentToServer",
							"ClientProtocolException - failed execute");
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					L.e("sendCommentToServer", "IOException - failed execute");
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
			multipartContent.addPart("fb_uid", new StringBody(this
					.getFacebookDetailsOfLoogedInUser().getFb_uid()));
			multipartContent.addPart("text", new StringBody(comment.getText()
					.toString(), Charset.forName("UTF-8")));
			multipartContent.addPart("post_key",
					new StringBody(comment.getPost_key()));
		} catch (UnsupportedEncodingException e) {
			L.e("createNewCommentPostRequest",
					"UnsupportedEncodingException - failed");
			e.printStackTrace();
		}

		commentPostRequest.setEntity(multipartContent);

		return commentPostRequest;
	}

	@Override
	public void registerGCM(String deviceId) {

		final HttpClient httpClient = new DefaultHttpClient();
		final HttpPost postRequest = new HttpPost(Constants.URIs.REGISTER);

		try {
			createRegisterRequest(postRequest, deviceId);
		} catch (UnsupportedEncodingException e) {
			L.e("login",
					"UnsupportedEncodingException - failed createLoginPostRequest");
			e.printStackTrace();
			return;
		}

		AsyncTask<Void, Void, HttpResponse> registerTask = new AsyncTask<Void, Void, HttpResponse>() {

			@Override
			protected HttpResponse doInBackground(Void... params) {
				try {
					return httpClient.execute(postRequest);
				} catch (ClientProtocolException e) {
					L.e("Register",
							"ClientProtocolException - failed execute registerReq");
					e.printStackTrace();
				} catch (IOException e) {
					L.e("login", "IOException - failed execute registerReq");
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(HttpResponse result) {
				// TODO: chack result or something
				L.i("Register", "result status = " + result.getStatusLine());
			}
		};
		registerTask.execute();
	}

	@Override
	public void unregisterGCM(String deviceId) {
		// TODO Auto-generated method stub

	}

	public User getActiveUser() {
		String firstName = this.getFacebookDetailsOfLoogedInUser().getFirstName();
		String lastName = this.getFacebookDetailsOfLoogedInUser().getLastName();
		String userName = firstName + " " + lastName;

		// TODO: get photo URL of the active user.
		String photoURL = Constants.URIs.FACEBOOK_PROFILE_PIC(this
				.getFacebookDetailsOfLoogedInUser().getFb_uid());
		String fbUid = this.getFacebookDetailsOfLoogedInUser().getFb_uid();

		return new User(userName, photoURL, fbUid);
	}

	@Override
	public void getUserDetailsFromServer(final User user,
			final Callback<Void, Void, UserDetails> callback) {

		AsyncTask<Void, Void, String> getUserDetailsTask = new AsyncTask<Void, Void, String>() {

			@Override
			protected void onPreExecute() {
				callback.onPre(null);
			}

			@Override
			protected String doInBackground(Void... p) {
				callback.onProgress(null);

				// create the GET request
				String uri = Constants.URIs.USER + "/" + user.getFbUid();
				final HttpGet httpGetRequest = new HttpGet(uri);

				// enable on server hebrew sync
				final HttpParams params = new BasicHttpParams();
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(params, "UTF-8");
				params.setBooleanParameter("http.protocol.expect-continue",
						false);

				final HttpClient client = new DefaultHttpClient(params);
				HttpResponse response = null;
				String responseString = "";
				try {
					L.i("Executing GET request : " + httpGetRequest.getURI());
					response = client.execute(httpGetRequest);
					responseString = EntityUtils.toString(response.getEntity());
					L.i("responseString = " + responseString);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return responseString;
			}

			protected void onPostExecute(String response) {
				callback.onFinish(parseResponseUserDetails(user, response));
			}
		};
		getUserDetailsTask.execute();
	}

	private UserDetails parseResponseUserDetails(User user, String response) {

		UserDetails ud = new UserDetails(user);
		if (response != null) {
			try {
				L.i("Creating JSON from " + response);
				JSONObject json = new JSONObject(response);

				ud.setNickname(json.getString("nick").equals("null") ? ""
						: json.getString("nick"));
				ud.setInfo(json.getString("info").equals("null") ? "" : json
						.getString("info"));
				ud.setNumPosts(json.getInt("num_posts"));
				ud.setNumVotes(json.getInt("num_votes"));

				// String d = json.getString("created_at");
				// Date date = Utils.convertStringToDateUTC(d);
				// L.i("bla " + date.toString());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ud;
	}

	public void updateUserDetailsInfo(final UserDetails ud,
			Callback<Void, Void, Void> callback) {
		L.i("updateUserDetailsInfo() - Start creating the HTTP request");

		HttpPost postRequest = createUpdateUserDetailsRequest(ud);

		executeUpdateUserDetailsRequest(postRequest, callback);

	}

	private HttpPost createUpdateUserDetailsRequest(UserDetails ud) {
		HttpPost postRequest = new HttpPost(
				Constants.URIs.EDIT_USER_DETAILS_URI + "/"
						+ ud.getUser().getFbUid());
		L.i("Created POST request with URI: \"" + postRequest.getURI() + "\"");

		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		try {
			entity.addPart("nick", new StringBody(ud.getNickname()));
			entity.addPart("info", new StringBody(ud.getInfo()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		postRequest.setEntity(entity);
		L.i("Update user details POST request = \"" + postRequest.getURI()
				+ "\"");
		return postRequest;
	}

	private void executeUpdateUserDetailsRequest(final HttpPost postRequest,
			final Callback<Void, Void, Void> callback) {
		AsyncTask<Void, Void, HttpResponse> updateDetailsPostRequest = new AsyncTask<Void, Void, HttpResponse>() {

			@Override
			protected void onPreExecute() {
				callback.onPre(null);
			}

			@Override
			protected HttpResponse doInBackground(Void... params) {
				HttpClient client = new DefaultHttpClient();
				HttpResponse response = null;
				L.i("Created the DefaultHttpClient()");

				// execute the POST request
				try {
					response = client.execute(postRequest);
					L.i("Executed the POST request!");
				} catch (Exception e) {
					// change return code to FALSE
					L.e("updateUserDetailsInfo()",
							"Failed executing the POST request in updateUserDetailsInfo()");
					e.printStackTrace();
				}
				return response;
			}

			@Override
			protected void onProgressUpdate(Void... values) {
				callback.onProgress(null);
			}

			@Override
			protected void onPostExecute(HttpResponse result) {
				callback.onFinish(null);
			}
		};
		updateDetailsPostRequest.execute();
	}
}
