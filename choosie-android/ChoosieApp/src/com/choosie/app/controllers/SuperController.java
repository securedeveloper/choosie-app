package com.choosie.app.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.choosie.app.Callback;
import com.choosie.app.ChoosieActivity;
import com.choosie.app.CommentScreenActivity;
import com.choosie.app.Constants;
import com.choosie.app.IntentData;
import com.choosie.app.Logger;
import com.choosie.app.VotePopupWindowUtils;
import com.choosie.app.EnlargePhotoActivity;
import com.choosie.app.R;
import com.choosie.app.Screen;
import com.choosie.app.Utils;
import com.choosie.app.VotesScreenActivity;
import com.choosie.app.caches.CacheCallback;
import com.choosie.app.caches.Caches;
import com.choosie.app.client.Client;
import com.choosie.app.Models.ChoosiePostData;
import com.choosie.app.Models.Comment;
import com.choosie.app.Models.Vote;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

public class SuperController {
	private Screen currentScreen;
	private ChoosieActivity activity;
	Map<Screen, ScreenController> screenToController;

	public SuperController(ChoosieActivity activity) {
		initializeSuperController(activity);
	}

	private void initializeSuperController(ChoosieActivity activity) {
		this.activity = activity;

		List<Pair<Screen, ScreenController>> screenControllerPairs = new ArrayList<Pair<Screen, ScreenController>>();

		screenControllerPairs.add(new Pair<Screen, ScreenController>(
				Screen.FEED, new FeedScreenController(activity
						.findViewById(R.id.layout_feed), this)));
		screenControllerPairs.add(new Pair<Screen, ScreenController>(
				Screen.POST, new PostScreenController(activity
						.findViewById(R.id.layout_post), this)));
		screenControllerPairs.add(new Pair<Screen, ScreenController>(Screen.ME,
				new MeScreenController(activity.findViewById(R.id.layout_me),
						this)));

		screenToController = new HashMap<Screen, ScreenController>();

		for (Pair<Screen, ScreenController> pair : screenControllerPairs) {
			screenToController.put(pair.first, pair.second);
		}

		for (ScreenController screen : screenToController.values()) {
			screen.onCreate();
		}

		Client.getInstance().login(new Callback<Void, Void, Void>() {
			@Override
			public void onFinish(Void param) {
			}
		});

		handleGCMRegister();

		setCurrentScreen(Screen.FEED);
	}

	public void switchToScreen(Screen screenToShow) {
		// Hide all screens except 'screen'
		for (Screen screen : screenToController.keySet()) {
			if (screen == screenToShow) {
				screenToController.get(screen).showScreen();
			} else {
				screenToController.get(screen).hideScreen();
			}
		}
	}

	public void voteFor(final ChoosiePostData post, int whichPhoto) {
		Logger.i("Issuing vote for: " + post.getPostKey());
		Tracker tracker = GoogleAnalytics.getInstance(getActivity())
				.getDefaultTracker();
		tracker.trackEvent("Ui Action", "Vote", String.valueOf(whichPhoto),
				null);
		Client.getInstance().sendVoteToServer(post, whichPhoto,
				new Callback<Void, Void, Boolean>() {

					@Override
					public void onFinish(Boolean param) {
						if (param) {
							refreshPost(post.getPostKey());
						}
					}

				});
	}

	private void refreshPost(String postKey) {
		Caches.getInstance().getPostsCache().invalidateKey(postKey);
		Caches.getInstance()
				.getPostsCache()
				.getValue(postKey,
						new CacheCallback<String, ChoosiePostData>() {
							@Override
							public void onValueReady(String key,
									ChoosiePostData result) {
								if (result == null) {
									// TODO: Handle error
									Toast.makeText(getActivity(),
											"Failed to update post.",
											Toast.LENGTH_SHORT).show();
									return;
								}
								((FeedScreenController) screenToController
										.get(Screen.FEED)).refreshPost(result);
							}
						});

	}

	public void CommentFor(final String post_key, String text) {
		Logger.i("commenting vote for: " + post_key);
		Tracker tracker = GoogleAnalytics.getInstance(getActivity())
				.getDefaultTracker();
		tracker.trackEvent("Ui Action", "comment", "text", null);
		Client.getInstance().sendCommentToServer(post_key, text,
				new Callback<Void, Void, Boolean>() {

					@Override
					public void onFinish(Boolean param) {
						if (param) {
							refreshPost(post_key);
						}
					}
				});
	}

