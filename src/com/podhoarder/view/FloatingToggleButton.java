package com.podhoarder.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.faizmalkani.FloatingActionButton;
import com.podhoarderproject.podhoarder.R;

/**
 * FloatingActionButton with two toggleable states. When toggled is true, the button displays the onDrawable, and vice versa.
 * @author Emil
 *
 */
public class FloatingToggleButton extends FloatingActionButton
{
	private Bitmap mOnBitmap, mOffBitmap;
	private boolean mToggled;
	
	public FloatingToggleButton(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingToggleButton);
		
		Drawable onDrawable = a.getDrawable(R.styleable.FloatingToggleButton_onDrawable);
		Drawable offDrawable = a.getDrawable(R.styleable.FloatingToggleButton_offDrawable);
        if (onDrawable != null && offDrawable != null) 
        {
            mOnBitmap = ((BitmapDrawable) onDrawable).getBitmap();
            mOffBitmap = ((BitmapDrawable) offDrawable).getBitmap();
        }
        
        a.recycle();
        mToggled = false;
	}

	public FloatingToggleButton(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
		
		TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.FloatingToggleButton);
		
		Drawable onDrawable = a.getDrawable(R.styleable.FloatingToggleButton_onDrawable);
		Drawable offDrawable = a.getDrawable(R.styleable.FloatingToggleButton_offDrawable);
        if (onDrawable != null && offDrawable != null) 
        {
            mOnBitmap = ((BitmapDrawable) onDrawable).getBitmap();
            mOffBitmap = ((BitmapDrawable) offDrawable).getBitmap();
        }
        
        a.recycle();
        mToggled = false;
	}

	public FloatingToggleButton(Context context)
	{
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), super.mButtonPaint);
		if (mToggled)
		{
			if (mOnBitmap != null) 
			{
	            canvas.drawBitmap(mOnBitmap, (getWidth() - mOnBitmap.getWidth()) / 2,
	                              (getHeight() - mOnBitmap.getHeight()) / 2, super.mDrawablePaint);
	        }
		}
		else
		{
			if (mOffBitmap != null) 
			{
	            canvas.drawBitmap(mOffBitmap, (getWidth() - mOffBitmap.getWidth()) / 2,
	                              (getHeight() - mOffBitmap.getHeight()) / 2, super.mDrawablePaint);
	        }
		}
        
	}
	
	public void setToggled(boolean toggled)
	{
		mToggled = toggled;
		invalidate();
	}
	
	public boolean isToggled()
	{
		return mToggled;
	}
	
	public void toggle()
	{
		if (mToggled)
			mToggled = false;
		else
			mToggled = true;
		invalidate();
	}

    public void animatedSetColor(int color) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getColor(), color);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                setColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.setDuration(100);
        colorAnimation.start();
    }
}
