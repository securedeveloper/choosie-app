package com.choosie.app.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.choosie.app.Callback;
import com.choosie.app.Constants;
import com.choosie.app.NewChoosiePostData;
import com.choosie.app.controllers.FeedCacheKey;
import com.choosie.app.Models.ChoosiePostData;
import com.choosie.app.Models.FacebookDetails;

public abstract class ClientBase {

	protected FacebookDetails fbDetails;

	public ClientBase(FacebookDetails fbDetails) {
		this.fbDetails = fbDetails;
	}

	public abstract void sendCommentToServer(String post_key, String text,
			final Callback<Void, Void, Boolean> callback);

	public abstract void sendVoteToServer(ChoosiePostData choosiePost,
			int whichPhoto, final Callback<Void, Void, Boolean> callback);

	public abstract void login(final Callback<Void, Void, Void> onLoginComplete);

	public Bitmap getPictureFromServerSync(final String pictureUrl,
			Callback<Void, Object, Void> progressCallback) {
		String urlToLoad = pictureUrl;
		Log.i(Constants.LOG_TAG, "getPictureFromServer: Loading URL: "
				+ urlToLoad);
		URL url;
		try {
			url = new URL(urlToLoad);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		progressCallback.onProgress(Integer.valueOf(1));
		try {
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(true);
			connection.connect();
			byte[] buffer = downloadFile(progressCallback, connection);
			progressCallback.onProgress(Integer.valueOf(99));
			Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0,
					buffer.length);
			return bitmap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public abstract FeedResponse getFeedByCursor(FeedCacheKey feedRequest,
			Callback<Void, Object, Void> progressCallback);

	public abstract void sendChoosiePostToServer(NewChoosiePostData data,
			Callback<Void, Integer, Void> progressCallback);

	public abstract ChoosiePostData getPostByKey(String param,
			Callback<Void, Object, Void> progressCallback);

	public ClientBase() {
		super();
	}

	public FacebookDetails getFacebookDetails() {
		return this.fbDetails;
	}

	private byte[] downloadFile(Callback<Void, Object, Void> progressCallback,
			HttpURLConnection connection) throws IOException {
		int imageSize = connection.getContentLength();
		// Sometimes the size isn't known: just read the stream.
		if (imageSize <= 0) {
			ByteArrayBuffer buf = new ByteArrayBuffer(1024);
			int b;
			while ((b = connection.getInputStream().read()) > -1) {
				buf.append(b);
			}
			return buf.toByteArray();
		}
		// When it is known, read the stream and public progress.
		progressCallback.onProgress(Integer.valueOf(2));
		InputStream input = connection.getInputStream();
		final int kBufferSize = 1024;
		byte[] buffer = new byte[imageSize];
		int downloaded = 0;
		int bytesRead = 0;
		while (bytesRead > -1) {
			int remaining = imageSize - downloaded;
			int toReadThisIteration = Math.min(remaining, kBufferSize);
			bytesRead = input.read(buffer, downloaded, toReadThisIteration);
			if (bytesRead >= 0) {
				downloaded += bytesRead;
				progressCallback.onProgress(100 * downloaded / imageSize);
			}
		}
		return buffer;
	}

}