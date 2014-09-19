package com.podhoarder.util;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/***
 * Used for decreasing the speed of the Viewpager.setCurrentItem transition when invoked programmatically.
 * @author Emil
 *
 */
public class CustomSpeedScroller extends Scroller {

	private double mScrollFactor = 1;

    public CustomSpeedScroller(Context context) {
        super(context);
    }

    public CustomSpeedScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public CustomSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollDurationFactor(double scrollFactor) {
        mScrollFactor = scrollFactor;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, (int) (duration * mScrollFactor));
    }
}
