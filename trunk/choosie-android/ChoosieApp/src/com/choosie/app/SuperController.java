package com.choosie.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.choosie.app.ChoosieClient.ChoosiePostData;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;

public class SuperController {
	private ChoosieClient client = new ChoosieClient();
	private final Caches caches = new Caches(this);
	Map<Screen, ScreenController> screenToController;

	public SuperController(Activity choosieActivity) {

		List<Pair<Screen, ScreenController>> screenControllerPairs = new ArrayList<Pair<Screen, ScreenController>>();

		screenControllerPairs
				.add(new Pair<Screen, ScreenController>(Screen.FEED,
						new FeedScreenController(choosieActivity
								.findViewById(R.id.layout_feed),
								choosieActivity, this)));
		screenControllerPairs
				.add(new Pair<Screen, ScreenController>(Screen.POST,
						new PostScreenController(choosieActivity
								.findViewById(R.id.layout_post),
								choosieActivity, this)));
		screenControllerPairs.add(new Pair<Screen, ScreenController>(Screen.ME,
				new MeScreenController(choosieActivity
						.findViewById(R.id.layout_me), choosieActivity, this)));

		screenToController = new HashMap<Screen, ScreenController>();

		for (Pair<Screen, ScreenController> pair : screenControllerPairs) {
			screenToController.put(pair.first, pair.second);
		}

		for (ScreenController screen : screenToController.values()) {
			screen.onCreate();
		}
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

	public void voteFor(ChoosiePostData post, int whichPhoto) {
		Log.i(ChoosieConstants.LOG_TAG, "Issuing vote for: " + post.getKey());
		this.client.sendVoteToServer(post, whichPhoto,
				new Callback<Void, Boolean>() {

					@Override
					void onFinish(Boolean param) {
						if (param) {
							screenToController.get(Screen.FEED).refresh();
						}
					}
				});
	}

	public ChoosieClient getClient() {
		return client;
	}

	public Caches getCaches() {
		return caches;
	}

}
