package com.choosie.app;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;

public class Cache {
	Map<String, Bitmap> picturesCache = new HashMap<String, Bitmap>();
	
	public Bitmap getPicture(String pictureUrl) {
		return picturesCache.get(pictureUrl);
	}
	public void putPicture(String pictureUrl, Bitmap bitmap) {
		picturesCache.put(pictureUrl, bitmap);
	}

}
