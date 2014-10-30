package com.podhoarder.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.service.PodHoarderService.PlayerState;
import com.podhoarder.service.PodHoarderService.PodHoarderBinder;
import com.podhoarder.view.FloatingToggleButton;
import com.podhoarder.view.ToggleImageButton;
import com.podhoarderproject.podhoarder.R;

public class PlayerActivity extends BaseActivity implements PodHoarderService.StateChangedListener
{
	private static final String LOG_TAG = "com.podhoarder.activity.PlayerActivity";

	private PodHoarderService mPlaybackService;
	private Intent mPlayIntent;
	private Handler mHandler;
	private boolean mIsMusicBound = false;
	private boolean mUpdateBlocked = false;

    private Palette mPalette;

	private EpisodeDBHelper mEDB;
	private FeedDBHelper mFDB;
	private Feed mCurrentFeed;
	private Episode mCurrentEpisode;
	
	private SeekBar mSeekBar;
	private ToggleImageButton mPlayPauseButton;
	private ImageButton mRewindButton, mForwardButton;
	private ProgressBar mLoadingCircle;
	
	private FloatingToggleButton mFAB;
	private LinearLayout mTextContainer;

	private ServiceConnection podConnection = new ServiceConnection() // connect
																		// to
																		// the
																		// service
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			PodHoarderBinder binder = (PodHoarderBinder) service;
			mPlaybackService = binder.getService();
			Log.i(LOG_TAG, "service bound!");
			mPlaybackService.setStateChangedListener(PlayerActivity.this);
			// TODO: Setup UI here. Service is safe to use.
			if (mPlaybackService.mCurrentEpisode == null)
				mPlaybackService.loadLastPlayedEpisode();
			mCurrentEpisode = mPlaybackService.mCurrentEpisode;
			mCurrentFeed = mFDB.getFeed(mCurrentEpisode.getFeedId());

