package com.podhoarder.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.service.PodHoarderService;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-10-28.
 */
public class BaseFragment extends Fragment implements PodHoarderService.StateChangedListener {
    protected String LOG_TAG;

    //Fragment Root View
    protected View mContentView;

    //Library Data Manager
    protected LibraryActivityManager mDataManager;

    //Service
    protected PodHoarderService mPlaybackService;

    protected int mStatusBarHeight;

    //Toolbar
    protected Toolbar mToolbar;
    protected int mToolbarSize;
    protected FrameLayout mToolbarContainer;
    protected View mToolbarBackground;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        mDataManager = ((LibraryActivity)getActivity()).getDataManager();
        mPlaybackService = ((LibraryActivity)getActivity()).getPlaybackService();

        mToolbar = ((BaseActivity)getActivity()).mToolbar;
        mToolbarSize = ((BaseActivity)getActivity()).mToolbarSize;
        mToolbarBackground = ((BaseActivity)getActivity()).mToolbarBackground;
        mToolbarContainer = ((BaseActivity)getActivity()).mToolbarContainer;
        mStatusBarHeight = ((BaseActivity)getActivity()).mStatusBarHeight;

        //setDrawerIconEnabled(true, 300);

        return mContentView;
    }

    @Override
    public void onResume() {
        ((LibraryActivity)getActivity()).setCurrentFragment(this);
        super.onResume();
    }

    public boolean onBackPressed() {
        return false;
    }

    public void onServiceConnected() {
        mPlaybackService = ((LibraryActivity) getActivity()).getPlaybackService();
        mPlaybackService.setStateChangedListener(BaseFragment.this);
    }

    public void onFragmentResumed() {

    }

    @Override
    public void onStateChanged(PodHoarderService.PlayerState newPlayerState) {
        Log.i(LOG_TAG, "New player state: " + newPlayerState);
    }

    protected void setToolbarTransparent(boolean transparent) {
        boolean shouldAnimate = false;
        mToolbarBackground.setAlpha(1f);
        mToolbarContainer.setTranslationY(0f);

        if (transparent) {
            if (((ColorDrawable)mToolbarBackground.getBackground()).getColor() == Color.TRANSPARENT) {

            }
            else {
                shouldAnimate = true;
            }
        }
        else {
            if (((ColorDrawable)mToolbarBackground.getBackground()).getColor() == ((BaseActivity)getActivity()).getCurrentPrimaryColor()) {

            }
            else {
                shouldAnimate = true;
            }
        }

        if (shouldAnimate) {
            ValueAnimator primaryColorAnimation;
            if (transparent)
                primaryColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), ((ColorDrawable)mToolbarBackground.getBackground()).getColor(), Color.TRANSPARENT);
            else
                primaryColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), ((ColorDrawable)mToolbarBackground.getBackground()).getColor(), ((BaseActivity)getActivity()).getCurrentPrimaryColor());

            primaryColorAnimation.setDuration(200);
            primaryColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    int value = (Integer) animator.getAnimatedValue();
                    mToolbarBackground.setBackgroundColor(value);
                }

            });
            primaryColorAnimation.start();
        }
    }

    protected void setDrawerIconEnabled(final boolean enabled, int animationDuration) {
        ((BaseActivity)getActivity()).setDrawerIconEnabled(enabled, animationDuration);
    }

    protected boolean isDrawerIconEnabled() {
        return ((BaseActivity)getActivity()).isDrawerIconEnabled();
    }

    /**
     * Tries to set top padding of the top banner scrim / shadow.
     * @return True if padding was set. False otherwise.
     */
    protected boolean trySetScrimPadding() {
        View scrim = mContentView.findViewById(R.id.banner_scrim);
        if (scrim != null) {
            scrim.setPadding(0,mStatusBarHeight,0,0);
            return true;
        }
        else
            return false;
    }
}
