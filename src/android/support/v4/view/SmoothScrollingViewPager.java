package android.support.v4.view;

import java.lang.reflect.Field;

import com.podhoarder.util.CustomSpeedScroller;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

public class SmoothScrollingViewPager extends ViewPager {

    public SmoothScrollingViewPager(Context context) {
        super(context);
        postInitViewPager();
    }

    public SmoothScrollingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        postInitViewPager();
    }

    private CustomSpeedScroller mScroller = null;

    /**
     * Override the Scroller instance with our own class so we can change the
     * duration
     */
    private void postInitViewPager() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = viewpager.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            mScroller = new CustomSpeedScroller(getContext(),
                    (Interpolator) interpolator.get(null));
            scroller.set(this, mScroller);
        } catch (Exception e) {
        }
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollDurationFactor(double scrollFactor) {
        mScroller.setScrollDurationFactor(scrollFactor);
    }
    
    void smoothScrollTo(int x, int y, int velocity) {
        super.smoothScrollTo(x, y, 1);
    }
    
}