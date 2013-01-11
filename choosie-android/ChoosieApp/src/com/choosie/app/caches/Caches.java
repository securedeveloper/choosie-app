package com.choosie.app.caches;

import java.io.ByteArrayOutputStream;

import com.choosie.app.Callback;
import com.choosie.app.Logger;
import com.choosie.app.Utils;
import com.choosie.app.client.Client;
import com.choosie.app.client.FeedResponse;
import com.choosie.app.controllers.FeedCacheKey;
import com.choosie.app.controllers.SuperController;
import com.choosie.app.Models.ChoosiePostData;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class Caches {
	private Cache<FeedCacheKey, FeedResponse> feedCache;
	private Cache<String, ChoosiePostData> postsCache;
	private Cache<String, Bitmap> photosCache;

	private static Caches instance = new Caches();

	private Caches() {
		initializeCaches();
	}

	public static Caches getInstance() {
		return instance;
	}

	public Cache<String, Bitmap> getPhotosCache() {
		return photosCache;
	}

	public Cache<FeedCacheKey, FeedResponse> getFeedCache() {
		return feedCache;
	}

	public Cache<String, ChoosiePostData> getPostsCache() {
		return this.postsCache;
	}

	private void initializeCaches() {
		int photoCacheMaxSize = 10 * 1024 * 1024; // 10 MB
		photosCache = new PersistentCache<String, Bitmap>(photoCacheMaxSize) {

			@Override
			protected ByteArrayOutputStream serialize(Bitmap value) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				value.compress(CompressFormat.JPEG, 100, bos);
				return bos;
			}

			@Override
			protected Bitmap readFromSdCard(String key,
					Callback<Void, Integer, Void> progressCallback) {
				Logger.d("in persistent, reading from Sd, key = "
						+ key.toString());
				return Utils.getBitmapFromURL(key, progressCallback);
			}

			@Override
			protected Bitmap beforePutInMemory(Bitmap result) {
				Logger.d("in persistent , beforePutInMemory with result = "
						+ result);
				if (result == null) {
					return null;
				}
				return Utils.shrinkBitmapToImageViewSizeIfNeeded(result);
			}

			@Override
			protected Bitmap downloadData(String key,
					Callback<Void, Integer, Void> progressCallback) {
				Logger.d("in persistent, downloading, key = " + key.toString());
				return Client.getInstance().getPictureFromServerSync(key,
						progressCallback);
			}

			@Override
			protected int calcSizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};

		int numberOfFeedResponsesToKeepInCache = 15;
		feedCache = new Cache<FeedCacheKey, FeedResponse>(
				numberOfFeedResponsesToKeepInCache) {

			@Override
			protected FeedResponse fetchData(FeedCacheKey key,
					Callback<Void, Integer, Void> progressCallback) {
				return Client.getInstance().getFeedByCursor(key,
						progressCallback);
			}
		};

		int numberOfChoosiePostDatasToKeepInCache = 40;
		postsCache = new Cache<String, ChoosiePostData>(
				numberOfChoosiePostDatasToKeepInCache) {

			@Override
			protected ChoosiePostData fetchData(String key,
					Callback<Void, Integer, Void> progressCallback) {
				return Client.getInstance().getPostByKey(key, progressCallback);
			}
		};
	}

}
