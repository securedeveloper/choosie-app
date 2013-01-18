package com.choozie.app.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.choozie.app.Callback;
import com.choozie.app.ChoosieActivity;
import com.choozie.app.CommentScreenActivity;
import com.choozie.app.Constants;
import com.choozie.app.EnlargePhotoActivity;
import com.choozie.app.IntentData;
import com.choozie.app.L;
import com.choozie.app.R;
import com.choozie.app.Screen;
import com.choozie.app.Utils;
import com.choozie.app.VotePopupWindowUtils;
import com.choozie.app.VotesScreenActivity;
import com.choozie.app.NewChoosiePostData.PostType;
import com.choozie.app.caches.CacheCallback;
import com.choozie.app.caches.Caches;
import com.choozie.app.client.Client;
import com.choozie.app.models.ChoosiePostData;
import com.choozie.app.models.Comment;
import com.choozie.app.models.Vote;
import com.choozie.app.models.VoteHandler;
import com.choozie.app.views.PostViewActionsHandler;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SuperController implements PostViewActionsHandler {
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

	public void voteFor(final String postKey, int whichPhoto) {
		issueVote(postKey, whichPhoto, getActivity(),
				((FeedScreenController) screenToController.get(Screen.FEED))
						.getFeedListAdapter());
	}

	public static void issueVote(final String postKey, int whichPhoto,
			final Activity theActivity, final FeedListAdapter feedListAdapter) {
		L.i("Issuing vote for: " + postKey);
		Tracker tracker = GoogleAnalytics.getInstance(theActivity)
				.getDefaultTracker();
		tracker.trackEvent("Ui Action", "Vote", String.valueOf(whichPhoto),
				null);
		VoteHandler voteHandler = new VoteHandler(theActivity);
		Callback<Void, Void, Boolean> theCallback = new Callback<Void, Void, Boolean>() {
			@Override
			public void onFinish(Boolean param) {
				if (param) {
					refreshPost(postKey, theActivity, feedListAdapter);
				}
			}
		};
		voteHandler.voteFor(postKey, whichPhoto, theCallback);
	}

	private static void refreshPost(String postKey, final Activity theActivity,
			final FeedListAdapter feedListAdapter) {
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
									Toast.makeText(theActivity,
											"Failed to update post.",
											Toast.LENGTH_SHORT).show();
									return;
								}
								feedListAdapter.refreshItem(result);
							}
						});

	}

	public static void commentFor(final String post_key, String text,
			final Activity theActivity, final FeedListAdapter feedListAdapter) {
		L.i("commenting vote for: " + post_key);
		Tracker tracker = GoogleAnalytics.getInstance(theActivity)
				.getDefaultTracker();
		tracker.trackEvent("Ui Action", "comment", "text", null);
		Client.getInstance().sendCommentToServer(post_key, text,
				new Callback<Void, Void, Boolean>() {

					@Override
					public void onFinish(Boolean param) {
						if (param) {
							refreshPost(post_key, theActivity, feedListAdapter);
						}
					}
				});
	}

	private void switchToCommentScreen(ChoosiePostData choosiePost,
			boolean openVotesWindow) {
		Activity theActivity = getActivity();
		switchToCommentScreen(choosiePost, openVotesWindow, theActivity);

	}

	public static void switchToCommentScreen(ChoosiePostData choosiePost,
			boolean openVotesWindow, final Activity theActivity) {
		final Intent intent = new Intent(theActivity.getApplicationContext(),
				CommentScreenActivity.class);

		intent.putExtra("post_key", choosiePost.getPostKey());
		intent.putExtra("no_second_photo",
				choosiePost.getPostType() == PostType.YesNo);
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
		ArrayList<String> fbUidList = new ArrayList<String>();

		for (Comment comment : choosiePost.getComments()) {
			nameList.add(comment.getUser().getUserName());
			commentList.add(comment.getText());
			commentierPhotoUrlList.add(comment.getUser().getPhotoURL());
			createdAtList.add(Utils.getTimeDifferenceTextFromNow(comment
					.getCreatedAt()));
			fbUidList.add(comment.getUser().getFbUid());
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
		intent.putStringArrayListExtra(Constants.IntentsCodes.fbUid, fbUidList);

		// this is to make sure that the user has the photos in his sd
		Caches.getInstance()
				.getPhotosCache()
				.getValue(choosiePost.getPostKey(),
						new CacheCallback<String, Bitmap>() {

							public void onValueReady(String key, Bitmap result) {
								theActivity.startActivityForResult(intent,
										Constants.RequestCodes.COMMENT);
							}
						});
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
			commentFor(post_key, text, getActivity(),
					getControllerForScreen(Screen.FEED).getFeedListAdapter());
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
		switchToEnlargeImage(v, choosiePost, getActivity());
	}

	public static void switchToEnlargeImage(View v,
			ChoosiePostData choosiePost, Activity theActivity) {
		Intent intent = new Intent(theActivity.getApplicationContext(),
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
		boolean noSecondPhoto = choosiePost.getPostType() == PostType.YesNo;
		String photo1Path = Utils.getFileNameForURL(choosiePost.getPhoto1URL());
		String photo2Path = Utils.getFileNameForURL(choosiePost.getPhoto2URL());
		String userPhotoPath = Utils.getFileNameForURL(choosiePost.getAuthor()
				.getPhotoURL());
		String userName = choosiePost.getAuthor().getUserName();
		String question = choosiePost.getQuestion();
		boolean isVotedAlready = choosiePost.isVotedAlready();

		IntentData intentData = new IntentData(startingImage, votes1, votes2,
				photo1Path, photo2Path, userPhotoPath, userName, question,
				isVotedAlready, noSecondPhoto, choosiePost.isVotedAlready(1),
				choosiePost.isVotedAlready(2), choosiePost.getPostKey());

		intent.putExtra(Constants.IntentsCodes.intentData, intentData);

		theActivity.startActivityForResult(intent,
				Constants.RequestCodes.EnalargeImage);
	}

	private void handleGCMRegister() {
		GCMRegistrar.checkDevice(getActivity());
		GCMRegistrar.checkManifest(getActivity());
		// GCMRegistrar.unregister(getActivity());
		String regId = GCMRegistrar.getRegistrationId(getActivity());
		if (regId.equals("")) {
			L.i("Registering with sender_id: "
					+ Constants.Notifications.SENDER_ID);
			GCMRegistrar.register(getActivity(),
					Constants.Notifications.SENDER_ID);
			L.i("succeeded registering!!!");
		} else {
			GCMRegistrar.setRegisteredOnServer(getActivity(), true);
			L.i("Already registered");
		}
	}

	public void handlePopupVoteWindow(String postKey, int position) {
		L.d("SuperController: entered handlePopupVoteWindow, postKey = "
				+ postKey + " position = " + position);
		ListView listView = getControllerForScreen(Screen.FEED)
				.getFeedListView();
		Activity theActivity = getActivity();
		handlePopupVoteWindow(postKey, position, listView, theActivity);
	}

	public static void handlePopupVoteWindow(String postKey, int position,
			ListView listView, Activity theActivity) {
		// first scroll the positioned item
		if (position != -1) {
			listView.smoothScrollToPosition(position);
		}
		VotePopupWindowUtils votesPopupWindowUtils = new VotePopupWindowUtils(
				theActivity);
		votesPopupWindowUtils.popUpVotesWindow(postKey);
	}

	public void switchToCommentScreen(String postKey) {
		switchToCommentScreen(postKey, false, getActivity());
	}

	public static void switchToCommentScreen(String postKey,
			final boolean openVotes, final Activity theActivity) {
		Caches.getInstance()
				.getPostsCache()
				.getValue(postKey,
						new CacheCallback<String, ChoosiePostData>() {
							@Override
							public void onValueReady(String key,
									ChoosiePostData result) {
								if (result == null) {
									L.e("ERROR : param is 'null'");
									// TODO: Handle error
									// Toast.makeText(getActivity(),
									// "Failed to update post.",
									// Toast.LENGTH_SHORT).show();
									return;
								}
								switchToCommentScreen(result, openVotes,
										theActivity);
							}
						});
	}

	public void switchToCommentScreenAndOpenVotes(String postKey) {
		switchToCommentScreen(postKey, true, getActivity());
	}
}
