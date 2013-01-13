package com.choosie.app.camera;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.choosie.app.Constants;
import com.choosie.app.L;
import com.choozie.app.R;
import com.choosie.app.Utils;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;

import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

	private Camera mCamera;
	// private CameraPreview mPreview;
	String TAG = "Mainctivity";

	private Display display;
	private SurfaceHolder mHolder;
	private boolean needToTakePhoto;
	private boolean isReturnedFromFocus;
	private boolean canTouchButton = false;
	private boolean canTouchPreview = false;

	private File pictureFile;
	private String path;
	private int topHeight;
	private int topHideHeight;
	private int bottomHeight;
	private int bottomHideHeight;
	private int bestHeight;
	private int bestWidth;
	private int topWrapperHeight;
	private int bottomWrapperHeight;
	private int screenHeight;
	private int screenWidth;
	private int camId;
	private CameraLayoutViewsHolder cameraLayoutViewHolder;
	private Intent intent;
	private boolean canUseBackKey = false;
	Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {

		public void run() {
			cameraLayoutViewHolder.focusImageView.setVisibility(View.GONE);
		}
	};

	private MediaPlayer mp;
	private Camera.AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

		public void onAutoFocus(boolean isFocus, Camera arg1) {

			mp = MediaPlayer.create(CameraActivity.this, R.raw.camera_focus);
			mp.setOnCompletionListener(new OnCompletionListener() {

				public void onCompletion(MediaPlayer mp) {
					mp.release();
				}
			});

			L.i("cameraApi", " enter onAutoFocus");
			if (isFocus) {
				mp.start();
				cameraLayoutViewHolder.focusImageView
						.setImageResource(R.drawable.focus_crosshair_image_in_focus);
			} else {
				cameraLayoutViewHolder.focusImageView
						.setImageResource(R.drawable.focus_crosshair_image_out_of_focus);
			}
			L.i("cameraApi",
					" exits onAutoFocus, setting isFocusLeft = true");

			if (needToTakePhoto) {
				cameraLayoutViewHolder.galleryImage.setEnabled(false);
				mCamera.takePicture(null, null, mPicture);
				cameraLayoutViewHolder.takePicButton.setEnabled(false);
				cameraLayoutViewHolder.preview.setEnabled(false);
			} else {
				cameraLayoutViewHolder.preview.setEnabled(true);
				mHandler.postDelayed(mRunnable, 2000); // makes he focus image
														// gone
			}

			isReturnedFromFocus = true;
		}
	};

	private PictureCallback mPicture = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {

			boolean result = manipulateDataIntoFile(data, pictureFile);
			if (result == false) {
				cancelWothError();
			}
			fillIntentsWithHeightsAndSetResult();
		}
	};

	protected void fillIntentsWithHeightsAndSetResult() {
		fillIntentWithHeight(intent);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	protected void cancelWothError() {
		Intent intent = new Intent();
		intent.putExtra(Constants.IntentsCodes.error, true);
		setResult(Activity.RESULT_CANCELED, intent);
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		L.i("cameraApi", "CameraActivity onCreate");
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_camera);
		L.i(TAG, "onCreate");

		// first thing!! getting the camera
		if ((mCamera = getCameraInstance(CameraInfo.CAMERA_FACING_BACK)) == null) {
			// showErrorDialog("Camera isnotavailable");
			cancelWothError();
			finish();
			return;
		}
		mCamera.setDisplayOrientation(90);
		mCamera.setDisplayOrientation(90);

		cameraLayoutViewHolder = new CameraLayoutViewsHolder();

		initializeView();

		initializeListeners();

		// initialize preview - it is used in mani...

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = cameraLayoutViewHolder.preview.getHolder();
		// mHolder.setFixedSize(640, 480);
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		L.i(TAG, "after constructor");

		if (Camera.getNumberOfCameras() == 1) {
			findViewById(R.id.cameraPreview_frontImage1).setVisibility(
					View.GONE);
		}

		intent = getIntent();
		path = intent.getStringExtra(Constants.IntentsCodes.path);
		pictureFile = new File(path);

		// set the height of all the layouts, so it will form a square
		display = getWindowManager().getDefaultDisplay();
		// manipulateLayoutsHeight(display);

		// set initial text
		cameraLayoutViewHolder.textView_takePhoto.setText("Choozie.");

		// on create. set the focus gone, anyway
		cameraLayoutViewHolder.focusImageView.setVisibility(View.GONE);
	}

	private void initializeView() {
		cameraLayoutViewHolder.takePicButton = (Button) findViewById(R.id.button_take_picture);
		cameraLayoutViewHolder.galleryImage = (ImageView) findViewById(R.id.cameraPreview_galleryIcon);
		cameraLayoutViewHolder.flashImage = (ImageView) findViewById(R.id.cameraPreview_flashImage);
		cameraLayoutViewHolder.frontImage = (ImageView) findViewById(R.id.cameraPreview_frontImage1);
		cameraLayoutViewHolder.preview = (SurfaceView) findViewById(R.id.camera_preview1);
		cameraLayoutViewHolder.textView_takePhoto = (TextView) findViewById(R.id.cameraLayout_textView_top);
		cameraLayoutViewHolder.focusImageView = (ImageView) findViewById(R.id.cameraPreview_focusImage);
		cameraLayoutViewHolder.layoutWrapperTop = (RelativeLayout) findViewById(R.id.layout_wrapper_top);
		cameraLayoutViewHolder.layoutTop = (RelativeLayout) findViewById(R.id.layout_top);
		cameraLayoutViewHolder.layoutBottom = (RelativeLayout) findViewById(R.id.layout_bottom);
		cameraLayoutViewHolder.hideTop = (RelativeLayout) findViewById(R.id.hide_layout_top);
		cameraLayoutViewHolder.hideBottom = (RelativeLayout) findViewById(R.id.hide_layout_bottom);
		cameraLayoutViewHolder.layoutWrapperBottom = (RelativeLayout) findViewById(R.id.layout_wrapper_bottom);
	}

	private void fillIntentWithHeight(Intent intent) {
		intent.putExtra(Constants.IntentsCodes.cameraTopWrapperHeight,
				topWrapperHeight);
		intent.putExtra(Constants.IntentsCodes.cameraTopHideHeight,
				topHideHeight + topHeight);
		intent.putExtra(Constants.IntentsCodes.cameraBottomWrapperHeight,
				bottomWrapperHeight);
		intent.putExtra(Constants.IntentsCodes.cameraBottomHideHeight,
				bottomHideHeight + bottomHeight);
	}

	private void showErrorDialog(String errorMsg) {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("");
		myAlertDialog.setMessage(errorMsg);

		myAlertDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg1, int arg3) {
						setResult(Activity.RESULT_CANCELED);
						finish();
					}
				});
		myAlertDialog.show();

	}

	protected void handleFlashClick(ImageView flashImage) {

		Camera.Parameters parameters = mCamera.getParameters();
		List<String> supportedFlashModes = parameters.getSupportedFlashModes();

		String currentFlashMode = parameters.getFlashMode();

		if (currentFlashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
			if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
				flashImage.setImageResource(R.drawable.flash_on);
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			} else {
				if (supportedFlashModes
						.contains(Camera.Parameters.FLASH_MODE_OFF)) {
					flashImage.setImageResource(R.drawable.flash_none);
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				}
			}
		}
		if (currentFlashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
			if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
				flashImage.setImageResource(R.drawable.flash_none);
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			} else {
				if (supportedFlashModes
						.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
					flashImage.setImageResource(R.drawable.flash_auto);
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
				}
			}
		}
		if (currentFlashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
			if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
				flashImage.setImageResource(R.drawable.flash_auto);
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			}
		}

		mCamera.setParameters(parameters);

	}

	private void manipulateLayoutsHeight(Display display) {

		L.i("cameraApi", "entered manipulateLayoutsHeight");

		Camera.Parameters parameters = mCamera.getParameters();
		List<Size> previewSupportedSizes = parameters
				.getSupportedPreviewSizes();
		List<Size> picturesSupportedSizes = parameters
				.getSupportedPictureSizes();

		screenHeight = display.getHeight();
		screenWidth = display.getWidth();
		Size bestSize = findBestSize(previewSupportedSizes, display);

		bestWidth = bestSize.width;
		bestHeight = bestSize.height;

		if (bestWidth > bestHeight) {
			bestWidth = bestSize.height;
			bestHeight = bestSize.width;
		}

		L.i("cameraApi", "screen width = " + screenWidth
				+ " screen height = " + screenHeight + "best width = "
				+ bestSize.width + "best height = " + bestSize.height);

		float density = getApplicationContext().getResources()
				.getDisplayMetrics().density;
		// first set the height of the wrappers - which hold the buttons...
		topWrapperHeight = Math.round(50 * density);
		bottomWrapperHeight = Math.round(85 * density);
		topHeight = topWrapperHeight;
		topHideHeight = (screenHeight - screenWidth - topHeight - bottomWrapperHeight) / 2;
		bottomHeight = screenHeight - bestHeight - topHideHeight - topHeight;
		bottomHideHeight = screenHeight - bottomHeight - screenWidth
				- topHeight - topHideHeight;

		cameraLayoutViewHolder.preview.getLayoutParams().height = bestWidth;

		L.i("cameraApi", "topWrapperHeight = " + topWrapperHeight
				+ " bottomWrapperHeight = " + bottomWrapperHeight
				+ " topHeight = " + topHeight + " bottomHeight = "
				+ bottomHeight + " topHideHeight = " + topHideHeight
				+ " bottomHideHeight = " + bottomHideHeight);

		// after calculating the sizes - setting heights
		Utils.setImageViewSize(cameraLayoutViewHolder.layoutTop, topHeight, 0);
		Utils.setImageViewSize(cameraLayoutViewHolder.layoutBottom,
				bottomHeight, 0);
		Utils.setImageViewSize(cameraLayoutViewHolder.hideTop, topHideHeight, 0);
		cameraLayoutViewHolder.hideTop.bringToFront();
		Utils.setImageViewSize(cameraLayoutViewHolder.hideBottom,
				bottomHideHeight, 0);
		cameraLayoutViewHolder.hideBottom.bringToFront();
		Utils.setImageViewSize(cameraLayoutViewHolder.layoutWrapperTop,
				topWrapperHeight, 0);
		cameraLayoutViewHolder.layoutWrapperTop.bringToFront();
		Utils.setImageViewSize(cameraLayoutViewHolder.layoutWrapperBottom,
				bottomWrapperHeight, 0);
		cameraLayoutViewHolder.layoutWrapperBottom.bringToFront();
		Utils.setImageViewSize(cameraLayoutViewHolder.takePicButton,
				bottomWrapperHeight, bottomWrapperHeight);
		Utils.setImageViewSize(cameraLayoutViewHolder.galleryImage,
				bottomWrapperHeight, bottomWrapperHeight);
		Utils.setImageViewSize(cameraLayoutViewHolder.flashImage,
				topWrapperHeight, topWrapperHeight);
		Utils.setImageViewSize(cameraLayoutViewHolder.frontImage,
				topWrapperHeight, topWrapperHeight);

		Utils.setImageViewSize(
				findViewById(R.id.cameraPreview_focusImage_layout),
				bottomWrapperHeight, bottomWrapperHeight);

		int topMargin = (screenHeight / 2)
				- (bottomWrapperHeight / 2)
				+ ((topHeight + topHideHeight) - (bottomHeight + bottomHideHeight))
				/ 2;

		((RelativeLayout.LayoutParams) findViewById(
				R.id.cameraPreview_focusImage_layout).getLayoutParams())
				.setMargins((screenWidth / 2) - (bottomWrapperHeight) / 2,
						topMargin, 0, 0);
	}

	// protected void startGalleryStuff() {
	// path = pictureFile.getAbsolutePath();
	// Logger.i("cameraApi", "enterd startGalleryStuff");
	// Intent intent = new Intent(this.getApplicationContext(),
	// GalleryActivity.class);
	// intent.putExtra(Constants.IntentsCodes.path, path);
	// startActivityForResult(intent,
	// Constants.RequestCodes.CAMERA_PICURE_GALLERY);
	// }

	private void startGalleryStuff() {

		cameraLayoutViewHolder.takePicButton.setEnabled(false);

		mCamera.stopPreview();
		L.i("cameraApi", "in startCropingStuff");

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", getWindowManager().getDefaultDisplay()
				.getWidth());
		intent.putExtra("outputY", getWindowManager().getDefaultDisplay()
				.getWidth());
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(path)));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", false); // lol, negative boolean
													// noFaceDetection
		startActivityForResult(intent,
				Constants.RequestCodes.CAMERA_GALLERY_CROP);

	}

	// find the optimal size - closest to a square
	private Size findBestSize(List<Size> supportedSizes, final Display display) {
		int basicSize = screenWidth;
		Size bestSize = null;
		int configeredOrientation = getResources().getConfiguration().orientation;
		for (Size size : supportedSizes) {
			int height = size.height;
			int width = size.width;
			int bestHeight = size.height;
			int bestWidth = size.width;
			if (configeredOrientation == Configuration.ORIENTATION_PORTRAIT) {
				height = size.width;
				width = size.height;
				if (bestSize != null) {
					bestHeight = bestSize.width;
					bestWidth = bestSize.height;
				} else {
					bestHeight = size.width;
					bestWidth = size.height;
				}
			}
			if (width <= basicSize) {
				if (bestSize == null) {
					bestSize = size;
				} else {
					if (width > bestWidth) {
						bestSize = size;
					} else {
						if ((width == bestWidth) && (height < bestHeight)) {
							bestSize = size;
						}
					}
				}
			}
		}

		return bestSize;
	}

	@Override
	protected void onPause() {
		L.i("cameraApi", "cameraActivity onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		L.i("cameraApi", "cameraActivity onResume, camId = " + camId);

		// Open the default i.e. the first rear facing camera.
		if (mCamera == null) {
			if ((mCamera = getCameraInstance(camId)) == null) {
				showErrorDialog("Camera is not available");
				return;
			} else {
				// mPreview.onResume(mCamera);
			}
		}
		manipulateLayoutsHeight(display);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	/** A safe way to get an instance of the Camera object. */
	private Camera getCameraInstance(int cameraId) {
		Camera c = null;
		try {
			c = Camera.open(cameraId); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			return null;
		}
		camId = cameraId;
		return c; // returns null if camera is unavailable
	}

	private boolean manipulateDataIntoFile(byte[] data, File pictureFile) {

		try {
			// phase 1: write the date into the file - this is for loading it
			// efficiently without creating large bitmap
			Utils.writeBytesIntoFile(data, pictureFile);

			// phase 2: get Bitmap from file

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), options);

			int normalheight = options.outHeight;

			// Calculate inSampleSize
			if (normalheight > screenWidth * 2) {
				options.inSampleSize = 2; // we deveide it by 2
				// calculateInSampleSize(options, 1224, 1224);
			} else {
				options.inSampleSize = 1;
			}

			L.i("cameraApi", "in manipulateDataIntoFile, inSampleSize = "
					+ options.inSampleSize);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			Bitmap beforeRotation = BitmapFactory.decodeFile(
					pictureFile.getAbsolutePath(), options);
			// Bitmap beforeRotation = BitmapFactory.decodeByteArray(data, 0,
			// data.length);

			L.i(
					"cameraApi",
					"created before rotation, width = "
							+ beforeRotation.getWidth() + " height = "
							+ beforeRotation.getHeight() + " size = "
							+ beforeRotation.getRowBytes()
							* beforeRotation.getHeight());

			// phase 3: rotate Bitmap
			Bitmap afterRotation = rotateBitmap(beforeRotation);

			// Logger.i("cameraApi",
			// "created after rotation, width = "
			// + afterRotation.getWidth() + " height = "
			// + afterRotation.getHeight() + " size = "
			// + afterRotation.getRowBytes()
			// * afterRotation.getHeight());

			if (beforeRotation != null) {
				beforeRotation.recycle();
				beforeRotation = null;
			}

			L.i("cameraApi", "recycled beforeRotation");

			// phase 4: crop it into a square and save it in the file
			int w = afterRotation.getWidth();
			int startPixel = ((w * topHideHeight) / screenWidth);
			Bitmap squareBitmap = Bitmap
					.createBitmap(afterRotation, 0, 0, w, w);

			L.i("cameraApi", "cropped into a square = start pixel (y) - "
					+ startPixel + " width = " + w);

			L.i("cameraApi",
					"scalled into asmallersize= " + squareBitmap.getWidth()
							+ " height = " + squareBitmap.getHeight()
							+ " size = " + squareBitmap.getRowBytes()
							* squareBitmap.getHeight());

			afterRotation.recycle();
			afterRotation = null;

			L.i("cameraApi", "recycled afterRotation");

			Utils.writeBitmapToFile(squareBitmap, pictureFile, 95);

			squareBitmap.recycle();
			squareBitmap = null;

			L.i("cameraApi", "recycled squareBitmap");
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private Bitmap rotateBitmap(Bitmap source) {
		CameraInfo cameraInfo = new CameraInfo();
		Camera.getCameraInfo(camId, cameraInfo);
		int result = cameraInfo.orientation;

		int width = source.getWidth();
		int height = source.getHeight();

		Matrix matrix = new Matrix();
		if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (360 - result) % 360; // compensate the mirror
			matrix.preScale(-1, 1);
		}

		matrix.postRotate(result);
		Bitmap resizedBitmap = null;
		try {
			resizedBitmap = Bitmap.createBitmap(source, 0, 0, width, height,
					matrix, true);
		} catch (OutOfMemoryError e) {

			L.i("cameraApi", "rotateBitmap - trying to set smaller bitmap");

			// try to load smaller size
			Bitmap smallerBitmap = Bitmap.createScaledBitmap(source, width / 2,
					height / 2, true);

			source.recycle();
			source = null;

			resizedBitmap = Bitmap.createBitmap(smallerBitmap, 0, 0, width / 2,
					height / 2, matrix, true);
			smallerBitmap.recycle();
			smallerBitmap = null;
		}

		return resizedBitmap;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		L.i("cameraApi", "entered onActivityResult");
		switch (requestCode) {
		case Constants.RequestCodes.CAMERA_GALLERY_CROP:
			// got back from gallery;
			if (resultCode == Activity.RESULT_OK) {
				// nice, lets go start to main camera super controller
				// activity!
				fillIntentsWithHeightsAndSetResult();
				break;
			} else {
				if (mCamera != null) {
					mCamera.startPreview();
				}
				cameraLayoutViewHolder.takePicButton.setEnabled(true);
			}
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	@Override
	public final boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (canUseBackKey == true) {
				L.i("cameraApi", "cameraActivity - keyDown");
				setResult(Activity.RESULT_CANCELED);
				finish();
			} else {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		L.i("cameraApi", "cameraActivity cameraActivity onDestroy()");
		super.onDestroy();
		this.finish();
	}

	@Override
	protected void onRestart() {
		L.i("cameraApi", "cameraActivity cameraActivity onRestart()");
		super.onRestart();
	}

	@Override
	protected void onStop() {
		L.i("cameraApi", "cameraActivity cameraActivity onStop()");
		super.onStop();
	}

	@Override
	protected void onStart() {
		L.i("cameraApi", "cameraActivity onStart()");
		super.onStart();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, final int w,
			final int h) {

		AsyncTask<Void, Void, Void> surfaceChangedTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				surfaceChangedTaskJob(w, h);
				canUseBackKey = true;
				return null;
			}

		};

		surfaceChangedTask.execute();

	}

	private void surfaceChangedTaskJob(int w, int h) {
		L.i("cameraApi", "enter surfaceChanged - enter with w = " + w
				+ " h = " + h + " bestw = " + bestWidth + " bestH = "
				+ bestHeight);

		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null) {
			L.i("cameraApi",
					"surfaceChanged - preview surface does not exist!");
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			L.i("cameraApi",
					"surfaceChanged - tried to stop a non-existent preview");
			// ignore: tried to stop a non-existent preview
		}

		Camera.Parameters parameters = mCamera.getParameters();
		final List<Size> supportedPictureSizes = parameters
				.getSupportedPictureSizes();
		parameters.setPictureSize(supportedPictureSizes.get(0).width,
				supportedPictureSizes.get(0).height);

		Size bestSize = findBestSize(parameters.getSupportedPreviewSizes(),
				display);
		h = bestSize.width;
		w = bestSize.height;

		if (display.getRotation() == Surface.ROTATION_0) {
			L.i("cameraApi", "surfaceChanged rotation = 0");
			parameters.setPreviewSize(h, w);
			mCamera.setDisplayOrientation(90);
		}

		if (display.getRotation() == Surface.ROTATION_90) {
			L.i("cameraApi", "surfaceChanged rotation = 90");
			parameters.setPreviewSize(w, h);
		}

		if (display.getRotation() == Surface.ROTATION_180) {
			L.i("cameraApi", "surfaceChanged rotation = 180");
			parameters.setPreviewSize(h, w);
		}

		if (display.getRotation() == Surface.ROTATION_270) {
			L.i("cameraApi", "surfaceChanged rotation = 270");
			parameters.setPreviewSize(w, h);
			mCamera.setDisplayOrientation(180);
		}

		mCamera.setParameters(parameters);

		// start preview with new settings
		try {
			// mHolder.setFixedSize(w1, h1);
			L.i("cameraApi",
					"surfaceChanged : tring to set preview display in camera");
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();

		} catch (Exception e) {
			L.d("Error starting camera preview: " + e.getMessage());
		}

		canTouchButton = true;
		canTouchPreview = true;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		L.i("cameraApi", "cameraApi : enter surfaceCreated");
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			L.i("cameraApi",
					"surfaceCreated - tring to set preview display in camera");
			mCamera.setPreviewDisplay(holder);

			mCamera.setPreviewCallback(new PreviewCallback() {
				// Called for each frame previewed
				public void onPreviewFrame(byte[] data, Camera camera) {
					L.d("onPreviewFrame called at: "
							+ System.currentTimeMillis());
					// CameraPreview.this.invalidate(); // <12>
				}
			});
			L.i("cameraApi", "surfaceCreated - startingpreview in camera");
			// mCamera.startPreview();

		} catch (IOException e) {
			L.i("cameraApi", "failllllled");
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		L.i("cameraApi", "cameraApi : enter surfaceDestroyed");
		canUseBackKey = false;
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	private void switchCamera() {

		canTouchButton = false;
		canTouchPreview = false;

		L.i("cameraApi",
				"cameraActivity - entered doSome - stoping preview ( mCamera.stopPreview)");
		mCamera.stopPreview();
		L.i(
				"cameraApi",
				"cameraActivity - entered doSome - releasing camera forothe applications ( mCamera.release())");
		mCamera.release();

		if (camId == CameraInfo.CAMERA_FACING_BACK) {
			switchToFront();
		} else if (camId == CameraInfo.CAMERA_FACING_FRONT) {
			switchToBack();
		}

		manipulateLayoutsHeight(display);
		L.i("cameraApi",
				"cameraActivity - entered doSome - calling surface created explicit");
		surfaceCreated(mHolder);
		L.i("cameraApi",
				"cameraActivity - entered doSome - calling surfaceChanged explicit");
		surfaceChanged(mHolder, 0, 480, 640);
	}

	private void switchToFront() {

		cameraLayoutViewHolder.flashImage.setVisibility(View.GONE);
		cameraLayoutViewHolder.preview.setEnabled(false);
		mCamera = getCameraInstance(CameraInfo.CAMERA_FACING_FRONT);
	}

	private void switchToBack() {

		mCamera = getCameraInstance(CameraInfo.CAMERA_FACING_BACK);
		if (mCamera.getParameters().getSupportedFlashModes() != null) {
			cameraLayoutViewHolder.flashImage.setVisibility(View.VISIBLE);
		}
		if (mCamera.getParameters().getSupportedFocusModes() != null) {
			cameraLayoutViewHolder.preview.setEnabled(true);
		}
	}

	private void initializeListeners() {

		CameraListeners cameraListeners = new CameraListeners();
		// adjust the button click
		cameraLayoutViewHolder.takePicButton
				.setOnTouchListener(cameraListeners.clickButtonTouchListener);

		// set on click gallery
		cameraLayoutViewHolder.galleryImage
				.setOnClickListener(cameraListeners.galleryListener);

		// set focus listener
		cameraLayoutViewHolder.preview
				.setOnTouchListener(cameraListeners.focusListener);

		// set on click flash
		final Camera.Parameters parameters = mCamera.getParameters();

		// set on click front camera
		cameraLayoutViewHolder.frontImage
				.setOnClickListener(cameraListeners.frontListener);

		// handle flash thing
		final List<String> supportedFlashModes = parameters
				.getSupportedFlashModes();

		if (supportedFlashModes == null) {
			cameraLayoutViewHolder.flashImage.setVisibility(View.GONE);
		} else {
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			mCamera.setParameters(parameters);
			cameraLayoutViewHolder.flashImage
					.setImageResource(R.drawable.flash_auto);
			cameraLayoutViewHolder.flashImage
					.setOnClickListener(cameraListeners.flashListener);
		}
	}

	private class CameraListeners {

		OnTouchListener clickButtonTouchListener = new OnTouchListener() {

			// CameraLayoutViewsHoldersetSelected(!volumemuteImageButton.isSelected());

			public boolean onTouch(View arg0, MotionEvent arg1) {

				if (canTouchButton == false) {
					return false;
				}

				cameraLayoutViewHolder.takePicButton
						.setSelected(!(cameraLayoutViewHolder.takePicButton
								.isSelected()));

				float y = arg1.getY();
				float x = arg1.getX();

				// if moved outside the button - make the focus image gone
				if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
					if ((x < 0) || (y < 0) || (x > bottomWrapperHeight)
							|| (y > bottomWrapperHeight)) {
						cameraLayoutViewHolder.focusImageView
								.setVisibility(View.GONE);
						cameraLayoutViewHolder.takePicButton
								.setBackgroundDrawable(getResources()
										.getDrawable(R.drawable.camera));
						return true;
					}
				}

				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {

					// just pressed the button
					cameraLayoutViewHolder.takePicButton
							.setBackgroundDrawable(getResources().getDrawable(
									R.drawable.camera_button_pressed));

					// set - don't take the photo just yet, only on ACTION_UP
					needToTakePhoto = false;

					if (mCamera == null) {
						// we don't suppose to get here...
						return false;
					}

					if (camId != CameraInfo.CAMERA_FACING_FRONT) {
						// start the focus only if camera facing front
						if (mCamera.getParameters().getFocusMode() != null) {
							isReturnedFromFocus = false;
							return startAutoFocus(myAutoFocusCallback);
						}
					} else {
						isReturnedFromFocus = true;
						;
					}
					return true;
				} else if (arg1.getAction() == MotionEvent.ACTION_UP) {

					// when ACTION_UP - take picture only if inside the button
					// area, and if it returned from focus.
					if ((x > 0) && (y > 0) && (x < bottomWrapperHeight)
							&& (y < bottomWrapperHeight)) {

						cameraLayoutViewHolder.takePicButton
								.setBackgroundDrawable(getResources()
										.getDrawable(R.drawable.camera));
						if (isReturnedFromFocus == true) {
							cameraLayoutViewHolder.takePicButton
									.setEnabled(false);
							cameraLayoutViewHolder.preview.setEnabled(false);
							cameraLayoutViewHolder.galleryImage
									.setEnabled(false);
							mCamera.takePicture(null, null, mPicture);
						} else {
							needToTakePhoto = true;
						}

					} else {
						// don't take photo
						needToTakePhoto = false;
					}

					return true;
				}
				return false;

			}
		};

		OnClickListener galleryListener = new OnClickListener() {

			public void onClick(View v) {
				startGalleryStuff();
			}
		};

		OnTouchListener focusListener = new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				if (canTouchPreview == false) {
					return false;
				}
				if (mCamera == null) {
					return false;
				}
				if (camId != CameraInfo.CAMERA_FACING_FRONT) {
					if (mCamera.getParameters().getFocusMode() != null) {
						return startAutoFocus(myAutoFocusCallback);
					}
				}
				return false;
			}

		};

		OnClickListener frontListener = new OnClickListener() {

			public void onClick(View arg0) {
				switchCamera();
			}
		};

		OnClickListener flashListener = new OnClickListener() {

			public void onClick(View v) {
				handleFlashClick(cameraLayoutViewHolder.flashImage);
			}
		};

		private boolean startAutoFocus(
				Camera.AutoFocusCallback autoFocusCallback) {
			L.i("cameraApi", "enter onTouch");
			mCamera.cancelAutoFocus();
			cameraLayoutViewHolder.focusImageView.setVisibility(View.VISIBLE);
			cameraLayoutViewHolder.focusImageView
					.setImageResource(R.drawable.focus_crosshair_image1);
			L.i("cameraApi", "in onTouch, calling auto focus");

			mHandler.removeCallbacks(mRunnable);
			mCamera.autoFocus(autoFocusCallback);
			cameraLayoutViewHolder.preview.setEnabled(false);
			return true;
		}
	}

	private class CameraLayoutViewsHolder {
		private TextView textView_takePhoto;
		private ImageView focusImageView;
		private RelativeLayout layoutWrapperTop;
		private RelativeLayout layoutTop;
		private RelativeLayout layoutBottom;
		private RelativeLayout hideTop;
		private RelativeLayout hideBottom;
		private RelativeLayout layoutWrapperBottom;
		private SurfaceView preview;
		private ImageView frontImage;
		private ImageView flashImage;
		private Button takePicButton;
		private ImageView galleryImage;
	}
}