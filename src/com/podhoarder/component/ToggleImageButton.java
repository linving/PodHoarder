package com.podhoarder.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.podhoarderproject.podhoarder.R;

public class ToggleImageButton extends ImageButton
{
	private boolean mChecked;	//mChecked = true means playing (should show pause), and mChecked = false means paused (should show play)
	private Drawable mCheckedDrawable, mUnCheckedDrawable;

	public ToggleImageButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		loadAttributeSet(context, attrs);
		this.setChecked(false);
	}

	public ToggleImageButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		loadAttributeSet(context, attrs);
		this.setChecked(false);
	}

	public ToggleImageButton(Context context)
	{
		super(context);
	}

	public boolean isChecked()
	{
		return mChecked;
	}

	public void setChecked(boolean mChecked)
	{
		this.mChecked = mChecked;
		if (mChecked)
			this.setImageDrawable(this.mCheckedDrawable);
		else
			this.setImageDrawable(this.mUnCheckedDrawable);
		//invalidate();
	}

	private void loadAttributeSet(Context context, AttributeSet attrs)
	{
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.ToggleImageButton,
		        0, 0);

		   try 
		   {
			   mCheckedDrawable = a.getDrawable(R.styleable.ToggleImageButton_checkedDrawable);
			   mUnCheckedDrawable = a.getDrawable(R.styleable.ToggleImageButton_unCheckedDrawable);
		   } 
		   finally 
		   {
		       a.recycle();
		   }
	}
}
