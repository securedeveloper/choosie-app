package com.choosie.app;

import android.graphics.Bitmap;

public class NewChoosiePostData {

	Bitmap image1;
	Bitmap image2;
	String question;
	
	public NewChoosiePostData(Bitmap mImage1, Bitmap mImage2, String mQuestion) {
		this.image1 = image1;
		this.image2 = image2;
		this.question = question;
	}
}
