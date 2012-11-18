package com.choosie.app;

import android.graphics.Bitmap;

public class Caches {
	Cache<String, Bitmap> photosCache;

	public Caches(final SuperController controller) {
		initializePhotosCache(controller);
	}

	public Cache<String, Bitmap> getPhotosCache() {
		return photosCache;
	}

	private void initializePhotosCache(final SuperController controller) {
		photosCache = new Cache<String, Bitmap>(
				new ResultCallback<Bitmap, String>() {

					@Override
					Bitmap getData(String param,
							Callback<Object, Void> progressCallback) {
						return controller.getClient().getPictureFromServerSync(
								param, progressCallback);
					}
				});
	}

}
