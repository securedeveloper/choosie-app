package com.choosie.app;

import android.graphics.Bitmap;

public class Caches {
	Cache<String, Bitmap> picturesCache;

	public Caches(final SuperController controller) {
		picturesCache = new Cache<String, Bitmap>(
				new ResultCallback<Bitmap, String>() {

					@Override
					Bitmap getData(String param,
							Callback<Object, Void> progressCallback) {
						return controller.getClient().getPictureFromServerSync(
								param, progressCallback);
					}
				});
	}

	public Cache<String, Bitmap> getPictureCache() {
		return picturesCache;
	}
}
