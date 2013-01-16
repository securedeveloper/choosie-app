package com.choozie.app.models;

import android.app.Activity;
import android.widget.ImageView;

import com.choozie.app.Callback;
import com.choozie.app.L;
import com.choozie.app.R;
import com.choozie.app.NewChoosiePostData.PostType;
import com.choozie.app.client.Client;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/*
 * class that handling voting
 * for voting: create instance of VoteHandler with callback argument and activate 'votFor'
 */
public class VoteHandler {

	private Activity activity;

	public VoteHandler(Activity activity) {
		this.activity = activity;
	}

	public void voteFor(final String postKey, int whichPhoto, Callback<Void, Void, Boolean> callback) {
		L.i("Issuing vote for: " + postKey);
		// Tracker tracker =
		// GoogleAnalytics.getInstance(this.getDefaultTracker();
		// tracker.trackEvent("Ui Action", "Vote", String.valueOf(whichPhoto),
		// null);
		Client.getInstance().sendVoteToServer(postKey, whichPhoto, callback);
	}

	public void setVoteButtonIcon(ImageView imgView, boolean isVoted,
			int photoNumber, PostType postType) {
		if (postType == PostType.YesNo && photoNumber == 2) {
			if (isVoted) {
				imgView.setImageDrawable(activity.getResources().getDrawable(
						R.drawable.thumbdown_voted));
			} else {
				imgView.setImageDrawable(activity.getResources().getDrawable(
						R.drawable.thumbdown_not_voted));
			}
		} else {
			if (isVoted) {
				imgView.setImageDrawable(activity.getResources().getDrawable(
						R.drawable.thumbup_voted));
			} else {
				imgView.setImageDrawable(activity.getResources().getDrawable(
						R.drawable.thumbup_not_voted));
			}
		}
	}
}
