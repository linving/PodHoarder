package com.podhoarder.fragment;

import android.animation.Animator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.adapter.QueueAdapter;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.util.ToastMessages;
import com.podhoarder.view.CircularSeekBar;
import com.podhoarder.view.ToggleImageButton;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-10-29.
 */
public class PlayerFragment extends BaseFragment implements PodHoarderService.StateChangedListener, QueueAdapter.OnItemSecondaryActionClickedListener, TimePickerFragment.OnTimePickedListener, PodHoarderService.SleepTimerListener {
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
    private CircularSeekBar mSeekBar;
    private ToggleImageButton mPlayPauseButton;
    private ImageButton mRewindButton, mForwardButton;
    private ProgressBar mLoadingCircle;
    private ImageView mPodcastBanner;
    private ImageButton mFAB;
    private ListView mPlaylist;

    private Menu mOptionsMenu;

    private boolean mExitAnimationsFinished = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.PlayerFragment";

        super.onCreateView(inflater, container, savedInstanceState);

        mContentView = inflater.inflate(R.layout.fragment_player, container, false);
        setHasOptionsMenu(true);

        mPlaybackService = ((LibraryActivity) getActivity()).getPlaybackService();
        mPlaybackService.setStateChangedListener(PlayerFragment.this);

        if (mPlaybackService.mCurrentEpisode == null)
            mPlaybackService.loadLastPlayedEpisode();
        mCurrentEpisode = mPlaybackService.mCurrentEpisode;
        mCurrentFeed = mDataManager.getFeed(mCurrentEpisode.getFeedId());

        mHandler = new Handler();

        setupUI();

