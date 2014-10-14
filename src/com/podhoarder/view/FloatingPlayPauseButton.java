package com.podhoarder.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.faizmalkani.FloatingActionButton;
import com.podhoarderproject.podhoarder.R;

public class FloatingPlayPauseButton extends FloatingActionButton
{
	private Bitmap mPlayBitmap, mPauseBitmap;
	private boolean mPlaying;
	
	public FloatingPlayPauseButton(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingPlayPauseButton);
		
		Drawable playDrawable = a.getDrawable(R.styleable.FloatingPlayPauseButton_playDrawable);
		Drawable pauseDrawable = a.getDrawable(R.styleable.FloatingPlayPauseButton_pauseDrawable);
        if (playDrawable != null && pauseDrawable != null) 
        {
            mPlayBitmap = ((BitmapDrawable) playDrawable).getBitmap();
            mPauseBitmap = ((BitmapDrawable) pauseDrawable).getBitmap();
        }
        
        a.recycle();
        mPlaying = false;
	}

	public FloatingPlayPauseButton(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
		
		TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.FloatingPlayPauseButton);
		
		Drawable playDrawable = a.getDrawable(R.styleable.FloatingPlayPauseButton_playDrawable);
		Drawable pauseDrawable = a.getDrawable(R.styleable.FloatingPlayPauseButton_pauseDrawable);
        if (playDrawable != null && pauseDrawable != null) 
        {
            mPlayBitmap = ((BitmapDrawable) playDrawable).getBitmap();
            mPauseBitmap = ((BitmapDrawable) pauseDrawable).getBitmap();
        }
        
        a.recycle();
        mPlaying = false;
	}

	public FloatingPlayPauseButton(Context context)
	{
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), super.mButtonPaint);
		if (mPlaying)
		{
			if (mPauseBitmap != null) 
			{
	            canvas.drawBitmap(mPauseBitmap, (getWidth() - mPauseBitmap.getWidth()) / 2,
	                              (getHeight() - mPauseBitmap.getHeight()) / 2, super.mDrawablePaint);
	        }
		}
		else
		{
			if (mPlayBitmap != null) 
			{
	            canvas.drawBitmap(mPlayBitmap, (getWidth() - mPlayBitmap.getWidth()) / 2,
	                              (getHeight() - mPlayBitmap.getHeight()) / 2, super.mDrawablePaint);
	        }
		}
        
	}
		
	public void setPlaying(boolean isPlaying)
	{
		mPlaying = isPlaying;
		invalidate();
	}
	
	public boolean isPlaying()
	{
		return mPlaying;
	}
	
	public void toggle()
	{
		if (mPlaying)
			mPlaying = false;
		else
			mPlaying = true;
		invalidate();
	}

	enum ButtonStatus { PLAYING, LOADING, PAUSED }
	
}
