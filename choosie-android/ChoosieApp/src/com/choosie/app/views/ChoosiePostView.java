package com.choosie.app.views;

import java.util.List;

import com.choosie.app.Callback;
import com.choosie.app.Comment;
import com.choosie.app.Constants;
import com.choosie.app.R;
import com.choosie.app.Screen;
import com.choosie.app.Models.*;
import com.choosie.app.R.id;
import com.choosie.app.R.layout;
import com.choosie.app.controllers.SuperController;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_choosie_post, this);

		this.findViewById(R.id.button_to_comment).setOnClickListener(
				new OnClickListener() {
					public void onClick(View arg0) {
						superController.switchToCommentScreen(choosiePost);
					}
				});
	}

	public void loadChoosiePost(ChoosiePostData post) {
		this.choosiePost = post;
		((TextView) findViewById(R.id.votes1)).setText(post.getVotes1()
				+ " votes");
		((TextView) findViewById(R.id.votes2)).setText(post.getVotes2()
				+ " votes");
		((TextView) findViewById(R.id.feedtext)).setText(post.getQuestion());
		((TextView) findViewById(R.id.feed_name)).setText(post.getUserName());
		((ImageView) findViewById(R.id.feedimage1)).setVisibility(View.GONE);
		((ImageView) findViewById(R.id.feedimage2)).setVisibility(View.GONE);
		((ImageView) findViewById(R.id.feed_userimage))
				.setVisibility(View.GONE);

		loadImageToView(post.getPhoto1URL(),
				(ImageView) findViewById(R.id.feedimage1),
				(ProgressBar) findViewById(R.id.progressBar1));
		loadImageToView(post.getPhoto2URL(),
				(ImageView) findViewById(R.id.feedimage2),
				(ProgressBar) findViewById(R.id.progressBar2));
		loadImageToView(post.getUserPhotoURL(),
				(ImageView) findViewById(R.id.feed_userimage), null);
		loadCommentsToView(post);
		
		if (choosiePost.isPostByMe()) {
			// TODO:show results
			return;
		}

		if (!choosiePost.isVotedAlready(1)) {
			this.findViewById(R.id.votes1).setOnClickListener(
					new OnClickListener() {
						public void onClick(View arg0) {
							superController.voteFor(choosiePost, 1);
						}
					});
		}
		if (!choosiePost.isVotedAlready(1)) {
			this.findViewById(R.id.votes2).setOnClickListener(
					new OnClickListener() {
						public void onClick(View arg0) {
							superController.voteFor(choosiePost, 2);
						}
					});
		}
	}
	
	private void loadCommentsToView(ChoosiePostData post) {
		LinearLayout commentLayout = (LinearLayout) findViewById(R.id.layout_comments);
		List<Comment> lstComment = post.getLstComment();
		for (Comment comment : lstComment) {
			TextView tv = new TextView(
					superController.getControllerForScreen(Screen.FEED).getActivity());
			// tv.setText("Bar Refaeli: " + comment.getText());
			final SpannableStringBuilder sb = new SpannableStringBuilder(
					"Bar Refaeli: " + comment.getText());
			final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(
					0, 0, 255));

			 // Span to make text bold
			   sb.setSpan(fcs, 0, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
			
			// Span to set text color to some RGB value
			final StyleSpan bss = new StyleSpan(android.graphics.Typeface.ITALIC);

			// Set the text color for first 4 characters
			sb.setSpan(bss, 0, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

			tv.setText(sb);
			
			commentLayout.addView(tv);
		}
	}


	private void loadImageToView(String urlToLoad, final ImageView imageView,
			final ProgressBar progressBar) {
		this.superController.getCaches().getPhotosCache()
				.getValue(urlToLoad, new Callback<Void, Object, Bitmap>() {
					@Override
					public void onFinish(Bitmap param) {
						imageView.setImageBitmap(param);
						imageView.setVisibility(View.VISIBLE);
						if (progressBar != null) {
							progressBar.setVisibility(View.GONE);
						}
					}

					@Override
					public void onProgress(Object progress) {
						if (!(progress instanceof Integer)) {
							Log.e(Constants.LOG_TAG, "Y u no integer???");
							return;
						}
						if (progressBar != null) {
							progressBar.setProgress((Integer) progress);
							progressBar.setMax(100);
						}
					}
				});
	}

}
