package com.choosie.app;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;

public abstract class PersistentCache<Key, Value> extends Cache<Key, Value> {
	protected abstract ByteArrayOutputStream serialize(Value value);

	protected abstract Value readFromSdCard(Key key,
			Callback<Void, Object, Void> progressCallback);

	protected abstract Value beforePutInMemory(Value result);

	protected abstract Value downloadData(Key key,
			Callback<Void, Object, Void> progressCallback);

	public PersistentCache(int maxSize) {
		super(maxSize);
	}

	@Override
	protected Value fetchData(Key key,
			Callback<Void, Object, Void> progressCallback) {
		// Check if available on SD
		if (isPersisted(key)) {
			return readFromSdCard(key, progressCallback);
		}
		return downloadData(key, progressCallback);
	}

	@Override
	protected Value onAfterFetching(Key key, Value result) {
		if (!isPersisted(key)) {
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
