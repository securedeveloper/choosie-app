package com.choosie.app;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

public class Cache<Key, Value> {
	final ResultCallback<Value, Key> downloader;
	final ResultCallback<ByteArrayOutputStream, Value> serializer;
	final ResultCallback<Value, Key> deSerializer;
	final ResultCallback<Value, Value> beforePutInMemory;
	final Object cacheLock = new Object();
	final Map<Key, Value> memoryCache = new HashMap<Key, Value>();
	final Map<Key, List<Callback<Void, Object, Value>>> callbacksForKey = new HashMap<Key, List<Callback<Void, Object, Value>>>();
	final Boolean persistent;

	public Cache(ResultCallback<Value, Key> downloader,
			ResultCallback<ByteArrayOutputStream, Value> serializer,
			final ResultCallback<Value, Key> deSerializer,
			ResultCallback<Value, Value> beforePutInMemory) {
		this.downloader = downloader;
		this.serializer = serializer;
		this.deSerializer = deSerializer;
		this.persistent = true;
		this.beforePutInMemory = beforePutInMemory;
	}

	public Cache(ResultCallback<Value, Key> downloader) {
		this.downloader = downloader;
		this.serializer = null;
		this.deSerializer = null;
		this.persistent = false;
		this.beforePutInMemory = null;
	}

	public void getValue(Key key, Callback<Void, Object, Value> callback) {
		Value fromMemoryCache;
		synchronized (cacheLock) {
			fromMemoryCache = memoryCache.get(key);
			if (fromMemoryCache == null) {

				// if in persistent - check if available on SD
				if ((persistent == true) && (isPersisted(key) == true)) {
					Value fromSdcard = deSerializer.getData(key, null);
//					Value inMemoryVersion = beforePutInMemory.getData(fromSdcard, null);
					memoryCache.put(key, fromSdcard);
					fromMemoryCache = memoryCache.get(key);
				} else {
					// Not in memory cache and not persisted
					if (!isCurrentlyDownloading(key)) {
						// In case this is the first time this key is
						// encountered,
						// initiate downloading it in the background.
						startDownload(key);
					}
					// Make sure that this callback is run when the download
					// is
					// finished.
					addCallback(key, callback);
					return;
				}
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
				savePersistentAndRunCallbacks(key, result);
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

	private void savePersistentAndRunCallbacks(final Key key, Value result) {
		List<Callback<Void, Object, Value>> callbacks;
		synchronized (cacheLock) {
			if (persistent == true) {
				savePersistent(key, result, serializer);
				result = beforePutInMemory.getData(result, null);
			}
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

	/*
	 * saves on sd params: key - this will be the file name value - what to save
	 */
	private void savePersistent(Key key, Value value,
			ResultCallback<ByteArrayOutputStream, Value> serializer) {

		String fileName = (String) Integer.toString(key.toString().hashCode());

		ByteArrayOutputStream bos = serializer.getData(value, null);

		Utils.writeByteStreamOnSD(bos, fileName);
	}

	private boolean isPersisted(Key key) {
		String fileName = (String) Integer.toString(key.toString().hashCode());
		return Utils.isFileExists(fileName);
	}
}