        setDrawerIconEnabled(false, 0);

        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlaybackService != null) {
            onServiceConnected();
        }
        mToolbarContainer.setTranslationY(0f);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onPause() {
        ((BaseActivity) getActivity()).resetUI();
        super.onPause();
    }

    @Override
    public boolean onBackPressed() {
        if (!mExitAnimationsFinished) {
            appbarHideIcons();
            endFragmentAnimation();
            return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.player_menu, menu);
        mOptionsMenu = menu;
        if (mPlaybackService.isTimerSet()) {   //If the timer is set we change the default icon to the "off" button.
            mOptionsMenu.findItem(R.id.menu_player_sleep).setIcon(R.drawable.ic_timer_off_white_24dp);  //Set
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_player_sleep:
                if (mPlaybackService.isTimerSet()) {
                    mPlaybackService.cancelTimer();
                    mOptionsMenu.findItem(R.id.menu_player_sleep).setIcon(R.drawable.ic_timer_white_24dp);
                    ToastMessages.TimerCancelled(getActivity()).show();
                }
                else {
                    TimePickerFragment newFragment = new TimePickerFragment();
                    newFragment.setOnTimePickedListener(this);  //Listen for the result.
                    newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimePicked(TimePicker view, int hourOfDay, int minute) {
        if (mPlaybackService.setSleepTimer(hourOfDay, minute)) {
            mOptionsMenu.findItem(R.id.menu_player_sleep).setIcon(R.drawable.ic_timer_off_white_24dp);
            ToastMessages.TimerSet(getActivity(), hourOfDay, minute).show();
        }
    }

    @Override
    public void onServiceConnected() {
        mPlaybackService.setSleepTimerListener(this);
    }

    @Override
    public void onSleepTimerFired() {
        if (mOptionsMenu != null)
            mOptionsMenu.findItem(R.id.menu_player_sleep).setIcon(R.drawable.ic_timer_white_24dp);
    }

    @Override
    public void onFragmentResumed() {
        mSeekBar.setScaleX(0f);
        mSeekBar.setScaleY(0f);
        mSeekBar.animate().scaleY(1f).scaleX(1f).setInterpolator(new AnticipateOvershootInterpolator()).setDuration(300).start();
    }

    @Override
    public void onItemSecondaryActionClicked(View v, int pos) {
        mDataManager.removeFromPlaylist(pos);
    }

    @Override
    public void onStateChanged(PodHoarderService.PlayerState newPlayerState) {
        Log.i(LOG_TAG, "New player state: " + newPlayerState);
        switch (newPlayerState) {
            case PLAYING:
                mSeekBar.setProgress(mPlaybackService.getPosn());

                /*if (mLoadingCircle.getVisibility() == View.VISIBLE)
                    mLoadingCircle.setVisibility(View.GONE);

                if (mPlayPauseButton.getVisibility() == View.GONE)
                    mPlayPauseButton.setVisibility(View.VISIBLE);

                mPlayPauseButton.setToggled(true);*/
                break;
            case PAUSED:
                /*if (mLoadingCircle.getVisibility() == View.VISIBLE)
                    mLoadingCircle.setVisibility(View.GONE);

                if (mPlayPauseButton.getVisibility() == View.GONE)
                    mPlayPauseButton.setVisibility(View.VISIBLE);

                mPlayPauseButton.setToggled(false);*/
                break;
            case LOADING:
                /*mPlayPauseButton.setVisibility(View.GONE);
                mLoadingCircle.setVisibility(View.VISIBLE);*/
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
        setToolbarTransparent(true);
        trySetScrimPadding();
        setupViews();
        colorUI(mCurrentFeed.getFeedImage().palette());
    }

    private void setupViews() {

        if (mCurrentEpisode != null) {
            TextView title = (TextView) mContentView.findViewById(R.id.nowplaying_title);

            title.setText(mCurrentEpisode.getTitle());
        }

        mPlaylist = (ListView) mContentView.findViewById(R.id.playlist);
        mDataManager.mPlaylistAdapter.setOnItemSecondaryActionClickListener(this);
        mPlaylist.setAdapter(mDataManager.mPlaylistAdapter);
        mPlaylist.setItemsCanFocus(true);

        setupPlayerControls();
    }

    private void setupPlayerControls() {

        mFAB = (ImageButton) mContentView.findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Do something
            }
        });


        mSeekBar = (CircularSeekBar) mContentView.findViewById(R.id.player_controls_seekbar);
        mSeekBar.setBackground(mCurrentFeed.getFeedImage().largeImage());
        mSeekBar.setMax(mCurrentEpisode.getTotalTime());
        mSeekBar.setProgress(mCurrentEpisode.getElapsedTime());
        mSeekBar.setProgressColor(getResources().getColor(R.color.windowBackground));
        mSeekBar.setBarWidth(50);

        mSeekBar.setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress, boolean fromTouch) {
                if (fromTouch)
                    mPlaybackService.seek(newProgress);
            }
        });

        mSeekBar.setOnImageClickListener(new CircularSeekBar.OnImageClickListener() {
            @Override
            public void onImageClicked(CircularSeekBar view) {
                if (mPlaybackService.isPlaying())
                    mPlaybackService.pause();
                else {
                    mHandler.post(UpdateRunnable);
                    mPlaybackService.play();
                }
            }
        });

        if (mPlaybackService.isPlaying())
            mHandler.post(UpdateRunnable);
    }

    private void colorUI(Palette p) {
        Palette.Swatch s = p.getDarkVibrantSwatch();
        //View container = mContentView.findViewById(R.id.player_text_container);
        //container.setBackgroundColor(s.getRgb());
        //((TextView)container.findViewById(R.id.nowplaying_title)).setTextColor(s.getTitleTextColor());
        //((TextView)container.findViewById(R.id.nowplaying_subtitle)).setTextColor(s.getBodyTextColor());
        //mToolbarBackground.setBackgroundColor(s.getRgb());
        int vibrantColor = p.getVibrantColor(Color.WHITE);

        mContentView.setBackgroundColor(vibrantColor);

        mSeekBar.setRingBackgroundColor(vibrantColor);

        ((BaseActivity) getActivity()).colorUI(vibrantColor);

        setToolbarTransparent(true);

        TextView mNowPlayingHeader = (TextView) mContentView.findViewById(R.id.nowplaying_header);
        TextView mPlaylistHeader = (TextView) mContentView.findViewById(R.id.playlist_header);

        mNowPlayingHeader.setTextColor(s.getBodyTextColor());
        GradientDrawable backgroundDrawable = (GradientDrawable) mNowPlayingHeader.getBackground();
        backgroundDrawable.setColor(s.getRgb());

        mPlaylistHeader.setTextColor(s.getBodyTextColor());
        backgroundDrawable = (GradientDrawable) mPlaylistHeader.getBackground();
        backgroundDrawable.setColor(s.getRgb());
    }

    private void endFragmentAnimation() {
        final int ANIMATION_DURATION = 300;
        mSeekBar.animate().scaleY(0f).scaleX(0f).setInterpolator(new AnticipateOvershootInterpolator()).setDuration(ANIMATION_DURATION).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
        ((BaseActivity) getActivity()).resetUI();

    }
}
