package com.choosie.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import com.choosie.app.client.FeedResponse;
import com.choosie.app.controllers.FeedCacheKey;
import com.choosie.app.controllers.SuperController;
import com.choosie.app.Models.ChoosiePostData;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

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
		photosCache = new Cache<String, Bitmap>(
				new ResultCallback<Bitmap, String>() {

					@Override
					Bitmap getData(String param,
							Callback<Void, Object, Void> progressCallback) {
						return controller.getClient().getPictureFromServerSync(
								param, progressCallback);
					}
				},
				// this is the serializer
				new ResultCallback<ByteArrayOutputStream, Bitmap>() {

					@Override
					ByteArrayOutputStream getData(Bitmap param,
							Callback<Void, Object, Void> progressCallback) {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						param.compress(CompressFormat.JPEG, 100, bos);
						return bos;
					}
				},
				// this is the deserializer
				new ResultCallback<Bitmap, String>() {

					@Override
					Bitmap getData(String param,
							Callback<Void, Object, Void> progressCallback) {
						return Utils.getBitmapFromURL(param);
					}
				},
				// this is the befoer-put-in-memory-er
				new ResultCallback<Bitmap, Bitmap>() {

					@Override
					Bitmap getData(Bitmap param,
							Callback<Void, Object, Void> progressCallback) {
						return Utils.shrinkBitmapToImageViewSize(param, controller);
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

		postsCache = new Cache<String, ChoosiePostData>(
				new ResultCallback<ChoosiePostData, String>() {

					@Override
					ChoosiePostData getData(String param,
							Callback<Void, Object, Void> progressCallback) {
						return controller.getClient().getPostByKey(param,
								progressCallback);
					}
				});
	}

	public Cache<String, ChoosiePostData> getPostsCache() {
		return this.postsCache;
	}

}
