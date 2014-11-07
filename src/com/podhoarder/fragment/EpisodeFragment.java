package com.podhoarder.fragment;

import android.animation.Animator;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.util.DataParser;
import com.podhoarder.view.ToggleImageButton;
import com.podhoarderproject.podhoarder.R;

import java.text.ParseException;

/**
 * Created by Emil on 2014-10-29.
 */
public class EpisodeFragment extends BaseFragment {

    //Episode and Podcast
    private Episode mCurrentEpisode;

    //UI Views
    private ToggleImageButton mFAB;
    private TextView mEpisodeDescription;
    private LinearLayout mTextContainer, mHeadlineContainer;
    private Toolbar mToolbar;
    private float mOriginalToolbarElevation = 2f;

    private boolean mExitAnimationsFinished = false;

    public static EpisodeFragment newInstance(int episodeId) {
        EpisodeFragment f = new EpisodeFragment();
        Bundle args = new Bundle();
        args.putInt("episodeId", episodeId);
        f.setArguments(args);
        return f;
    }

    //Overrides
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.EpisodeFragment";
        mContentView = inflater.inflate(R.layout.activity_episode, container, false);
        setHasOptionsMenu(true);

        mToolbar = ((BaseActivity)getActivity()).mToolbar;
        mOriginalToolbarElevation = mToolbar.getElevation();


        mDataManager = ((LibraryActivity) getActivity()).getDataManager();

        int episodeId = getArguments().getInt("episodeId", 0);

        mCurrentEpisode = mDataManager.getEpisode(episodeId);

        setupUI();

        ((LibraryActivity)getActivity()).setCurrentFragment(this);

        return mContentView;
    }

    @Override
    public void onDestroy() {
        mToolbar.setElevation(mOriginalToolbarElevation);
        mOriginalToolbarElevation = 2f;
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contextual_menu_episode, menu);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onBackPressed() {
        if (!mExitAnimationsFinished) {
            endFragmentAnimation();
            return true;
        }
        return false;
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onFragmentResumed() {

    }

    private void setupUI() {

        mToolbar.setElevation(0f);
        TextView episodeTitle = (TextView) mContentView.findViewById(R.id.episode_title);
        episodeTitle.setText(mCurrentEpisode.getTitle());

        TextView episodeTimestamp = (TextView) mContentView.findViewById(R.id.episode_timeStamp);
        try {
            episodeTimestamp.setText(DateUtils.getRelativeTimeSpanString(
                    DataParser.correctFormat.parse(
                            mCurrentEpisode.getPubDate()).getTime()));	//Set a time stamp since Episode publication.
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mEpisodeDescription = (TextView) mContentView.findViewById(R.id.episode_description);
        mEpisodeDescription.setText(mCurrentEpisode.getDescription());
        mEpisodeDescription.setTranslationY(-1000f);
        mEpisodeDescription.animate().translationY(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300).start();

        mTextContainer = (LinearLayout) mContentView.findViewById(R.id.episode_text_container);
        mHeadlineContainer = (LinearLayout) mContentView.findViewById(R.id.episode_headline_text_container);
        mHeadlineContainer.setMinimumHeight(mToolbar.getMinimumHeight() * 2);

        mFAB = (ToggleImageButton) mContentView.findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEpisode.setFavorite(!mCurrentEpisode.isFavorite()); //Set the object property
                mDataManager.updateEpisode(mCurrentEpisode);    //Commit update to the DB
                ((ToggleImageButton) v).setToggled(mCurrentEpisode.isFavorite());    //Toggle the button
            }
        });
        mFAB.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTextContainer.invalidate();
                return false;
            }
        });
        mFAB.setToggled(mCurrentEpisode.isFavorite());

        mFAB.setScaleY(0f);
        mFAB.animate().scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).start();
    }

    private void endFragmentAnimation() {
        mFAB.animate().scaleY(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).start();
        mEpisodeDescription.animate().translationY(-(mEpisodeDescription.getMeasuredHeight()+10)).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimationFinished();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void onAnimationFinished() {
        mFAB.setAlpha(0f);
        mEpisodeDescription.setAlpha(0f);
        mHeadlineContainer.animate().translationY(-mHeadlineContainer.getMeasuredHeight()).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mExitAnimationsFinished = true;
                //onBackPressed();
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

}
