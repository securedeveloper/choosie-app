package com.choosie.app.views;

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
	private FeedViewHolder feedViewHolder;

	public ChoosiePostView(Context context, SuperController superController,
			int position) {
		super(context);
		inflateLayout(position);
		this.superController = superController;
		initializeHolder();
	}

	private void initializeHolder() {
		feedViewHolder = new FeedViewHolder();

		feedViewHolder.commentLayout = (LinearLayout) findViewById(R.id.layout_comments);
		feedViewHolder.commentLayoutMain = (LinearLayout) findViewById(R.id.layout_comments_main);
		feedViewHolder.votes1 = (TextView) findViewById(R.id.votes1);
		feedViewHolder.votes2 = (TextView) findViewById(R.id.votes2);
		feedViewHolder.feedtext = (TextView) findViewById(R.id.feedtext);
		feedViewHolder.feed_name = (TextView) findViewById(R.id.feed_name);
		feedViewHolder.time_text = (TextView) findViewById(R.id.time_text);
		feedViewHolder.feed_userimage = (ImageView) findViewById(R.id.feed_userimage);
		feedViewHolder.imgView1 = (ImageView) findViewById(R.id.feedimage1);
		feedViewHolder.imgView2 = (ImageView) findViewById(R.id.feedimage2);
		feedViewHolder.imgSelected1 = (ImageView) findViewById(R.id.feed_imageSelect1);
		feedViewHolder.imgSelected2 = (ImageView) findViewById(R.id.feed_imageSelect2);
		feedViewHolder.progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		feedViewHolder.progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
	}

	private void inflateLayout(final int position) {
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_choosie_post, this);

		this.findViewById(R.id.button_to_comment).setOnClickListener(
				new OnClickListener() {
					public void onClick(View arg0) {
						superController.switchToCommentScreen(choosiePost);
					}
				});

		// this.findViewById(R.id.votes1).setOnClickListener(
		// new OnClickListener() {
		// public void onClick(View arg0) {
		// listView.smoothScrollToPosition(position);
		// popUpVotesWindow(choosiePost);
		// }
		// });

		this.findViewById(R.id.votes2).setOnClickListener(
				new OnClickListener() {
					public void onClick(View arg0) {
						superController.switchToVotesScreen(choosiePost);
					}
				});
	}

	public void loadChoosiePost(final ChoosiePostData post, final int position) {
		this.choosiePost = post;

		feedViewHolder.feedtext.setText(post.getQuestion());
		feedViewHolder.feed_name.setText(post.getAuthor().getUserName());
		feedViewHolder.time_text.setText(Utils
				.getTimeDifferenceTextFromNow(post.getCreatedAt()));

		feedViewHolder.imgView1.setVisibility(View.GONE);
		feedViewHolder.imgView2.setVisibility(View.GONE);
		feedViewHolder.imgSelected1.setVisibility(View.GONE);
		feedViewHolder.imgSelected2.setVisibility(View.GONE);
		feedViewHolder.feed_userimage.setVisibility(View.GONE);

		// set the size of the image view to bew a square sized half of the
		// screen width
		int screenWidth = Utils.getScreenWidth();
		feedViewHolder.imgView1.getLayoutParams().height = screenWidth / 2;
		feedViewHolder.imgView1.getLayoutParams().width = screenWidth / 2;
		feedViewHolder.imgView2.getLayoutParams().height = screenWidth / 2;
		feedViewHolder.imgView2.getLayoutParams().width = screenWidth / 2;

		loadImageToView(post.getPhoto1URL(), feedViewHolder.imgView1,
				feedViewHolder.progressBar1, feedViewHolder.imgSelected1);
		loadImageToView(post.getPhoto2URL(), feedViewHolder.imgView2,
				feedViewHolder.progressBar2, feedViewHolder.imgSelected2);
		loadImageToView(post.getAuthor().getPhotoURL(),
				feedViewHolder.feed_userimage, null, null);
		loadCommentsToView(post, feedViewHolder);
		saveVotersPhotos(post);

		// DECIDE IF SHOW RESUTLS OR NOT
		if (choosiePost.isVotedAlready() || choosiePost.isPostByMe()) {
			ChangeVotingResultsVisibility(feedViewHolder.votes1,
					feedViewHolder.votes2, View.VISIBLE);
		} else {
			ChangeVotingResultsVisibility(feedViewHolder.votes1,
					feedViewHolder.votes2, View.INVISIBLE);
		}

		// Set border for voted image
		setImageBorder(feedViewHolder.imgSelected1,
				choosiePost.isVotedAlready(1));
		setImageBorder(feedViewHolder.imgSelected2,
				choosiePost.isVotedAlready(2));

		// TODO: Merge both listeners below to a single one that accepts an
		// argument.
		feedViewHolder.imgView1
				.setOnLongClickListener(new OnLongClickListener() {
					public boolean onLongClick(View v) {
						Log.i(Constants.LOG_TAG,
								"onLongClick signaled for voting 1");
						return handleVote1(feedViewHolder.votes1,
								feedViewHolder.votes2,
								feedViewHolder.imgSelected1,
								feedViewHolder.imgSelected2);
					}
				});

		feedViewHolder.imgSelected1.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				handleVote1(feedViewHolder.votes1, feedViewHolder.votes2,
						feedViewHolder.imgSelected1,
						feedViewHolder.imgSelected2);
			}
		});

		feedViewHolder.imgView2
				.setOnLongClickListener(new OnLongClickListener() {

					public boolean onLongClick(View v) {
						Log.i(Constants.LOG_TAG,
								"onLongClick signaled for voting 2");
						return handleVote2(feedViewHolder.votes1,
								feedViewHolder.votes2,
								feedViewHolder.imgSelected1,
								feedViewHolder.imgSelected2);
					}

				});

		feedViewHolder.imgSelected2.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				handleVote2(feedViewHolder.votes1, feedViewHolder.votes2,
						feedViewHolder.imgSelected1,
						feedViewHolder.imgSelected2);
			}
		});

		// listener for handling enlarge image
		OnClickListener enlargeListener = new OnClickListener() {

			public void onClick(View v) {
				superController.switchToEnlargeImage(v, post);
			}
		};

		// listener for handling votes popUpWindow
		OnClickListener votesListener = new OnClickListener() {

			public void onClick(View v) {
				superController.handlePopupVoteWindow(choosiePost, position);
			}
		};

		feedViewHolder.votes1.setOnClickListener(votesListener);
		feedViewHolder.votes2.setOnClickListener(votesListener);

		feedViewHolder.imgView1.setOnClickListener(enlargeListener);
		feedViewHolder.imgView2.setOnClickListener(enlargeListener);
	}

	private boolean handleVote2(final TextView votes1, final TextView votes2,
			final ImageView imgSelected1, final ImageView imgSelected2) {
		if (!choosiePost.isVotedAlready(2)) {
			Log.i(Constants.LOG_TAG, "voting 2 (Not voted 2 yet)");
			superController.voteFor(choosiePost, 2);

			// SHOW VOTES RESULTS
			ChangeVotingResultsVisibility(votes1, votes2, View.VISIBLE);

			// Set border for relevant image
			Log.i(Constants.LOG_TAG, "Setting border for image 2");
			setImageBorder(imgSelected2, true);
			setImageBorder(imgSelected1, false);
			return true;
		}
		Log.i(Constants.LOG_TAG, "Already voted for 2. vote not sent");
		return false;
	}

	private boolean handleVote1(final TextView votes1, final TextView votes2,
			final ImageView imgSelected1, final ImageView imgSelected2) {
		if (!choosiePost.isVotedAlready(1)) {
			Log.i(Constants.LOG_TAG, "voting 1 (Not voted 1 yet)");
			superController.voteFor(choosiePost, 1);

			// SHOW VOTES RESULTS
			ChangeVotingResultsVisibility(votes1, votes2, View.VISIBLE);

			// Set border for relevant image
			Log.i(Constants.LOG_TAG, "Setting border for image 1");
			setImageBorder(imgSelected1, true);
			setImageBorder(imgSelected2, false);
			return true;
		}
		Log.i(Constants.LOG_TAG, "Already voted for 1. vote not sent");
		return false;
	}

	private void saveVotersPhotos(ChoosiePostData post) {
		List<Vote> lstVotes = post.getVotes();
		for (Vote vote : lstVotes) {
			// first, save commentier photo on sd
			if (vote.getIsNeedToSave() == true) {
				Utils.saveURLonSD(vote.getUsers().getPhotoURL(),
						superController);
				vote.setsNeedToSave();
			}
		}

	}

	private void setImageBorder(ImageView imgView, boolean isBorderVisable) {
		if (isBorderVisable) {
			imgView.setImageDrawable(getResources().getDrawable(
					R.drawable.image_selected_v));
			// imgView.setBackgroundResource(R.drawable.image_selected);
		} else {
			imgView.setImageDrawable(getResources().getDrawable(
					R.drawable.image_not_selected_v));
			// imgView.setBackgroundResource(R.drawable.image_not_selected);
		}
	}

	private void ChangeVotingResultsVisibility(TextView votes1,
			TextView votes2, int visibility) {

		// If visibility==true then show vote count
		if (visibility == View.VISIBLE) {

			int voteCount1 = choosiePost.getVotes1();
			int voteCount2 = choosiePost.getVotes2();

			votes1.setText(voteCount1 + " votes");
			votes2.setText(voteCount2 + " votes");
		}
		votes1.setVisibility(visibility);
		votes2.setVisibility(visibility);
	}

	private void loadCommentsToView(ChoosiePostData post,
			FeedViewHolder feedViewHolder) {

		feedViewHolder.commentLayout.removeAllViews();
		List<Comment> lstComment = post.getComments();
		int i = 0;
		for (Comment comment : lstComment) {
			// first, save commentier photo on sd
			if (comment.getIsNeedToSave() == true) {
				Utils.saveURLonSD(comment.getUser().getPhotoURL(),
						superController);
				comment.setIsNeedToSave();
			}
			if (i == 0) {
				feedViewHolder.commentLayoutMain
						.setVisibility(LinearLayout.VISIBLE);
			}
			showCommentByLocatin(feedViewHolder.commentLayout,
					lstComment.size(), i, comment);
			i++;
		}
	}

	private void showCommentByLocatin(final LinearLayout commentLayout,
			int size, int i, Comment comment) {
		if (i == 0) { // build first comment
			View commentView = buildViewForComment(comment);
			ImageView im = (ImageView) findViewById(R.id.chat_icon);
			float h = ((TextView) commentView).getTextSize();
			im.getLayoutParams().height = (int) h;
			im.getLayoutParams().width = (int) h;
			// commentLayout.addView(commentView);
		}
		if ((size > 3) && (i == 0)) {
			TextView tv = new TextView(superController.getControllerForScreen(
					Screen.FEED).getActivity());
			tv.setText("View all " + size + " comments");
			tv.setTextColor(superController.getActivity().getResources()
					.getColor(R.color.Gray));
			tv.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					superController.switchToCommentScreen(choosiePost);
				}
			});
			commentLayout.addView(tv);
		}
		if ((i == size - 1) || (i == size - 2) || (i == size - 3)) {
			// build view for vurrent comment
			View commentView = buildViewForComment(comment);
			commentLayout.addView(commentView);
		}
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
			final ProgressBar progressBar, final ImageView img) {
		this.superController.getCaches().getPhotosCache()
				.getValue(urlToLoad, new Callback<Void, Object, Bitmap>() {
					@Override
					public void onFinish(Bitmap param) {

						imageView.setImageBitmap(param);
						imageView.setVisibility(View.VISIBLE);
						if (progressBar != null) {
							progressBar.setVisibility(View.GONE);
						}
						if (img != null) {
							img.setVisibility(View.VISIBLE);
							img.bringToFront();
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

	private class FeedViewHolder {
		public LinearLayout commentLayout;
		public LinearLayout commentLayoutMain;
		public TextView votes1;
		public TextView votes2;
		public TextView feedtext;
		public TextView feed_name;
		public TextView time_text;
		public ImageView feed_userimage;
		public ImageView imgView1;
		public ImageView imgView2;
		public ImageView imgSelected1;
		public ImageView imgSelected2;
		public ProgressBar progressBar1;
		public ProgressBar progressBar2;
	}
}
