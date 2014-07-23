package com.podhoarderproject.podhoarder.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/***
 * A class that overrides the standard Android ViewPager, but adds support for enabling/disabling swiping in individual directions.
 * @author Emil
 * 2014-07-23
 */
public class DynamicViewPager extends ViewPager {

private boolean leftEnabled, rightEnabled;

public DynamicViewPager(Context context, AttributeSet attrs) 
{
    super(context, attrs);
    this.leftEnabled = true;
    this.rightEnabled = true;
}

	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
	    if (shouldHandleSwipe(event)) 
	    {
	        return super.onTouchEvent(event);
	    }
	    else return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) 
	{
	    if (shouldHandleSwipe(event)) 
	    {
	        return super.onInterceptTouchEvent(event);
	    }
	    else return false;
	}

	// To enable/disable swipe 
	public void setLeftSwipeEnabled(boolean enabled) 
	{
	    this.leftEnabled = enabled;
	}
	public void setRightSwipeEnabled(boolean enabled)
	{
		this.rightEnabled = enabled;
	}
	
	public boolean shouldHandleSwipe(MotionEvent event)
	{
		int initialXValue = 0; // as we have to detect swipe to right
		final int SWIPE_THRESHOLD = 100; // detect swipe
		try 
		{                
			float diffX = event.getX() - initialXValue;
			if (Math.abs(diffX) > SWIPE_THRESHOLD ) 
			{
	            if (diffX > 0) 
	            {
	                // swipe from left to right detected ie.SwipeRight
	                if (this.rightEnabled) return true;
	                else return false;
	            } 
	            else 
	            {
	                // swipe from right to left detected ie.SwipeLeft
	            	if (this.leftEnabled) return true;
	                else return false;
	            }
	        }
		} 
		catch (Exception exception) 
		{
			exception.printStackTrace();
		}
		return true;
	}
}
