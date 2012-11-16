package com.choosie.app;

import java.util.List;

import com.choosie.app.ChoosieClient.ChoosiePostData;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class FeedScreenController extends ScreenController {

	public FeedScreenController(View layout) {
		super(layout);
	}

	@Override
	protected void onShow() {
		Log.i(ChoosieConstants.LOG_TAG, "Feed.onShow()");
		refreshFeed();
	}

	private void refreshFeed() {
		this.client.getFeedFromServer(new Callback<List<ChoosiePostData>>() {
			
			@Override
			void handleOperationFinished(List<ChoosiePostData> param) {
				loadPosts(param);
			}
		});
	}

	protected void loadPosts(List<ChoosiePostData> param) {
		Log.i(ChoosieConstants.LOG_TAG, "Feed before getPictureFromServer");
		if (param.size() == 0) {
			Log.i(ChoosieConstants.LOG_TAG, "No images in feed.");
		}
		String urlToLoad = param.get(param.size() - 1).photo1URL;
		this.client.getPictureFromServer(urlToLoad, new Callback<Bitmap>() {
			
			@Override
			void handleOperationFinished(Bitmap param) {
				Log.i(ChoosieConstants.LOG_TAG, "Feed after getPictureFromServer");
				ImageView imageView = (ImageView)view.findViewById(R.id.feedimage);
				imageView.setImageBitmap(param);
			}
		});
	}

	@Override
	protected void onHide() {
		// TODO Auto-generated method stub
		
	}

}
