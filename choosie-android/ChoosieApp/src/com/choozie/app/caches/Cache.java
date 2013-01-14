package com.choozie.app.caches;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.choozie.app.Callback;
import com.choozie.app.L;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;

public abstract class Cache<Key, Value> {
	final Object cacheLock = new Object();
	final LruCache<Key, Value> memoryCache;
	final Map<Key, List<CacheCallback<Key, Value>>> callbacksForKey = new HashMap<Key, List<CacheCallback<Key, Value>>>();

	protected abstract Value fetchData(Key key,
			Callback<Void, Integer, Void> progressCallback);

	public Cache(int maxSize) {
		this.memoryCache = new LruCache<Key, Value>(maxSize) {
			@Override
			protected int sizeOf(Key key, Value value) {
				return calcSizeOf(key, value);
			}
		};
	}

	protected int calcSizeOf(Key key, Value value) {
		// The default: '1' for each entry in cache, making maxCache size
		// in units of 'entries'.
		return 1;
	}

	public void getValue(Key key, CacheCallback<Key, Value> callback) {
		Value fromMemoryCache = null;
		synchronized (cacheLock) {
			fromMemoryCache = memoryCache.get(key);
			if (fromMemoryCache == null) {
				L.d("in GetValue, key = " + key.toString()
						+ ", is not in cache");
				if (!isCurrentlyFetching(key)) {
					// In case this is the first time this key is encountered,
					// initiate downloading it (or loading from the sdcard) in
					// the background.
					L.d("in GetValue, key = " + key.toString()
							+ ", is not been downloaded");
					startFetching(key);
				}
				addCallback(key, callback);
			} else {
				L.d("in getValue, key is in cache! " + key.toString());
			}
		}
		if (fromMemoryCache != null) {
			callback.onValueReady(key, fromMemoryCache);
		}
	}

	public void invalidateKey(Key key) {
		synchronized (cacheLock) {
			Value fromMemoryCache = memoryCache.get(key);
			if (fromMemoryCache == null) {
				// Not in memory cache. If it is currently downloading, restart
				// the download. Otherwise do nothing.
				if (isCurrentlyFetching(key)) {
					restartDownload(key);
				}
			} else {
				// In memory cache: remove from cache
				memoryCache.remove(key);
			}
		}
	}

	protected boolean isCurrentlyFetching(Key key) {
		return callbacksForKey.containsKey(key);
	}

	private void addCallback(Key key, CacheCallback<Key, Value> callback) {
		callbacksForKey.get(key).add(callback);
	}

	protected void startFetching(final Key key) {
		L.d("in startFetching, starting fething key = " + key.toString());
		// This marks that this key is currently being downloaded.
		callbacksForKey.put(key, new ArrayList<CacheCallback<Key, Value>>());

		AsyncTask<Void, Integer, Value> task = new AsyncTask<Void, Integer, Value>() {
			@Override
			protected Value doInBackground(Void... params) {
				// fetchData() is implemented outside this class, and it allows
				// the users to decide how to download data that is missing from
				// the cache.
				L.d("starting doInBackground for StartFetching with key = "
						+ key.toString());
				Value result = fetchData(key,
						new Callback<Void, Integer, Void>() {
							@Override
							public void onProgress(Integer progress) {
								publishProgress(progress);
							}
						});
				L.d("In startFetching, doInBackground, starting onAfterFetching for key = "
						+ key.toString());
				return onAfterFetching(key, result);

			}

			@Override
			protected void onPreExecute() {
				runOnPreCallbacks(key);
			}

			@Override
			protected void onProgressUpdate(Integer... progress) {
				runProgressCallbacks(key, progress[0]);
			}

			@Override
			protected void onPostExecute(Value result) {
				onDataReadyBeforeRunCallbacks(key, result);
				putInMemoryCacheAndRunCallbacks(key, result);
			}
		};

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			executeOnThreadPoolExecutor(task);
		} else {
			task.execute();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void executeOnThreadPoolExecutor(AsyncTask<Void, ?, Value> task) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void restartDownload(Key key) {
		// TODO: Cancel running download
		// Only then
		// startDownload(key);
	}

	protected void runOnPreCallbacks(Key key) {
//		List<CacheCallback<Key, Value>> callbacks;
//		synchronized (cacheLock) {
//			callbacks = callbacksForKey.get(key);
//			for (CacheCallback<Key, Value> callback : callbacks) {
//				// TODO: Are we using this??
//				// callback.onPre(null);
//
//			}
//		}
	}

	protected void runProgressCallbacks(Key key, int progress) {
		List<CacheCallback<Key, Value>> callbacks;
		synchronized (cacheLock) {
			callbacks = callbacksForKey.get(key);
			assert (callbacks != null);
			for (CacheCallback<Key, Value> callback : callbacks) {
				callback.onProgress(progress);
			}
		}
	}

	private void putInMemoryCacheAndRunCallbacks(final Key key, Value result) {
		List<CacheCallback<Key, Value>> callbacks;
		synchronized (cacheLock) {
			if (result != null) {
				memoryCache.put(key, result);
			}
			callbacks = callbacksForKey.get(key);
			callbacksForKey.remove(key);
		}
		for (CacheCallback<Key, Value> callback : callbacks) {
			callback.onValueReady(key, result);
		}
	}

	protected void putInCache(Key key, Value result) {
		synchronized (cacheLock) {
			if (result != null) {
				memoryCache.put(key, result);
			}
		}
	}

	protected void onDataReadyBeforeRunCallbacks(Key key, Value result) {
	}

	protected Value onAfterFetching(Key key, Value result) {
		// Do nothing by default; persistent cache does additional things here.
		return result;
	}

}
