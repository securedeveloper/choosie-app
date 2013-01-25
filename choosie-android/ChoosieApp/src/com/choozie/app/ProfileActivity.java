package com.choozie.app;

import com.choozie.app.client.Client;
import com.choozie.app.controllers.FeedListAdapter;
import com.choozie.app.controllers.SuperController;
import com.choozie.app.models.ChoosiePostData;
import com.choozie.app.models.User;
import com.choozie.app.models.UserDetails;
import com.choozie.app.views.BottomNavigationBarView;
import com.choozie.app.views.PostViewActionsHandler;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.content.Intent;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
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
	private ImageButton ibEdit;
	private TextView tvInvite;
	private ImageButton ibInvite;
	private UserDetails userDetails;
	private TextView tvNumPosts;
	private TextView tvNumVotes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		Intent intent = getIntent();
		user = intent.getParcelableExtra(Constants.IntentsCodes.user);

		handleActionHandler();

		initializeComponents();

		refreshUserDetails();
	}

	private void refreshUserDetails() {
		// Gets all user details from server
		// and refreshes the private member 'userDetails'
		refreshProfileDetailsFromServer(user);
	}

	private void handleActionHandler() {
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
	}

	private void initializeComponents() {

		startTheListView();

		// initialize all resources
		ibEdit = (ImageButton) findViewById(R.id.profile_edit_image_button);
		tvInvite = (TextView) findViewById(R.id.tvInvite);
		tvFullName = (TextView) findViewById(R.id.profile_user_name);
		tvFullName.setText(user.getUserName());
		tvNumPosts = (TextView) findViewById(R.id.profile_num_posts_number);
		tvNumVotes = (TextView) findViewById(R.id.profile_num_votes_number);

		// set all listeners
		ibEdit.setOnClickListener(editButtonListener);
		tvInvite.setOnClickListener(inviteFriendListener);

		// set visibility
		tvInvite.setVisibility(View.GONE);

		// initialize bottom navigation bar
		LinearLayout bottomView = (LinearLayout) findViewById(R.id.profile_bottom_nav_bar);
		BottomNavigationBarView customView = new BottomNavigationBarView(this,
				this, Screen.USER_PROFILE);
		bottomView.addView(customView);

		// Set the Profile navigation bar as 'selected' only
		// if I enter my own profile.
		if (user.isActiveUser()) {

			customView
					.changeSelectedButton((RelativeLayout) findViewById(R.id.view_navBar_layout_button_profile));
			ibEdit.setVisibility(View.VISIBLE);
			tvInvite.setVisibility(View.VISIBLE);
		}
	}

	protected void inviteFriend() {

		Linkify.addLinks(tvInvite, Linkify.ALL);

		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT,
				getResources().getText(R.string.invite_message));
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, getResources()
				.getString(R.string.invite_title)));
	}

	protected void startProfileEditActivity() {

		refreshProfileDetailsFromServer(user);

		Intent intent = new Intent(this, ProfileEditActivity.class);
		intent.putExtra(Constants.IntentsCodes.userDetails, userDetails);
		startActivityForResult(intent,
				Constants.RequestCodes.EDIT_PROFILE_SCREEN);
	}

	private void refreshProfileDetailsFromServer(final User user) {
		// get profile details from server
		Client.getInstance().getUserDetails(user,
				new Callback<Void, Void, UserDetails>() {
					@Override
					public void onFinish(UserDetails ud) {
						if (ud == null) {
							L.i("ud equals NULL. creating new UserDtails(user)");
							ud = new UserDetails(user);
						}

						// Setting all the details in the VIEW
						userDetails = ud;
						setAllDetails(userDetails);
					}
				});
	}

	protected void setAllDetails(UserDetails ud) {

		tvNumPosts.setText(String.valueOf(ud.getNumPosts()));
		tvNumVotes.setText(String.valueOf(ud.getNumVotes()));
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

		// case Constants.RequestCodes.COMMENT:
		// if (resultCode == Activity.RESULT_OK) {
		// String text = data.getStringExtra(Constants.IntentsCodes.text);
		// String post_key = data
		// .getStringExtra(Constants.IntentsCodes.post_key);
		// SuperController.commentFor(post_key, text, this,
		// choosiePostsItemAdapter);
		// }
		// break;
		// case Constants.RequestCodes.NEW_POST:
		// if (resultCode == Activity.RESULT_OK) {
		// choosiePostsItemAdapter.refreshFeed();
		// }
		case Constants.RequestCodes.EDIT_PROFILE_SCREEN:
			if (resultCode == Activity.RESULT_OK) {

			}
			break;
		case Constants.RequestCodes.PICK_CONTACT:
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();
			}
			break;
		}
	}

	private OnClickListener editButtonListener = new OnClickListener() {

		public void onClick(View v) {
			startProfileEditActivity();
		}
	};
	private OnClickListener inviteFriendListener = new OnClickListener() {

		public void onClick(View v) {
			inviteFriend();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_profile, menu);
		return true;
	}

}
