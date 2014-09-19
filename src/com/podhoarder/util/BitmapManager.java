package com.podhoarder.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class BitmapManager {
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarder.util.BitmapManager";
	
    private final Map<String, Bitmap> BitmapMap;

    public BitmapManager() {
        BitmapMap = new HashMap<String, Bitmap>();
    }

    public Bitmap fetchBitmap(String urlString, int dimen) {
        if (BitmapMap.containsKey(urlString)) {
            return BitmapMap.get(urlString);
        }

        //Log.d(this.getClass().getSimpleName(), "image url:" + urlString);
        try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();

			// First decode with inJustDecodeBounds=true to check dimensions
			options.inJustDecodeBounds = true;
			InputStream in;
			
			in = new java.net.URL(urlString).openStream();
			
			BitmapFactory.decodeStream(in, null, options);

			in.close();
			
			// Calculate inSampleSize
			options.inSampleSize = ImageUtils.calculateInSampleSize(options, dimen, dimen);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;

			in = new java.net.URL(urlString).openStream();
			
			Bitmap img = BitmapFactory.decodeStream(in, null, options);
			if (img != null)
				this.BitmapMap.put(urlString, img);
			else
				Log.w(LOG_TAG, "Failed to get thumbnail!");
			return img;
			
		} catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    public void fetchBitmapOnThread(final String urlString, final ImageView imageView) {
        if (BitmapMap.containsKey(urlString)) {
            imageView.setImageBitmap(BitmapMap.get(urlString));
        }
        else
        {
        	final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    imageView.setImageBitmap((Bitmap) message.obj);
                }
            };

            Thread thread = new Thread() {
                @Override
                public void run() {
                    //TODO : set imageView to a "pending" image
                    Bitmap bitmap = fetchBitmap(urlString, imageView.getMaxWidth());
                    Message message = handler.obtainMessage(1, bitmap);
                    handler.sendMessage(message);
                }
            };
            thread.start();
        }
        
    }

    public boolean isCached(final String urlString)
    {
    	if (BitmapMap.containsKey(urlString)) {
            return true;
        }
    	else return false;
    }
}
