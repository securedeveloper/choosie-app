package com.choozie.app.controllers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.choozie.app.L;
import com.choozie.app.R;

public class UserProfileScreenController {

	private ListView listView;
	private SuperController superController;
	private FeedListAdapter choosiePostsItemAdapter;
	private String fbUid;
	private View view;

	public UserProfileScreenController(SuperController superController,
			View layout, String fbUid) {
		this.superController = superController;
		this.view = layout;
		this.fbUid = fbUid;
	}

	public void startBuiss() {

		LayoutInflater layoutInflater = (LayoutInflater) superController
				.getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//		// Inflate the userProfile xml and add it to layout_post
//		RelativeLayout layoutUserProfile = (RelativeLayout) superController
//				.getActivity().findViewById(R.id.layout_userProfile);
//		layoutUserProfile.addView(layoutInflater.inflate(
//				R.layout.screen_user_profile, null));
//
//		layoutUserProfile.setVisibility(View.VISIBLE);
//		layoutUserProfile.bringToFront();
//
//		// Create a progress bar to display while the list loads
//		TextView textView = new TextView(superController.getActivity());
//		textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
//				LayoutParams.WRAP_CONTENT));
//		textView.setText(R.string.feed_is_empty_message);
//
//		listView = (ListView) view
//				.findViewById(R.id.screen_userProfile_feedListView);
//		listView.setEmptyView(textView);
//
//		// Must add the progress bar to the root of the layout
//		ViewGroup root = (ViewGroup) view.findViewById(R.id.layout_userProfile);
//		root.addView(textView);
//		choosiePostsItemAdapter = new FeedListAdapter(
//				superController.getActivity(), R.id.layout_me,
//				this.superController, fbUid);
//		listView.setAdapter(choosiePostsItemAdapter);
//
//		listView.setOnScrollListener(new OnScrollListener() {
//
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//			}
//
//			public void onScroll(AbsListView view, int firstVisibleItem,
//					int visibleItemCount, int totalItemCount) {
//				/* maybe add a padding */
//				boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount - 3;
//				if (loadMore) {
//					L.i("LOADMORE");
//					choosiePostsItemAdapter.appendToList();
//				}
//			}
//
//		});

	}

}
