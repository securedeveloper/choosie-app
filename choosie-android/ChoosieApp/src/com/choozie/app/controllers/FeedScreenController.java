package com.choozie.app.controllers;

import com.choozie.app.L;
import com.choozie.app.R;
import com.choozie.app.Screen;
import com.choozie.app.models.ChoosiePostData;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FeedScreenController extends ScreenController {
	private ListView listView;

	private FeedListAdapter choosiePostsItemAdapter;

	public FeedScreenController(View layout, SuperController superController) {
		super(layout, superController);
	}

	@Override
	protected void onCreate() {
		L.i("Feed.onShow()");
		// Create a progress bar to display while the list loads
		TextView textView = new TextView(this.getActivity());
		textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		textView.setText(R.string.feed_is_empty_message);

		listView = (ListView) view.findViewById(R.id.feedListView);
		listView.setEmptyView(textView);

		// Must add the progress bar to the root of the layout
		ViewGroup root = (ViewGroup) view.findViewById(R.id.layout_feed);
		root.addView(textView);
		choosiePostsItemAdapter = new FeedListAdapter(getActivity(),
				R.id.layout_me, this.superController);
		listView.setAdapter(choosiePostsItemAdapter);

		listView.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				/* maybe add a padding */
				boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount - 3;
				if (loadMore) {
//					L.i("LOADMORE");
					choosiePostsItemAdapter.appendToList();
				}
			}

		});
		
		
		
		
	}

	@Override
	protected void onShow() {
		superController.setCurrentScreen(Screen.FEED);
		((RelativeLayout) getActivity().findViewById(R.id.view_navBar_layout_button_feed))
				.setBackgroundResource(R.drawable.selected_button);
		superController.getActivity().findViewById(R.id.refresh_button)
				.setVisibility(View.VISIBLE);
		refresh();
	}

	@Override
	protected void onHide() {
		((RelativeLayout) getActivity().findViewById(R.id.view_navBar_layout_button_feed))
				.setBackgroundResource(R.drawable.unselected_button);
	}

	@Override
	public void refresh() {
		// TODO Refresh if needed only.
		choosiePostsItemAdapter.refreshFeed();
	}

	public void refreshPost(ChoosiePostData post) {
		choosiePostsItemAdapter.refreshItem(post);
	}

	@Override
	public ListView getFeedListView() {
		return listView;
	}

	@Override
	public FeedListAdapter getFeedListAdapter() {
		return choosiePostsItemAdapter;
	}

}
