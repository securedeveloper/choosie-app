package com.choosie.app;

import com.choosie.app.Models.ChoosiePostData;
import com.choosie.app.controllers.SuperController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChannelingActivity {

	SuperController superController;
	String postKey;

	public ChannelingActivity(SuperController superController, String postKey) {
		this.superController = superController;
		this.postKey = postKey;
	}

	Intent intent;

	public void handleJob(ChannelingJob channelingJob, Activity activity) {
		switch (channelingJob) {

		case POPUP_VOTES_WINDOW:
			channelToPopupVotesWindow(activity);
		}

	}

	private void channelToPopupVotesWindow(Activity activity) {
		final VotePopupWindowUtils votesPopupWindowUtils = new VotePopupWindowUtils(
				activity);

		superController
				.getCaches()
				.getPostsCache()
				.getValue(postKey,
						new Callback<Void, Object, ChoosiePostData>() {
							@Override
							public void onFinish(ChoosiePostData param) {
								if (param == null) {
									Logger.e("ERROR : param is 'null'");
									// TODO: Handle error
									// Toast.makeText(getActivity(),
									// "Failed to update post.",
									// Toast.LENGTH_SHORT).show();
									return;
								}
								votesPopupWindowUtils.popUpVotesWindow(param);
							}
						});
	}

}
