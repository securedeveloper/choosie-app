package com.choosie.app.controllers;

import com.choosie.app.Callback;
import com.choosie.app.Constants;
import com.choosie.app.Logger;
import com.choosie.app.client.FeedResponse;
import com.choosie.app.Models.ChoosiePostData;
import com.choosie.app.views.ChoosiePostView;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FeedListAdapter extends ArrayAdapter<ChoosiePostData> {
	enum State {
		REFRESHING_FEED, APPENDING_TO_FEED, FEED_UPDATED, FEED_COMPLETE, ERROR,
	}

	State state;
	private View loadingItemView = buildLoadingItemView();

	private static final String LOADING_ITEM_TEXT = "LOADING_ITEM";
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
			// TODO: This is a hack.
			return loadingItemView;
		}

		ChoosiePostView itemView = null;
		// Log.i("getView", "getView called, convertView = " + convertView);
		if ((convertView == null)
				|| (!(convertView instanceof ChoosiePostView))) {
			// Log.i("getView", "making a new view...");

			// we can't use this convertView, we will create new view
			itemView = new ChoosiePostView(this.getContext(),
					this.superController, position);
		} else {
			// Yey!! we can reuse convertView
			itemView = (ChoosiePostView) convertView;
		}

		itemView.loadChoosiePost(item, position);
		return itemView;
	}

	private View buildLoadingItemView() {
		RelativeLayout loadingItemLayout = new RelativeLayout(this.getContext());

		final int TEXT_VIEW_ID = 32555;
		final int PROGRESS_BAR_ID = 32556;

		String message = this.state == State.REFRESHING_FEED ? "Checking for new items..."
				: "Loading items...";

		ProgressBar progressBar = new ProgressBar(this.getContext());
		progressBar.setId(PROGRESS_BAR_ID);
		progressBar.setIndeterminate(true);

		TextView textView = new TextView(this.getContext());
		textView.setText(message);
		textView.setId(TEXT_VIEW_ID);

		RelativeLayout.LayoutParams progressBarLayoutParams = new RelativeLayout.LayoutParams(
				40, 40);
		progressBarLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
				RelativeLayout.TRUE);

		RelativeLayout.LayoutParams textViewLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		textViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
				RelativeLayout.TRUE);
		textViewLayoutParams.addRule(RelativeLayout.BELOW, PROGRESS_BAR_ID);

		progressBar.setLayoutParams(progressBarLayoutParams);
		textView.setLayoutParams(textViewLayoutParams);

		loadingItemLayout.addView(progressBar);
		loadingItemLayout.addView(textView);
		loadingItemLayout.setPadding(0, 8, 0, 8);

		return loadingItemLayout;
	}

	String lastCursor;
	private ChoosiePostData loadingItem;

	private void update(FeedResponse param) {
		Logger.i("FeedListAdapter: update. Posts count: "
				+ param.getPosts().size() + ". Append: " + param.isAppend());
		if (param.getPosts().size() == 0) {
			Logger.i("No images in feed.");
		}
		State relevantState = param.isAppend() ? State.APPENDING_TO_FEED
				: State.REFRESHING_FEED;
		if (relevantState != this.state) {
			Logger.i("FeedListAdapter: update. Got new posts, but not updating"
					+ " because not in relevant state. " + "Relevant = "
					+ relevantState + ", Real state = " + this.state);
			return;
		}
		if (param.isAppend() && lastCursor == param.getCursor()) {
			Log.w(Constants.LOG_TAG,
					"Not supposed to get here: an update that was "
							+ "recevied twice.");
			return;
		}
		if (this.state == State.REFRESHING_FEED) {
			Logger.i("Clearing feed (cause in Refresh state)");
			this.clear();
		}
		Logger.i("Adding posts.");
		for (ChoosiePostData item : param.getPosts()) {
			this.add(item);
		}
		lastCursor = param.getCursor();
		Logger.i("Last cursor is now = " + lastCursor);
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
				|| (newState == State.APPENDING_TO_FEED && oldState == State.REFRESHING_FEED)
				|| (newState == State.APPENDING_TO_FEED && oldState == State.FEED_COMPLETE)) {
			return;
		}
		this.state = newState;
		Logger.i("Changing to state " + this.state);
		switch (this.state) {
		case APPENDING_TO_FEED:
			showLoadingItem();
			addItemsToList();
			break;
		case REFRESHING_FEED:
			showLoadingItem();
			addItemsToList();
			break;
		case FEED_UPDATED:
			hideLoadingItem();
			break;
		case FEED_COMPLETE:
			hideLoadingItem();
			break;
		case ERROR:
			hideLoadingItem();
			showErrorToast();
			break;
		}
		Logger.i("Finished changing to state " + this.state);

	}

	private void showErrorToast() {
		Toast toast = Toast.makeText(this.getContext(),
				"Error retrieving posts from server. Please try again later.",
				Toast.LENGTH_LONG);
		toast.show();
	}

	private void addItemsToList() {
		Logger.i("addItemsToList. state = " + state);

		FeedCacheKey request = null;
		if (this.state == State.REFRESHING_FEED) {
			request = new FeedCacheKey(null, false);
			// In case feed needs refreshing, make sure an actual request
			// to the server is made.
			// TODO: Don't cache that result in the first place.
			this.superController.getCaches().getFeedCache()
					.invalidateKey(request);
		} else if (this.state == State.APPENDING_TO_FEED) {
			request = new FeedCacheKey(this.feedCursor, true);
		}

		if (request == null) {
			// Not supposed to get here
			throw new NullPointerException(
					"addItemsToList() was called in a forbidden state.");
		}

		this.superController.getCaches().getFeedCache()
				.getValue(request, new AdapterUpdater());

	}

	private class AdapterUpdater extends Callback<Void, Object, FeedResponse> {
		@Override
		public void onFinish(FeedResponse param) {
			Logger.i("onFINISH!!!");
			if (param == null) {
				changeState(State.ERROR);
			} else {
				feedCursor = param.getCursor();
				update(param);
				if (param.isAppend() && param.getPosts().size() == 0) {
					changeState(State.FEED_COMPLETE);
				} else {
					changeState(State.FEED_UPDATED);
				}
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
					LOADING_ITEM_TEXT, null, null, null, null);
		}
		Logger.i("Showing 'Loading items...'");
		if (this.state == State.APPENDING_TO_FEED) {
			this.add(loadingItem);
		} else if (this.state == State.REFRESHING_FEED) {
			this.insert(loadingItem, 0);
		}
	}

	private boolean loadingItemExists() {
		return this.getPosition(this.loadingItem) >= 0;
	}

	private void hideLoadingItem() {
		int i = 0;
		while (loadingItemExists()) {
			if (++i > 3) {
				break;
			}
			Logger.i("Hiding 'Loading items...'");
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

	public int findPositionByPostKey(String postKey) {
		if (postKey == null) {
			Logger.e("Got to findPositionByPostKey with null postKey.");
			return -1;
		}
		for (int i = 0; i < this.getCount(); ++i) {
			// Crash alert: this.getItem(i).getPostKey() might be null,
			// for example when this.getItem(0) is the Loading item.
			String postKeyAtI = this.getItem(i).getPostKey();
			if (postKeyAtI != null && postKeyAtI.equals(postKey)) {
				Logger.i("findPoitionByPostKey: post is found! position = " + i);
				return i;
			}
		}
		Logger.i("findPoitionByPostKey: did not find the position of post \""
				+ postKey + "\"");
		return -1;
	}

}
