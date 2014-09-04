package com.podhoarder.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.podhoarderproject.podhoarder.R;

public class PullRefreshLayout extends SwipeRefreshLayout
{
	private boolean mIsRefreshGesture;
	private int mHandleHeight;
	
	/** When this is enabled, swipes to the right side of the screen will not be detected. **/
	private boolean mIgnoreScrollMotions;

	public PullRefreshLayout(Context context)
	{
		super(context);
		this.mHandleHeight = 0;
		this.mIgnoreScrollMotions = false;
		this.mIsRefreshGesture = false;
	}

	public PullRefreshLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		loadAttributeSet(context, attrs);
		this.mIsRefreshGesture = false;
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			this.mIsRefreshGesture = false;
		}
        return super.onTouchEvent(event);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) 
	{
		processMotionEvent(event);
		return this.mIsRefreshGesture;
	}
	
	private void processMotionEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			float x = event.getX(), y = event.getY();
			this.mIsRefreshGesture = isValidRefreshGesture(x,y); //If the touch event starts at a valid location on the screen, we will intercept all further touch events until MotionEvent.UP in onTouchEvent.
		}
	}

	
	private boolean isValidRefreshGesture(float x, float y)
	{
		boolean ret = false;
		if (this.isRefreshing())
			return ret;
		
		if (this.mIgnoreScrollMotions)
		{
			ret = ((y < this.mHandleHeight) 
					&& (x < (this.getContext().getResources().getDisplayMetrics().widthPixels / 6)*5));
		}
		else
		{					
			ret = (y < this.mHandleHeight);	//Check if the start location of the touch event is in the hotspot.				
		}
		return ret;
	}

	
	private void loadAttributeSet(Context context, AttributeSet attrs)
	{
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PullRefreshLayout, 0, 0);

		   try 
		   {
			   mIgnoreScrollMotions = a.getBoolean(R.styleable.PullRefreshLayout_ignoreScrollMotions, false);
			   mHandleHeight = a.getDimensionPixelOffset(R.styleable.PullRefreshLayout_handleHeight, 25);
		   } 
		   finally 
		   {
		       a.recycle();
		   }
	}
}
