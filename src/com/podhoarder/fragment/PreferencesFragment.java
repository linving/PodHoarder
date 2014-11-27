package com.podhoarder.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.util.Constants;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-26.
 */
public class PreferencesFragment extends PreferenceFragment {

    private DummyPreferenceFragment mDummyFragment;


    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        addPreferencesFromResource(R.xml.preferences);
        Preference lastEpisodePreference = findPreference(Constants.SETTINGS_KEY_LASTEPISODE);
        Preference gridItemPreference = findPreference(Constants.SETTINGS_KEY_GRIDITEMSIZE);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removePreference(lastEpisodePreference);
        preferenceScreen.removePreference(gridItemPreference);

        if (((BaseActivity)getActivity()).isDrawerIconEnabled()) {
            ((BaseActivity)getActivity()).setDrawerIconEnabled(false,300);
        }

        mDummyFragment = new DummyPreferenceFragment();

        //getPreferenceScreen().getView().setPadding(0,(mDummyFragment.mToolbarSize + mDummyFragment.mStatusBarHeight),0,0);

    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        View v = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.player_menu, menu);
        //menu.findItem(R.id.menu_player_sleep).setVisible(false);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        final int statusBarHeight = ((LibraryActivity)getActivity()).mStatusBarHeight;
        final int toolbarHeight = ((LibraryActivity)getActivity()).mToolbarSize;
        getListView().setPadding(0,(toolbarHeight + statusBarHeight),0,0);
        setToolbarTransparent(false);
        ((LibraryActivity)getActivity()).setCurrentFragment(mDummyFragment);
        super.onResume();
    }

    private void setToolbarTransparent(boolean transparent) {
        final FrameLayout mToolbarContainer = ((LibraryActivity)getActivity()).mToolbarContainer;
        final View mToolbarBackground = ((LibraryActivity)getActivity()).mToolbarBackground;

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
}
