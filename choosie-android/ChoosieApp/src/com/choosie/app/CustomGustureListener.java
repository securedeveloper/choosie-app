package com.choosie.app;

//gesture
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public class CustomGustureListener implements OnGestureListener {

	private final Callback<Void, Void, Void> callback;

	public CustomGustureListener(Callback<Void, Void, Void> callback) {
		this.callback = callback;
	}

	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		callback.onFinish(null);
		return false;
	}

	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
