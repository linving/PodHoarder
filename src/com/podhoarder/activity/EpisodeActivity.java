package com.podhoarder.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.view.FloatingToggleButton;
import com.podhoarderproject.podhoarder.R;

public class EpisodeActivity extends BaseActivity
{
    private Palette mPalette;

	private EpisodeDBHelper mEDB;
	private FeedDBHelper mFDB;
	private Feed mCurrentFeed;
	private Episode mCurrentEpisode;
	
	private FloatingToggleButton mFAB;
	
	private int mEpId;

    private TextView episodeTitle, episodeTimestamp, episodeDescription;

    private LinearLayout mTextContainer;

    private String title, timeStamp, description;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        // Initialisation
        setContentView(R.layout.activity_episode);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        Bundle b = getIntent().getExtras();
        new backgroundSetupTask().execute(b.getInt("id"));
        title = b.getString("title");
        timeStamp = b.getString("timeStamp");
        description = b.getString("description");

        episodeTitle = (TextView)findViewById(R.id.episode_title);
        episodeTitle.setText(title);

        episodeTimestamp = (TextView)findViewById(R.id.episode_timeStamp);
        episodeTimestamp.setText(timeStamp);

        episodeDescription = (TextView)findViewById(R.id.episode_description);
        episodeDescription.setText(description);

        mTextContainer = (LinearLayout)findViewById(R.id.episode_text_container);

        mFAB = (FloatingToggleButton)findViewById(R.id.episode_favorite_toggle);
        mFAB.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentEpisode.setFavorite(!mCurrentEpisode.isFavorite()); //Set the object property
                mEDB.updateEpisode(mCurrentEpisode);    //Commit update to the DB
                ((FloatingToggleButton) v).setToggled(mCurrentEpisode.isFavorite());    //Toggle the button
            }
        });
        mFAB.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTextContainer.invalidate();
                return false;
            }
        });

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
            mFAB.setToggled(mCurrentEpisode.isFavorite());
        	banner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());
            Palette.generateAsync(mCurrentFeed.getFeedImage().imageObject(), 8,
                    new Palette.PaletteAsyncListener() {
                        @Override public void onGenerated(Palette palette) {
                            // do something with the colors
                            mPalette = palette;
                            colorUI(palette);
                        }
                    });
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
        if (mEDB != null)
		    mEDB.closeDatabaseIfOpen();
        if (mFDB != null)
            mFDB.closeDatabaseIfOpen();
		super.onDestroy();
	}

    @Override
    public void startMainActivity()
    {
        this.onBackPressed();
    }

    /**
     * Changes the UI components to fitting colors within the supplied Palette.
     * @param p Palette generated from something that you want colors matched to.
     */
    private void colorUI(Palette p)
    {
        //Color the Floating Action Button
        //mFAB.setColor(mPalette.getVibrantColor(getResources().getColor(R.color.app_favorite)));
        mTextContainer.invalidate();    //Force the overlaying layout to redraw with the correct button color
    }
}
