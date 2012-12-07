package com.choosie.app.views;

import java.util.Date;
import java.util.List;

import com.choosie.app.Callback;
import com.choosie.app.Constants;
import com.choosie.app.R;
import com.choosie.app.Screen;
import com.choosie.app.Utils;
import com.choosie.app.controllers.SuperController;
import com.choosie.app.Models.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Debug;
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
		final TextView feedtext = (TextView) findViewById(R.id.feedtext);
		final TextView feed_name = (TextView) findViewById(R.id.feed_name);
		final TextView time_text = (TextView) findViewById(R.id.time_text);
		final ImageView feed_userimage = (ImageView) findViewById(R.id.feed_userimage);
		final ImageView imgView1 = (ImageView) findViewById(R.id.feedimage1);
		final ImageView imgView2 = (ImageView) findViewById(R.id.feedimage2);
		final ProgressBar progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		final ProgressBar progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);

		feedtext.setText(post.getQuestion());
		feed_name.setText(post.getAuthor().getUserName());
		// Utils.getTimeDifferenceTextFromNow formats the text as '13d', '2h',
		// etc
		time_text.setText(Utils.getTimeDifferenceTextFromNow(post
				.getCreatedAt()));

		imgView1.setVisibility(View.GONE);
		imgView2.setVisibility(View.GONE);

		feed_userimage.setVisibility(View.GONE);

		loadImageToView(post.getPhoto1URL(), imgView1, progressBar1);
		loadImageToView(post.getPhoto2URL(), imgView2, progressBar2);
		loadImageToView(post.getAuthor().getPhotoURL(), feed_userimage, null);
		loadCommentsToView(post);

		// DECIDE IF SHOW RESUTLS OR NOT
		if (choosiePost.isVotedAlready() || choosiePost.isPostByMe()) {
			ChangeVotingResultsVisibility(votes1, votes2, View.VISIBLE);
		} else {
			ChangeVotingResultsVisibility(votes1, votes2, View.INVISIBLE);
		}

		// Set border for voted image
		setImageBorder(imgView1, choosiePost.isVotedAlready(1));
		setImageBorder(imgView2, choosiePost.isVotedAlready(2));

		// TODO: Merge both listeners below to a single one that accepts an
		// argument.
		imgView1.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				Log.i(Constants.LOG_TAG, "onLongClick signaled for voting 1");

				if (!choosiePost.isVotedAlready(1)) {
					Log.i(Constants.LOG_TAG, "voting 1 (Not voted 1 yet)");
					superController.voteFor(choosiePost, 1);

					// SHOW VOTES RESULTS
					ChangeVotingResultsVisibility(votes1, votes2, View.VISIBLE);

					// Set border for relevant image
					Log.i(Constants.LOG_TAG, "Setting border for image 1");
					setImageBorder(imgView1, true);
					setImageBorder(imgView2, false);
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
					ChangeVotingResultsVisibility(votes1, votes2, View.VISIBLE);

					// Set border for relevant image
					Log.i(Constants.LOG_TAG, "Setting border for image 2");
					setImageBorder(imgView2, true);
					setImageBorder(imgView1, false);
					return true;
				}
				Log.i(Constants.LOG_TAG, "Already voted for 2. vote not sent");
				return false;
			}
		});
	}

	private void setImageBorder(ImageView imgView, boolean isBorderVisable) {
		if (isBorderVisable) {
			imgView.setBackgroundResource(R.drawable.image_selected);
		} else {
			imgView.setBackgroundResource(R.drawable.image_not_selected);
		}
	}

	private void ChangeVotingResultsVisibility(TextView votes1,
			TextView votes2, int visibility) {

		// If visibility==true then show vote count
		if (visibility == View.VISIBLE) {

			int voteCount1 = choosiePost.getVotes1();
			int voteCount2 = choosiePost.getVotes2();

			votes1.setText(voteCount1 + " Votes");
			votes2.setText(voteCount2 + " Votes");
		}
		votes1.setVisibility(visibility);
		votes2.setVisibility(visibility);

	}

	private void loadCommentsToView(ChoosiePostData post) {
		final LinearLayout commentLayout = (LinearLayout) findViewById(R.id.layout_comments);
		List<Comment> lstComment = post.getComments();
		for (Comment comment : lstComment) {
			// first, save commentier photo on sd
			saveURLonSD(comment.getUser().getPhotoURL());
			// build view for vurrent comment
			View commentView = buildViewForComment(comment);
			commentLayout.addView(commentView);
		}
	}

	private void saveURLonSD(final String photoURL) {
		this.superController.getCaches().getPhotosCache()
				.getValue(photoURL, new Callback<Void, Object, Bitmap>() {
					@Override
					public void onFinish(Bitmap param) {
						Utils.getInstance().saveBitmapOnSd(photoURL, param);
					}
				});
	}

	private TextView buildViewForComment(Comment comment) {
		TextView tv = new TextView(superController.getControllerForScreen(
				Screen.FEED).getActivity());

		final SpannableStringBuilder sb = new SpannableStringBuilder(comment
				.getUser().getUserName() + " " + comment.getText());

		// Same as the User textColor in the XML.
		// TODO: Make it a resource that both use
		final ForegroundColorSpan blueLinkColor = new ForegroundColorSpan(
				Color.rgb(42, 30, 176));

		// Span to make text bold
		int charsToBoldify = comment.getUser().getUserName().length();
		sb.setSpan(blueLinkColor, 0, charsToBoldify,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		// Span to set text color to some RGB value
		final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

		// Set the text color for first 4 characters
		sb.setSpan(bss, 0, charsToBoldify, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		tv.setText(sb);
		return tv;
	}

	private void loadImageToView(String urlToLoad, final ImageView imageView,
			final ProgressBar progressBar) {
		this.superController.getCaches().getPhotosCache()
				.getValue(urlToLoad, new Callback<Void, Object, Bitmap>() {
					@Override
					public void onFinish(Bitmap param) {
						Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
						Debug.getMemoryInfo(memoryInfo);

						String memMessage = String
								.format("Memory: Pss=%.2f MB, Private=%.2f MB, Shared=%.2f MB",
										memoryInfo.getTotalPss() / 1024.0,
										memoryInfo.getTotalPrivateDirty() / 1024.0,
										memoryInfo.getTotalSharedDirty() / 1024.0);
						Log.d("loadImageToView", memMessage);

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
