package com.podhoarder.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.podhoarder.view.ToggleImageButton;
import com.podhoarderproject.podhoarder.R;

public class PlayerActivity extends Activity implements PodHoarderService.StateChangedListener
{
	private static final String LOG_TAG = "com.podhoarder.activity.PlayerActivity";

	private PodHoarderService mPlaybackService;
	private Intent mPlayIntent;
	private Handler mHandler;
	private boolean mIsMusicBound = false;
	private boolean mUpdateBlocked = false;

	private EpisodeDBHelper mEDB;
	private FeedDBHelper mFDB;
	private Feed mCurrentFeed;
	private Episode mCurrentEpisode;
	
	private SeekBar mSeekBar;
	private ToggleImageButton mPlayPauseButton;
	private ImageButton mRewindButton, mForwardButton;
	private ProgressBar mLoadingCircle;

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
			banner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());

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
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
		setTitle(getString(R.string.now_playing));
		setContentView(R.layout.activity_player);

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
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	protected void onDestroy()
	{
		this.unbindService(this.podConnection);
		// mEDB.closeDatabaseIfOpen();
		// mFDB.closeDatabaseIfOpen();
		super.onDestroy();
	}

	private void setupPlayerControls()
	{
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
}
