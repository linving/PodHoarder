package com.podhoarder.object;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class ReverseInterpolator extends AccelerateDecelerateInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float paramFloat) {
        return (float)(Math.cos(((paramFloat - 1) + 1) * Math.PI) / 2.0f) + 0.5f;
    }
}
