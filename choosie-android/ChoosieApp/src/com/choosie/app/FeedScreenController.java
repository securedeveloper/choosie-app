package com.choosie.app;

import java.util.List;

import com.choosie.app.ChoosieClient.ChoosiePostData;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;


public class FeedScreenController extends ScreenController {

	private ChoosiePostsItemAdapter choosiePostsItemAdapter;

	public FeedScreenController(View layout, Activity activity, SuperController superController) {
		super(layout, activity, superController);
	}


	@Override
	protected void onCreate() {
		Log.i(ChoosieConstants.LOG_TAG, "Feed.onShow()");
		// Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this.activity);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(true);
        
        ListView listView = (ListView) view.findViewById(R.id.feedListView);
        listView.setEmptyView(progressBar);
        
        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) view.findViewById(R.id.layout_feed);
        root.addView(progressBar);
        choosiePostsItemAdapter = new ChoosiePostsItemAdapter(activity, R.id.layout_me, this.client);
		listView.setAdapter(choosiePostsItemAdapter);
		refreshFeed();
	}
	
	@Override
	protected void onShow() {
		Log.i(ChoosieConstants.LOG_TAG, "Feed.onShow()");
		refreshFeed();
	}

	@Override
	protected void onHide() {
		// TODO Auto-generated method stub

	}

	private void refreshFeed() {
		this.client.getFeedFromServer(new Callback<List<ChoosiePostData>>() {

			@Override
			void onOperationFinished(List<ChoosiePostData> param) {
				updateAdapterWithPosts(param);
			}
		});

	}

	protected void updateAdapterWithPosts(List<ChoosiePostData> param) {
		Log.i(ChoosieConstants.LOG_TAG, "Feed: loadPosts. Posts count: " + param.size());
		if (param.size() == 0) {
			Log.i(ChoosieConstants.LOG_TAG, "No images in feed.");
		}
		choosiePostsItemAdapter.clear();
		for (ChoosiePostData item : param) {
			choosiePostsItemAdapter.add(item);
		}
	}
}
