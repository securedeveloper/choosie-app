package com.choosie.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.AsyncTask;

public class Cache<Key, Value> {
	ResultCallback<Value, Key> downloader;
	Object cacheLock = new Object();
	Map<Key, Value> memoryCache = new HashMap<Key, Value>();
	Map<Key, List<Callback<?, Value>>> callbacksForKey = new HashMap<Key, List<Callback<?, Value>>>();

	public Cache(ResultCallback<Value, Key> downloader) {
		this.downloader = downloader;
	}

	public void getValue(Key key, Callback<?, Value> callback) {
		Value fromMemoryCache;
		synchronized (cacheLock) {
			fromMemoryCache = memoryCache.get(key);
			if (fromMemoryCache == null) {
				// Not in memory cache
				if (!isCurrentlyDownloading(key)) {
					// In case this is the first time this key is encountered,
					// initiate downloading it in the background.
					startDownload(key);
				}
				// Make sure that this callback is run when the download is
				// finished.
				addCallback(key, callback);
				return;
			}
		}
		callback.onOperationFinished(fromMemoryCache);
	}

	private boolean isCurrentlyDownloading(Key key) {
		return callbacksForKey.containsKey(key);
	}

	private void addCallback(Key key, Callback<?, Value> callback) {
		callbacksForKey.get(key).add(callback);
	}

	private void startDownload(final Key key) {
		// This marks that this key is currently being downloaded.
		callbacksForKey.put(key, new ArrayList<Callback<?, Value>>());

		AsyncTask<Void, Void, Value> task = new AsyncTask<Void, Void, Value>() {
			@Override
			protected Value doInBackground(Void... params) {
				return downloader.getData(key);
			}

			@Override
			protected void onPostExecute(Value result) {
				runCallbacks(key, result);
			}
		};

		task.execute();
	}

	private void runCallbacks(final Key key, Value result) {
		List<Callback<?, Value>> callbacks;
		synchronized (cacheLock) {
			memoryCache.put(key, result);
			callbacks = callbacksForKey.get(key);
			callbacksForKey.remove(key);
		}
		for (Callback<?, Value> callback : callbacks) {
			callback.onOperationFinished(result);
		}
	}

}
