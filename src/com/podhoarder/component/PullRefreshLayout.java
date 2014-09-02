package com.podhoarder.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.podhoarderproject.podhoarder.R;

public class PullRefreshLayout extends SwipeRefreshLayout
{
	/** Used to keep track of whether the current motion event is a valid refresh gesture.**/
	private boolean mIsRefreshGesture;
	
	/** When this is enabled, swipe to the right side of the screen will not be detected. **/
	private boolean mIgnoreScrollMotions;

	public PullRefreshLayout(Context context)
	{
		super(context);
		this.mIsRefreshGesture = false;
		this.mIgnoreScrollMotions = false;
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
		if (!this.mIsRefreshGesture)
		{
			int action = event.getAction();
			switch (action)
			{
				case MotionEvent.ACTION_DOWN:
					if (this.mIgnoreScrollMotions)
					{
						this.mIsRefreshGesture = ((event.getY() < this.getContext().getResources().getDisplayMetrics().heightPixels / 6) 
								&& (event.getX() < (this.getContext().getResources().getDisplayMetrics().widthPixels / 6)*5));
					}
					else
						this.mIsRefreshGesture = (event.getY() < this.getContext().getResources().getDisplayMetrics().heightPixels / 6);	//Check if the start location of the touch event is in the hotspot.
					break;
				case MotionEvent.ACTION_UP:
					this.mIsRefreshGesture = false;
					break;
				case MotionEvent.ACTION_MOVE:
					if (this.mIsRefreshGesture)
						return super.onTouchEvent(event);
					else
						return false;
			}
		}
		else if (this.isRefreshing())
		{
			return false;
		}
		else
		{
			int action = event.getAction();
			switch (action)
			{
				case MotionEvent.ACTION_UP:
					this.mIsRefreshGesture = false;
					break;
				default:
					return super.onTouchEvent(event);
			}
		}
		return super.onTouchEvent(event);
	}

	private void loadAttributeSet(Context context, AttributeSet attrs)
	{
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.PullRefreshLayout,
		        0, 0);

		   try 
		   {
			   mIgnoreScrollMotions = a.getBoolean(R.styleable.PullRefreshLayout_ignoreScrollMotions, false);
		   } 
		   finally 
		   {
		       a.recycle();
		   }
	}
}
