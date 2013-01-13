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
	private class PhotosCache extends PersistentCache<String, Bitmap> {
		private PhotosCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected ByteArrayOutputStream serialize(Bitmap value) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			value.compress(CompressFormat.JPEG, 100, bos);
			return bos;
		}

		@Override
		protected Bitmap readFromSdCard(String key,
				Callback<Void, Integer, Void> progressCallback) {
			Logger.d("in persistent, reading from Sd, key = " + key.toString());
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
	}

	private Cache<FeedCacheKey, FeedResponse> feedCache;
	private Cache<String, ChoosiePostData> postsCache;
	private Cache<String, Bitmap> photosCache;
	private Cache<String, Bitmap> blurredCache;
	private PhotosCache blurredPhotosCache;

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

	public Cache<String, Bitmap> getBlurredPhotosCache() {
		return blurredPhotosCache;
	}

	public Cache<FeedCacheKey, FeedResponse> getFeedCache() {
		return feedCache;
	}

	public Cache<String, ChoosiePostData> getPostsCache() {
		return this.postsCache;
	}

	private void initializeCaches() {
		int photoCacheMaxSize = 8 * 1024 * 1024; // 10 MB
		photosCache = new PhotosCache(photoCacheMaxSize);

		int blurredPhotoCacheMaxSize = 2 * 1024 * 1024; // 10 MB
		blurredPhotosCache = new PhotosCache(blurredPhotoCacheMaxSize) {
			@Override
			protected Bitmap beforePutInMemory(Bitmap result) {
				result = Utils.fastblur(result, 20);
				return super.beforePutInMemory(result);
			}
		};

		// Temp: feedCache is disabled by changing this to 1.
		int numberOfFeedResponsesToKeepInCache = 1;
		feedCache = new Cache<FeedCacheKey, FeedResponse>(
				numberOfFeedResponsesToKeepInCache) {

			@Override
			protected FeedResponse fetchData(FeedCacheKey key,
					Callback<Void, Integer, Void> progressCallback) {
				return Client.getInstance().getFeedByCursor(key,
						progressCallback);
			}

			@Override
			protected void onDataReadyBeforeRunCallbacks(FeedCacheKey key,
					FeedResponse result) {
				if (result != null && result.getPosts() != null) {
					for (ChoosiePostData post : result.getPosts()) {
						postsCache.putInCache(post.getPostKey(), post);
					}
				}
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
