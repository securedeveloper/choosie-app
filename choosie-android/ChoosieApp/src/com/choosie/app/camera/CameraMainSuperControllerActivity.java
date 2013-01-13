package com.choosie.app.camera;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.choosie.app.Callback;
import com.choosie.app.Constants;
import com.choosie.app.L;
import com.choosie.app.NewChoosiePostData;
import com.choozie.app.R;
import com.choosie.app.Utils;
import com.choosie.app.NewChoosiePostData.PostType;
import com.choosie.app.client.Client;
import com.facebook.Session;

import com.facebook.SessionState;
import com.facebook.Session.ReauthorizeRequest;
import com.facebook.Session.StatusCallback;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CameraMainSuperControllerActivity extends Activity {

	// private File pictureFile;
	private String imagePath1;
	private String imagePath2;
	private Bundle bundle;

	private PostType postType = PostType.TOT;

	private ImageView totImageView;
	private ImageView yaanaaImageView;

	private RelativeLayout topLayout;
	private LinearLayout images12Layout;
	private EditText mQuestion;
	private ImageView mImage1;
	private ImageView mImage2;
	private ImageView mImageMiddle;
	private ToggleButton mTbFacebook;
	private TableRow mTrFacebook;
	private Session session;
	private ImageButton mBtnSubmit;

	private File imageFile1;
	private File imageFile2;

	private Bitmap image1BitmapTot;
	private Bitmap image2BitmapTot;
	private Bitmap image1BitmapYaanaa;
	private Bitmap image2BitmapYaanaa;
	private boolean isFirstTimeReturnFromCamera;

	private StatusCallback statusCallback = new SessionStatusCallback();
	private OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {
			mTbFacebook.setChecked(!mTbFacebook.isChecked());
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_main);

		InitializeComponents();

		// initializing bundle;
		bundle = new Bundle();
		// creating the first file and starting cameraActivity
		imageFile1 = Utils.createImageFile(1);
		imageFile2 = Utils.createImageFile(2);
		imagePath1 = imageFile1.getAbsolutePath();
		imagePath2 = imageFile2.getAbsolutePath();
		L.i("CameraMainActivity - onCreate - about to startNewCameraActivity, imaggePath1 = "
				+ imagePath1 + " imagePath2 = " + imagePath2);
		isFirstTimeReturnFromCamera = true;
		startNewCameraActivity(Constants.RequestCodes.CAMERA_PICURE_FIRST,
				imagePath1);
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	private void InitializeComponents() {

		// initialize all components
		this.topLayout = (RelativeLayout) findViewById(R.id.post_layout_top);
		this.images12Layout = (LinearLayout) findViewById(R.id.postCamera_images12_layout);
		this.mQuestion = (EditText) findViewById(R.id.post_tvQuestion);
		this.mImage1 = (ImageView) findViewById(R.id.image_photo1);
		this.mImage2 = (ImageView) findViewById(R.id.postCamera_image_photo2);
		this.mImageMiddle = (ImageView) findViewById(R.id.image_photoMiddle);
		this.mTbFacebook = (ToggleButton) findViewById(R.id.tbFacebook);
		this.mBtnSubmit = (ImageButton) findViewById(R.id.post_btnSubmit);
		this.session = Session.getActiveSession();
		this.yaanaaImageView = (ImageView) findViewById(R.id.post_yaanaaButton_image);
		this.totImageView = (ImageView) findViewById(R.id.post_totButton_image);
		this.mTrFacebook = (TableRow) findViewById(R.id.tableRowShareFB);
		// this.mProgressBar = (ProgressBar)
		// findViewById(R.id.post_progressBar);
		images12Layout.bringToFront();
		mImageMiddle.setEnabled(false);

		this.mTrFacebook.setOnClickListener(listener);
		this.mTbFacebook.setOnCheckedChangeListener(checkChangedListener);
		this.mBtnSubmit.setOnClickListener(onClickListenter);
		float density = getApplicationContext().getResources()
				.getDisplayMetrics().density;
		int topHeight = Math.round(50 * density);

		// set heights and shit
		Utils.setImageViewSize(mImageMiddle, Utils.getScreenWidth() / 2,
				Utils.getScreenWidth() / 2);
		Utils.setImageViewSize(mImage1, Utils.getScreenWidth() / 2,
				Utils.getScreenWidth() / 2);
		Utils.setImageViewSize(mImage2, Utils.getScreenWidth() / 2,
				Utils.getScreenWidth() / 2);
		Utils.setImageViewSize(topLayout, topHeight, 0);
		Utils.setImageViewSize(mBtnSubmit, topHeight, topHeight);
		Utils.setImageViewSize(findViewById(R.id.post_imagesLayout),
				(Utils.getScreenWidth() / 2) + 10, 0);

		yaanaaImageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				switchToYaanaaMode();
			}
		});

		yaanaaImageView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {

				if (postType == PostType.YesNo) {
					return true;
				}

				handleOnTouch(yaanaaImageView, arg1, R.drawable.yaa_naa,
						R.drawable.yaa_naa_pressed);

				return true;

			}
		});

		totImageView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {

				if (postType == PostType.TOT) {
					return true;
				}

				handleOnTouch(totImageView, arg1, R.drawable.tot,
						R.drawable.tot_pressed);

				return true;

			}
		});

		totImageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				switchToTotMode();
			}
		});

		mImage1.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				handleImage1Click();
			}
		});

		mImage2.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				handleImage2Click();

			}
		});

		mImageMiddle.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				handleImage1Click();

			}
		});
	}

	protected void handleOnTouch(ImageView view, MotionEvent motion,
			int drawable, int drawablePressed) {

		// for the conflict with the scrollView
		view.getParent().requestDisallowInterceptTouchEvent(true);

		int maxX = view.getWidth();
		int maxY = view.getHeight();

		float x = motion.getX();
		float y = motion.getY();

		if (motion.getAction() == MotionEvent.ACTION_DOWN) {
			view.setImageResource(drawablePressed);
		}

		// if moved outside the button - make the focus image gone
		if (motion.getAction() == MotionEvent.ACTION_MOVE) {
			if ((x < 0) || (y < 0) || (x > maxX) || (y > maxY)) {
				view.setImageResource(drawable);
				return;
			}
		}

		if (motion.getAction() == MotionEvent.ACTION_UP) {

			if ((x > 0) && (y > 0) && (x < maxX) && (y < maxY)) {
				if (view.getId() == R.id.post_yaanaaButton_image) {
					yaanaaImageView.performClick();
				} else if (view.getId() == R.id.post_totButton_image) {
					totImageView.performClick();
				}
			}
		}
	}

	protected void handleImage1Click() {

		startConfirmActivity(Constants.RequestCodes.CAMERA_CONFIRM_FIRST,
				imagePath1);
	}

	protected void handleImage2Click() {
		if (postType == PostType.TOT) {

			if (image2BitmapTot != null) {
				startConfirmActivity(
						Constants.RequestCodes.CAMERA_CONFIRM_SECOND,
						imagePath2);
			} else {
				startNewCameraActivity(
						Constants.RequestCodes.CAMERA_PICURE_SECOND, imagePath2);
			}
		} else if (postType == PostType.YesNo) {
			startConfirmActivity(Constants.RequestCodes.CAMERA_CONFIRM_FIRST,
					imagePath1);
		}
	}

	protected void switchToTotMode() {

		if (postType == PostType.TOT) {
			return;
		}

		yaanaaImageView.setImageResource(R.drawable.yaa_naa);
		totImageView.setImageResource(R.drawable.tot_pressed);

		final Animation image2fadeInAnimation = AnimationUtils.loadAnimation(
				this, R.anim.fadein);

		image2fadeInAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				if (image2BitmapTot == null) {
					mImage2.setImageResource(R.drawable.plus);
				} else {
					mImage2.setImageBitmap(image2BitmapTot);
				}

			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation animation) {

			}
		});

		final Animation image1PushLeftAnimation = AnimationUtils.loadAnimation(
				this, R.anim.push_left_out);

		image1PushLeftAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {

			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation animation) {
				mImage1.setImageBitmap(image1BitmapTot);
				mImageMiddle.setImageBitmap(null);
				mImage2.startAnimation(image2fadeInAnimation);

			}
		});

		mImageMiddle.startAnimation(image1PushLeftAnimation);

		images12Layout.bringToFront();
		images12Layout.setEnabled(true);
		mImageMiddle.setEnabled(false);
		mImage1.setEnabled(true);
		mImage1.setEnabled(true);
		postType = PostType.TOT;

	}

	protected void switchToYaanaaMode() {

		if (postType == PostType.YesNo) {
			return;
		}
		yaanaaImageView.setImageResource(R.drawable.yaa_naa_pressed);
		totImageView.setImageResource(R.drawable.tot);

		final Animation image2FadeOutAnimationAndThatsIt = AnimationUtils
				.loadAnimation(this, R.anim.fadeout);

		final Animation imageMiddlePushRightAnimation = AnimationUtils
				.loadAnimation(this, R.anim.push_right_out);

		imageMiddlePushRightAnimation
				.setAnimationListener(new AnimationListener() {

					public void onAnimationStart(Animation animation) {

						// mImageMiddle.setVisibility(View.VISIBLE);

					}

					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					public void onAnimationEnd(Animation animation) {
						mImageMiddle.setImageBitmap(image1BitmapTot);
						mImage1.setImageBitmap(null);

					}
				});

		image2FadeOutAnimationAndThatsIt
				.setAnimationListener(new AnimationListener() {

					public void onAnimationStart(Animation animation) {

					}

					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					public void onAnimationEnd(Animation animation) {
						mImage2.setImageBitmap(null);
						mImage1.startAnimation(imageMiddlePushRightAnimation);

					}
				});

		mImage2.startAnimation(image2FadeOutAnimationAndThatsIt);

		mImageMiddle.bringToFront();
		mImageMiddle.setEnabled(true);
		images12Layout.setEnabled(false);
		mImage1.setEnabled(false);
		mImage1.setEnabled(false);
		postType = PostType.YesNo;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		L.i("onActivityResult");
		L.i("requestCode = " + requestCode);
		L.i("resultCode = " + resultCode);

		switch (requestCode) {
		case Constants.RequestCodes.CAMERA_PICURE_FIRST:
			// get back from camera with first file
			// handleResultFromCamera(resultCode, data,
			// Constants.RequestCodes.CAMERA_CONFIRM_FIRST, imagePath1);
			if (resultCode == Activity.RESULT_OK) {
				// user confirmed, save it in the gallery, and starting
				// cmaeraActivity with second image
				bundle = data.getExtras();
				Utils.galleryAddPic(Uri.fromFile(imageFile1), this);
				L.i("CameraMainActivity - got back from first confirm, startNewCameraActivity, imaggePath2 = "
						+ imagePath2);
				saveImage1Bitmap();
				setImages();
				isFirstTimeReturnFromCamera = false;
			} else if (resultCode == Activity.RESULT_CANCELED) {
				if (data != null) {
					if (data.getBooleanExtra(Constants.IntentsCodes.error,
							false) == true) {
						showDialog(Constants.DialogId.ERROR);
					}
				}

				if (isFirstTimeReturnFromCamera == true) {
					goBackToChoosieActivity(Activity.RESULT_OK);
				}
			}
			break;

		case Constants.RequestCodes.CAMERA_PICURE_SECOND:
			// get back from camera with first file
			// handleResultFromCamera(resultCode, data,
			// Constants.RequestCodes.CAMERA_CONFIRM_SECOND, imagePath2);
			if (resultCode == Activity.RESULT_OK) {
				// user confirmed. starting NewPostActivity
				bundle = data.getExtras();
				Utils.galleryAddPic(Uri.fromFile(imageFile2), this);

				saveImage2Bitmap();
				mImage2.setBackgroundDrawable(null);
				mImage2.setImageBitmap(image2BitmapTot);

			}
			break;

		case Constants.RequestCodes.CAMERA_CONFIRM_FIRST:
			// got back from confirmation with first image
			if (resultCode == Activity.RESULT_OK) {
				// user confirmed, save it in the gallery, and starting
				// cmaeraActivity with second image
				Utils.galleryAddPic(Uri.fromFile(imageFile1), this);
				L.i("CameraMainActivity - got back from first confirm, startNewCameraActivity, imaggePath2 = "
						+ imagePath2);
				saveImage1Bitmap();
				setImages();

			} else if (resultCode == Activity.RESULT_CANCELED) {
				// user declined, let's start the CameraActivity again with
				// first image
				if (data != null) {
					if (data.getBooleanExtra(
							Constants.IntentsCodes.stayOnScreen, false) == true) {
						break;
					}
				}
				startNewCameraActivity(
						Constants.RequestCodes.CAMERA_PICURE_FIRST, imagePath1);

			}
			break;

		case Constants.RequestCodes.CAMERA_CONFIRM_SECOND:
			// got back from confirmation with first image
			if (resultCode == Activity.RESULT_OK) {
				// user confirmed. starting NewPostActivity
				Utils.galleryAddPic(Uri.fromFile(imageFile2), this);
				saveImage2Bitmap();
				mImage2.setBackgroundDrawable(null);
				mImage2.setImageBitmap(image2BitmapTot);

			} else if (resultCode == Activity.RESULT_CANCELED) {
				// user declined, let's start the CameraActivity again with
				// second image
				if (data != null) {
					if (data.getBooleanExtra(
							Constants.IntentsCodes.stayOnScreen, false) == true) {
						break;
					}
				}
				startNewCameraActivity(
						Constants.RequestCodes.CAMERA_PICURE_SECOND, imagePath2);
			}
			break;

		case Constants.RequestCodes.NEW_POST:
			// got beck from postActivity
			if (resultCode == Activity.RESULT_OK) {
				// good, we can go back to choosieActivity!!
				goBackToChoosieActivity(Activity.RESULT_OK);
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// mmm... so the user is not sure about his image, hey?

			}
			break;

		case Constants.RequestCodes.FB_REQUEST_PUBLISH_PERMISSION:
			L.i("after activity fb");
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
			if (resultCode == Constants.RequestCodes.FB_PERMISSIONS_GRANTED) {
				changeFacebookShareState(true);
			} else if (resultCode == Constants.RequestCodes.FB_PERMISSIONS_DENIED) {
				changeFacebookShareState(false);
			}
		}

	}

	private void setImages() {
		if (postType == PostType.TOT) {
			mImage1.setImageBitmap(image1BitmapTot);
		} else if (postType == PostType.YesNo) {
			mImageMiddle.setImageBitmap(image1BitmapTot);
		}
	}

	private void saveImage2Bitmap() {

		mImage2.setImageBitmap(null);

		if (image2BitmapTot != null) {
			image2BitmapTot.recycle();
			image2BitmapTot = null;
		}

		// get images bitmaps
		image2BitmapTot = Utils.getBitmapFromFileByViewSize(imagePath2, 350,
				350);

	}

	private void saveImage1Bitmap() {

		mImage1.setImageBitmap(null);

		if (image1BitmapTot != null) {
			image1BitmapTot.recycle();
			image1BitmapTot = null;
		}

		if (image1BitmapYaanaa != null) {
			image1BitmapYaanaa.recycle();
			image1BitmapYaanaa = null;
		}

		if (image2BitmapYaanaa != null) {
			image2BitmapYaanaa.recycle();
			image2BitmapYaanaa = null;
		}

		// get images bitmaps
		image1BitmapTot = Utils.getBitmapFromFileByViewSize(imagePath1, 350,
				350);
		// set image 1 and 2 yaanaa

		Bitmap image = image1BitmapTot;
		image1BitmapYaanaa = image1BitmapTot;// YesNoUtils.generateVoteUpImage(this,
		// image1BitmapTot);
		image2BitmapYaanaa = image1BitmapTot;// YesNoUtils.generateVoteDownImage(this,
		// image1BitmapTot);
	}

	private void goBackToChoosieActivity(int result) {
		setResult(result);
		finish();
	}

	private void startConfirmActivity(int requestCode, String path) {
		Intent intent = new Intent(this.getApplication(),
				ConfirmationActivity.class);
		bundle.remove(Constants.IntentsCodes.path);
		L.i("CameraMain - inserting to intent path = " + path);
		intent.putExtra(Constants.IntentsCodes.path, path);
		intent.putExtras(bundle);
		startActivityForResult(intent, requestCode);
	}

	private void startNewCameraActivity(int requestCode, String path) {
		L.i("CameraMainActivity - startNewCameraActivity, path = " + path);
		Intent cameraIntent = new Intent(this.getApplication(),
				CameraActivity.class);

		cameraIntent.putExtra(Constants.IntentsCodes.path, path);

		startActivityForResult(cameraIntent, requestCode);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showDialog(Constants.DialogId.EXIT_ALERT_DIALOG);
		}
		return true;
	}

	private OnCheckedChangeListener checkChangedListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {

				boolean userHasPublishPermissions = isUserHasPublishPermissions();

				if (userHasPublishPermissions) {
					L.i("Already have publish permissions: "
							+ session.getPermissions().toString());
					changeFacebookShareState(true);

				} else {
					askForPublishPermissions();
				}
			} else {
				changeFacebookShareState(false);
			}
		}
	};

	protected boolean isUserHasPublishPermissions() {
		boolean userHasPublishPermissions = false;
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			List<String> perms = session.getPermissions();
			userHasPublishPermissions = perms.contains("publish_stream");
		} else {
			L.i("isUserHasPublishPermissions(): session is not opened!");
		}
		return userHasPublishPermissions;
	}

	protected void changeFacebookShareState(boolean enable) {
		mTbFacebook.setChecked(enable);

		if (enable) {
			mTbFacebook.setBackgroundResource(R.drawable.facebook_square_blue);
		} else {
			mTbFacebook.setBackgroundResource(R.drawable.facebook_square_bw);
		}
	}

	protected void askForPublishPermissions() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			List<String> permissions = new ArrayList<String>();
			permissions.add("publish_stream");

			ReauthorizeRequest request = new ReauthorizeRequest(this,
					permissions);
			request.setCallback(statusCallback);

			try {
				session.reauthorizeForPublish(request);
			} catch (Exception ex) {
				L.e("Exception in reauthorizeForPublish() : " + ex.toString());
			}
			Session.setActiveSession(session);
			L.i("on set active session permissions: "
					+ session.getPermissions().toString());
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		public void call(Session session, SessionState state,
				Exception exception) {

			L.i("Entered SessionStatusCallback()");
			L.i("session = " + session.toString());
			L.i("state = " + state.toString());
			L.i("exception = " + exception.toString());
		}
	}

	private OnClickListener onClickListenter = new OnClickListener() {

		public void onClick(View v) {
			NewChoosiePostData ncpd;
			if (postType == PostType.TOT) {
				ncpd = new NewChoosiePostData(image1BitmapTot, image2BitmapTot,
						mQuestion.getText().toString(), mTbFacebook.isChecked());
			} else {
				ncpd = new NewChoosiePostData(image1BitmapTot, mQuestion
						.getText().toString(), mTbFacebook.isChecked());
			}
			submitPost(ncpd);
		}
	};

	protected void submitPost(NewChoosiePostData ncpd) {
		if (ncpd.isShareOnFacebook() && !isUserHasPublishPermissions()) {
			L.i("Share on facebook is checked!");
			askForPublishPermissions();
		}
		L.i("executing submitChoosiePost()");
		submitChoosiePost(ncpd);
	}

	private void submitChoosiePost(NewChoosiePostData ncpd) {

		boolean isPostValid = isPostValid();
		if (isPostValid) {
			Tracker tracker = GoogleAnalytics.getInstance(this)
					.getDefaultTracker();
			tracker.trackEvent("Ui action", "Post Screen", "Share", null);

			// final ProgressBar progressBar = (ProgressBar)
			// findViewById(R.id.post_progressBar);
			Client.getInstance().sendChoosiePostToServer(ncpd,
					new Callback<Void, Integer, Void>() {

						@Override
						public void onPre(Void param) {

							// TODO: change to 'DialogFragment'
							showDialog(Constants.DialogId.WAIT_LOADING);

							// progressBar.setProgress(0);
							// progressBar.setMax(100);
							// progressBar.setVisibility(View.VISIBLE);
							// progressBar.bringToFront();
						}

						@Override
						public void onProgress(Integer param) {
							// progressBar.setProgress(param);
						}

						@Override
						public void onFinish(Void param) {
							// progressBar.setVisibility(View.GONE);
							goBackToChoosieActivity(Activity.RESULT_OK);
						}
					});
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Constants.DialogId.EXIT_ALERT_DIALOG:
			// Create out AlterDialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage("This will discard the pictures you have taken.");
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new OkOnClickListener());
			builder.setNegativeButton("Back to camera",
					new CancelOnClickListener());
			AlertDialog dialog = builder.create();
			dialog.show();
			break;

		case Constants.DialogId.WAIT_LOADING:
			ProgressDialog dialog1 = new ProgressDialog(this);
			dialog1.setMessage("Please wait while uploading...");
			dialog1.setIndeterminate(true);
			dialog1.setCancelable(false);
			return dialog1;

		case Constants.DialogId.ERROR:
			AlertDialog.Builder builderError = new AlertDialog.Builder(this);
			builderError.setMessage("Camera is having issues");
			builderError.setCancelable(false);
			builderError.setPositiveButton("OK", new OkOnClickListener());
			AlertDialog dialogError = builderError.create();
			dialogError.show();
			break;
		}

		return super.onCreateDialog(id);
	}

	private boolean isPostValid() {

		if (postType == PostType.TOT) {
			if ((image1BitmapTot == null) || (image2BitmapTot == null)) {
				Toast toast = Toast.makeText(this, "Please add two photos",
						Toast.LENGTH_SHORT);
				toast.show();
				return false;
			}
		} else {
			if ((image2BitmapYaanaa == null) || (image1BitmapYaanaa == null)) {
				Toast toast = Toast.makeText(this, "Please add two photos",
						Toast.LENGTH_SHORT);
				toast.show();
				return false;
			}
		}

		if (mQuestion.getText().toString().equals("")) {
			Toast toast = Toast.makeText(this, "Please add a question",
					Toast.LENGTH_SHORT);
			toast.show();
			return false;
		}
		return true;
	}

	private final class CancelOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {

		}
	}

	private final class OkOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			setResult(Activity.RESULT_OK);
			CameraMainSuperControllerActivity.this.finish();
		}
	}

	private final class ErrorOnClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			setResult(Activity.RESULT_CANCELED);
			CameraMainSuperControllerActivity.this.finish();
		}
	}
}
