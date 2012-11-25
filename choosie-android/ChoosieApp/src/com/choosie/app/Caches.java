package com.choosie.app;

import com.choosie.app.client.FeedResponse;
import com.choosie.app.controllers.FeedCacheKey;
import com.choosie.app.controllers.SuperController;

import android.graphics.Bitmap;

public class Caches {
	Cache<String, Bitmap> photosCache;
	private Cache<FeedCacheKey, FeedResponse> feedCache;

	public Caches(final SuperController controller) {
		initializePhotosCache(controller);
	}

	public Cache<String, Bitmap> getPhotosCache() {
		return photosCache;
	}

	public Cache<FeedCacheKey, FeedResponse> getFeedCache() {
		return feedCache;
	}

	private void initializePhotosCache(final SuperController controller) {
		photosCache = new Cache<String, Bitmap>(
				new ResultCallback<Bitmap, String>() {

					@Override
					Bitmap getData(String param,
							Callback<Void, Object, Void> progressCallback) {
						return controller.getClient().getPictureFromServerSync(
								param, progressCallback);
					}
				});

		feedCache = (new Cache<FeedCacheKey, FeedResponse>(
				new ResultCallback<FeedResponse, FeedCacheKey>() {

					@Override
					FeedResponse getData(FeedCacheKey param,
							Callback<Void, Object, Void> progressCallback) {
						return controller.getClient().getFeedByCursor(param,
								progressCallback);
					}
				}));
	}

}
