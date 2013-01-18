package com.choozie.app.views;

import android.app.Activity;
import android.view.View;

import com.choozie.app.Screen;
import com.choozie.app.controllers.SuperController;
import com.choozie.app.models.ChoosiePostData;

public interface PostViewActionsHandler {

	void switchToCommentScreen(String postKey);

	void switchToEnlargeImage(View v, ChoosiePostData post);

	void handlePopupVoteWindow(String postKey, int position);

	Activity getActivity();

	void voteFor(String postKey, int photoNumber);

}
