package com.choozie.app;

import com.choozie.app.caches.CacheCallback;
import com.choozie.app.caches.Caches;
import com.choozie.app.client.Client;
import com.choozie.app.controllers.FeedListAdapter;
import com.choozie.app.controllers.SuperController;
import com.choozie.app.models.ChoosiePostData;
import com.choozie.app.models.FacebookDetails;
import com.choozie.app.models.User;
import com.choozie.app.views.BottomNavigationBarView;
import com.choozie.app.views.PostViewActionsHandler;
import com.facebook.android.FbDialog;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class ProfileActivity extends Activity {

	private TextView tvFullName;
	private ImageButton ibUserPicture;
	private User user;
	private ListView listView;
	private FeedListAdapter choosiePostsItemAdapter;
	private PostViewActionsHandler actionHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		Intent intent = getIntent();
		user = intent.getParcelableExtra(Constants.IntentsCodes.user);

		LinearLayout bottomView = (LinearLayout) findViewById(R.id.profile_bottom_nav_bar);

		BottomNavigationBarView customView = new BottomNavigationBarView(this,
				this, Screen.USER_PROFILE);
		bottomView.addView(customView);
		if (Client.getInstance().getActiveUser().equals(user)) {
			customView
					.changeSelectedButton((RelativeLayout) findViewById(R.id.view_navBar_layout_button_profile));
		}

		tvFullName = (TextView) findViewById(R.id.profile_user_name);
		tvFullName.setText(user.getUserName());

		final ProfileActivity thisActivity = this;
		actionHandler = new PostViewActionsHandler() {

			public void voteFor(String postKey, int photoNumber) {
				SuperController.issueVote(postKey, photoNumber, thisActivity,
						choosiePostsItemAdapter);
			}

			public void switchToEnlargeImage(View v, ChoosiePostData post) {
				SuperController.switchToEnlargeImage(v, post, thisActivity);
			}

			public void switchToCommentScreen(String postKey) {
				SuperController.switchToCommentScreen(postKey, false,
						thisActivity);
			}

			public void handlePopupVoteWindow(String postKey, int position) {
				SuperController.handlePopupVoteWindow(postKey, position,
						listView, thisActivity);
			}

			public Activity getActivity() {
				return thisActivity;
			}
		};

		startTheListView();
	}

	private void startTheListView() {

		listView = (ListView) findViewById(R.id.profile_feedListView);
		choosiePostsItemAdapter = new FeedListAdapter(this, R.id.layout_me,
				this.actionHandler, user.getFbUid());

		View header = getLayoutInflater().inflate(
				R.layout.header_profile_listview, null);

		initializeHeader(header);

		View footer = getLayoutInflater().inflate(
				R.layout.footer_profile_listview, null);

		listView.addHeaderView(header);
		listView.addFooterView(footer);

		listView.setAdapter(choosiePostsItemAdapter);

		listView.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				/* maybe add a padding */
				boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount - 3;
				if (loadMore) {
					L.i("LOADMORE");
					choosiePostsItemAdapter.appendToList();
				}
			}
		});
	}

	private void initializeHeader(View header) {

		ibUserPicture = (ImageButton) header
				.findViewById(R.id.profile_user_picture);

		String userImagePath = Utils.getFileNameForURL(user.getPhotoURL());
		Utils.setImageFromPath(userImagePath, ibUserPicture);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case Constants.RequestCodes.COMMENT:
			if (resultCode == Activity.RESULT_OK) {
				String text = data.getStringExtra(Constants.IntentsCodes.text);
				String post_key = data
						.getStringExtra(Constants.IntentsCodes.post_key);
				SuperController.commentFor(post_key, text, this,
						choosiePostsItemAdapter);
			}
			break;
		case Constants.RequestCodes.NEW_POST:
			if (resultCode == Activity.RESULT_OK) {
				choosiePostsItemAdapter.refreshFeed();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_profile, menu);
		return true;
	}

}
