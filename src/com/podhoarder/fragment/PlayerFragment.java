package com.podhoarder.fragment;

import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.adapter.QueueAdapter;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.view.ToggleImageButton;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-10-29.
 */
public class PlayerFragment extends BaseFragment implements PodHoarderService.StateChangedListener, QueueAdapter.OnItemSecondaryActionClickedListener {
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
    private ImageView mPodcastBanner;
    private ImageButton mFAB;
    private ListView mPlaylist;

    //Toolbar

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.PlayerFragment";

        super.onCreateView(inflater, container, savedInstanceState);

        mContentView = inflater.inflate(R.layout.activity_player, container, false);
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
        ((LibraryActivity)getActivity()).setCurrentFragment(this);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.player_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onFragmentResumed() {

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

        mPodcastBanner = (ImageView) mContentView.findViewById(R.id.podcast_banner);
        mPodcastBanner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());
        mPodcastBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaybackService.isPlaying())
                    mPlaybackService.pause();
                else
                    mPlaybackService.play();
            }
        });

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

        /*mPlayPauseButton = (ToggleImageButton) mContentView.findViewById(R.id.player_controls_button_playpause);
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
        });*/

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

    private void colorUI(Palette p) {
        Palette.Swatch s = p.getDarkVibrantSwatch();
        //View container = mContentView.findViewById(R.id.player_text_container);
        //container.setBackgroundColor(s.getRgb());
        //((TextView)container.findViewById(R.id.nowplaying_title)).setTextColor(s.getTitleTextColor());
        //((TextView)container.findViewById(R.id.nowplaying_subtitle)).setTextColor(s.getBodyTextColor());
        //mToolbarBackground.setBackgroundColor(s.getRgb());

        LayerDrawable ld = (LayerDrawable) mSeekBar.getProgressDrawable();
        ClipDrawable d1 = (ClipDrawable) ld.findDrawableByLayerId(android.R.id.progress);
        d1.setColorFilter(s.getRgb(), PorterDuff.Mode.SRC_IN);

        TextView mNowPlayingHeader = (TextView) mContentView.findViewById(R.id.nowplaying_header);
        TextView mPlaylistHeader = (TextView) mContentView.findViewById(R.id.playlist_header);

        mNowPlayingHeader.setTextColor(s.getBodyTextColor());
        GradientDrawable backgroundDrawable = (GradientDrawable) mNowPlayingHeader.getBackground();
        backgroundDrawable.setColor(s.getRgb());

        mPlaylistHeader.setTextColor(s.getBodyTextColor());
        backgroundDrawable = (GradientDrawable) mPlaylistHeader.getBackground();
        backgroundDrawable.setColor(s.getRgb());
    }


}
