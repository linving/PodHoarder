package com.podhoarder.object;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class ReverseInterpolator extends AccelerateDecelerateInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float paramFloat) {
        return Math.abs(super.getInterpolation(paramFloat) -1f);
    }
}
