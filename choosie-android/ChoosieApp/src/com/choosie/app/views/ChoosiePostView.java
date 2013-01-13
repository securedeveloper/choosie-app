package com.choosie.app.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.choosie.app.Constants;
import com.choosie.app.Logger;
import com.choosie.app.NewChoosiePostData.PostType;
import com.choosie.app.R;
import com.choosie.app.Screen;
import com.choosie.app.Utils;
import com.choosie.app.caches.Cache;
import com.choosie.app.caches.CacheCallback;
import com.choosie.app.caches.Caches;
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
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class ChoosiePostView extends RelativeLayout {
	private ChoosiePostData choosiePost;
	private SuperController superController;
	private FeedViewHolder postViewHolder;

	public ChoosiePostView(Context context, SuperController superController,
			int position) {
		super(context);
		inflateLayout(position);
		this.superController = superController;
		initializeHolder();
	}

	private void initializeHolder() {
		postViewHolder = new FeedViewHolder();

		postViewHolder.commentLayout = (LinearLayout) findViewById(R.id.layout_comments);
		postViewHolder.commentLayoutMain = (LinearLayout) findViewById(R.id.layout_comments_main);
		postViewHolder.votes1 = (TextView) findViewById(R.id.votes1);
		postViewHolder.votes2 = (TextView) findViewById(R.id.votes2);
		postViewHolder.feedtext = (TextView) findViewById(R.id.feedtext);
		postViewHolder.feed_name = (TextView) findViewById(R.id.feed_name);
		postViewHolder.time_text = (TextView) findViewById(R.id.time_text);
		postViewHolder.feed_userimage = (ImageView) findViewById(R.id.feed_userimage);
		postViewHolder.imgView1 = (ImageView) findViewById(R.id.feedimage1);
		postViewHolder.imgView2 = (ImageView) findViewById(R.id.feedimage2);
		postViewHolder.voteButton1 = (ImageView) findViewById(R.id.feed_image_vote_icon1);
		postViewHolder.voteButton2 = (ImageView) findViewById(R.id.feed_image_vote_icon2);
		postViewHolder.progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		postViewHolder.progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
		postViewHolder.layoutForLeftPhoto = (RelativeLayout) findViewById(R.id.layout_for_left_photo);
		postViewHolder.layoutForRightPhoto = (RelativeLayout) findViewById(R.id.layout_for_right_photo);
		postViewHolder.layoutForCenterPhoto = (RelativeLayout) findViewById(R.id.layout_for_center_photo);
		postViewHolder.progressBarCenter = (ProgressBar) findViewById(R.id.progressBarCenter);
		postViewHolder.imgViewCenter = (ImageView) findViewById(R.id.feedimageCenter);
		postViewHolder.voteImageAnimationCenter = (ImageView) findViewById(R.id.choosiePost_voteImage_animation_center);
		postViewHolder.voteImageAnimationLeft = (ImageView) findViewById(R.id.choosiePost_voteImage_animation_left);
		postViewHolder.voteImageAnimationRight = (ImageView) findViewById(R.id.choosiePost_voteImage_animation_right);

		postViewHolder.voteImageAnimationLeft.setImageBitmap(null);
		postViewHolder.voteImageAnimationRight.setImageBitmap(null);
		postViewHolder.voteImageAnimationCenter.setImageBitmap(null);

		// set the size of the image view to be a square sized half of the
		// screen width
		int screenWidth = Utils.getScreenWidth();
		resizeViews(screenWidth / 2, screenWidth / 2, postViewHolder.imgView1,
				postViewHolder.imgView2, postViewHolder.layoutForLeftPhoto,
				postViewHolder.layoutForRightPhoto);

		resizeViews(screenWidth, screenWidth, postViewHolder.imgViewCenter,
				postViewHolder.layoutForCenterPhoto);
	}

	private void handleLayoutsVisibilityByPostType(PostType postType) {
		if (postType == PostType.TOT) {
			postViewHolder.layoutForCenterPhoto.setVisibility(View.GONE);

		} else {

			postViewHolder.progressBar1.setVisibility(View.GONE);
			postViewHolder.progressBar2.setVisibility(View.GONE);
		}

	}

	private void resizeViews(int width, int height, View... views) {
		for (View view : views) {
			view.getLayoutParams().width = width;
			view.getLayoutParams().height = height;
		}
	}

	private void inflateLayout(final int position) {
		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_choosie_post, this);

		this.findViewById(R.id.button_to_comment).setOnClickListener(
				new OnClickListener() {
					public void onClick(View arg0) {
						superController.switchToCommentScreen(choosiePost
								.getPostKey());
					}
				});
	}

	public void loadChoosiePost(final ChoosiePostData post, final int position) {
		this.choosiePost = post;

		postViewHolder.feedtext.setText(post.getQuestion());
		postViewHolder.feed_name.setText(post.getAuthor().getUserName());
		postViewHolder.time_text.setText(Utils
				.getTimeDifferenceTextFromNow(post.getCreatedAt()));

		postViewHolder.imgView1.setVisibility(View.GONE);
		postViewHolder.imgView2.setVisibility(View.GONE);
		postViewHolder.imgViewCenter.setVisibility(View.GONE);
		postViewHolder.voteButton1.setVisibility(View.GONE);
		postViewHolder.voteButton2.setVisibility(View.GONE);
		postViewHolder.feed_userimage.setVisibility(View.GONE);

		handleLayoutsVisibilityByPostType(post.getPostType());

		if (post.getPostType() == PostType.YesNo) {
			postViewHolder.layoutForCenterPhoto.setVisibility(View.VISIBLE);
			postViewHolder.layoutForLeftPhoto.setBackgroundColor(getResources()
					.getColor(R.color.Transparent));
			postViewHolder.layoutForRightPhoto
					.setBackgroundColor(getResources().getColor(
							R.color.Transparent));

			Logger.i(post.getPhoto2URL());
			loadImageToView(post.getPhoto1URL(), postViewHolder.imgViewCenter,
					postViewHolder.progressBarCenter,
					postViewHolder.voteButton1);
			loadImageToView(post.getPhoto1URL(), postViewHolder.imgViewCenter,
					postViewHolder.progressBarCenter,
					postViewHolder.voteButton2);
			int screenWidth = Utils.getScreenWidth();
			resizeViews((int) (screenWidth / 5.5), (int) (screenWidth / 5.5),
					postViewHolder.voteButton1, postViewHolder.voteButton2);
		} else {
			loadImageToView(post.getPhoto1URL(), postViewHolder.imgView1,
					postViewHolder.progressBar1, postViewHolder.voteButton1);
			loadImageToView(post.getPhoto2URL(), postViewHolder.imgView2,
					postViewHolder.progressBar2, postViewHolder.voteButton2);
			int screenWidth = Utils.getScreenWidth();
			resizeViews((int) (screenWidth / 5.5), (int) (screenWidth / 5.5),
					postViewHolder.voteButton1, postViewHolder.voteButton2);

		}
		loadImageToView(post.getAuthor().getPhotoURL(),
				postViewHolder.feed_userimage, null, null);
		loadCommentsToView(post, postViewHolder);

		// DECIDE IF SHOW RESUTLS OR NOT
		if (choosiePost.isVotedAlready() || choosiePost.isPostByMe()) {
			ChangeVotingResultsVisibility(postViewHolder.votes1,
					postViewHolder.votes2, View.VISIBLE);
		} else {
			postViewHolder.votes1.setText("");
			postViewHolder.votes2.setText("");
			// ChangeVotingResultsVisibility(feedViewHolder.votes1,
			// feedViewHolder.votes2, View.INVISIBLE);
		}

		// Set border for voted image
		setVoteButtonIcon(postViewHolder.voteButton1,
				choosiePost.isVotedAlready(1), 1);
		setVoteButtonIcon(postViewHolder.voteButton2,
				choosiePost.isVotedAlready(2), 2);

		// TODO: Merge both listeners below to a single one that accepts an
		// argument.
		postViewHolder.imgView1
				.setOnLongClickListener(new OnLongClickListener() {
					public boolean onLongClick(View v) {
						Log.i(Constants.LOG_TAG,
								"onLongClick signaled for voting 1");
						return handleVote1(postViewHolder.votes1,
								postViewHolder.votes2,
								postViewHolder.voteButton1,
								postViewHolder.voteButton2);
					}
				});

		postViewHolder.voteButton1.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				handleVote1(postViewHolder.votes1, postViewHolder.votes2,
						postViewHolder.voteButton1, postViewHolder.voteButton2);
				if (choosiePost.getPostType() == PostType.YesNo) {
					setAndStartAnimationForCenter(
							postViewHolder.voteImageAnimationCenter,
							R.drawable.thumbs_up, R.anim.enter_from_left);
				} else {
					setAndStartAnimationForCenter(
							postViewHolder.voteImageAnimationLeft,
							R.drawable.thumbs_up, R.anim.enter_from_left);
				}
			}
		});

		postViewHolder.imgView2
				.setOnLongClickListener(new OnLongClickListener() {

					public boolean onLongClick(View v) {
						Log.i(Constants.LOG_TAG,
								"onLongClick signaled for voting 2");
						return handleVote2(postViewHolder.votes1,
								postViewHolder.votes2,
								postViewHolder.voteButton1,
								postViewHolder.voteButton2);
					}

				});

		postViewHolder.voteButton2.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				handleVote2(postViewHolder.votes1, postViewHolder.votes2,
						postViewHolder.voteButton1, postViewHolder.voteButton2);
				if (choosiePost.getPostType() == PostType.YesNo) {
					setAndStartAnimationForCenter(
							postViewHolder.voteImageAnimationCenter,
							R.drawable.thumbs_down, R.anim.enter_from_right);
				} else {
					setAndStartAnimationForCenter(
							postViewHolder.voteImageAnimationRight,
							R.drawable.thumbs_up, R.anim.enter_from_right);
				}
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
				Logger.d("User click to show votes, choosiePost.getPostKey() = "
						+ choosiePost.getPostKey() + " position = " + position);
				superController.handlePopupVoteWindow(choosiePost.getPostKey(),
						position);
			}
		};

		if (choosiePost.isVotedAlready() || choosiePost.isPostByMe()) {
			postViewHolder.votes1.setOnClickListener(votesListener);
			postViewHolder.votes2.setOnClickListener(votesListener);
		} else {
			postViewHolder.votes1.setOnClickListener(null);
			postViewHolder.votes2.setOnClickListener(null);
		}

		postViewHolder.imgView1.setOnClickListener(enlargeListener);
		postViewHolder.imgView2.setOnClickListener(enlargeListener);
	}

	protected void setAndStartAnimationForCenter(final ImageView imageView,
			final int resourceId, int animationId) {

		final Animation fadeOutAnimation = AnimationUtils.loadAnimation(
				superController.getActivity(), R.anim.fadeout);

		fadeOutAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				imageView.setImageBitmap(null);
			}
		});

		final Animation fadeInAnimation = AnimationUtils.loadAnimation(
				superController.getActivity(), animationId);

		fadeInAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				imageView.bringToFront();
				imageView.setImageResource(resourceId);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				imageView.startAnimation(fadeOutAnimation);
			}
		});
		imageView.startAnimation(fadeInAnimation);

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
			setVoteButtonIcon(imgSelected2, true, 2);
			setVoteButtonIcon(imgSelected1, false, 1);
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
			setVoteButtonIcon(imgSelected1, true, 1);
			setVoteButtonIcon(imgSelected2, false, 2);
			return true;
		}
		Log.i(Constants.LOG_TAG, "Already voted for 1. vote not sent");
		return false;
	}

	private void setVoteButtonIcon(ImageView imgView, boolean isVoted,
			int photoNumber) {
		if (choosiePost.getPostType() == PostType.YesNo && photoNumber == 2) {
			if (isVoted) {
				imgView.setImageDrawable(getResources().getDrawable(
						R.drawable.thumbdown_voted));
			} else {
				imgView.setImageDrawable(getResources().getDrawable(
						R.drawable.thumbdown_not_voted));
			}
		} else {
			if (isVoted) {
				imgView.setImageDrawable(getResources().getDrawable(
						R.drawable.thumbup_voted));
			} else {
				imgView.setImageDrawable(getResources().getDrawable(
						R.drawable.thumbup_not_voted));
			}
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
					superController.switchToCommentScreen(choosiePost
							.getPostKey());
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

	Map<ImageView, String> lastRequestOnImageView = new HashMap<ImageView, String>();

	private void loadImageToView(String urlToLoad, final ImageView imageView,
			final ProgressBar progressBar, final ImageView img) {
		loadImageToView(urlToLoad, imageView, progressBar, img, false, false);
	}

	private void loadImageToView(String urlToLoad, final ImageView imageView,
			final ProgressBar progressBar, final ImageView img,
			final boolean isYesNo, final boolean isSecond) {

		lastRequestOnImageView.put(imageView, urlToLoad);
		Cache<String, Bitmap> cache;
		if (isYesNo && isSecond) {
			cache = Caches.getInstance().getBlurredPhotosCache();
		} else {
			cache = Caches.getInstance().getPhotosCache();
		}
		cache.getValue(urlToLoad, new CacheCallback<String, Bitmap>() {
			@Override
			public void onValueReady(final String key, final Bitmap result) {
				if (!key.equals(lastRequestOnImageView.get(imageView))) {
					return;
				}
				imageView.setImageBitmap(result);
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
			public void onProgress(int percent) {
				if (progressBar != null) {
					progressBar.setProgress(percent);
					progressBar.setMax(100);
				}
			}
		});
	}

	private class FeedViewHolder {
		public ImageView voteImageAnimationLeft;
		public ImageView voteImageAnimationRight;
		public ImageView voteImageAnimationCenter;
		public ImageView imgViewCenter;
		public ProgressBar progressBarCenter;
		public RelativeLayout layoutForCenterPhoto;
		public RelativeLayout layoutForRightPhoto;
		public RelativeLayout layoutForLeftPhoto;
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
		public ImageView voteButton1;
		public ImageView voteButton2;
		public ProgressBar progressBar1;
		public ProgressBar progressBar2;
	}
}
