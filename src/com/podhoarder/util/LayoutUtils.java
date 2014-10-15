package com.podhoarder.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

public class LayoutUtils
{
	public static void expand(View summary, int desiredHeight)
	{
		// set Visible
		summary.setVisibility(View.VISIBLE);

		final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		summary.measure(widthSpec, desiredHeight);

		ValueAnimator mAnimator = slideAnimator(0, desiredHeight, summary);

		mAnimator.start();
	}

	public static void collapse(final View summary)
	{
		int finalHeight = summary.getHeight();

		ValueAnimator mAnimator = slideAnimator(finalHeight, 0, summary);

		mAnimator.addListener(new Animator.AnimatorListener()
		{
			@Override
			public void onAnimationEnd(Animator animator)
			{
				// Height=0, but it set visibility to GONE
				summary.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationStart(Animator animator)
			{
			}

			@Override
			public void onAnimationCancel(Animator animator)
			{
			}

			@Override
			public void onAnimationRepeat(Animator animator)
			{
			}
		});
		mAnimator.start();
	}

	private static ValueAnimator slideAnimator(int start, int end, final View summary)
	{

		ValueAnimator animator = ValueAnimator.ofInt(start, end);

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator)
			{
				// Update Height
				int value = (Integer) valueAnimator.getAnimatedValue();

				ViewGroup.LayoutParams layoutParams = summary.getLayoutParams();
				layoutParams.height = value;
				summary.setLayoutParams(layoutParams);
			}
		});
		return animator;
	}
}
