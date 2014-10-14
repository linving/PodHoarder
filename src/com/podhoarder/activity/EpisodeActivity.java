package com.podhoarder.activity;

import java.text.ParseException;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.DataParser;
import com.podhoarder.view.FloatingToggleButton;
import com.podhoarder.view.StrokedTextView;
import com.podhoarderproject.podhoarder.R;

public class EpisodeActivity extends Activity
{
	private EpisodeDBHelper mEDB;
	private FeedDBHelper mFDB;
	private Feed mCurrentFeed;
	private Episode mCurrentEpisode;
	
	private int mEpId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        // Initialisation
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_episode);
        
        Bundle b = getIntent().getExtras();
        new backgroundSetupTask().execute(b.getInt("id"));
        
        ActionBar actionBar =  getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        setTitle(" ");
        
        new UISetupTask().execute(b);
    	Animation slideInAnim = AnimationUtils.loadAnimation(EpisodeActivity.this, R.anim.slide_in_right);
		findViewById(R.id.root).startAnimation(slideInAnim);
	}
	
	private class UISetupTask extends AsyncTask<Bundle, Void, Void> 
	{
		private StrokedTextView episodeTitle;
		private TextView episodeDescription, episodeTimestamp;
		private FloatingToggleButton FAB;
		private LinearLayout mTextContainer;
		
		private String title, description, timestamp;
        @Override
        protected Void doInBackground(Bundle... params) 
        {
            Bundle b = params[0];
            title = b.getString("title");
            description = b.getString("description");
            timestamp = b.getString("timestamp");
            
            mTextContainer = (LinearLayout)findViewById(R.id.episode_text_container);
            
            FAB = (FloatingToggleButton)findViewById(R.id.episode_favorite_toggle);
            
            FAB.setOnClickListener(new OnClickListener()
    		{
    			
    			@Override
    			public void onClick(View v)
    			{
    				boolean favorite = false;
    				
    				if (!mCurrentEpisode.isFavorite())
    					favorite = true;
    				
    				mCurrentEpisode.setFavorite(favorite);
    				((FloatingToggleButton)v).setToggled(favorite);
    				//mTextContainer.invalidate();
    			}
    		});
            FAB.setOnTouchListener(new OnTouchListener()
			{
				
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					mTextContainer.invalidate();
					return false;
				}
			});
            
            
            episodeTitle = (StrokedTextView)findViewById(R.id.episode_title);
            
            episodeDescription = (TextView)findViewById(R.id.episode_description);
            
            episodeTimestamp = (TextView)findViewById(R.id.episode_timeStamp);
			return null;
        }

        @Override
        protected void onPostExecute(Void res) 
        {
        	
        	episodeTitle.setText(title);
        	episodeDescription.setText(description);
        	
        	try
    		{
            	episodeTimestamp.setText(getString(R.string.timestamp_posted) + " " + DateUtils.getRelativeTimeSpanString(
    								DataParser.correctFormat.parse(timestamp).getTime()));	//Set a time stamp since Episode publication.
    		} 
    		catch (ParseException e)
    		{
    			e.printStackTrace();
    		}
        	

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
	}
	
	private class backgroundSetupTask extends AsyncTask<Integer, Void, Void> {

		private ImageView banner;
        @Override
        protected Void doInBackground(Integer... params) 
        {
        	mEDB = new EpisodeDBHelper(EpisodeActivity.this);
            mFDB = new FeedDBHelper(EpisodeActivity.this);
            
            mEpId = params[0];
            
            mCurrentEpisode = mEDB.getEpisode(mEpId);
            mCurrentFeed = mFDB.getFeed(mCurrentEpisode.getFeedId());
            
            banner = (ImageView)findViewById(R.id.episode_banner);
			return null;
        }

        @Override
        protected void onPostExecute(Void res) 
        {
        	banner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());

        	Animation fadeInAnim = AnimationUtils.loadAnimation(EpisodeActivity.this, R.anim.slide_in_right);
        	banner.startAnimation(fadeInAnim);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
	}
	
	private class completeUISetupTask extends AsyncTask<Bundle, Void, Void>
	{
		private StrokedTextView episodeTitle;
		private TextView episodeDescription, episodeTimestamp;
		private FloatingToggleButton FAB;
		private LinearLayout mTextContainer;
		private ImageView banner;
		private String title, description, timestamp;
		private int epid;
        @Override
        protected Void doInBackground(Bundle... params) 
        {
        	mEDB = new EpisodeDBHelper(EpisodeActivity.this);
            mFDB = new FeedDBHelper(EpisodeActivity.this);
            
            Bundle b = params[0];
            epid = b.getInt("id");
            title = b.getString("title");
            description = b.getString("description");
            timestamp = b.getString("timestamp");
            
            mCurrentEpisode = mEDB.getEpisode(epid);
            mCurrentFeed = mFDB.getFeed(mCurrentEpisode.getFeedId());
            
            mTextContainer = (LinearLayout)findViewById(R.id.episode_text_container);
            
            FAB = (FloatingToggleButton)findViewById(R.id.episode_favorite_toggle);
            
            FAB.setOnClickListener(new OnClickListener()
    		{
    			
    			@Override
    			public void onClick(View v)
    			{
    				boolean favorite = false;
    				
    				if (!mCurrentEpisode.isFavorite())
    					favorite = true;
    				
    				mCurrentEpisode.setFavorite(favorite);
    				((FloatingToggleButton)v).setToggled(favorite);
    				//mTextContainer.invalidate();
    			}
    		});
            FAB.setOnTouchListener(new OnTouchListener()
			{
				
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					mTextContainer.invalidate();
					return false;
				}
			});
                        
            banner = (ImageView)findViewById(R.id.episode_banner);
            
            episodeTitle = (StrokedTextView)findViewById(R.id.episode_title);
            
            episodeDescription = (TextView)findViewById(R.id.episode_description);
            
            episodeTimestamp = (TextView)findViewById(R.id.episode_timeStamp);
			return null;
        }

        @Override
        protected void onPostExecute(Void res) 
        {
        	banner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());
        	episodeTitle.setText(title);
        	episodeDescription.setText(description);
        	
        	try
    		{
            	episodeTimestamp.setText(getString(R.string.timestamp_posted) + " " + DateUtils.getRelativeTimeSpanString(
    								DataParser.correctFormat.parse(timestamp).getTime()));	//Set a time stamp since Episode publication.
    		} 
    		catch (ParseException e)
    		{
    			e.printStackTrace();
    		}
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
	}
	
	@Override
    public boolean onCreateOptionsMenu(final Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextual_menu_episode, menu);
        
        if (mCurrentEpisode != null)
        {
        	if (mCurrentEpisode.isDownloaded())
            {
            	menu.removeItem(R.id.menu_episode_available_offline);
            }
        	else
        	{
        		
        	}
        	if (mCurrentEpisode.isListened())
        	{
        		menu.removeItem(R.id.menu_episode_markAsListened);
        	}
        	else
        	{
        		
        	}
        }
        
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
