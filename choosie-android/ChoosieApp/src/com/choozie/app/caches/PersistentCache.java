package com.choozie.app.caches;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.choozie.app.Callback;
import com.choozie.app.L;
import com.choozie.app.Utils;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;

public abstract class PersistentCache<Key, Value> extends Cache<Key, Value> {
	protected abstract ByteArrayOutputStream serialize(Value value);

	protected abstract Value readFromSdCard(Key key,
			Callback<Void, Integer, Void> progressCallback);

	protected abstract Value beforePutInMemory(Value result);

	protected abstract Value downloadData(Key key,
			Callback<Void, Integer, Void> progressCallback);

	public PersistentCache(int maxSize) {
		super(maxSize);
	}

	@Override
	protected Value fetchData(Key key,
			Callback<Void, Integer, Void> progressCallback) {
		L.d("in persistentache - pething key = " + key.toString());
		// Check if available on SD
		if (isPersisted(key)) {
			L.d("in persistentache , key is persisted, reading from SD!! = "
					+ key.toString());
			return readFromSdCard(key, progressCallback);
		}
		L.d("in persistentache , key is NOT persisted, downloading it!! = "
				+ key.toString());
		return downloadData(key, progressCallback);
	}

	@Override
	protected Value onAfterFetching(Key key, Value result) {
		L.d("in persistentache - onAfterFetching = " + key.toString()
				+ " result = " + result);
		if (!isPersisted(key) && result != null) {
			L.d("in onAfterFetching , key is not persisted, need to save it = "
					+ key.toString());
			savePersistent(key, result);
		}
		return beforePutInMemory(result);
	}

	/*
	 * saves on sd params: key - this will be the file name value - what to save
	 */
	private void savePersistent(Key key, Value value) {
		String fileName = (String) Integer.toString(key.toString().hashCode());
		ByteArrayOutputStream bos = serialize(value);
		Utils.writeByteStreamOnSD(bos, fileName);
	}

	private boolean isPersisted(Key key) {
		String fileName = (String) Integer.toString(key.toString().hashCode());
		return Utils.isFileExists(fileName);
	}
}
