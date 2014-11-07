package com.podhoarder.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.view.ToggleImageButton;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-10-29.
 */
public class PlayerFragment extends BaseFragment implements PodHoarderService.StateChangedListener {
    //Playback Service
    private PodHoarderService mPlaybackService;

    //Current Feed & Episode
    private Feed mCurrentFeed;
    private Episode mCurrentEpisode;

    //Runnable Handler
    private Handler mHandler;

    //UI Update Blocker
    private boolean mUpdateBlocked = false;

    //UI Controls
    private SeekBar mSeekBar;
    private ToggleImageButton mPlayPauseButton;
    private ImageButton mRewindButton, mForwardButton;
    private ProgressBar mLoadingCircle;
    private ToggleImageButton mFAB;

    //Toolbar
    private Toolbar mToolbar;
    private float mOriginalToolbarElevation = 2f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.PlayerFragment";
        mContentView = inflater.inflate(R.layout.activity_player, container, false);
        mDataManager = ((LibraryActivity) getActivity()).getDataManager();

        mPlaybackService = ((LibraryActivity) getActivity()).getPlaybackService();
        mPlaybackService.setStateChangedListener(PlayerFragment.this);

        if (mPlaybackService.mCurrentEpisode == null)
            mPlaybackService.loadLastPlayedEpisode();
        mCurrentEpisode = mPlaybackService.mCurrentEpisode;
        mCurrentFeed = mDataManager.getFeed(mCurrentEpisode.getFeedId());

        mHandler = new Handler();

        mToolbar = ((BaseActivity)getActivity()).mToolbar;

        setupUI();
        setupPlayerControls();

        ((LibraryActivity)getActivity()).setCurrentFragment(this);

        return mContentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        //getActivity().getSupportFragmentManager().popBackStack();
        return false;
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onFragmentResumed() {

    }

    @Override
    public void onStateChanged(PodHoarderService.PlayerState newPlayerState) {
        Log.i(LOG_TAG, "New player state: " + newPlayerState);
        switch (newPlayerState) {
            case PLAYING:
                mSeekBar.setProgress(mPlaybackService.getPosn());

                if (mLoadingCircle.getVisibility() == View.VISIBLE)
                    mLoadingCircle.setVisibility(View.GONE);

                if (mPlayPauseButton.getVisibility() == View.GONE)
                    mPlayPauseButton.setVisibility(View.VISIBLE);

                mPlayPauseButton.setToggled(true);
                break;
            case PAUSED:
                if (mLoadingCircle.getVisibility() == View.VISIBLE)
                    mLoadingCircle.setVisibility(View.GONE);

                if (mPlayPauseButton.getVisibility() == View.GONE)
                    mPlayPauseButton.setVisibility(View.VISIBLE);

                mPlayPauseButton.setToggled(false);
                break;
            case LOADING:
                mPlayPauseButton.setVisibility(View.GONE);
                mLoadingCircle.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Post a Runnable that updates the seekbar position in relation to player progress continuously.
     */
    private Runnable UpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mUpdateBlocked) mSeekBar.setProgress(mPlaybackService.getPosn());
            if (mPlaybackService.isPlaying()) {
                mHandler.postDelayed(UpdateRunnable, 350);
            }
        }
    };

    private void setupUI() {
        ImageView banner = (ImageView) mContentView.findViewById(R.id.episode_banner);
        banner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());
        //((BaseActivity)getActivity()).colorUI(mCurrentFeed.getFeedImage().palette());
        //mToolbar.setTitle(mCurrentEpisode.getTitle());
        //mToolbar.setSubtitle(mCurrentFeed.getTitle());
    }

    private void setupPlayerControls() {

        mFAB = (ToggleImageButton) mContentView.findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentEpisode.setFavorite(!mCurrentEpisode.isFavorite()); //Toggle favorite status of the Episode.
                mDataManager.updateEpisode(mCurrentEpisode);    //Update the db with the new value
                ((ToggleImageButton) v).setToggled(mCurrentEpisode.isFavorite());    //Toggle the button.
            }
        });
        mFAB.setToggled(mCurrentEpisode.isFavorite());

        mPlayPauseButton = (ToggleImageButton) mContentView.findViewById(R.id.player_controls_button_playpause);
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mPlaybackService.isPlaying())
                    mPlaybackService.pause();
                else {
                    mHandler.post(UpdateRunnable);
                    mPlaybackService.play();
                }
            }
        });
        mPlayPauseButton.setToggled(mPlaybackService.isPlaying()); //Set the initial button status depending on the Service.

        mLoadingCircle = (ProgressBar) mContentView.findViewById(R.id.player_controls_loading_circle);

        mForwardButton = (ImageButton) mContentView.findViewById(R.id.player_controls_button_skip_forward);
        mForwardButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mPlaybackService != null && mPlaybackService.isPlaying())
                    mPlaybackService.skipForward();
            }
        });
        mRewindButton = (ImageButton) mContentView.findViewById(R.id.player_controls_button_skip_backwards);
        mRewindButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mPlaybackService != null && mPlaybackService.isPlaying())
                    mPlaybackService.skipBackward();
            }
        });

        mSeekBar = (SeekBar) mContentView.findViewById(R.id.player_controls_seekbar);

        mSeekBar.setMax(mCurrentEpisode.getTotalTime());
        mSeekBar.setProgress(mCurrentEpisode.getElapsedTime());


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUpdateBlocked = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUpdateBlocked = false;
            }

            @Override
            public void onProgressChanged(SeekBar circularSeekBar,
                                          int progress, boolean fromUser) {
                if (fromUser)
                    mPlaybackService.seek(progress);
            }
        });


        if (mPlaybackService.isPlaying())
            mHandler.post(UpdateRunnable);
    }
}
