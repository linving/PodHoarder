package com.podhoarder.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.podhoarderproject.podhoarder.R;

public class ToggleImageButton extends ImageButton
{
	private boolean mToggled;	//mChecked = true means playing (should show pause), and mChecked = false means paused (should show play)
	private Drawable mOnDrawable, mOffDrawable;

	public ToggleImageButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		loadAttributeSet(context, attrs);
		this.setToggled(false);
	}

	public ToggleImageButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		loadAttributeSet(context, attrs);
		this.setToggled(false);
	}

	public ToggleImageButton(Context context)
	{
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (mToggled)
			this.setImageDrawable(this.mOnDrawable);
		else
			this.setImageDrawable(this.mOffDrawable);
		super.onDraw(canvas);
	}
	
	public boolean isToggled()
	{
		return mToggled;
	}

	public void setToggled(boolean toggled)
	{
		this.mToggled = toggled;
		if (toggled)
			this.setImageDrawable(this.mOnDrawable);
		else
			this.setImageDrawable(this.mOffDrawable);
		//invalidate();
	}

	public void toggle()
	{
		if (mToggled)
			mToggled = false;
		else
			mToggled = true;
		invalidate();
	}
	
	private void loadAttributeSet(Context context, AttributeSet attrs)
	{
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.ToggleImageButton,
		        0, 0);

		   try 
		   {
			   mOnDrawable = a.getDrawable(R.styleable.ToggleImageButton_checkedDrawable);
			   mOffDrawable = a.getDrawable(R.styleable.ToggleImageButton_unCheckedDrawable);
		   } 
		   finally 
		   {
		       a.recycle();
		   }
	}

}
