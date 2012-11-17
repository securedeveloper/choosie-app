package com.choosie.app;

import com.choosie.app.ChoosieClient.ChoosiePostData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
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

	public ChoosiePostData getChoosiePost() {
		return choosiePost;
	}

	public void loadChoosiePost(ChoosiePostData post) {
		this.choosiePost = post;
		((TextView) findViewById(R.id.votes1)).setText(post.votes1 + " votes");
		((TextView) findViewById(R.id.votes2)).setText(post.votes2 + " votes");
		((TextView) findViewById(R.id.feedtext)).setText(post.question);
		((ImageView) findViewById(R.id.feedimage1))
				.setImageBitmap(BitmapFactory.decodeResource(getContext()
						.getResources(), R.drawable.ic_launcher));
		((ImageView) findViewById(R.id.feedimage2))
				.setImageBitmap(BitmapFactory.decodeResource(getContext()
						.getResources(), R.drawable.ic_launcher));

		loadImageToView(post.photo1URL,
				(ImageView) findViewById(R.id.feedimage1));
		loadImageToView(post.photo2URL,
				(ImageView) findViewById(R.id.feedimage2));
	}

	private void loadImageToView(String urlToLoad, final ImageView imageView) {
		this.superController.getCaches().getPictureFromServer(urlToLoad,
				new Callback<Void, Bitmap>() {

					@Override
					void onOperationFinished(Bitmap param) {
						Log.i(ChoosieConstants.LOG_TAG,
								"Feed after getPictureFromServer");
						imageView.setImageBitmap(param);
					}
				});
	}
}
