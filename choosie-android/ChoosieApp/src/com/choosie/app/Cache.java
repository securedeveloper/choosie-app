package com.choosie.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;

public class Cache<Key, Value> {
	final ResultCallback<Value, Key> downloader;
	final Object cacheLock = new Object();
	final Map<Key, Value> memoryCache = new HashMap<Key, Value>();
	final Map<Key, List<Callback<Void, Object, Value>>> callbacksForKey = new HashMap<Key, List<Callback<Void, Object, Value>>>();

	public Cache(ResultCallback<Value, Key> downloader) {
		this.downloader = downloader;
	}

	public void getValue(Key key, Callback<Void, Object, Value> callback) {
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

	public void invalidateKey(Key key) {
		synchronized (cacheLock) {
			Value fromMemoryCache = memoryCache.get(key);
			if (fromMemoryCache == null) {
				// Not in memory cache. If it is currently downloading, restart
				// the
				// download. Otherwise do nothing.
				if (isCurrentlyDownloading(key)) {
					restartDownload(key);
				}
			} else {
				// In memory cache: remove from cache
				memoryCache.remove(key);
			}
		}
	}

	private boolean isCurrentlyDownloading(Key key) {
		return callbacksForKey.containsKey(key);
	}

	private void addCallback(Key key, Callback<Void, Object, Value> callback) {
		callbacksForKey.get(key).add(callback);
	}

	private void startDownload(final Key key) {
		// This marks that this key is currently being downloaded.
		callbacksForKey
				.put(key, new ArrayList<Callback<Void, Object, Value>>());

		AsyncTask<Void, ?, Value> task = new AsyncTask<Void, Object, Value>() {
			@Override
			protected Value doInBackground(Void... params) {
				return downloader.getData(key,
						new Callback<Void, Object, Void>() {
							@Override
							public void onProgress(Object progress) {
								publishProgress(progress);
							}
						});
			}

			@Override
			protected void onPreExecute() {
				runOnPreCallbacks(key);
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

	private void restartDownload(Key key) {
		// TODO: Cancel running download
		// Only then
		// startDownload(key);
	}

	protected void runOnPreCallbacks(Key key) {
		List<Callback<Void, Object, Value>> callbacks;
		synchronized (cacheLock) {
			callbacks = callbacksForKey.get(key);
			for (Callback<Void, Object, Value> callback : callbacks) {
				callback.onPre(null);
			}
		}
	}

	protected void runProgressCallbacks(Key key, Object progress) {
		List<Callback<Void, Object, Value>> callbacks;
		synchronized (cacheLock) {
			callbacks = callbacksForKey.get(key);
			assert (callbacks != null);
			for (Callback<Void, Object, Value> callback : callbacks) {
				callback.onProgress(progress);
			}
		}
	}

	private void runCallbacks(final Key key, Value result) {
		List<Callback<Void, Object, Value>> callbacks;
		synchronized (cacheLock) {
			if (result != null) {
				memoryCache.put(key, result);
			}
			callbacks = callbacksForKey.get(key);
			callbacksForKey.remove(key);
		}
		for (Callback<Void, ?, Value> callback : callbacks) {
			callback.onFinish(result);
		}
	}

}
