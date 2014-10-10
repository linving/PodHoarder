package com.podhoarder.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.service.PodHoarderService.PodHoarderBinder;
import com.podhoarder.view.Banner;
import com.podhoarderproject.podhoarder.R;

public class PlayerActivity extends Activity
{
	private static final String LOG_TAG = "com.podhoarder.activity.PlayerActivity";
	
	private PodHoarderService mPlaybackService;
    private Intent mPlayIntent;
	private boolean mIsMusicBound = false;
	
	private EpisodeDBHelper mEDB;
	private FeedDBHelper mFDB;
	private Feed mCurrentFeed;
	private Episode mCurrentEpisode;
	
	private ServiceConnection podConnection = new ServiceConnection()	//connect to the service
    { 
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service) 
	    {
		   PodHoarderBinder binder = (PodHoarderBinder)service;
		   mPlaybackService = binder.getService();
		   Log.i(LOG_TAG, "service bound!");
		   //TODO: Setup UI here. Service is safe to use.
		   if (mPlaybackService.mCurrentEpisode == null)
			   mPlaybackService.loadLastPlayedEpisode();
		   mCurrentEpisode = mPlaybackService.mCurrentEpisode;
           mCurrentFeed = mFDB.getFeed(mCurrentEpisode.getFeedId());

           Banner banner = (Banner)findViewById(R.id.episode_banner);
           banner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());
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
        ActionBar actionBar =  getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        setTitle(" ");
        setContentView(R.layout.activity_player);
        

		mEDB = new EpisodeDBHelper(PlayerActivity.this);
        mFDB = new FeedDBHelper(PlayerActivity.this);
        
        //Bind Service.
        mPlayIntent = new Intent(this, PodHoarderService.class);
		this.mIsMusicBound = this.bindService(mPlayIntent, podConnection, Context.BIND_AUTO_CREATE);
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    switch (item.getItemId()) 
	    {
		    // Respond to the action bar's Up/Home button
		    case android.R.id.home:
		        NavUtils.navigateUpFromSameTask(this);
		        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
		//mEDB.closeDatabaseIfOpen();
        //mFDB.closeDatabaseIfOpen();
		super.onDestroy();
	}
}