	private void switchToCommentScreen(ChoosiePostData choosiePost,
			boolean openVotesWindow) {
		Intent intent = new Intent(screenToController.get(Screen.FEED)
				.getActivity().getApplicationContext(),
				CommentScreenActivity.class);

		intent.putExtra("post_key", choosiePost.getPostKey());
		intent.putExtra("question", choosiePost.getQuestion());

		String photo1Path = Utils.getFileNameForURL(choosiePost.getPhoto1URL());
		String photo2Path = Utils.getFileNameForURL(choosiePost.getPhoto2URL());
		String userPhotoPath = Utils.getFileNameForURL(choosiePost.getAuthor()
				.getPhotoURL());

		intent.putExtra(Constants.IntentsCodes.photo1Path, photo1Path);
		intent.putExtra(Constants.IntentsCodes.photo2Path, photo2Path);
		intent.putExtra(Constants.IntentsCodes.userPhotoPath, userPhotoPath);
		intent.putExtra(Constants.IntentsCodes.userName, choosiePost
				.getAuthor().getUserName());
		intent.putExtra(Constants.IntentsCodes.votes1, choosiePost.getVotes1());
		intent.putExtra(Constants.IntentsCodes.votes2, choosiePost.getVotes2());
		intent.putExtra(Constants.IntentsCodes.isAlreadyVoted,
				choosiePost.isVotedAlready());
		intent.putExtra(Constants.IntentsCodes.isPostByMe,
				choosiePost.isPostByMe());
		intent.putExtra(Constants.IntentsCodes.post_key,
				choosiePost.getPostKey());
		intent.putExtra(Constants.IntentsCodes.openVotesWindow, openVotesWindow);

		// create the comments list
		ArrayList<String> nameList = new ArrayList<String>();
		ArrayList<String> commentList = new ArrayList<String>();
		ArrayList<String> commentierPhotoUrlList = new ArrayList<String>();
		ArrayList<CharSequence> createdAtList = new ArrayList<CharSequence>();

		for (Comment comment : choosiePost.getComments()) {
			nameList.add(comment.getUser().getUserName());
			commentList.add(comment.getText());
			commentierPhotoUrlList.add(comment.getUser().getPhotoURL());
			createdAtList.add(Utils.getTimeDifferenceTextFromNow(comment
					.getCreatedAt()));
		}

		intent.putStringArrayListExtra(Constants.IntentsCodes.nameList,
				nameList);
		intent.putStringArrayListExtra(Constants.IntentsCodes.commentList,
				commentList);
		intent.putStringArrayListExtra(
				Constants.IntentsCodes.commentierPhotoUrlList,
				commentierPhotoUrlList);
		intent.putCharSequenceArrayListExtra(
				Constants.IntentsCodes.createdAtList, createdAtList);

		screenToController.get(Screen.FEED).getActivity()
				.startActivityForResult(intent, Constants.RequestCodes.COMMENT);
	}

	public void switchToVotesScreen(ChoosiePostData choosiePost) {
		Intent intent = new Intent(screenToController.get(Screen.FEED)
				.getActivity().getApplicationContext(),
				VotesScreenActivity.class);

		// intent.putExtra("post_key", choosiePost.getPostKey());
		intent.putExtra(Constants.IntentsCodes.question,
				choosiePost.getQuestion());

		String photo1Path = Utils.getFileNameForURL(choosiePost.getPhoto1URL());
		String photo2Path = Utils.getFileNameForURL(choosiePost.getPhoto2URL());
		String userPhotoPath = Utils.getFileNameForURL(choosiePost.getAuthor()
				.getPhotoURL());

		intent.putExtra(Constants.IntentsCodes.photo1Path, photo1Path);
		intent.putExtra(Constants.IntentsCodes.photo2Path, photo2Path);
		intent.putExtra(Constants.IntentsCodes.userPhotoPath, userPhotoPath);

		// create the votes list
		ArrayList<String> nameList = new ArrayList<String>();
		ArrayList<String> votersPhotoUrlList = new ArrayList<String>();
		ArrayList<Integer> voteForList = new ArrayList<Integer>();

		for (Vote vote : choosiePost.getVotes()) {
			nameList.add(vote.getUsers().getUserName());
			votersPhotoUrlList.add(Utils.getFileNameForURL(vote.getUsers()
					.getPhotoURL()));
			voteForList.add(vote.getVote_for());
		}

		intent.putStringArrayListExtra(Constants.IntentsCodes.nameList,
				nameList);
		intent.putStringArrayListExtra(
				Constants.IntentsCodes.votersPhotoUrlList, votersPhotoUrlList);
		intent.putIntegerArrayListExtra(Constants.IntentsCodes.voteForList,
				voteForList);

		screenToController.get(Screen.FEED).getActivity().startActivity(intent);// Constants.RequestCodes.VOTES);
	}

