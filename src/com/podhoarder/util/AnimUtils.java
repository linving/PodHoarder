package com.podhoarder.util;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.podhoarderproject.podhoarder.R;

public class AnimUtils
{
	/**
	 * Displays an animation on viewToFade that gradually decreases its alpha value to 0 from the maximum value.
	 * @param context		Application context.
	 * @param viewToFade	The View object to fade out.
	 * @param duration	Duration of the fading animation.
	 */
	public static void fadeOutAnimation(Context context, final View viewToFade, int duration)
    {
    	Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_out);
    	animation.setDuration(duration);
    	animation.setFillEnabled(true);
    	animation.setFillAfter(true);
    	animation.setAnimationListener(new Animation.AnimationListener(){
      
    	    

			@Override
			public void onAnimationRepeat(Animation arg0)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation arg0)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
    	    public void onAnimationEnd(Animation arg0) 
			{
				viewToFade.setVisibility(View.INVISIBLE);
    	    }
    	});
    	viewToFade.startAnimation(animation);
    }
	
	/**
	 * Displays an animation on viewToFade that gradually increases its alpha value to the maximum value from zero.
	 * @param context		Application context.
	 * @param viewToFade	The View object to fade out.
	 * @param duration	Duration of the fading animation.
	 */
	public static void fadeInAnimation(Context context, final View viewToFade, int duration)
    {
    	Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
    	animation.setDuration(duration);
    	animation.setFillEnabled(true);
    	animation.setFillAfter(true);
    	animation.setAnimationListener(new Animation.AnimationListener(){
      
    	    

			@Override
			public void onAnimationRepeat(Animation arg0)
			{
				
			}

			@Override
			public void onAnimationStart(Animation arg0)
			{
				viewToFade.setVisibility(View.VISIBLE);
			}
			
			@Override
    	    public void onAnimationEnd(Animation arg0) 
			{
				
    	    }
    	});
    	viewToFade.startAnimation(animation);
    }

	/**
	 * Performs a vertical movement animation.
	 * @param viewToAnimate The view to move.
	 * @param fromDeltaY	The original Y delta value.
	 * @param toDeltaY The target Y delta value.
	 */
	public static void verticalTranslateAnimation(View viewToAnimate, final float fromDeltaY ,final float toDeltaY)
	{
		TranslateAnimation anim = new TranslateAnimation(0, 0, fromDeltaY, toDeltaY);
		anim.setDuration(100);
		viewToAnimate.startAnimation(anim);
	}

	
	public static void selectionAnimation(View viewToAnimate)
	{
		ScaleAnimation animation = new ScaleAnimation(1.0f, 0.95f, 1.0f, 0.95f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(50);
		viewToAnimate.startAnimation(animation);
	}
}
