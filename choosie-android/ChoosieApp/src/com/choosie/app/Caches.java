package com.choosie.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.choosie.app.client.FeedResponse;
import com.choosie.app.controllers.FeedCacheKey;
import com.choosie.app.controllers.SuperController;
import com.choosie.app.Models.ChoosiePostData;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
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

	/*
	 * description - gets url of an image, convert it to string and insert it as
	 * extra into the intent. input - photoUrl - url of a photo - intent - an
	 * intent. dahhh
	 */

	public void insertPhotoUriToIntent(String photoUrl, final Intent intent,
			final String key) {
		Log.i(Constants.LOG_TAG, "in insertPhotoStringToIntent");
		getPhotosCache().getValue(photoUrl,
				new Callback<Void, Object, Bitmap>() {

					@Override
					public void onFinish(Bitmap param) {
						ByteArrayOutputStream bos1 = new ByteArrayOutputStream();

						param.compress(CompressFormat.JPEG, 100, bos1);

						File f = new File(Environment
								.getExternalStorageDirectory()
								+ File.separator
								+ key);
						FileOutputStream fo = null;
						try {
							f.createNewFile();
							fo = new FileOutputStream(f);
							fo.write(bos1.toByteArray());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Uri outputFileUri = Uri.fromFile(f);

						intent.putExtra(key, outputFileUri.toString());
					}
				});
	}

	public Cache<String, ChoosiePostData> getPostsCache() {
		return this.postsCache;
	}

}
