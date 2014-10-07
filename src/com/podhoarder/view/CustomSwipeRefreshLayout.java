package com.podhoarder.view;

import java.lang.reflect.Field;

import com.podhoarderproject.podhoarder.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;


public class CustomSwipeRefreshLayout extends SwipeRefreshLayout implements ViewTreeObserver
        .OnGlobalLayoutListener {

    private static float MAX_SWIPE_DISTANCE_FACTOR = 0.6f;
    private static int DEFAULT_REFRESH_TRIGGER_DISTANCE = 200;

    private int refreshTriggerDistance = DEFAULT_REFRESH_TRIGGER_DISTANCE;

    ViewTreeObserver vto;

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomSwipeRefreshLayout);
        
        int dist = a.getInteger(R.styleable.CustomSwipeRefreshLayout_triggerDistance, DEFAULT_REFRESH_TRIGGER_DISTANCE);
        setRefreshTriggerDistance(dist);
        
        a.recycle();
        
        vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        // Calculate the trigger distance.
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        Float mDistanceToTriggerSync = Math.min(
                ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                refreshTriggerDistance * metrics.density);

        try {
            // Set the internal trigger distance using reflection.
            Field field = SwipeRefreshLayout.class.getDeclaredField("mDistanceToTriggerSync");
            field.setAccessible(true);
            field.setFloat(this, mDistanceToTriggerSync);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Only needs to be done once so remove listener.
        ViewTreeObserver obs = getViewTreeObserver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            obs.removeOnGlobalLayoutListener(this);
        } else {
            //noinspection deprecation
            obs.removeGlobalOnLayoutListener(this);
        }
    }

    private int getRefreshTriggerDistance() {
        return refreshTriggerDistance;
    }

    private void setRefreshTriggerDistance(int refreshTriggerDistance) {
        this.refreshTriggerDistance = refreshTriggerDistance;
    }
}
