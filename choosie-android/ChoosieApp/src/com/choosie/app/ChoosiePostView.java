package com.choosie.app;

import com.choosie.app.ChoosieClient.ChoosiePostData;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChoosiePostView extends RelativeLayout {
	private ChoosiePostData choosiePost;
	private SuperController superController;

	public ChoosiePostView(Context context, SuperController superController) {
		super(context);
		inflateLayout();
		this.superController = superController;
	}

	private void inflateLayout() {
		Log.i(ChoosieConstants.LOG_TAG, "ChoosiePostView: inflateLayout");
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_choosie_post, this);

		this.findViewById(R.id.votes1).setOnClickListener(
				new OnClickListener() {
					public void onClick(View arg0) {
						superController.voteFor(choosiePost, 1);
					}
				});
		this.findViewById(R.id.votes2).setOnClickListener(
				new OnClickListener() {
					public void onClick(View arg0) {
						superController.voteFor(choosiePost, 2);
					}
				});
	}

	public void loadChoosiePost(ChoosiePostData post) {
		this.choosiePost = post;
		((TextView) findViewById(R.id.votes1)).setText(post.votes1 + " votes");
		((TextView) findViewById(R.id.votes2)).setText(post.votes2 + " votes");
		((TextView) findViewById(R.id.feedtext)).setText(post.question);
		((ImageView) findViewById(R.id.feedimage1)).setVisibility(View.GONE);
		((ImageView) findViewById(R.id.feedimage2)).setVisibility(View.GONE);

		loadImageToView(post.photo1URL,
				(ImageView) findViewById(R.id.feedimage1),
				(ProgressBar) findViewById(R.id.progressBar1));
		loadImageToView(post.photo2URL,
				(ImageView) findViewById(R.id.feedimage2),
				(ProgressBar) findViewById(R.id.progressBar2));
	}

	private void loadImageToView(String urlToLoad, final ImageView imageView,
			final ProgressBar progressBar) {
		this.superController.getCaches().getPhotosCache()
				.getValue(urlToLoad, new Callback<Void, Object, Bitmap>() {
					@Override
					void onFinish(Bitmap param) {
						Log.i(ChoosieConstants.LOG_TAG,
								"Feed after getPictureFromServer");
						imageView.setImageBitmap(param);
						imageView.setVisibility(View.VISIBLE);
						progressBar.setVisibility(View.GONE);
					}

					@Override
					void onProgress(Object progress) {
						if (!(progress instanceof Integer)) {
							Log.e(ChoosieConstants.LOG_TAG, "Y u no integer???");
							return;
						}
						progressBar.setProgress((Integer) progress);
						progressBar.setMax(100);
					}
				});
	}
}
