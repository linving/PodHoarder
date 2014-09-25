package com.podhoarder.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable 
{
	private boolean mChecked = false;
	
	public CheckableRelativeLayout(Context context)
	{
		super(context);
	}
	
	public CheckableRelativeLayout(Context context, AttributeSet attrs,
			int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	public void setChecked(boolean checked) {
	    this.mChecked = checked;
	    refreshDrawableState();
	}
	
	public boolean isChecked() {
	    return mChecked;
	}
	
	public void toggle() {
	    setChecked(!mChecked);
	}
	
//	@Override
//	protected int[] onCreateDrawableState(int extraSpace) {
//	    int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
//	    if (mChecked) mergeDrawableStates(drawableState, STATE_CHECKABLE);
//	
//	    return drawableState;
//	}
}