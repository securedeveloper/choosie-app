package com.choosie.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.choosie.app.Client.ChoosiePostData;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;

public class SuperController {
	private Client client;
	private final Caches caches = new Caches(this);
	Map<Screen, ScreenController> screenToController;

	public SuperController(Activity choosieActivity, FacebookDetails fbDetails) {
		client = new Client(fbDetails);
		
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
		
		client.login(new Callback<Void, Void, Void>() {
			@Override
			void onFinish(Void param) {
				screenToController.get(Screen.FEED).refresh();
			}
		});
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
		Log.i(Constants.LOG_TAG, "Issuing vote for: " + post.getKey());
		this.client.sendVoteToServer(post, whichPhoto,
				new Callback<Void, Void, Boolean>() {

					@Override
					void onFinish(Boolean param) {
						if (param) {
							screenToController.get(Screen.FEED).refresh();
						}
					}
				});
	}

	public Client getClient() {
		return client;
	}

	public Caches getCaches() {
		return caches;
	}

}
