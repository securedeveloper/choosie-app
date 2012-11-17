package com.choosie.app;

import android.graphics.Bitmap;

public class Caches {
	Cache<String, Bitmap> picturesCache;

	public Caches(final SuperController controller) {
		picturesCache = new Cache<String, Bitmap>(
				new ResultCallback<Bitmap, String>() {

					@Override
					Bitmap getData(String param) {
						return controller.getClient().getPictureFromServerSync(
								param);
					}
				});
	}

	public void getPictureFromServer(String urlToLoad,
			Callback<?, Bitmap> callback) {
		picturesCache.getValue(urlToLoad, callback);
	}
}
