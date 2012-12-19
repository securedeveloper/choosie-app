package com.choosie.app;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class CustomEnlargePagerAdapter extends PagerAdapter {
	private String image1Path;
	private String image2Path;
	private Display display;

	public CustomEnlargePagerAdapter(String image1Path, String image2Path,
			Display display) {
		super();
		this.image1Path = image1Path;
		this.image2Path = image2Path;
		this.display = display;
	}

	public int getCount() {
		return 2;
	}

	public Object instantiateItem(View collection, int position) {
		LayoutInflater inflater = (LayoutInflater) collection.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int resId = R.layout.view_enlarge_photo;
		View view = inflater.inflate(resId, null);
		ImageView imageView = (ImageView) view
				.findViewById(R.id.view_enlarge_image1);
		String imagePath = null;
		switch (position) {
		case 0:
			imagePath = image1Path;
			break;
		case 1:
			imagePath = image2Path;
			break;
		}

		imageView.getLayoutParams().height = display.getWidth();
		imageView.getLayoutParams().width = display.getWidth();

		if (imagePath != null) {
			imageView.setImageURI(Uri.fromFile(new File(imagePath)));
		}

		((ViewPager) collection).addView(view, 0);
		return view;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView((View) arg2);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == ((View) arg1);
	}

	@Override
	public Parcelable saveState() {
		return null;
	}
}
