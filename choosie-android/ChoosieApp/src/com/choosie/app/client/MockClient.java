package com.choosie.app.client;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.util.Log;

import com.choosie.app.Callback;
import com.choosie.app.Constants;
import com.choosie.app.FacebookDetails;
import com.choosie.app.NewChoosiePostData;
import com.choosie.app.Vote;
import com.choosie.app.Models.ChoosiePostData;
import com.choosie.app.controllers.FeedCacheKey;

public class MockClient extends ClientBase {

	public MockClient(FacebookDetails fbDetails) {
		super(fbDetails);
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
		
		Log.i(Constants.LOG_TAG, "Got feed request. Cursor = " + cursor);
		
		List<ChoosiePostData> mockPosts = new ArrayList<ChoosiePostData>();

		for (int i = 0 + 1 * cursor.length(); i < 1 + 1 * cursor.length(); ++i) {
			ChoosiePostData mockPost = new ChoosiePostData(fbDetails);
			mockPost.setQuestion("Question number " + Integer.toString(i + 1));
			mockPost.setPhoto1URL("http://wiki.eternal-wow.com/images/3/3b/Kitty-kitties-9109284-500-460.jpg");
			mockPost.setPhoto2URL("http://www.pupmart.co.uk/wp-content/uploads/2011/10/puppies-rehoming.jpg");
			mockPost.setUserName("Choosie McChoose");
			mockPost.setUser_fb_uid("77345345");
			mockPost.setUserPhotoURL("http://graph.facebook.com/jonathan.erez/picture");
			mockPost.setLstVotes(new ArrayList<Vote>());
			mockPosts.add(mockPost);
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

	@Override
	public void sendChoosiePostToServer(NewChoosiePostData data,
			Callback<Void, Integer, Void> progressCallback) {
		// TODO Auto-generated method stub

	}

}
