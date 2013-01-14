package com.choozie.app.camera;

import android.content.Context;
import android.graphics.Bitmap;

import com.choozie.app.R;
import com.choozie.app.Utils;

public class YesNoUtils {
	public static Bitmap generateVoteDownImage(Context context, Bitmap image) {
		return Utils.combine(Utils.fastblur(image, 20), context.getResources()
				.getDrawable(R.drawable.naa));
	}

	public static Bitmap generateVoteDownImageNoBlur(Context context,
			Bitmap image) {
		return Utils.combine(image,
				context.getResources().getDrawable(R.drawable.naa));
	}

	public static Bitmap generateVoteUpImage(Context context, Bitmap image) {
		return Utils.combine(image,
				context.getResources().getDrawable(R.drawable.yaa));
	}

}
