package com.choozie.app.caches;

import com.choozie.app.Callback;

public abstract class CacheWrapper<Key, Value> extends Cache<Key, Value> {
	Cache<Key, Value> wrappedCache;

	public CacheWrapper(int maxSize, Cache<Key, Value> wrappedCache) {
		super(maxSize);
		this.wrappedCache = wrappedCache;
	}

//	@Override
//	protected Value fetchData(Key key,
//			Callback<Void, Integer, Void> progressCallback) {
////		if (wrappedCache.getValue(key, callback))
//	}

}
