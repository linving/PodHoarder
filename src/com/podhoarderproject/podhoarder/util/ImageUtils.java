package com.podhoarderproject.podhoarder.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;

public class ImageUtils
{
	public static Bitmap getCircularBitmap(Bitmap bitmap)
	{
		Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

		BitmapShader shader = new BitmapShader (bitmap,  TileMode.CLAMP, TileMode.CLAMP);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShader(shader);

		Canvas c = new Canvas(circleBitmap);
		c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);

		return circleBitmap;
	}
}
