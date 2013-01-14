package com.choozie.app.models;

import com.choozie.app.Callback;
import com.choozie.app.L;
import com.choozie.app.client.Client;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/*
 * class that handling voting
 * for voting: create instance of VoteHandler with callback argument and activate 'votFor'
 */
public class VoteHandler {

	private Callback<Void, Void, Boolean> callback = null;

	public VoteHandler(Callback<Void, Void, Boolean> callback) {
		this.callback = callback;
	}

	public void voteFor(final ChoosiePostData post, int whichPhoto) {
		L.i("Issuing vote for: " + post.getPostKey());
		// Tracker tracker =
		// GoogleAnalytics.getInstance(this.getDefaultTracker();
		// tracker.trackEvent("Ui Action", "Vote", String.valueOf(whichPhoto),
		// null);
		Client.getInstance().sendVoteToServer(post, whichPhoto, callback);
	}
}
