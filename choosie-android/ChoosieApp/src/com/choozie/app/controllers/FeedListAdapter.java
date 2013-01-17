package com.choozie.app.controllers;

import com.choozie.app.Callback;
import com.choozie.app.Constants;
import com.choozie.app.L;
import com.choozie.app.NewChoosiePostData.PostType;
import com.choozie.app.caches.Cache;
import com.choozie.app.caches.CacheCallback;
import com.choozie.app.caches.Caches;
import com.choozie.app.client.FeedResponse;
import com.choozie.app.models.ChoosiePostData;
import com.choozie.app.views.ChoosiePostView;

import android.content.Context;

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
	private String fbUid;

	private static final String LOADING_ITEM_TEXT = "LOADING_ITEM";
	private String feedCursor;
	private SuperController superController;

	public FeedListAdapter(Context context, int textViewResourceId,
			SuperController superController) {
		super(context, textViewResourceId);
		this.superController = superController;
	}

	public FeedListAdapter(Context context, int textViewResourceId,
			SuperController superController, String fbUid) {
		this(context, textViewResourceId, superController);
		this.fbUid = fbUid;
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
		L.i("FeedListAdapter: update. Posts count: " + param.getPosts().size()
				+ ". Append: " + param.isAppend());
		if (param.getPosts().size() == 0) {
			L.i("No images in feed.");
		}
		State relevantState = param.isAppend() ? State.APPENDING_TO_FEED
				: State.REFRESHING_FEED;
		if (relevantState != this.state) {
			L.i("FeedListAdapter: update. Got new posts, but not updating"
					+ " because not in relevant state. " + "Relevant = "
					+ relevantState + ", Real state = " + this.state);
			return;
		}
		if (param.isAppend() && lastCursor == param.getCursor()) {
			L.w("Not supposed to get here: an update that was "
					+ "recevied twice.");
			return;
		}
		if (this.state == State.REFRESHING_FEED) {
			L.i("Clearing feed (cause in Refresh state)");
			this.clear();
		}
		L.i("Adding posts.");
		for (ChoosiePostData item : param.getPosts()) {
			this.add(item);
		}
		lastCursor = param.getCursor();
		L.i("Last cursor is now = " + lastCursor);
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
		L.i("Changing to state " + this.state);
		switch (this.state) {
		case APPENDING_TO_FEED:
			showLoadingItem();
			addItemsToList();
			break;
		case REFRESHING_FEED:
			L.i("bla bla", "fdfd");
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
		L.i("Finished changing to state " + this.state);

	}

	private void showErrorToast() {
		Toast toast = Toast.makeText(this.getContext(),
				"Error retrieving posts from server. Please try again later.",
				Toast.LENGTH_LONG);
		toast.show();
	}

	private void addItemsToList() {
		L.i("addItemsToList. state = " + state);

		FeedCacheKey request = null;
		if (this.state == State.REFRESHING_FEED) {
			request = new FeedCacheKey(null, false, fbUid);
		} else if (this.state == State.APPENDING_TO_FEED) {
			L.i("feedCursor = " + feedCursor);
			request = new FeedCacheKey(this.feedCursor, true, fbUid);
		}

		if (request == null) {
			// Not supposed to get here
			throw new NullPointerException(
					"addItemsToList() was called in a forbidden state.");
		}

		Caches.getInstance().getFeedCache()
				.getValue(request, new AdapterUpdater());

	}

	private class AdapterUpdater extends
			CacheCallback<FeedCacheKey, FeedResponse> {
		@Override
		public void onValueReady(FeedCacheKey key, FeedResponse result) {
			L.i("onFINISH!!!");
			if (result == null) {
				changeState(State.ERROR);
			} else {
				if (key.getCursor() == null) {
					// HACK: We don't want to cache the 'refresh' requests
					// (where cursor was null)
					Caches.getInstance().getFeedCache().invalidateKey(key);
				}
				feedCursor = result.getCursor();
				update(result);
				if (result.isAppend() && result.getPosts().size() == 0) {
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
					LOADING_ITEM_TEXT, null, null, null, null, PostType.TOT);
		}
		L.i("Showing 'Loading items...'");
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
			L.i("Hiding 'Loading items...'");
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
			L.e("Got to findPositionByPostKey with null postKey.");
			return -1;
		}
		for (int i = 0; i < this.getCount(); ++i) {
			// Crash alert: this.getItem(i).getPostKey() might be null,
			// for example when this.getItem(0) is the Loading item.
			String postKeyAtI = this.getItem(i).getPostKey();
			if (postKeyAtI != null && postKeyAtI.equals(postKey)) {
				L.i("findPoitionByPostKey: post is found! position = " + i);
				return i;
			}
		}
		L.i("findPoitionByPostKey: did not find the position of post \""
				+ postKey + "\"");
		return -1;
	}

}
