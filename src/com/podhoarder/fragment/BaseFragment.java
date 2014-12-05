package com.podhoarder.fragment;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.service.PodHoarderService;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

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
        ((LibraryActivity)getActivity()).setCurrentFragment(this);
    }

    public void onFragmentResumed() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu.size() == ((BaseActivity)getActivity()).mPreviousMenuItems.size()) {
            for (int r=0; r<menu.size(); r++) {
                if (menu.getItem(r).getItemId() != ((BaseActivity)getActivity()).mPreviousMenuItems.get(r).getItemId()) {
                    appBarShowIcons();
                    break;
                }
            }
        }
        else {
            appBarShowIcons();
        }
        //TODO: If menu contains the same items there's no need to animate.
        ((BaseActivity)getActivity()).mPreviousMenuItems.clear();
        for (int i=0; i<menu.size(); i++) {
            ((BaseActivity)getActivity()).mPreviousMenuItems.add((MenuItemImpl)menu.getItem(i));
        }
    }

    @Override
    public void onStateChanged(PodHoarderService.PlayerState newPlayerState) {

        Log.i(LOG_TAG, "New player state: " + newPlayerState);
    }

    protected void setToolbarTransparent(boolean transparent) {
        boolean shouldAnimate = false;

        if (mToolbarContainer.getTranslationY() != 0) {
            mToolbarBackground.setAlpha(1f);
            mToolbarBackground.setBackgroundColor(Color.TRANSPARENT);
            mToolbarContainer.setTranslationY(0f);
        }

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

    public void appBarShowIcons() {
        // get the center for the clipping circle
        int cy = (mToolbar.getTop() + mToolbar.getBottom()) / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(mToolbar.getWidth(), mToolbar.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(mToolbar, 0, cy, mToolbar.getHeight(), finalRadius);

        anim.setDuration(100);
        anim.start();
        iconFade(true, 200);
    }

    public void appbarHideIcons() {
        iconFade(false, 150);

        /*if (mToolbar != null) {
            // get the center for the clipping circle
            int cy = (mToolbar.getTop() + mToolbar.getBottom()) / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(mToolbar.getWidth(), mToolbar.getHeight());

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(mToolbar, 0, cy, mToolbar.getHeight(), finalRadius);

            anim.setDuration(150);

            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    //mToolbar.setVisibility(View.INVISIBLE);
                }
            });

            anim.start();
        }*/
    }

    private void iconFade(final boolean shouldFadeIn, final int duration) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int[] ids = new int[]{R.id.menu_episode_playnow, R.id.menu_episode_show_info, R.id.menu_episode_available_offline, R.id.menu_episode_delete_file, R.id.menu_episode_markAsListened, R.id.menu_episode_add_playlist,R.id.menu_player_sleep, R.id.action_add, R.id.action_search};
                List<View> icons = new ArrayList<View>();
                View v;
                for (int i : ids) {
                    v = mToolbarContainer.findViewById(i);
                    if (v != null)
                        icons.add(v);
                }
                //Find the ActionMenuView inside the Toolbar
                for (int i = 0; i<mToolbar.getChildCount(); i++ ) {
                    View toolbarChild = mToolbar.getChildAt(i);
                    if (toolbarChild instanceof ActionMenuView) {
                        //We found the ActionMenuView. Now we need to find the Overflow button view.
                        for (int r=0; r<((ActionMenuView) toolbarChild).getChildCount(); r++) {
                            v = ((ActionMenuView) toolbarChild).getChildAt(r);
                            ActionMenuView.LayoutParams params = (ActionMenuView.LayoutParams) v.getLayoutParams();
                            if (params.isOverflowButton) {
                                //Found the overflow button view. Add it to the list of icons that should be animated.
                                icons.add(v);
                                break;
                            }
                        }
                        break;
                    }
                }

                int delay = 40;
                for (View icon : icons) {
                    if (icon != null) {
                        if (shouldFadeIn) {
                            icon.setAlpha(0f);
                            icon.setScaleX(0.5f);
                            icon.setScaleY(0.5f);
                            icon.animate().scaleX(1f).scaleY(1f).alpha(1f).setInterpolator(new DecelerateInterpolator()).setStartDelay(delay).setDuration(duration).start();
                            delay += 40;
                        }
                        else {
                            icon.animate().scaleX(0.5f).scaleY(0.5f).alpha(0f).setInterpolator(new DecelerateInterpolator()).setStartDelay(delay).setDuration(duration).start();
                            delay += 40;
                        }
                    }
                }
            }
        });
    }
}
