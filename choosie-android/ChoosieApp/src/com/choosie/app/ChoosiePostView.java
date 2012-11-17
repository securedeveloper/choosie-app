package com.choosie.app;

import com.choosie.app.ChoosieClient.ChoosiePostData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChoosiePostView extends RelativeLayout {
	private ChoosiePostData choosiePost;

	public ChoosiePostView(Context context) {
		super(context);
		inflateLayout();
	}

	public ChoosiePostView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflateLayout();
	}

	public ChoosiePostView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inflateLayout();
	}

	private void inflateLayout() {
		Log.i(ChoosieConstants.LOG_TAG, "ChoosiePostView: inflateLayout");
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_choosie_post, this);
	}

	public ChoosiePostData getChoosiePost() {
		return choosiePost;
	}

	public void loadChoosiePost(ChoosieClient client, ChoosiePostData post) {
		this.choosiePost = post;
		((TextView)findViewById(R.id.votes1)).setText(post.votes1 + " votes");
		((TextView)findViewById(R.id.votes2)).setText(post.votes2 + " votes");
		((TextView)findViewById(R.id.feedtext)).setText(post.question);
		((ImageView)findViewById(R.id.feedimage1)).setImageBitmap(
				BitmapFactory.decodeResource(
				getContext().getResources(), R.drawable.ic_launcher));
		((ImageView)findViewById(R.id.feedimage2)).setImageBitmap(
				BitmapFactory.decodeResource(
				getContext().getResources(), R.drawable.ic_launcher));
		
		
		loadImageToView(client, post.photo1URL, (ImageView)findViewById(R.id.feedimage1));
		loadImageToView(client, post.photo2URL, (ImageView)findViewById(R.id.feedimage2));
	}
	
	private void loadImageToView(ChoosieClient client, String urlToLoad, final ImageView imageView) {
		client.getPictureFromServer(urlToLoad, new Callback<Bitmap>() {

			@Override
			void onOperationFinished(Bitmap param) {
				Log.i(ChoosieConstants.LOG_TAG,
						"Feed after getPictureFromServer");
				imageView.setImageBitmap(param);
			}
		});
	}


}
