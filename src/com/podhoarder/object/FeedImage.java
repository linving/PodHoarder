package com.podhoarder.object;
/**
 * @author Emil Almrot
 * 2013-03-15
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class FeedImage 
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.FeedImage";
	
	private int		feedId;
	private String 	imageURL;
	private Bitmap 	imageObject;
	private Bitmap  thumbnail;
	
	private ImageDownloadListener downloadListener;
	
	private Context	ctx;
	
	/**
     * Creates a new FeedImage object. Image will be downloaded and stored locally upon creation using the supplied URL.
     * @param feedId	Locally unique feed identifier to associate the image with the correct feed in the file system.
     * @param onlineURL	URL to the online image.
     * @param shouldCreateImage Indicates whether to actually create the Bitmap object or not.
     * @param ctx		App Context.
     */
	public FeedImage(int feedId, String onlineURL, boolean shouldCreateImage, Context ctx)
	{
		this.feedId = feedId;
		this.imageURL = onlineURL;
		this.imageObject = null;
		this.thumbnail = null;
		this.ctx = ctx;
		if (this.feedId > 0 && shouldCreateImage)
		{
			this.loadImage(onlineURL);
		}
	}
	
	/**
	 * Getter for the imageObject
	 * @return
	 */
	public BitmapDrawable imageObjectDrawable()
	{
		return new BitmapDrawable(this.ctx.getResources(),this.imageObject);
	}
	
	public BitmapDrawable thumbnailDrawable()
	{
		return new BitmapDrawable(this.ctx.getResources(),this.thumbnail);
	}
	
	public Bitmap imageObject()
	{
		return this.imageObject;
	}
	
	public Bitmap thumbnail()
	{
		return this.thumbnail;
	}
	
	/**
	 * Method for saving image to local storage.
	 */
	private void saveImage() 
	{
		String fName = this.feedId + ".jpg";
		File file = new File(ctx.getFilesDir(), fName);
	    try 
	    {
	    	FileOutputStream out = new FileOutputStream(file);
	    	this.imageObject.compress(Bitmap.CompressFormat.JPEG, 50, out);
	    	out.flush();
	    	out.close();
	    	Log.d(LOG_TAG, "File downloaded from URL.");
	    } 
	    catch (IOException e) 
	    {
	    	Log.e(LOG_TAG, "Error when saving image " + fName, e);
	    }
	    this.imageObject = decodeSampledBitmap(this.feedId + ".jpg", 100, 100);
	    this.thumbnail = decodeSampledBitmap(this.feedId + ".jpg", 50, 50);
	    if (this.downloadListener != null) this.downloadListener.downloadFinished(this.feedId);
	}
	
	/**
	 * Method for loading Feed image.
	 * The method tries to load an image from local storage. 
	 * If that fails (i.e. the file isn't found) it downloads the file in the background.
	 */
	private void loadImage(String url)
	{
		this.imageObject = decodeSampledBitmap(this.feedId + ".jpg", 100, 100);
		this.thumbnail = decodeSampledBitmap(this.feedId + ".jpg", 50, 50);
		Log.d(LOG_TAG, "File loaded from local storage.");
	}

	 /**
     * AsyncTask for downloading an image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> 
    {
        private String url;

        public BitmapDownloaderTask() 
        {
        	imageObject = null;;
        }

        /**
         * Actual download method.
         * This function does all the work "in the background". (In this case it downloads the image)
         */
        @Override
        protected Bitmap doInBackground(String... params) 
        {
            url = params[0];
            return downloadBitmap(url);
        }

        /**
         * Fires when Task has been executed.
         * This function associates the downloaded bitmap with the object property.
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) 
        {
            if (isCancelled()) 
            {
            	Log.w(LOG_TAG, "Task cancelled.");
                bitmap = null;
            }
            else
            {
            	imageObject = bitmap;
            	saveImage();
            }
        }
    }

    /**
     * Code for actually retrieving the image from an URL. Must be run from an AsyncTask!
     * @param url	URL of the image file that will be downloaded.
     */
    private static Bitmap downloadBitmap(String url) 
    {
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) 
            {            	
            	Header[] headers = response.getHeaders("Location");
            	if (headers != null && headers.length != 0) {
            		Log.w(LOG_TAG, "Error " + statusCode + " while retrieving bitmap from " + url + ". Trying to follow redirect..."); 
                    String newUrl = headers[headers.length - 1].getValue();	//Extract the redirect URL from the HTTP Headers.
                    return downloadBitmap(newUrl);	 // Call this method again with new URL.
                } 
            	else 
            	{
            		Log.e(LOG_TAG, "Error " + statusCode + " while retrieving bitmap from " + url); 
                    return null;
                }
            }
            final HttpEntity entity = response.getEntity();
            if (entity != null) 
            {
                InputStream inputStream = null;
                try 
                {
                    inputStream = entity.getContent(); 
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } 
                finally 
                {
                    if (inputStream != null) 
                    {
                        inputStream.close();  
                    }
                    entity.consumeContent();
                }
            }
        } 
        catch (Exception e) 
        {
            getRequest.abort();
            Log.e(LOG_TAG, "Error while retrieving bitmap from " + url, e);
        } 
        finally 
        {
            if (client != null) 
            {
                client.close();
            }
        }
        return null;
    }

    public String getImageURL()
    {
    	return this.imageURL;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) 
	{
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
    
    private Bitmap decodeSampledBitmap(String fileName, int reqWidth, int reqHeight) 
    {
        
        try
		{
        	// First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            
            options.inJustDecodeBounds = true;
            
			BitmapFactory.decodeStream(ctx.openFileInput(fileName), null, options);
			
			// Calculate inSampleSize
	        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	        // Decode bitmap with inSampleSize set
	        options.inJustDecodeBounds = false;
	        
	        return BitmapFactory.decodeStream(ctx.openFileInput(fileName), null, options);
		} 
        catch (FileNotFoundException e)
		{
			new BitmapDownloaderTask().execute(this.imageURL);
			return null;
		}

        
    }

    public void setImageDownloadListener(ImageDownloadListener listener)
    {
    	this.downloadListener = listener;
    }
    
    public interface ImageDownloadListener 
    {
    	  public void downloadFinished(int feedId);
    } 
}
