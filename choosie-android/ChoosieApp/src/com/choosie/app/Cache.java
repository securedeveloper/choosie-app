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
	Map<Key, List<Callback<Object, Value>>> callbacksForKey = new HashMap<Key, List<Callback<Object, Value>>>();

	public Cache(ResultCallback<Value, Key> downloader) {
		this.downloader = downloader;
	}

	public void getValue(Key key, Callback<Object, Value> callback) {
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
		callback.onFinish(fromMemoryCache);
	}

	private boolean isCurrentlyDownloading(Key key) {
		return callbacksForKey.containsKey(key);
	}

	private void addCallback(Key key, Callback<Object, Value> callback) {
		callbacksForKey.get(key).add(callback);
	}

	private void startDownload(final Key key) {
		// This marks that this key is currently being downloaded.
		callbacksForKey.put(key, new ArrayList<Callback<Object, Value>>());

		AsyncTask<Void, ?, Value> task = new AsyncTask<Void, Object, Value>() {
			@Override
			protected Value doInBackground(Void... params) {
				return downloader.getData(key, new Callback<Object, Void>() {
					@Override
					public void onProgress(Object progress) {
						publishProgress(progress);
					}
				});
			}

			@Override
			protected void onProgressUpdate(Object... progress) {
				runProgressCallbacks(key, progress[0]);
			}

			@Override
			protected void onPostExecute(Value result) {
				runCallbacks(key, result);
			}
		};

		task.execute();
	}

	protected void runProgressCallbacks(Key key, Object progress) {
		List<Callback<Object, Value>> callbacks;
		synchronized (cacheLock) {
			callbacks = callbacksForKey.get(key);
			// TODO think about moving this out of the synchornized block
			for (Callback<Object, Value> callback : callbacks) {
				callback.onProgress(progress);
			}
		}
	}

	private void runCallbacks(final Key key, Value result) {
		List<Callback<Object, Value>> callbacks;
		synchronized (cacheLock) {
			memoryCache.put(key, result);
			callbacks = callbacksForKey.get(key);
			callbacksForKey.remove(key);
		}
		for (Callback<?, Value> callback : callbacks) {
			callback.onFinish(result);
		}
	}

}
