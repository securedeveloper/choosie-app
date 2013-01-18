package com.choozie.app;

import java.util.ArrayList;

import com.choozie.app.R;
import com.choozie.app.caches.CacheCallback;
import com.choozie.app.caches.Caches;
import com.choozie.app.camera.YesNoUtils;
import com.choozie.app.models.ChoosiePostData;
import com.choozie.app.models.CommentData;
import com.choozie.app.models.UserManger;
import com.google.analytics.tracking.android.EasyTracker;
import com.nullwire.trace.ExceptionHandler;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommentScreenActivity extends Activity {
	Intent intent;
	int votes1;
	int votes2;
	boolean isVotedAlready;
	boolean isPostByMe;
	Bitmap image1Bitmap;
	Bitmap image2Bitmap;
	ImageView image1View;
	ImageView image2View;
	String postKey;
	private boolean openVotesWindow;
	private boolean noSecondPhoto;
	private Activity activity;

	// RelativeLayout mainCommentLayout;

	// key listener - for: when the user pressing the enter key - sends the
	// comment
	OnKeyListener sendKeyListener = new OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					activateSendButton(intent);
					return true;
				default:
					break;
				}
			}
			return false;
		}
	};

	private OnClickListener votesListenter = new OnClickListener() {
		public void onClick(View v) {
			openVotesWindow();
		}
	};

	private void openVotesWindow() {
		VotePopupWindowUtils voteWindow = new VotePopupWindowUtils(this);
		voteWindow.popUpVotesWindow(postKey);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		L.i("in comment screen");
		super.onCreate(savedInstanceState);
		this.activity = this;
		setContentView(R.layout.activity_comment_screen);
		ExceptionHandler.register(this, Constants.URIs.CRASH_REPORT);

		// setting onclikelistener
		EditText questionEditText = (EditText) findViewById(R.id.editText_comment);
		questionEditText.setOnKeyListener(sendKeyListener);

		intent = getIntent();
		noSecondPhoto = intent.getBooleanExtra("no_second_photo", false);

		String photo1Path = intent
				.getStringExtra(Constants.IntentsCodes.photo1Path);
		String photo2Path = intent
				.getStringExtra(Constants.IntentsCodes.photo2Path);
		postKey = intent.getStringExtra(Constants.IntentsCodes.post_key);
		// make this global for recycling later
		image1Bitmap = BitmapFactory.decodeFile(photo1Path);

		// decode second picture only if exist
		if (noSecondPhoto == false) {
			image2Bitmap = BitmapFactory.decodeFile(photo2Path);
		}
		isVotedAlready = intent.getBooleanExtra(
				Constants.IntentsCodes.isAlreadyVoted, false);
		isPostByMe = intent.getBooleanExtra(Constants.IntentsCodes.isPostByMe,
				false);
		openVotesWindow = intent.getBooleanExtra(
				Constants.IntentsCodes.openVotesWindow, false);
		if (isVotedAlready || isPostByMe) {
			votes1 = intent.getIntExtra(Constants.IntentsCodes.votes1, 0);
			votes2 = intent.getIntExtra(Constants.IntentsCodes.votes2, 0);
		}

		fillUpperArea(intent);

		ArrayAdapter<CommentData> commentScreenAdapter = makeCommentScreenAdapter(intent);

		ListView listView = (ListView) findViewById(R.id.commentsListView);
		listView.setAdapter(commentScreenAdapter);

		Button buttonSendComment = (Button) findViewById(R.id.button_send_comment);
		buttonSendComment.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				activateSendButton(intent);
			}
		});

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (openVotesWindow) {
			this.getWindow().getDecorView().post(new Runnable() {
				public void run() {

					openVotesWindow();
				}
			});
		}
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	private ArrayAdapter<CommentData> makeCommentScreenAdapter(
			final Intent intent) {
		ArrayAdapter<CommentData> adi = new ArrayAdapter<CommentData>(this,
				R.layout.view_comment) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				CommentData item = getItem(position);

				return createViewComment(item, position, convertView);
			}
		};

		// fill the adapter:

		ArrayList<String> nameList = new ArrayList<String>();
		ArrayList<String> commentList = new ArrayList<String>();
		ArrayList<String> commentierPhotoUrlList = new ArrayList<String>();
		ArrayList<CharSequence> createdAtList = new ArrayList<CharSequence>();
		ArrayList<String> fbUidList = new ArrayList<String>();

		nameList = intent
				.getStringArrayListExtra(Constants.IntentsCodes.nameList);
		commentList = intent
				.getStringArrayListExtra(Constants.IntentsCodes.commentList);
		commentierPhotoUrlList = intent
				.getStringArrayListExtra(Constants.IntentsCodes.commentierPhotoUrlList);
		createdAtList = intent
				.getCharSequenceArrayListExtra(Constants.IntentsCodes.createdAtList);
		fbUidList = intent
				.getStringArrayListExtra(Constants.IntentsCodes.fbUid);

		// if there are no comments - put a dummy item for showing the pictures
		if (nameList.size() == 0) {
			adi.add(new CommentData(true));
		} else {
			for (int i = 0; i < nameList.size(); i++) {
				CommentData newCommentData = new CommentData(nameList.get(i),
						commentList.get(i), commentierPhotoUrlList.get(i),
						createdAtList.get(i), fbUidList.get(i));
				adi.add(newCommentData);
			}
		}

		return adi;
	}

	private void fillUpperArea(Intent intent) {
		final ImageView imageViewUserPhoto = (ImageView) findViewById(R.id.userPhoto_commetns);

		String userPhotoPath = intent
				.getStringExtra(Constants.IntentsCodes.userPhotoPath);

		Utils.setImageFromPath(userPhotoPath, imageViewUserPhoto);

		// set the user name
		TextView userNameTextView = (TextView) findViewById(R.id.comment_activity_user_name);
		userNameTextView.setText(intent
				.getStringExtra(Constants.IntentsCodes.userName));

		// set the question
		((TextView) findViewById(R.id.textImage_comment_question))
				.setText(intent.getStringExtra("question"));
	}

	private View createViewComment(final CommentData item, int position,
			View convertView) {

		RelativeLayout itemView = (RelativeLayout) convertView;
		CommentViewHolder commentViewHolder = null;

		// if convertView is null we will create a new View
		if (convertView == null) {
			// create new holder

			// inflate view_comment into itemView
			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = (RelativeLayout) inflater.inflate(R.layout.view_comment,
					null);

			// initialize the holder
			commentViewHolder = initializeHolder(itemView, commentViewHolder);

			// set the holder inside the view we just created
			itemView.setTag(commentViewHolder);
		} else {
			// convertView is not null, thus we can reuse it - grabing the
			// holder.
			commentViewHolder = (CommentViewHolder) itemView.getTag();
		}

		if ((position == 0) || (item.checkIfDummyComment() == true)) {
			commentViewHolder.imagesLayout.setVisibility(View.VISIBLE);
			commentViewHolder.viewComment_votes_layout
					.setVisibility(View.VISIBLE);

			if (noSecondPhoto == false) {
				// two photos mode
				commentViewHolder.votesImageView1.setVisibility(View.GONE);
				commentViewHolder.votesImageView2.setVisibility(View.GONE);
				commentViewHolder.imageViewPhoto1.setImageBitmap(image1Bitmap);
				commentViewHolder.imageViewPhoto2.setImageBitmap(image2Bitmap);
				image1View = commentViewHolder.imageViewPhoto1;
				image2View = commentViewHolder.imageViewPhoto2;
			} else {
				// only one photo
				commentViewHolder.votesImageView1
						.setImageResource(R.drawable.thumbs_up);
				commentViewHolder.votesImageView2
						.setImageResource(R.drawable.thumbs_down);

				commentViewHolder.imagesLayout.setVisibility(View.GONE);
				commentViewHolder.layoutMiddle.setVisibility(View.VISIBLE);
				commentViewHolder.photoMiddle.setVisibility(View.VISIBLE);
				commentViewHolder.photoMiddle.setImageBitmap(image1Bitmap);
				commentViewHolder.photoMiddle.bringToFront();
			}
			if (isVotedAlready || isPostByMe) {
				commentViewHolder.votes1.setText(votes1 + " votes");
				commentViewHolder.votes2.setText(votes2 + " votes");
				commentViewHolder.votes1.setOnClickListener(votesListenter);
				commentViewHolder.votes2.setOnClickListener(votesListenter);
			} else {
				commentViewHolder.votes1.setText("");
				commentViewHolder.votes2.setText("");
				commentViewHolder.votesImageView1.setVisibility(View.GONE);
				commentViewHolder.votesImageView2.setVisibility(View.GONE);
			}
		} else {
			// commentViewHolder.layoutTwoPhotos.setVisibility(View.GONE);
			commentViewHolder.layoutMiddle.setVisibility(View.GONE);
			commentViewHolder.photoMiddle.setVisibility(View.GONE);
			commentViewHolder.viewComment_votes_layout.setVisibility(View.GONE);
			commentViewHolder.imagesLayout.setVisibility(View.GONE);
		}

		// comtinue only if it is a real comment
		if (item.checkIfDummyComment() == false) {

			// set the comment text
			setTextOntv(item, commentViewHolder.tv);
			final CommentViewHolder holder = commentViewHolder;
			// set the commentier photo
			Caches.getInstance()
					.getPhotosCache()
					.getValue(item.getCommentierPhotoUrl(),
							new CacheCallback<String, Bitmap>() {
								@Override
								public void onValueReady(String key,
										Bitmap result) {
									holder.commentierPhotoImageView
											.setImageBitmap(result);
								}
							});

			// set the comment time
			commentViewHolder.commentTime.setText(item.getCreatedAt() + " ago");
		} else {
			((ImageView) itemView.findViewById(R.id.view_comment_clockImage))
					.setVisibility(View.GONE);
		}

		OnClickListener profileListener = new OnClickListener() {

			public void onClick(View v) {
				UserManger userManager = new UserManger(activity,
						item.getUser());
				userManager.goToProfile();
			}
		};

		commentViewHolder.commentierPhotoImageView
				.setOnClickListener(profileListener);
		return itemView;
	}

	private CommentViewHolder initializeHolder(RelativeLayout itemView,
			CommentViewHolder commentViewHolder) {
		commentViewHolder = new CommentViewHolder();

		commentViewHolder.imagesLayout = (LinearLayout) itemView
				.findViewById(R.id.layout_images_comment);
		commentViewHolder.viewComment_votes_layout = (RelativeLayout) itemView
				.findViewById(R.id.viewComment_votes_layout);
		commentViewHolder.votes1 = (TextView) itemView
				.findViewById(R.id.viewComment_votes1);
		commentViewHolder.votes2 = (TextView) itemView
				.findViewById(R.id.viewComment_votes2);
		commentViewHolder.imageViewPhoto1 = (ImageView) itemView
				.findViewById(R.id.photo1_comment_screen);
		commentViewHolder.imageViewPhoto2 = (ImageView) itemView
				.findViewById(R.id.photo2_comment_screen);
		commentViewHolder.tv = (TextView) itemView
				.findViewById(R.id.view_comment_comment);
		commentViewHolder.commentierPhotoImageView = (ImageView) itemView
				.findViewById(R.id.commentScreen_commentierPhoto);
		commentViewHolder.commentTime = (TextView) itemView
				.findViewById(R.id.commentScreen_commentTime);
		commentViewHolder.photoMiddle = (ImageView) itemView
				.findViewById(R.id.commentView_photo_midle);
		commentViewHolder.layoutMiddle = (RelativeLayout) itemView
				.findViewById(R.id.commentView_middle_layout);
		commentViewHolder.votesImageView1 = (ImageView) itemView
				.findViewById(R.id.viewComment_votes1_pointing);
		commentViewHolder.votesImageView2 = (ImageView) itemView
				.findViewById(R.id.viewComment_votes2_pointing);

		Utils.setImageViewSize(commentViewHolder.layoutMiddle,
				Utils.getScreenWidth() / 2, Utils.getScreenWidth() / 2);
		Utils.setImageViewSize(commentViewHolder.photoMiddle,
				Utils.getScreenWidth() / 2, Utils.getScreenWidth() / 2);
		commentViewHolder.layoutMiddle.setVisibility(View.GONE);
		commentViewHolder.photoMiddle.setImageBitmap(null);

		// set the size of the image view to be a square sized half of the
		// screen width
		int screenWidth = Utils.getScreenWidth();
		if (screenWidth != -1) {
			Utils.setImageViewSize(commentViewHolder.imageViewPhoto1,
					screenWidth / 2, screenWidth / 2);
			Utils.setImageViewSize(commentViewHolder.imageViewPhoto2,
					screenWidth / 2, screenWidth / 2);
		}

		return commentViewHolder;
	}

	private void activateSendButton(final Intent intent) {
		String text = ((EditText) findViewById(R.id.editText_comment))
				.getText().toString();

		// get the intent and add the comment
		intent.putExtra(Constants.IntentsCodes.text, text);

		// activate the 'onActivityResult'
		setResult(RESULT_OK, intent);
		// resetCommetnScreen();
		resetCommentScreen();
		finish();
	}

	private void setTextOntv(CommentData item, TextView tv) {
		final SpannableStringBuilder sb = new SpannableStringBuilder(
				item.getName() + " " + item.getComment());

		// Same as the User textColor in the XML.
		// TODO: Make it a resource that both use
		final ForegroundColorSpan blueLinkColor = new ForegroundColorSpan(
				Color.rgb(42, 30, 176));

		// Span to make text bold
		int charsToBoldify = item.getName().length();
		sb.setSpan(blueLinkColor, 0, charsToBoldify,
				Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		// Span to set text color to some RGB value
		final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

		// Set the text color for first charsToBoldify characters
		sb.setSpan(bss, 0, charsToBoldify, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		tv.setText(sb);
	}

	// private void setImageFromPath(String photoPath, ImageView imageViewPhoto)
	// {
	// if (photoPath != null) {
	// imageViewPhoto.setImageBitmap(BitmapFactory.decodeFile(photoPath));
	// }
	// }

	// TODO: check if can delete this function
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED, null);
		resetCommentScreen();
		finish();
		return;
	}

	// setting the backKey to cancel the commentizatzia
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			setResult(RESULT_CANCELED, null);
			resetCommentScreen(); // finish is taking care of it...
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void resetCommentScreen() {
		final ImageView imageViewUserPhoto = (ImageView) findViewById(R.id.userPhoto_commetns);
		Bitmap userPhotoBitmap = ((BitmapDrawable) imageViewUserPhoto
				.getDrawable()).getBitmap();
		if (userPhotoBitmap != null) {
			userPhotoBitmap.recycle();
		}

		if (noSecondPhoto == false) {

			image1View.setImageDrawable(null);
			image2View.setImageDrawable(null);

			if (image1Bitmap != null) {
				image1Bitmap.recycle();
				image1Bitmap = null;
			}
			if (image2Bitmap != null) {
				image2Bitmap.recycle();
				image2Bitmap = null;
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_comment_screen, menu);
		return true;
	}

	private class CommentViewHolder {
		public ImageView votesImageView1;
		public ImageView votesImageView2;
		// public RelativeLayout layoutTwoPhotos;
		public RelativeLayout layoutMiddle;
		public ImageView photoMiddle;
		LinearLayout imagesLayout;
		RelativeLayout viewComment_votes_layout;
		ImageView imageViewPhoto1;
		ImageView imageViewPhoto2;
		TextView votes1;
		TextView votes2;
		TextView tv;
		ImageView commentierPhotoImageView;
		TextView commentTime;
	}
}
