package com.podhoarder.fragment;

import android.graphics.Color;
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
        mToolbarBackground.setAlpha(1f);
        mToolbarContainer.setTranslationY(0f);
        if (transparent)
            mToolbarBackground.setBackgroundColor(Color.parseColor("#00000000"));
        else
            mToolbarBackground.setBackgroundColor(((BaseActivity)getActivity()).getCurrentPrimaryColor());
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
