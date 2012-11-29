package com.choosie.app.views;

import java.util.List;

import com.choosie.app.Callback;
import com.choosie.app.Comment;
import com.choosie.app.Constants;
import com.choosie.app.R;
import com.choosie.app.Screen;
import com.choosie.app.Models.*;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;

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

		final TextView votes1 = (TextView) findViewById(R.id.votes1);
		final TextView votes2 = (TextView) findViewById(R.id.votes2);

		((TextView) findViewById(R.id.feedtext)).setText(post.getQuestion());
		((TextView) findViewById(R.id.feed_name)).setText(post.getUserName());

		final ImageView imgView1 = (ImageView) findViewById(R.id.feedimage1);
		final ImageView imgView2 = (ImageView) findViewById(R.id.feedimage2);

		imgView1.setVisibility(View.GONE);
		imgView2.setVisibility(View.GONE);

		((ImageView) findViewById(R.id.feed_userimage))
				.setVisibility(View.GONE);

		loadImageToView(post.getPhoto1URL(), imgView1,
				(ProgressBar) findViewById(R.id.progressBar1));
		loadImageToView(post.getPhoto2URL(), imgView2,
				(ProgressBar) findViewById(R.id.progressBar2));
		loadImageToView(post.getUserPhotoURL(),
				(ImageView) findViewById(R.id.feed_userimage), null);
		loadCommentsToView(post);

		// DECIDE IF SHOW RESUTLS OR NOT
		if (choosiePost.isVotedAlready() || choosiePost.isPostByMe()) {
			ChangeVotingResultsVisability(votes1, votes2, View.VISIBLE);
		} else {
			ChangeVotingResultsVisability(votes1, votes2, View.INVISIBLE);
		}

		imgView1.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {
				Log.i(Constants.LOG_TAG, "onLongClick signaled for voting 1");

				if (!choosiePost.isVotedAlready(1)) {
					Log.i(Constants.LOG_TAG, "voting 1 (Not voted 1 yet)");
					superController.voteFor(choosiePost, 1);

					// SHOW VOTES RESULTS
					ChangeVotingResultsVisability(votes1, votes2, View.VISIBLE);
					
					// Set border for relevant image
					Log.i(Constants.LOG_TAG, "Setting border for image 1");
					SetImageBorder(imgView1 , true);
					SetImageBorder(imgView2, false);
					return true;
				}
				Log.i(Constants.LOG_TAG, "Already voted for 1. vote not sent");
				return false;
			}
		});

		imgView2.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {
				Log.i(Constants.LOG_TAG, "onLongClick signaled for voting 2");

				if (!choosiePost.isVotedAlready(2)) {
					Log.i(Constants.LOG_TAG, "voting 2 (Not voted 2 yet)");
					superController.voteFor(choosiePost, 2);

					// SHOW VOTES RESULTS
					ChangeVotingResultsVisability(votes1, votes2, View.VISIBLE);
					
					// Set border for relevant image
					Log.i(Constants.LOG_TAG, "Setting border for image 2");
					SetImageBorder(imgView2 , true);
					SetImageBorder(imgView1, false);
					return true;
				}
				Log.i(Constants.LOG_TAG, "Already voted for 2. vote not sent");
				return false;
			}
		});
	}
	
	private void SetImageBorder(ImageView imgView, boolean isBorderVisable) {

		if (isBorderVisable) {
			imgView.setBackgroundResource(R.drawable.image_selected);
		} else {
			imgView.setBackgroundResource(R.drawable.image_not_selected);
		}
	}

	private void ChangeVotingResultsVisability(TextView votes1,
			TextView votes2, int visability) {

		// If Visablity=true then count votes and display them
		if (visability == View.VISIBLE) {

			int voteCount1 = choosiePost.CountVotes(1);
			int voteCount2 = choosiePost.CountVotes(2);

			votes1.setText(voteCount1 + " Votes");
			votes2.setText(voteCount2 + " Votes");

			choosiePost.setVotes1(voteCount1);
			choosiePost.setVotes2(voteCount2);
		}
		votes1.setVisibility(visability);
		votes2.setVisibility(visability);

	}

	private void loadCommentsToView(ChoosiePostData post) {
		LinearLayout commentLayout = (LinearLayout) findViewById(R.id.layout_comments);
		List<Comment> lstComment = post.getLstComment();
		for (Comment comment : lstComment) {
			TextView tv = new TextView(superController.getControllerForScreen(
					Screen.FEED).getActivity());
			// tv.setText("Bar Refaeli: " + comment.getText());
			final SpannableStringBuilder sb = new SpannableStringBuilder(
					"Bar Refaeli: " + comment.getText());
			final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(
					0, 0, 255));

			// Span to make text bold
			sb.setSpan(fcs, 0, 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

			// Span to set text color to some RGB value
			final StyleSpan bss = new StyleSpan(
					android.graphics.Typeface.ITALIC);

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
