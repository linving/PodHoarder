package com.podhoarder.activity;

import java.text.ParseException;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.DataParser;
import com.podhoarder.view.Banner;
import com.podhoarderproject.podhoarder.R;

public class EpisodeActivity extends Activity
{
	private EpisodeDBHelper mEDB;
	private FeedDBHelper mFDB;
	private Feed mCurrentFeed;
	private Episode mCurrentEpisode;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        // Initialisation
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ActionBar actionBar =  getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        setTitle(" ");
        setContentView(R.layout.activity_episode);
        
        Bundle b = getIntent().getExtras();
        final int episodeId = b.getInt("id");
        
        final Handler handler = new Handler();
        
        Thread t =new Thread()
        {
            public void run() 
            {
                handler.post(new Runnable() 
                {
                    public void run() {
                    	mEDB = new EpisodeDBHelper(EpisodeActivity.this);
                        mFDB = new FeedDBHelper(EpisodeActivity.this);
                        
                        mCurrentEpisode = mEDB.getEpisode(episodeId);
                        mCurrentFeed = mFDB.getFeed(mCurrentEpisode.getFeedId());

                        Banner banner = (Banner)findViewById(R.id.episode_banner);
                        banner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());
                        
                        TextView episodeTitle = (TextView)findViewById(R.id.episode_title);
                        episodeTitle.setText(mCurrentEpisode.getTitle());
                        //episodeTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.text_giant));
                        
                        TextView episodeDescription = (TextView)findViewById(R.id.episode_description);
                        episodeDescription.setText(mCurrentEpisode.getDescription());
                        
                        TextView episodeTimestamp = (TextView)findViewById(R.id.episode_timeStamp);
                        try
                		{
                        	episodeTimestamp.setText(DateUtils.getRelativeTimeSpanString(
                								DataParser.correctFormat.parse(
                										mCurrentEpisode.getPubDate()).getTime()));	//Set a time stamp since Episode publication.
                		} 
                		catch (ParseException e)
                		{
                			e.printStackTrace();
                		}
                    }
                });
            }
        };
        t.start();
        
        
        
        
        
       
        
        
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
		mEDB.closeDatabaseIfOpen();
        mFDB.closeDatabaseIfOpen();
		super.onDestroy();
	}
}
