package com.podhoarder.util;

import android.content.Context;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.podhoarder.adapter.QueueAdapter;
import com.podhoarder.datamanager.DataManager;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.view.ToggleImageButton;
import com.podhoarderproject.podhoarder.R;

/**
 * A class for managing all the player controls embedded in the navigation drawer through the use of simple functions.
 **/
public class PlayerControlsManager implements QueueAdapter.OnItemSecondaryActionClickedListener, PodHoarderService.OnEpisodeUpdateListener {
    private static final String LOG_TAG = "com.podhoarder.util.PlayerManager";

    private final Context mContext;
    private final PodHoarderService mService;
    private final DataManager mDataManager;

    private final LinearLayout mPlayerControlsExtendedContainer;
    private final FrameLayout mPlayerControlsContainer;
    private final ListView mNavigationMenu;

    private final ImageView mPlayerControlsPodcastImage;
    private final TextView mPlayerControlsNowPlayingTitle, mPlayerControlsNowPlayingSubtitle;

    private final SeekBar mSeekBar;
    private final ToggleImageButton mPlayPauseButton;
    private final ImageButton mRewindButton, mForwardButton;
    private final ProgressBar mLoadingCircle;
    private final ListView mPlaylist;

    private boolean mControlsVisible, mExtendedControlsVisible;

    /**
     * @param navMenu Navigation menu ListView.
     * @param playerControlsContainer Layout containing the compoact player controls.
     * @param playerControlsExtendedContainer   Layout containing the extended player controls.
     * @param service Background service for media playback.
     * @param ctx Application context.
     */
    public PlayerControlsManager(FrameLayout playerControlsContainer, LinearLayout playerControlsExtendedContainer, ListView navMenu, PodHoarderService service, DataManager dataManager, Context ctx) {
        mContext = ctx;
        mService = service;
        mDataManager = dataManager;

        mNavigationMenu = navMenu;

        mPlayerControlsContainer = playerControlsContainer;
        mPlayerControlsPodcastImage = (ImageView) mPlayerControlsContainer.findViewById(R.id.player_controls_podcast_image);

        mPlayPauseButton = (ToggleImageButton) mPlayerControlsContainer.findViewById(R.id.player_controls_button_playpause);
        mLoadingCircle = (ProgressBar) mPlayerControlsContainer.findViewById(R.id.player_controls_loading_circle);
        mRewindButton = (ImageButton) mPlayerControlsContainer.findViewById(R.id.player_controls_button_skip_backwards);
        mForwardButton = (ImageButton) mPlayerControlsContainer.findViewById(R.id.player_controls_button_skip_forward);

        mPlayerControlsExtendedContainer = playerControlsExtendedContainer;
        mSeekBar = (SeekBar) mPlayerControlsExtendedContainer.findViewById(R.id.player_controls_extended_seekbar);
        mPlayerControlsNowPlayingTitle = (TextView) mPlayerControlsExtendedContainer.findViewById(R.id.player_controls_nowplaying_title);
        mPlayerControlsNowPlayingSubtitle = (TextView) mPlayerControlsExtendedContainer.findViewById(R.id.player_controls_nowplaying_subtitle);

        mPlaylist = (ListView) mPlayerControlsExtendedContainer.findViewById(R.id.player_controls_extended_playlist);
        setupViews();
        setupExtendedViews();

        mControlsVisible = false;
        mExtendedControlsVisible = false;
    }

