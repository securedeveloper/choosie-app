package com.choosie.app.camera;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.choosie.app.Callback;
import com.choosie.app.Constants;
import com.choosie.app.Logger;
import com.choosie.app.NewChoosiePostData;
import com.choosie.app.R;
import com.choosie.app.Utils;
import com.choosie.app.NewChoosiePostData.PostType;
import com.choosie.app.client.Client;
import com.facebook.Session;

import com.facebook.SessionState;
import com.facebook.Session.ReauthorizeRequest;
import com.facebook.Session.StatusCallback;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
	private EditText mQuestion;
	private ImageView mImage1;
	private ImageView mImage2;
	private TextView mTvFacebook;
	private ToggleButton mTbFacebook;
	private TableRow mTrFacebook;
	private Session session;
	// private StatusCallback statusCallback = new SessionStatusCallback();
	private ImageButton mBtnSubmit;

	private File imageFile1;
	private File imageFile2;

	private Bitmap image1BitmapTot;
	private Bitmap image2BitmapTot;
	private Bitmap image1BitmapYaanaa;
	private Bitmap image2BitmapYaanaa;

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
		Logger.i("CameraMainActivity - onCreate - about to startNewCameraActivity, imaggePath1 = "
				+ imagePath1 + " imagePath2 = " + imagePath2);
		startNewCameraActivity(Constants.RequestCodes.CAMERA_PICURE_FIRST,
				imagePath1);
	}

	private void InitializeComponents() {

		// initialize all components
		this.topLayout = (RelativeLayout) findViewById(R.id.post_layout_top);
		this.mQuestion = (EditText) findViewById(R.id.post_tvQuestion);
		this.mImage1 = (ImageView) findViewById(R.id.image_photo1);
		this.mImage2 = (ImageView) findViewById(R.id.image_photo2);
		this.mTvFacebook = (TextView) findViewById(R.id.tvFacebook);
		this.mTbFacebook = (ToggleButton) findViewById(R.id.tbFacebook);
		this.mBtnSubmit = (ImageButton) findViewById(R.id.post_btnSubmit);
		this.session = Session.getActiveSession();
		this.yaanaaImageView = (ImageView) findViewById(R.id.post_yaanaaButton_image);
		this.totImageView = (ImageView) findViewById(R.id.post_totButton_image);
		this.mTrFacebook = (TableRow) findViewById(R.id.tableRowShareFB);

		this.mTrFacebook.setOnClickListener(listener);
		this.mTbFacebook.setOnCheckedChangeListener(checkChangedListener);
		this.mBtnSubmit.setOnClickListener(onClickListenter);

		float density = getApplicationContext().getResources()
				.getDisplayMetrics().density;
		int topHeight = Math.round(50 * density);

		// set heights and shit
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

				int maxX = arg0.getWidth();
				int maxY = arg0.getHeight();

				float x = arg1.getX();
				float y = arg1.getY();

				// if moved outside the button - make the focus image gone
				if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
					if ((x < 0) || (y < 0) || (x > maxX) || (y > maxY)) {
						yaanaaImageView.setImageResource(R.drawable.yaa_naa);

						return true;
					}
				}

				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
					yaanaaImageView
							.setImageResource(R.drawable.yaa_naa_pressed);
				}

				if (arg1.getAction() == MotionEvent.ACTION_UP) {

					if ((x > 0) && (y > 0) && (x < maxX) && (y < maxY)) {
						yaanaaImageView.performClick();
					}
				}

				return true;

			}
		});

		totImageView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {

				if (postType == PostType.TOT) {
					return true;
				}

				int maxX = arg0.getWidth();
				int maxY = arg0.getHeight();

				float x = arg1.getX();
				float y = arg1.getY();

				// if moved outside the button - make the focus image gone
				if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
					if ((x < 0) || (y < 0) || (x > maxX) || (y > maxY)) {
						totImageView.setImageResource(R.drawable.tot);

						return true;
					}
				}

				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
					totImageView.setImageResource(R.drawable.tot_pressed);
				}

				if (arg1.getAction() == MotionEvent.ACTION_UP) {

					if ((x > 0) && (y > 0) && (x < maxX) && (y < maxY)) {
						totImageView.performClick();
					}
				}

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

		mImage1.setImageBitmap(image1BitmapTot);

		final Animation fadeOutAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fadein);

		postType = PostType.TOT;

		Animation fadeInAnimation = AnimationUtils.loadAnimation(this,
				R.anim.push_left_out);
		fadeInAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation animation) {
				if (image2BitmapTot != null) {
					mImage2.setImageBitmap(image2BitmapTot);

				} else {
					mImage2.setImageDrawable(getResources().getDrawable(
							R.drawable.plus));
				}
				mImage2.startAnimation(fadeOutAnimation);

			}
		});
		mImage2.startAnimation(fadeInAnimation);

		postType = PostType.TOT;

	}

	protected void switchToYaanaaMode() {

		if (postType == PostType.YesNo) {
			return;
		}
		yaanaaImageView.setImageResource(R.drawable.yaa_naa_pressed);
		totImageView.setImageResource(R.drawable.tot);

		final Animation fadeInAnimation = AnimationUtils.loadAnimation(this,
				R.anim.push_right_in);
		fadeInAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {

				mImage2.setImageBitmap(image2BitmapYaanaa);

			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

		postType = PostType.YesNo;
			}

			public void onAnimationEnd(Animation animation) {
				mImage2.setImageBitmap(image2BitmapYaanaa);

			}
		});

		final Animation fadeOutAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fadeout);
		fadeOutAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation animation) {
				mImage2.setImageBitmap(null);
				mImage2.startAnimation(fadeInAnimation);
			}
		});

		mImage2.startAnimation(fadeOutAnimation);
		// mImage1.startAnimation(fadeInAnimation);

		mImage1.setImageBitmap(image1BitmapYaanaa);
		// mImage2.setAnimation(null);

		// Animation fadeInAnimation = AnimationUtils.loadAnimation(this,
		// R.anim.push_right_in);
		// fadeInAnimation.setAnimationListener(new AnimationListener() {
		//
		// public void onAnimationStart(Animation animation) {
		//
		// mImage2.setImageBitmap(image2BitmapYaanaa);
		//
		// }
		//
		// public void onAnimationRepeat(Animation animation) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// public void onAnimationEnd(Animation animation) {
		// mImage2.setImageBitmap(image2BitmapYaanaa);
		//
		// }
		// });
		// mImage2.setAnimation(fadeInAnimation);
		// fadeInAnimation.startNow();
		// mImage2.startAnimation(fadeInAnimation);

		postType = PostType.YesNo;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

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
				Logger.i("CameraMainActivity - got back from first confirm, startNewCameraActivity, imaggePath2 = "
						+ imagePath2);
				saveImage1Bitmap();
				setImages();
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
				Logger.i("CameraMainActivity - got back from first confirm, startNewCameraActivity, imaggePath2 = "
						+ imagePath2);
				saveImage1Bitmap();
				setImages();

				// startSecondPicDialog();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// user declined, let's start the CameraActivity again with
				// first image
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
			Log.i(Constants.LOG_TAG, "after activity fb");
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
		}

	}

	private void setImages() {
		if (postType == PostType.TOT) {
			mImage1.setImageBitmap(image1BitmapTot);
		} else if (postType == PostType.YesNo) {
			mImage1.setImageBitmap(image1BitmapYaanaa);
			mImage2.setImageBitmap(image2BitmapYaanaa);
		}
	}

	private void saveImage2Bitmap() {

		mImage2.setImageBitmap(null);

		if (image2BitmapTot != null) {
			image2BitmapTot.recycle();
			image2BitmapTot = null;
		}

		// get images bitmaps
		image2BitmapTot = Utils.getBitmapFromFileByViewSize(imagePath2,
				Utils.getScreenWidth() / 2, Utils.getScreenWidth() / 2);

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
		image1BitmapTot = Utils.getBitmapFromFileByViewSize(imagePath1,
				Utils.getScreenWidth() / 2, Utils.getScreenWidth() / 2);

		// set image 1 and 2 yaanaa

		image1BitmapYaanaa = combine(image1BitmapTot, getResources()
				.getDrawable(R.drawable.yaa));
		image2BitmapYaanaa = combine(fastblur(image1BitmapTot, 20),
				getResources().getDrawable(R.drawable.naa));
	}

	private void handleResultFromCamera(int resultCode, Intent data,
			int requestCode, String path) {
		if (resultCode == Activity.RESULT_OK) {
			// starting ConfirmationActivity
			bundle = data.getExtras();
			startConfirmActivity(requestCode, path);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			goBackToChoosieActivity(Activity.RESULT_CANCELED);
		}
	}

	private void goBackToChoosieActivity(int result) {
		setResult(result);
		finish();
	}

	private void startConfirmActivity(int requestCode, String path) {
		Intent intent = new Intent(this.getApplication(),
				ConfirmationActivity.class);
		bundle.remove(Constants.IntentsCodes.path);
		Logger.i("CameraMain - inserting to intent path = " + path);
		intent.putExtra(Constants.IntentsCodes.path, path);
		intent.putExtras(bundle);
		startActivityForResult(intent, requestCode);
	}

	private void startNewCameraActivity(int requestCode, String path) {
		Logger.i("CameraMainActivity - startNewCameraActivity, path = " + path);
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
			setResult(Activity.RESULT_CANCELED);
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public Bitmap fastblur(Bitmap sentBitmap, int radius) {

		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return (bitmap);
	}

	private Bitmap combine(Bitmap bitmap1, Drawable drawable2) {

		int width = bitmap1.getWidth();
		int height = bitmap1.getHeight();

		Bitmap bitmap = null;
		try {
			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas c = new Canvas(bitmap);
			// Resources res = getResources();

			// Bitmap bitmap1 = BitmapFactory
			// .decodeResource(res, R.drawable.test1); // blue

			// Bitmap bitmap2 = BitmapFactory
			// .decodeResource(res, R.drawable.test2); // green
			Drawable drawable1 = new BitmapDrawable(bitmap1);
			// Drawable drawable2 = new BitmapDrawable(bitmap2);

			drawable1.setBounds(0, 0, width, height);
			drawable2.setBounds(0, 0, width, height);
			drawable1.draw(c);
			drawable2.draw(c);

		} catch (Exception e) {

		}
		return bitmap;
	}

	private OnCheckedChangeListener checkChangedListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {

				boolean userHasPublishPermissions = isUserHasPublishPermissions();

				if (userHasPublishPermissions) {
					Logger.i("Already have publish permissions: "
							+ session.getPermissions().toString());
					mTbFacebook.setChecked(true);
					mTbFacebook
							.setBackgroundResource(R.drawable.facebook_square_blue);

				} else {
					askForPublishPermissions();
					if (isUserHasPublishPermissions()) {
						mTbFacebook.setChecked(true);
						mTbFacebook
								.setBackgroundResource(R.drawable.facebook_square_blue);
					}
				}
			} else {
				mTbFacebook.setChecked(false);
				mTbFacebook
						.setBackgroundResource(R.drawable.facebook_square_bw);
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
			Logger.i("isUserHasPublishPermissions(): session is not opened!");
		}
		return userHasPublishPermissions;
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
				Log.e(Constants.LOG_TAG,
						"Exception in reauthorizeForPublish() : "
								+ ex.toString());
			}
			Session.setActiveSession(session);
			Logger.i("on set active session permissions: "
					+ session.getPermissions().toString());
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		public void call(Session session, SessionState state,
				Exception exception) {
			Logger.i("Entered SessionStatusCallback()");
		}
	}

	private OnClickListener onClickListenter = new OnClickListener() {

		public void onClick(View v) {
			NewChoosiePostData ncpd;
			Bitmap bmp1, bmp2;
		
			if (postType == PostType.TOT) {
				bmp1 = image1BitmapTot;
				bmp2 = image2BitmapTot;
			} else {
				bmp1 = image1BitmapYaanaa;
				bmp2 = image2BitmapYaanaa;
			}
				
			ncpd = new NewChoosiePostData(bmp1, bmp2, mQuestion.getText()
					.toString(), mTbFacebook.isChecked(), postType);
			submitPost(ncpd);
		}
	};

	protected void submitPost(NewChoosiePostData ncpd) {
		if (ncpd.isShareOnFacebook() && !isUserHasPublishPermissions()) {
			Logger.i("Share on facebook is checked!");
			askForPublishPermissions();
		}
		Logger.i("executing submitChoosiePost()");
		submitChoosiePost(ncpd);
	}

	private void submitChoosiePost(NewChoosiePostData ncpd) {

		boolean isPostValid = isPostValid();
		if (isPostValid) {
			Tracker tracker = GoogleAnalytics.getInstance(this)
					.getDefaultTracker();
			tracker.trackEvent("Ui action", "Post Screen", "Share", null);

			Client.getInstance().sendChoosiePostToServer(ncpd,
					new Callback<Void, Integer, Void>() {

						@Override
						public void onPre(Void param) {
							// open progress bar
						}

						@Override
						public void onProgress(Integer param) {
							// show progress
						}

						@Override
						public void onFinish(Void param) {
							goBackToChoosieActivity(Activity.RESULT_OK);
						}
					});
		}
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
}
