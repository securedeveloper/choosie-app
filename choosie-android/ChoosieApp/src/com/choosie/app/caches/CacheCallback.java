package com.choosie.app.caches;

public abstract class CacheCallback<Key, Value> {

	public void onValueReady(Key key, Value result) {
	}

	public void onProgress(int percent) {
	}
}
