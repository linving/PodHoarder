package com.podhoarder.object;
/**
 * @author Emil Almrot
 * 2013-03-15
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;

import com.podhoarder.util.Constants;
import com.podhoarder.util.ImageUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FeedImage 
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.FeedImage";
	
	private int mImageSize;
	private int mThumbnailSize;
	
	private int		mFeedId;
	private String 	mImageURL;
	private	Bitmap 	mLargeImage;
	private Bitmap 	mImageObject;
	private Bitmap  mThumbnail;
    private Palette mPalette;   //TODO: Implement a way to save this value to the DB so it doesn't have to be generated every time.
	
	private ImageDownloadListener mDownloadListener;
	
	private Context	mContext;
	
	/**
     * Creates a new FeedImage object. Image will be downloaded and stored locally upon creation using the supplied URL.
     * @param feedId	Locally unique feed identifier to associate the image with the correct feed in the file system.
     * @param onlineURL	URL to the online image.
     * @param shouldCreateImage Indicates whether to actually create the Bitmap object or not.
     * @param ctx		App Context.
     */
	public FeedImage(int feedId, String onlineURL, boolean shouldCreateImage, Context ctx)
	{
		this.mFeedId = feedId;
		this.mImageURL = onlineURL;
		this.mImageObject = null;
		this.mThumbnail = null;
		this.mContext = ctx;
		setupImageDimensions();
		if (this.mFeedId > 0 && shouldCreateImage)
		{
			this.loadImage(onlineURL);
		}
	}
	
	/**
	 * Getter for the medium sized imageObject.
	 * @return A medium sized, downscaled BitmapDrawable object representation of the image.
	 */
	public BitmapDrawable imageObjectDrawable()
	{
		return new BitmapDrawable(this.mContext.getResources(),this.mImageObject);
	}
    /**
     * Getter for the thumbnail sized imageObject.
     * @return A thumbnail sized, downscaled BitmapDrawable object representation of the image.
     */
	public BitmapDrawable thumbnailDrawable()
	{
		return new BitmapDrawable(this.mContext.getResources(),this.mThumbnail);
	}
    /**
     * Getter for the medium sized imageObject.
     * @return A medium sized, downscaled Bitmap object representation of the image.
     */
	public Bitmap imageObject()
	{
		return this.mImageObject;
	}
    /**
     * Getter for the medium sized imageObject.
     * @return A Bitmap object representation of the image in the original size.
     */
	public Bitmap largeImage()
	{
		return mLargeImage;
	}
    /**
     * Getter for the thumbnail sized imageObject.
     * @return A thumbnail sized, downscaled Bitmap object representation of the image.
     */
	public Bitmap thumbnail()
	{
		return this.mThumbnail;
	}
    /**
     * Getter for the image Palette.
     * @return A Palette object with colors that can be used with the image.
     */
    public Palette palette() {
        if (this.mPalette == null) {    //If the palette hasn't been generated yet we need to generate it on the UI thread so it's always available.
            if (mImageObject == null)
                this.mPalette = Palette.generate(Bitmap.createBitmap( mImageSize, mImageSize, Bitmap.Config.RGB_565)); //We generate a temporary palette from a solid black bitmap
            else
                this.mPalette = Palette.generate(mImageObject);
            return this.mPalette;
        }
        else
            return this.mPalette;
    }
	
	/**
	 * Method for saving image to local storage.
	 */
	private void saveImage() 
	{
		String fName = this.mFeedId + ".jpg";
		File file = new File(mContext.getFilesDir(), fName);
	    try 
	    {
	    	FileOutputStream out = new FileOutputStream(file);
	    	this.mImageObject.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Palette.generateAsync(this.mImageObject, new Palette.PaletteAsyncListener() {   //Generate a Palette object to go with the image.
                @Override
                public void onGenerated(Palette palette) {
                    mPalette = palette;
                }
            });
	    	out.flush();
	    	out.close();
	    	Log.d(LOG_TAG, "File downloaded from URL.");
	    } 
	    catch (IOException e) 
	    {
	    	Log.e(LOG_TAG, "Error when saving image " + fName, e);
	    }
		this.mLargeImage = decodeSampledBitmap(this.mFeedId + ".jpg", mImageSize, mImageSize);
	    this.mImageObject = ImageUtils.scaleImage(mContext, this.mLargeImage, mImageSize);
	    this.mThumbnail = ThumbnailUtils.extractThumbnail(mImageObject, mThumbnailSize, mThumbnailSize);
	    if (this.mDownloadListener != null) {
            this.mDownloadListener.downloadFinished(this.mFeedId);  //Notify the listener that the image is downloaded.
        }
	}
	
	/**
	 * Method for loading Feed image.
	 * The method tries to load an image from local storage. 
	 * If that fails (i.e. the file isn't found) it downloads the file in the background.
	 */
	private void loadImage(String url)
	{
		try
		{
			this.mLargeImage = decodeSampledBitmap(this.mFeedId + ".jpg", mImageSize, mImageSize);
			this.mImageObject = ImageUtils.scaleImage(mContext, this.mLargeImage, mImageSize);
			this.mThumbnail = ThumbnailUtils.extractThumbnail(mImageObject, mThumbnailSize, mThumbnailSize);
            Palette.generateAsync(this.mImageObject, new Palette.PaletteAsyncListener() {   //Generate a Palette object to go with the image.
                @Override
                public void onGenerated(Palette palette) {
                    mPalette = palette;
                }
            });
			Log.d(LOG_TAG, "File loaded from local storage.");
		}
		catch (NullPointerException ex)
		{
			Log.d(LOG_TAG,"Couldn't load file from local storage. Downloading.");
		}
	}
	
	/**
	 * Loads a scaled version of the saved Bitmap file.
	 * @param width Desired width.
	 * @param height Desired height.
	 * @return A properly scaled Bitmap object.
	 */
	public Bitmap loadScaledImage(int width, int height)
	{
		return decodeSampledBitmap(this.mFeedId + ".jpg", width, height);
	}

	 /**
     * AsyncTask for downloading an image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> 
    {
        private String url;

        public BitmapDownloaderTask() 
        {
        	mImageObject = null;
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
            	mImageObject = bitmap;
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
    	return this.mImageURL;
    }

    /**
     * Defines Image and Thumbnail dimensions relative to screen size.
     */
    private void setupImageDimensions()
    {
        int storedValue = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.SETTINGS_KEY_GRIDITEMSIZE,"-1"));
        if (storedValue > 0) {
            mImageSize = storedValue;
            mThumbnailSize = mImageSize / 2;
        }
        else {
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            this.mImageSize = displayMetrics.widthPixels / 2;
            this.mThumbnailSize = this.mImageSize / 2;
        }
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
            
			BitmapFactory.decodeStream(mContext.openFileInput(fileName), null, options);
			
			// Calculate inSampleSize
	        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	        // Decode bitmap with inSampleSize set
	        options.inJustDecodeBounds = false;
	        
	        return BitmapFactory.decodeStream(mContext.openFileInput(fileName), null, options);
		} 
        catch (FileNotFoundException e)
		{
			new BitmapDownloaderTask().execute(this.mImageURL);
			return null;
		}

        
    }

    public void setImageDownloadListener(ImageDownloadListener listener)
    {
    	this.mDownloadListener = listener;
    }
    
    public interface ImageDownloadListener 
    {
    	  public void downloadFinished(int feedId);
    } 
}
