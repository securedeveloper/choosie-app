package com.choosie.app.controllers;

import com.choosie.app.Callback;
import com.choosie.app.Constants;
import com.choosie.app.Models.ChoosiePostData;
import com.choosie.app.client.FeedResponse;
import com.choosie.app.views.ChoosiePostView;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FeedListAdapter extends ArrayAdapter<ChoosiePostData> {
	enum State {
		REFRESHING_FEED, APPENDING_TO_FEED, FEED_UPDATED, ERROR
	}

	State state;

	private static final String LOADING_ITEM_TEXT = "I AM LOADING ITEM!!!!";
	private String feedCursor;
	private SuperController superController;

	public FeedListAdapter(Context context, int textViewResourceId,
			SuperController superController) {
		super(context, textViewResourceId);
		this.superController = superController;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChoosiePostData item = getItem(position);

		if (item.getQuestion() == LOADING_ITEM_TEXT) {
			Log.i(Constants.LOG_TAG, "Showing view for I AM LOADING ITEM!!!");
			TextView progressBar = new TextView(this.getContext());
			progressBar.setText("Loading items...");
			return progressBar;
		}

		ChoosiePostView itemView = new ChoosiePostView(this.getContext(),
				this.superController);

		itemView.loadChoosiePost(item);
		return itemView;
	}

	String lastCursor;
	private ChoosiePostData loadingItem;

	private void update(FeedResponse param) {
		Log.i(Constants.LOG_TAG, "FeedListAdapter: update. Posts count: "
				+ param.getPosts().size() + ". Append: " + param.isAppend());
		if (param.getPosts().size() == 0) {
			Log.i(Constants.LOG_TAG, "No images in feed.");
		}
		State relevantState = param.isAppend() ? State.APPENDING_TO_FEED
				: State.REFRESHING_FEED;
		if (relevantState != this.state) {
			Log.i(Constants.LOG_TAG,
					"FeedListAdapter: update. Got new posts, but not updating"
							+ " because not in relevant state. "
							+ "Relevant = " + relevantState + ", Real state = "
							+ this.state);
			return;
		}
		if (param.isAppend() && lastCursor == param.getCursor()) {
			Log.w(Constants.LOG_TAG,
					"Not supposed to get here: an update that was "
							+ "recevied twice.");
			return;
		}
		if (this.state == State.REFRESHING_FEED) {
			Log.i(Constants.LOG_TAG, "Clearing feed (cause in Refresh state)");
			this.clear();
		}
		Log.i(Constants.LOG_TAG, "Adding posts.");
		for (ChoosiePostData item : param.getPosts()) {
			this.add(item);
		}
		lastCursor = param.getCursor();
		Log.i(Constants.LOG_TAG, "Last cursor is now = " + lastCursor);
	}

	public void refreshFeed() {
		changeState(State.REFRESHING_FEED);
	}

	public void appendToList() {
		changeState(State.APPENDING_TO_FEED);
	}

	private void changeState(State newState) {
		State oldState = this.state;
		if (oldState == newState
				|| (newState == State.APPENDING_TO_FEED && oldState == State.REFRESHING_FEED)) {
			return;
		}
		this.state = newState;
		Log.i(Constants.LOG_TAG, "Changing to state " + this.state);
		switch (this.state) {
		case APPENDING_TO_FEED:
		case REFRESHING_FEED:
			addItemsToList();
			showLoadingItem();
			break;
		case FEED_UPDATED:
			hideLoadingItem();
			break;
		case ERROR:
			showErrorToast();
			break;
		}
		Log.i(Constants.LOG_TAG, "Finished changing to state " + this.state);

	}

	private void showErrorToast() {
		Toast toast = Toast.makeText(this.getContext(),
				"Error retrieving posts from server. Please try again later.",
				Toast.LENGTH_LONG);
		toast.show();
	}

	private void addItemsToList() {
		boolean append = this.state == State.APPENDING_TO_FEED;
		Log.i(Constants.LOG_TAG, "addItemsToList. append = " + append);

		String cursor = append ? this.feedCursor : null;
		FeedCacheKey request = new FeedCacheKey(cursor, append);
		this.superController.getCaches().getFeedCache()
				.getValue(request, new AdapterUpdater());

		showLoadingItem();
	}

	private class AdapterUpdater extends Callback<Void, Object, FeedResponse> {
		@Override
		public void onPre(Void param) {
			Log.i(Constants.LOG_TAG, "onPRE!!!");
			showLoadingItem();
		}

		@Override
		public void onFinish(FeedResponse param) {
			Log.i(Constants.LOG_TAG, "onFINISH!!!");
			hideLoadingItem();
			if (param == null) {
				changeState(State.ERROR);
			} else {
				feedCursor = param.getCursor();
				update(param);
				changeState(State.FEED_UPDATED);
			}
		}
	}

	public void showLoadingItem() {
		if (loadingItemExists()) {
			hideLoadingItem();
		}
		if (loadingItem == null) {
			// TODO: This is really a bad hack.
			loadingItem = new ChoosiePostData(null, null, null, null,
					LOADING_ITEM_TEXT, null, null, null, null, null);
		}
		Log.i(Constants.LOG_TAG, "Showing I AM LOADING ITEM!!!");
		this.add(loadingItem);
	}

	private boolean loadingItemExists() {
		return this.getPosition(this.loadingItem) >= 0;
	}

	private void hideLoadingItem() {
		int i = 0;
		while (loadingItemExists()) {
			if (++i >= 3) {
				break;
			}
			Log.i(Constants.LOG_TAG, "Hiding I AM LOADING ITEM!!!");
			this.remove(loadingItem);
		}
	}

	public void refreshItem(ChoosiePostData updatedPost) {
		int existingPostPosition = findPositionByPostKey(updatedPost
				.getPostKey());
		if (existingPostPosition != -1) {
			this.remove(this.getItem(existingPostPosition));
			this.insert(updatedPost, existingPostPosition);
		}
	}

	private int findPositionByPostKey(String postKey) {
		for (int i = 0; i < this.getCount(); ++i) {
			if (this.getItem(i).getPostKey().equals(postKey)) {
				return i;
			}
		}
		return -1;
	}

}