	public void onActivityResult(int resultCode, Intent data) {
		if (resultCode == ChoosieActivity.RESULT_OK) {
			String text = data.getStringExtra(Constants.IntentsCodes.text);
			String post_key = data
					.getStringExtra(Constants.IntentsCodes.post_key);
			CommentFor(post_key, text);
		}
	}

	public ScreenController getControllerForScreen(Screen screen) {
		return screenToController.get(screen);
	}

	public Activity getActivity() {
		return this.activity;
	}

	public Screen getCurrentScreen() {
		return currentScreen;
	}

	public void setCurrentScreen(Screen screen) {
		currentScreen = screen;
	}

	public void switchToEnlargeImage(View v, ChoosiePostData choosiePost) {
		Intent intent = new Intent(screenToController.get(Screen.FEED)
				.getActivity().getApplicationContext(),
				EnlargePhotoActivity.class);

		int startingImage = 2;
		switch (v.getId()) {
		case (R.id.feedimage1):
			startingImage = 0;
			break;
		case (R.id.feedimage2):
			startingImage = 1;
			break;
		}

		int votes1 = choosiePost.getVotes1();
		int votes2 = choosiePost.getVotes2();
		String photo1Path = Utils.getFileNameForURL(choosiePost.getPhoto1URL());
		String photo2Path = Utils.getFileNameForURL(choosiePost.getPhoto2URL());
		String userPhotoPath = Utils.getFileNameForURL(choosiePost.getAuthor()
				.getPhotoURL());
		String userName = choosiePost.getAuthor().getUserName();
		String question = choosiePost.getQuestion();
		boolean isVotedAlready = choosiePost.isVotedAlready();

		IntentData intentData = new IntentData(startingImage, votes1, votes2,
				photo1Path, photo2Path, userPhotoPath, userName, question,
				isVotedAlready);

		intent.putExtra(Constants.IntentsCodes.intentData, intentData);

		getActivity().startActivityForResult(intent,
				Constants.RequestCodes.EnalargeImage);
	}

	private void handleGCMRegister() {
		GCMRegistrar.checkDevice(getActivity());
		GCMRegistrar.checkManifest(getActivity());
		// GCMRegistrar.unregister(getActivity());
		String regId = GCMRegistrar.getRegistrationId(getActivity());
		if (regId.equals("")) {
			Logger.i("Registering with sender_id: "
					+ Constants.Notifications.SENDER_ID);
			GCMRegistrar.register(getActivity(),
					Constants.Notifications.SENDER_ID);
			Logger.i("succeeded registering!!!");
		} else {
			GCMRegistrar.setRegisteredOnServer(getActivity(), true);
			Logger.i("Already registered");
		}
	}

	public void handlePopupVoteWindow(String postKey, int position) {
		Logger.d("SuperController: entered handlePopupVoteWindow, postKey = "
				+ postKey + " position = " + position);
		// first scroll the positioned item
		if (position != -1) {
			getControllerForScreen(Screen.FEED).getFeedListView()
					.smoothScrollToPosition(position);
		}
		VotePopupWindowUtils votesPopupWindowUtils = new VotePopupWindowUtils(
				getActivity());
		votesPopupWindowUtils.popUpVotesWindow(postKey);
	}

	public void switchToCommentScreen(String postKey) {
		switchToCommentScreen(postKey, false);
	}

	private void switchToCommentScreen(String postKey, final boolean openVotes) {
		Caches.getInstance()
				.getPostsCache()
				.getValue(postKey,
						new CacheCallback<String, ChoosiePostData>() {
							@Override
							public void onValueReady(String key,
									ChoosiePostData result) {
								if (result == null) {
									Logger.e("ERROR : param is 'null'");
									// TODO: Handle error
									// Toast.makeText(getActivity(),
									// "Failed to update post.",
									// Toast.LENGTH_SHORT).show();
									return;
								}
								switchToCommentScreen(result, openVotes);
							}
						});
	}

	public void switchToCommentScreenAndOpenVotes(String postKey) {
		switchToCommentScreen(postKey, true);
	}
}
