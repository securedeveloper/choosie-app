package com.choosie.app.caches;

import java.io.ByteArrayOutputStream;

import com.choosie.app.Callback;
import com.choosie.app.Utils;
import com.choosie.app.client.FeedResponse;
import com.choosie.app.controllers.FeedCacheKey;
import com.choosie.app.controllers.SuperController;
import com.choosie.app.Models.ChoosiePostData;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class Caches {
	Cache<String, Bitmap> photosCache;
	private Cache<FeedCacheKey, FeedResponse> feedCache;
	private Cache<String, ChoosiePostData> postsCache;

	public Caches(final SuperController controller) {
		initializeCaches(controller);
	}

	public Cache<String, Bitmap> getPhotosCache() {
		return photosCache;
	}

	public Cache<FeedCacheKey, FeedResponse> getFeedCache() {
		return feedCache;
	}

	private void initializeCaches(final SuperController controller) {
		int photoCacheMaxSize = 10 * 1024 * 1024;
		photosCache = new PersistentCache<String, Bitmap>(photoCacheMaxSize) {

			@Override
			protected ByteArrayOutputStream serialize(Bitmap value) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				value.compress(CompressFormat.JPEG, 100, bos);
				return bos;
			}

			@Override
			protected Bitmap readFromSdCard(String key,
					Callback<Void, Object, Void> progressCallback) {
				return Utils.getBitmapFromURL(key, progressCallback);
			}

			@Override
			protected Bitmap beforePutInMemory(Bitmap result) {
				return Utils.shrinkBitmapToImageViewSize(result, controller);
			}

			@Override
			protected Bitmap downloadData(String key,
					Callback<Void, Object, Void> progressCallback) {
				return controller.getClient().getPictureFromServerSync(key,
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
					Callback<Void, Object, Void> progressCallback) {
				return controller.getClient().getFeedByCursor(key,
						progressCallback);
			}
		};

		int numberOfChoosiePostDatasToKeepInCache = 40;
		postsCache = new Cache<String, ChoosiePostData>(
				numberOfChoosiePostDatasToKeepInCache) {

			@Override
			protected ChoosiePostData fetchData(String key,
					Callback<Void, Object, Void> progressCallback) {
				return controller.getClient().getPostByKey(key,
						progressCallback);
			}
		};
	}

	public Cache<String, ChoosiePostData> getPostsCache() {
		return this.postsCache;
	}

}