    private void setupViews() {
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService.isPlaying())
                    mService.pause();
                else
                    mService.play();
            }
        });
        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.skipForward();
            }
        });
        mRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.skipBackward();
            }
        });
    }
    private void setupExtendedViews() {

        mPlaylist.setAdapter(mDataManager.mPlaylistAdapter);
        mDataManager.mPlaylistAdapter.setOnItemSecondaryActionClickListener(this);
        mPlaylist.setItemsCanFocus(true);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mService.seek(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mService.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mService.resume();
            }
        });

        mService.setOnEpisodeUpdateListener(this);  //Start listening for updates firing from the Service.
    }


    /**
     * Shows the compact player controls.
     */
    public void showPlayerControls() {
        mPlayerControlsContainer.setVisibility(View.VISIBLE);
        mControlsVisible = true;
    }

    /**
     * Hides the compact player controls.
     */
    public void hidePlayerControls() {
        if (mExtendedControlsVisible)
            hideExtendedPlayerControls();

        mPlayerControlsContainer.setVisibility(View.GONE);
        mControlsVisible = false;
    }

    /**
     * Check whether or not the compact player controls are currently visible.
     * @return True if the controls are visible. False otherwise.
     */
    public boolean controlsVisible() {
        return mControlsVisible;
    }

    /**
     * Shows the extended player controls.
     */
    public void showExtendedPlayerControls() {
        if (!controlsVisible())
            showPlayerControls();

        mNavigationMenu.setVisibility(View.GONE);
        mPlayerControlsExtendedContainer.setVisibility(View.VISIBLE);
        mSeekBar.setVisibility(View.VISIBLE);
        mExtendedControlsVisible = true;
    }

    /**
     * Hides the extended player controls.
     */
    public void hideExtendedPlayerControls() {

        mNavigationMenu.setVisibility(View.VISIBLE);
        mPlayerControlsExtendedContainer.setVisibility(View.GONE);
        mSeekBar.setVisibility(View.GONE);
        mExtendedControlsVisible = false;
    }

    /**
     * Check whether or not the extended player controls are currently visible.
     * @return True if the controls are visible. False otherwise.
     */
    public boolean extendedControlsVisible() {
        return mExtendedControlsVisible;
    }

    /**
     * Updates all the player control views to reflect the currently playing track.
     */
    public void updateNowPlaying() {
        final Episode ep = mService.mCurrentEpisode;
        final Feed f = mDataManager.getFeed(ep.getFeedId());

        final Palette.Swatch s = f.getFeedImage().palette().getVibrantSwatch();
        //mBanner.setBackgroundColor(s.getRgb());
        mPlayerControlsPodcastImage.setImageBitmap(f.getFeedImage().largeImage());
        mPlayerControlsPodcastImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!extendedControlsVisible())
                    showExtendedPlayerControls();
                else
                    hideExtendedPlayerControls();
            }
        });

        mPlayerControlsNowPlayingTitle.setText(ep.getTitle());
        mPlayerControlsNowPlayingSubtitle.setText(f.getTitle());

        //Extended controls
        mSeekBar.setMax(ep.getTotalTime());
        mSeekBar.setProgress(ep.getElapsedTime());
        mSeekBar.invalidate();
    }

    /**
     * Updates all the currently visible player controls to reflect the current state of the media playback.
     * @param newPlayerState Enum describing the current media player status.
     */
    public void onStateChanged(PodHoarderService.PlayerState newPlayerState) {
        switch (newPlayerState) {
            case PLAYING:
                if (controlsVisible()) {
                    if (mLoadingCircle.getVisibility() == View.VISIBLE)
                        mLoadingCircle.setVisibility(View.GONE);

                    if (mPlayPauseButton.getVisibility() == View.GONE)
                        mPlayPauseButton.setVisibility(View.VISIBLE);
                    mPlayPauseButton.setToggled(true);
                }

                if (extendedControlsVisible())
                    mSeekBar.setProgress(mService.mCurrentEpisode.getElapsedTime());
                break;
            case PAUSED:
                if (controlsVisible()) {
                    if (mLoadingCircle.getVisibility() == View.VISIBLE)
                        mLoadingCircle.setVisibility(View.GONE);

                    if (mPlayPauseButton.getVisibility() == View.GONE)
                        mPlayPauseButton.setVisibility(View.VISIBLE);

                    mPlayPauseButton.setToggled(false);

                    if (extendedControlsVisible())
                        mSeekBar.setProgress(mService.mCurrentEpisode.getElapsedTime());
                }
                break;
            case LOADING:
                if (controlsVisible()) {
                    if (mPlayPauseButton.getVisibility() == View.VISIBLE)
                        mPlayPauseButton.setVisibility(View.GONE);
                    if (mLoadingCircle.getVisibility() == View.GONE)
                        mLoadingCircle.setVisibility(View.VISIBLE);
                }
                break;
            case EPISODE_CHANGED:
                updateNowPlaying();
                break;
        }
    }

    @Override
    public void onItemSecondaryActionClicked(View v, int pos) {
        mDataManager.removeFromPlaylist(pos);
    }

    @Override
    public void onEpisodeUpdate(Episode ep) {
        mSeekBar.setMax(ep.getTotalTime());
        mSeekBar.setProgress(ep.getElapsedTime());
    }
}
