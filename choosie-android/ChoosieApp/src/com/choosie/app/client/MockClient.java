package com.choosie.app.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.choosie.app.Callback;
import com.choosie.app.Logger;
import com.choosie.app.NewChoosiePostData;
import com.choosie.app.controllers.FeedCacheKey;
import com.choosie.app.Models.ChoosiePostData;
import com.choosie.app.Models.Comment;
import com.choosie.app.Models.FacebookDetails;
import com.choosie.app.Models.User;
import com.choosie.app.Models.Vote;

public class MockClient extends Client {

	protected MockClient() {
	}
	
	@Override
	public void sendCommentToServer(String post_key, String text,
			Callback<Void, Void, Boolean> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendVoteToServer(ChoosiePostData choosiePost, int whichPhoto,
			Callback<Void, Void, Boolean> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void login(Callback<Void, Void, Void> onLoginComplete) {
		// TODO Auto-generated method stub

	}

	@Override
	public FeedResponse getFeedByCursor(FeedCacheKey feedRequest,
			Callback<Void, Object, Void> progressCallback) {
		String cursor = feedRequest.getCursor();
		if (cursor == null) {
			cursor = "";
		}

		Logger.i("Got feed request. Cursor = " + cursor);

		List<ChoosiePostData> mockPosts = new ArrayList<ChoosiePostData>();

		for (int i = 0 + 1 * cursor.length(); i < 1 + 1 * cursor.length(); ++i) {
			mockPosts.add(getMockPost(i));
		}

		FeedResponse response = new FeedResponse(feedRequest.isAppend(), cursor
				+ "a", mockPosts);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return response;
	}

	private ChoosiePostData getMockPost(int i) {
		String postKey = Integer.toString(i);
		String question = "Question number " + Integer.toString(i + 1);
		String photo1URL = "http://wiki.eternal-wow.com/images/3/3b/Kitty-kitties-9109284-500-460.jpg";
		String photo2URL = "http://www.pupmart.co.uk/wp-content/uploads/2011/10/puppies-rehoming.jpg";
		String userName = "Choosie McChoose";
		String fbUID = "77345345";
		String userPhotoUrl = "http://graph.facebook.com/jonathan.erez/picture";

		ChoosiePostData mockPost = new ChoosiePostData(fbDetails, postKey,
				photo1URL, photo2URL, question, new User(userName,
						userPhotoUrl, fbUID), new Date(),
				new ArrayList<Vote>(), new ArrayList<Comment>());
		return mockPost;
	}

	@Override
	public ChoosiePostData getPostByKey(String param,
			Callback<Void, Object, Void> progressCallback) {
		return getMockPost(0);
	}

	@Override
	public void sendChoosiePostToServer(NewChoosiePostData data,
			Callback<Void, Integer, Void> progressCallback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerGCM(String deviceId) {
		// TODO Auto-generated method stub
		
	}

}
