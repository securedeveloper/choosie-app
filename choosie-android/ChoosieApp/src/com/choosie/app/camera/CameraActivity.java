package com.choosie.app.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.choosie.app.R;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.util.Log;
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
	private int numOfPhotoToTake;
	private int screenHeight;
	private int screenWidth;
	private int camId;
	private CameraLayoutViewsHolder cameraLayoutViewHolder;

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

			Log.i("cameraApi", " enter onAutoFocus");
			if (isFocus) {
				mp.start();
				cameraLayoutViewHolder.focusImageView
						.setImageResource(R.drawable.focus_crosshair_image_in_focus);
			} else {
				cameraLayoutViewHolder.focusImageView
						.setImageResource(R.drawable.focus_crosshair_image_out_of_focus);
			}
			Log.i("cameraApi", " exits onAutoFocus, setting isFocusLeft = true");
			mHandler.postDelayed(mRunnable, 2000);
			cameraLayoutViewHolder.preview.setEnabled(true);

		}
	};

	private Camera.AutoFocusCallback myAutoFocusCallbackTakePicture = new AutoFocusCallback() {

		public void onAutoFocus(boolean isFocus, Camera arg1) {
			Log.i("cameraApi", " enter onAutoFocus");
			if (isFocus) {
				cameraLayoutViewHolder.focusImageView
						.setImageResource(R.drawable.focus_crosshair_image_in_focus);
			} else {
				cameraLayoutViewHolder.focusImageView
						.setImageResource(R.drawable.focus_crosshair_image_out_of_focus);
			}
			Log.i("cameraApi", " exits onAutoFocus, setting isFocusLeft = true");
			cameraLayoutViewHolder.preview.setEnabled(true);

		}
	};

	private PictureCallback mPicture = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			AsyncTask<byte[], Void, Boolean> manipulateTask = new AsyncTask<byte[], Void, Boolean>() {

				@Override
				protected Boolean doInBackground(byte[]... params) {
					return manipulateDataIntoFile(params[0], pictureFile);
				}

				@Override
				protected void onPostExecute(Boolean result) {
					if (result == false) {
						showErrorDialog("couldn't manipulate bitmaps");
						return;
					}

					path = pictureFile.getAbsolutePath();
					Log.i("cameraApi",
							"starting startConfirmationActivity with numOfPhotoToTake = "
									+ numOfPhotoToTake);
					cameraLayoutViewHolder.takePicButton.setEnabled(true);
					cameraLayoutViewHolder.preview.setEnabled(true);
					startConfirmationActivity(numOfPhotoToTake);
				}
			};

			manipulateTask.execute(data);
		}
	};

	protected void startConfirmationActivity(int number) {
		Intent confirmationIntent = new Intent(this.getApplicationContext(),
				ConfirmationActivity.class);
		confirmationIntent.putExtra("path", path);
		confirmationIntent.putExtra("topWrapperHeight", topWrapperHeight);
		confirmationIntent.putExtra("topHideHeight", topHideHeight + topHeight
				- topWrapperHeight);
		confirmationIntent.putExtra("bottomWrapperHeight", bottomWrapperHeight);
		confirmationIntent.putExtra("bottomHideHeight", bottomHideHeight
				+ bottomHeight - bottomWrapperHeight);
		confirmationIntent.putExtra("photoNumber", number);

		startActivityForResult(confirmationIntent, number);
	}

	private void writeDataIntoFile(byte[] data, File pictureFile) {
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();

		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}
	}

	private void writeBitmapToFile(Bitmap bitmap, File file, int quality) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
		byte[] bitmapdata = bos.toByteArray();
		writeDataIntoFile(bitmapdata, file);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_camera);
		Log.i(TAG, "onCreate");

		// first thing!! getting the camera
		if ((mCamera = getCameraInstance(CameraInfo.CAMERA_FACING_BACK)) == null) {
			showErrorDialog("Camera isnotavailable");
			return;
		}

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
		Log.i(TAG, "after constructor");

		if (Camera.getNumberOfCameras() == 1) {
			findViewById(R.id.cameraPreview_frontImage1).setVisibility(
					View.GONE);
		}

		// getting the file
		if ((pictureFile = getOutputMediaFile()) == null) {
			showErrorDialog("Couldn't create media file");
			return;
		}

		// set the height of all the layouts, so it will form a square
		display = getWindowManager().getDefaultDisplay();
		manipulateLayoutsHeight(display);

		// set initial text
		cameraLayoutViewHolder.textView_takePhoto.setText("Choozie first");

		// initial to the first photo
		numOfPhotoToTake = 1;

		cameraLayoutViewHolder.focusImageView.setVisibility(View.GONE);

		// create preview
		// mPreview = new CameraPreview(this, mCamera, display);
		// preview.addView(mPreview);

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

		Log.i("cameraApi", "entered manipulateLayoutsHeight");

		Camera.Parameters parameters = mCamera.getParameters();
		List<Size> previewSupportedSizes = parameters
				.getSupportedPreviewSizes();
		List<Size> picturesSupportedSizes = parameters
				.getSupportedPictureSizes();

		screenHeight = display.getHeight();
		screenWidth = display.getWidth();
		Size bestSize = findBestSize(previewSupportedSizes, display);

		Log.i("cameraApi", "screen width = " + screenWidth
				+ " screen height = " + screenHeight + "best width = "
				+ bestSize.width + "best height = " + bestSize.height);

		bestWidth = bestSize.width;
		bestHeight = bestSize.height;

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			bestWidth = bestSize.height;
			bestHeight = bestSize.width;
		}

		float density = getApplicationContext().getResources()
				.getDisplayMetrics().density;
		// topHeight =(screenHeight - bestHeight) / 2; //this is a fixed thing
		topHeight = 40;
		bottomHeight = screenHeight - bestHeight - topHeight;
		topHideHeight = (int) ((bestHeight - bestWidth) / 1.5);
		bottomHideHeight = bestHeight - bestWidth - topHideHeight;
		topWrapperHeight = Math.round(50 * density);
		bottomWrapperHeight = Math.max(Math.round(50 * density), bottomHeight);

		cameraLayoutViewHolder.preview.getLayoutParams().height = bestWidth;

		Log.i("cameraApi", "topHeight = " + topHeight + " bottomHeight = "
				+ bottomHeight + " topHideHeight = " + topHideHeight
				+ " bottomHideHeight = " + bottomHideHeight);

		// after calculating the sizes - setting heights
		setImageViewSize(cameraLayoutViewHolder.layoutTop, topHeight, 0);
		setImageViewSize(cameraLayoutViewHolder.layoutBottom, bottomHeight, 0);
		setImageViewSize(cameraLayoutViewHolder.hideTop, topHideHeight, 0);
		cameraLayoutViewHolder.hideTop.bringToFront();
		setImageViewSize(cameraLayoutViewHolder.hideBottom, bottomHideHeight, 0);
		cameraLayoutViewHolder.hideBottom.bringToFront();
		setImageViewSize(cameraLayoutViewHolder.layoutWrapperTop,
				topWrapperHeight, 0);
		cameraLayoutViewHolder.layoutWrapperTop.bringToFront();
		setImageViewSize(cameraLayoutViewHolder.layoutWrapperBottom,
				bottomWrapperHeight, 0);
		cameraLayoutViewHolder.layoutWrapperBottom.bringToFront();
		setImageViewSize(cameraLayoutViewHolder.takePicButton,
				bottomWrapperHeight, bottomWrapperHeight);
		setImageViewSize(cameraLayoutViewHolder.galleryImage,
				bottomWrapperHeight, bottomWrapperHeight);
		setImageViewSize(cameraLayoutViewHolder.flashImage, topWrapperHeight,
				topWrapperHeight);
		setImageViewSize(cameraLayoutViewHolder.frontImage, topWrapperHeight,
				topWrapperHeight);

		setImageViewSize(findViewById(R.id.cameraPreview_focusImage_layout),
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

	private void setImageViewSize(View imageView, int height, int width) {
		imageView.getLayoutParams().height = height;
		if (width > 0) {
			imageView.getLayoutParams().width = width;
		}
	}

	protected void startGalleryStuff() {
		path = pictureFile.getAbsolutePath();
		Log.i("cameraApi", "enterd startGalleryStuff");
		Intent intent = new Intent(this.getApplicationContext(),
				GalleryActivity.class);
		intent.putExtra("path", path);
		startActivityForResult(intent, 10);
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
		Log.i("cameraApi", "cameraActivity onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("cameraApi", "cameraActivity onResume, camId = " + camId);

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

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile() {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp1", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "IMG_" + timeStamp + ".jpg");

		return mediaFile;
	}

	private boolean manipulateDataIntoFile(byte[] data, File pictureFile) {

		try {
			// phase 1: write the date into the file - this is for loading it
			// efficiently without creating large bitmap
			writeDataIntoFile(data, pictureFile);

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

			Log.i("cameraApi", "in manipulateDataIntoFile, inSampleSize = "
					+ options.inSampleSize);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			Bitmap beforeRotation = BitmapFactory.decodeFile(
					pictureFile.getAbsolutePath(), options);
			// Bitmap beforeRotation = BitmapFactory.decodeByteArray(data, 0,
			// data.length);

			Log.i("cameraApi",
					"created before rotation, width = "
							+ beforeRotation.getWidth() + " height = "
							+ beforeRotation.getHeight() + " size = "
							+ beforeRotation.getRowBytes()
							* beforeRotation.getHeight());

			// phase 3: rotate Bitmap
			Bitmap afterRotation = rotateBitmap(beforeRotation);

			// Log.i("cameraApi",
			// "created after rotation, width = "
			// + afterRotation.getWidth() + " height = "
			// + afterRotation.getHeight() + " size = "
			// + afterRotation.getRowBytes()
			// * afterRotation.getHeight());

			beforeRotation.recycle();
			beforeRotation = null;

			Log.i("cameraApi", "recycled beforeRotation");

			// phase 4: crop it into a square and save it in the file
			int w = afterRotation.getWidth();
			int startPixel = ((w * topHideHeight) / screenWidth);
			Bitmap squareBitmap = Bitmap.createBitmap(afterRotation, 0,
					startPixel, w, w);

			Log.i("cameraApi", "cropped into a square = start pixel (y) - "
					+ startPixel + " width = " + w);

			Log.i("cameraApi",
					"scalled into asmallersize= " + squareBitmap.getWidth()
							+ " height = " + squareBitmap.getHeight()
							+ " size = " + squareBitmap.getRowBytes()
							* squareBitmap.getHeight());

			afterRotation.recycle();
			afterRotation = null;

			Log.i("cameraApi", "recycled afterRotation");

			writeBitmapToFile(squareBitmap, pictureFile, 95);

			squareBitmap.recycle();
			squareBitmap = null;

			Log.i("cameraApi", "recycled squareBitmap");
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void galleryAddPic(Uri uri) {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(uri);
		sendBroadcast(mediaScanIntent);
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
		Bitmap resizedBitmap = Bitmap.createBitmap(source, 0, 0, width, height,
				matrix, true);

		return resizedBitmap;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("cameraApi", "entered onActivityResult");
		switch (requestCode) {
		case 1:
			Log.i("cameraApi", "onActivityResult case 1");
			if (resultCode == Activity.RESULT_OK) {
				cameraLayoutViewHolder.textView_takePhoto
						.setText("Choozie second");
				numOfPhotoToTake = 2;
				galleryAddPic(Uri.fromFile(new File(path)));
				pictureFile = getOutputMediaFile(); // get new media picture
			}
			// mCamera.startPreview(); //it starts in onResume()
			break;
		case 2:
			if (resultCode == Activity.RESULT_OK) {
				cameraLayoutViewHolder.textView_takePhoto
						.setText("Choozie second");
				numOfPhotoToTake = 2;
				galleryAddPic(Uri.fromFile(new File(path)));
				pictureFile = getOutputMediaFile();
				setResult(Activity.RESULT_OK);
				finish();
			}
			break;

		case 10:
			if (resultCode == Activity.RESULT_OK) {
				startConfirmationActivity(numOfPhotoToTake);
				// startCropingStuff();
				break;
			}
		case 20:
			if (resultCode == Activity.RESULT_OK) {
				handleCroppedImage(data);
			}

		}

	}

	private void handleCroppedImage(Intent data) {
		final Bundle extras = data.getExtras();

		if (extras != null) {
			startConfirmationActivity(numOfPhotoToTake);
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
		Log.i("cameraApi", "cameraActivity - keyDown");
		finish();
		return true;
	}

	@Override
	protected void onDestroy() {
		Log.i("cameraApi", "cameraActivity cameraActivity onDestroy()");
		super.onDestroy();
		this.finish();
	}

	@Override
	protected void onRestart() {
		Log.i("cameraApi", "cameraActivity cameraActivity onRestart()");
		super.onRestart();
	}

	@Override
	protected void onStop() {
		Log.i("cameraApi", "cameraActivity cameraActivity onStop()");
		super.onStop();
	}

	@Override
	protected void onStart() {
		Log.i("cameraApi", "cameraActivity onStart()");
		super.onStart();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.i("cameraApi", "enter surfaceChanged - enter with w = " + w
				+ " h = " + h + " bestw = " + bestWidth + " bestH = "
				+ bestHeight);

		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null) {
			Log.i("cameraApi",
					"surfaceChanged - preview surface does not exist!");
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			Log.i("cameraApi",
					"surfaceChanged - tried to stop a non-existent preview");
			// ignore: tried to stop a non-existent preview
		}

		Camera.Parameters parameters = mCamera.getParameters();
		final List<Size> supportedPictureSizes = parameters
				.getSupportedPictureSizes();
		parameters.setPictureSize(supportedPictureSizes.get(0).width,
				supportedPictureSizes.get(0).height);

		if (display.getRotation() == Surface.ROTATION_0) {
			Log.i("cameraApi", "surfaceChanged rotation = 0");
			parameters.setPreviewSize(bestHeight, bestWidth);
			mCamera.setDisplayOrientation(90);
		}

		if (display.getRotation() == Surface.ROTATION_90) {
			Log.i("cameraApi", "surfaceChanged rotation = 90");
			parameters.setPreviewSize(w, h);
		}

		if (display.getRotation() == Surface.ROTATION_180) {
			Log.i("cameraApi", "surfaceChanged rotation = 180");
			parameters.setPreviewSize(h, w);
		}

		if (display.getRotation() == Surface.ROTATION_270) {
			Log.i("cameraApi", "surfaceChanged rotation = 270");
			parameters.setPreviewSize(w, h);
			mCamera.setDisplayOrientation(180);
		}

		mCamera.setParameters(parameters);

		// start preview with new settings
		try {
			// mHolder.setFixedSize(w1, h1);
			Log.i("cameraApi",
					"surfaceChanged : tring to set preview display in camera");
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d("OHHHno2", "Error starting camera preview: " + e.getMessage());
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("cameraApi", "cameraApi : enter surfaceCreated");
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			Log.i("cameraApi",
					"surfaceCreated - tring to set preview display in camera");
			mCamera.setPreviewDisplay(holder);

			mCamera.setPreviewCallback(new PreviewCallback() {
				// Called for each frame previewed
				public void onPreviewFrame(byte[] data, Camera camera) {
					Log.d(TAG,
							"onPreviewFrame called at: "
									+ System.currentTimeMillis());
					// CameraPreview.this.invalidate(); // <12>
				}
			});
			Log.i("cameraApi", "surfaceCreated - startingpreview in camera");
			mCamera.startPreview();

		} catch (IOException e) {
			Log.i("cameraApi", "failllllled");
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("cameraApi", "cameraApi : enter surfaceDestroyed");

		mCamera.stopPreview();
		mCamera.release();
		Log.i("cameraApi", "surfaceDestroyed - only settingcamera=null");
		mCamera = null;
	}

	private void switchCamera() {

		Log.i("cameraApi",
				"cameraActivity - entered doSome - stoping preview ( mCamera.stopPreview)");
		mCamera.stopPreview();
		Log.i("cameraApi",
				"cameraActivity - entered doSome - releasing camera forothe applications ( mCamera.release())");
		mCamera.release();

		if (camId == CameraInfo.CAMERA_FACING_BACK) {
			switchToFront();
		} else if (camId == CameraInfo.CAMERA_FACING_FRONT) {
			switchToBack();
		}

		// mCamera = getCameraInstance((camId + 1) % 2);
		manipulateLayoutsHeight(display);
		Log.i("cameraApi",
				"cameraActivity - entered doSome - calling surface created explicit");
		surfaceCreated(mHolder);
		Log.i("cameraApi",
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
				.setOnClickListener(cameraListeners.teakPictureListener);
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

		OnClickListener teakPictureListener = new OnClickListener() {

			public void onClick(View arg0) {
				if (mp != null) {
					mp.release();
				}

				mCamera.cancelAutoFocus();
				mCamera.takePicture(null, null, mPicture);
				cameraLayoutViewHolder.takePicButton.setEnabled(false);
				cameraLayoutViewHolder.preview.setEnabled(false);
			}
		};

		OnTouchListener clickButtonTouchListener = new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {

				Runnable mRunnable = new Runnable() {

					public void run() {
						cameraLayoutViewHolder.focusImageView
								.setVisibility(View.GONE);
						cameraLayoutViewHolder.focusImageView
								.setVisibility(View.GONE);
						mCamera.cancelAutoFocus();
						cameraLayoutViewHolder.takePicButton.performClick();
					}
				};

				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
					if (mCamera == null) {
						return false;
					}

					if (camId != CameraInfo.CAMERA_FACING_FRONT) {
						if (mCamera.getParameters().getFocusMode() != null) {
							return startAutoFocus(myAutoFocusCallbackTakePicture);
						}
					}
					return true;
				} else if ((arg1.getAction() == MotionEvent.ACTION_UP)
						&& ((arg1.getAction() != MotionEvent.ACTION_OUTSIDE))) {

					if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
						return false;
					}

					cameraLayoutViewHolder.preview.setEnabled(false);
					cameraLayoutViewHolder.takePicButton.setEnabled(false);
					Handler mHandler = new Handler();
					mHandler.postDelayed(mRunnable, 600);
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
			Log.i("cameraApi", "enter onTouch");
			mCamera.cancelAutoFocus();
			cameraLayoutViewHolder.focusImageView.setVisibility(View.VISIBLE);
			cameraLayoutViewHolder.focusImageView
					.setImageResource(R.drawable.focus_crosshair_image1);
			Log.i("cameraApi", "in onTouch, calling auto focus");

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