			ImageView banner = (ImageView) findViewById(R.id.episode_banner);
            mPalette = Palette.generate(mCurrentFeed.getFeedImage().imageObject());
			banner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());
            Palette.generateAsync(mCurrentFeed.getFeedImage().imageObject(), 8,
                    new Palette.PaletteAsyncListener() {
                        @Override public void onGenerated(Palette palette) {
                            // do something with the colors
                            colorUI(palette);
                        }
                    });

			TextView mTitle = (TextView) findViewById(R.id.episode_title);
			mTitle.setText(mCurrentEpisode.getTitle());

			TextView mPodcastName = (TextView) findViewById(R.id.podcast_title);
			mPodcastName.setText(mCurrentFeed.getTitle());

			setupPlayerControls();
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Initialisation
		setContentView(R.layout.activity_player);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		mEDB = new EpisodeDBHelper(PlayerActivity.this);
		mFDB = new FeedDBHelper(PlayerActivity.this);
		
		mHandler = new Handler();

		// Bind Service.
		mPlayIntent = new Intent(this, PodHoarderService.class);
		this.mIsMusicBound = this.bindService(mPlayIntent, podConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
    public boolean onCreateOptionsMenu(final Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_menu, menu);
        
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				overridePendingTransition(R.anim.slide_in_left,
						R.anim.slide_out_right);
				return true;
			case R.id.menu_player_sleep:
				setSleepTimer();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		overridePendingTransition(R.anim.activity_stay_transition, R.anim.player_fade_out);
	}

	@Override
	protected void onDestroy()
	{
		this.unbindService(this.podConnection);
		// mEDB.closeDatabaseIfOpen();
		// mFDB.closeDatabaseIfOpen();
		super.onDestroy();
	}

    public void startMainActivity()
    {
        this.onBackPressed();
    }

	private void setupPlayerControls()
	{

		mFAB = (FloatingToggleButton)findViewById(R.id.episode_favorite_toggle);
		mTextContainer = (LinearLayout)findViewById(R.id.episode_text_container);
		mFAB.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentEpisode.setFavorite(!mCurrentEpisode.isFavorite()); //Toggle favorite status of the Episode.
                mEDB.updateEpisode(mCurrentEpisode);    //Update the db with the new value
                ((FloatingToggleButton) v).setToggled(mCurrentEpisode.isFavorite());    //Toggle the button.
            }
        });
        mFAB.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTextContainer.invalidate();
                return false;
            }
        });
        mFAB.setToggled(mCurrentEpisode.isFavorite());
        
		mPlayPauseButton = (ToggleImageButton) findViewById(R.id.player_controls_button_playpause);		
		mPlayPauseButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View view)
			{
				if (mPlaybackService.isPlaying())
					mPlaybackService.pause();
				else
				{
					mHandler.post(UpdateRunnable);
					mPlaybackService.play();
				}
			}
		});
		mPlayPauseButton.setToggled(mPlaybackService.isPlaying()); //Set the initial button status depending on the Service.
		
		mLoadingCircle = (ProgressBar)findViewById(R.id.player_controls_loading_circle);
		
		mForwardButton = (ImageButton)findViewById(R.id.player_controls_button_skip_forward);
		mForwardButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View arg0)
			{
				if (mPlaybackService != null && mPlaybackService.isPlaying())
					mPlaybackService.skipForward();
			}
		});
		mRewindButton = (ImageButton)findViewById(R.id.player_controls_button_skip_backwards);
		mRewindButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View arg0)
			{
				if (mPlaybackService != null && mPlaybackService.isPlaying())
					mPlaybackService.skipBackward();
			}
		});
		
		mSeekBar = (SeekBar)findViewById(R.id.player_controls_seekbar);
		
		mSeekBar.setMax(mCurrentEpisode.getTotalTime());
		mSeekBar.setProgress(mCurrentEpisode.getElapsedTime());


		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				mUpdateBlocked = true;
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				mUpdateBlocked = false;
			}
			
			@Override
			public void onProgressChanged(SeekBar circularSeekBar,
					int progress, boolean fromUser)
			{
				if (fromUser)
					mPlaybackService.seek(progress);
			}
		});
		
		
		
		if (mPlaybackService.isPlaying())
			mHandler.post(UpdateRunnable);
	}

	private void setSleepTimer()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						Log.i(LOG_TAG, "Picked: " + which);
						return;
					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						return;
					}
				});
		View view = getLayoutInflater().inflate(R.layout.sleep_popup, null);
		builder.setView(view);

		final AlertDialog dialog = builder.create();
		NumberPicker picker = (NumberPicker) view.findViewById(R.id.sleep_numberPicker);
		picker.setMinValue(1);
		picker.setMaxValue(60);

		picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		dialog.setTitle(getString(R.string.menu_player_sleep));
		dialog.show();
	}
	
	/**
     * Post a Runnable that updates the seekbar position in relation to player progress continuously.
     */
    private Runnable UpdateRunnable = new Runnable() {
        @Override
        public void run() 
        {
        	if (!mUpdateBlocked) mSeekBar.setProgress(mPlaybackService.getPosn());
            if (mPlaybackService.isPlaying()) 
            {
            	mHandler.postDelayed(UpdateRunnable, 350);
            }
        }
    };

    @Override
	public void onStateChanged(PlayerState newPlayerState)
	{
    	Log.i(LOG_TAG, "New player state: " + newPlayerState);
		switch (newPlayerState)
		{
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
     * Changes the UI components to fitting colors within the supplied Palette.
     * @param p Palette generated from something that you want colors matched to.
     */
    private void colorUI(Palette p)
    {
        //Color the seekbar
        LayerDrawable ld = (LayerDrawable) mSeekBar.getProgressDrawable();
        final ClipDrawable pd = (ClipDrawable) ld.findDrawableByLayerId(android.R.id.progress);
        pd.setColorFilter(p.getLightVibrantColor(Color.parseColor("#000000")), PorterDuff.Mode.SRC_IN);
        //Color the Floating Action Button
       // mFAB.setColor(mPalette.getVibrantColor(getResources().getColor(R.color.windowBackground)));

    }

    private void animateSeekBarColorChange(int color)
    {
        LayerDrawable ld = (LayerDrawable) mSeekBar.getProgressDrawable();
        final ClipDrawable pd = (ClipDrawable) ld.findDrawableByLayerId(android.R.id.progress);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(R.color.windowBackground), color);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                pd.setColorFilter((Integer) animator.getAnimatedValue(), PorterDuff.Mode.SRC_IN);
            }

        });
        colorAnimation.setDuration(100);
        colorAnimation.start();
    }

}
