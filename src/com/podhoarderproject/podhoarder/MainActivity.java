package com.podhoarderproject.podhoarder;
 
import com.podhoarderproject.podhoarder.adapter.TabsPagerAdapter;
import com.podhoarderproject.podhoarder.gui.*;
import com.podhoarderproject.podhoarder.service.PodHoarderService;
import com.podhoarderproject.podhoarder.service.PodHoarderService.PodHoarderBinder;

import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener
{
	//UI Elements
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    
    //Fragment Objects
    private FeedFragment feedFragment;
    private LatestEpisodesFragment episodesFragment;
    private PlayerFragment playerFragment;
    
    //Podcast Helper
    public PodcastHelper helper;
    
    //Playback service
    private PodHoarderService podService;
	
	private Intent playIntent;
	private boolean musicBound = false;
    
	private ServiceConnection podConnection = new ServiceConnection()	//connect to the service
    { 
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service) 
	    {
		    PodHoarderBinder binder = (PodHoarderBinder)service;
		    //get service
		    podService = binder.getService();
		    //pass list
		    podService.setList(helper.playlistAdapter.playList);
		    musicBound = true;
	    }
	    
	    @Override
	    public void onServiceDisconnected(ComponentName name) 
	    {
	    	musicBound = false;
	    }
    };   
    
    // Tab titles
    private String[] tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
	    
        setContentView(R.layout.activity_main);
	    
        // Initialisation
        doFragmentSetup(savedInstanceState);
        
        doTabSetup();
        
        this.helper = new PodcastHelper(this);     
    }
    
    
    @Override
    protected void onStart()
    {
    	super.onStart();
    	if(playIntent==null)
		{
			playIntent = new Intent(this, PodHoarderService.class);
			boolean ret = this.bindService(playIntent, podConnection, Context.BIND_AUTO_CREATE);
			this.startService(playIntent);
			
		}
    
    	
    	//this.helper.refreshFeeds();
    	//Collections.reverse(((PlaylistListAdapter)this.helper.playlistAdapter).playList);
    	//this.helper.plDbH.savePlaylist(((PlaylistListAdapter)this.helper.playlistAdapter).playList);
    	//this.helper.addFeed("http://smodcast.com/channels/plus-one/feed/");
    	//this.helper.addFeed("http://smodcast.com/channels/hollywood-babble-on/feed/");
    	//this.helper.addFeed("http://feeds.feedburner.com/filipochfredrik/podcast?format=xml");
    	//this.helper.addFeed("http://smodcast.com/channels/fatman-on-batman/feed/");
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();    	
    }
    
    @Override
    protected void onDestroy()
    {
    	this.stopService(playIntent);
	    this.podService=null;
	    super.onDestroy();
    }
  
    private void doFragmentSetup(Bundle savedInstanceState)
    {
    	// If we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }
        
    	this.feedFragment = new FeedFragment();
    	this.episodesFragment = new LatestEpisodesFragment();
    	this.playerFragment = new PlayerFragment();
    	
    	getSupportFragmentManager().beginTransaction().add(R.id.pager, this.feedFragment, ""+R.id.feedFragment_container).commit();
    	getSupportFragmentManager().beginTransaction().add(R.id.pager, this.episodesFragment, ""+R.id.episodesFragment_container).commit();
    	getSupportFragmentManager().beginTransaction().add(R.id.pager, this.playerFragment, ""+R.id.playerFragment_container).commit();
    }
    
    private void doTabSetup()
    {
    	viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        //actionBar.setHomeButtonEnabled(false);
        //actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        tabs = new String[] {this.getString(R.string.feed_tab),this.getString(R.string.episodes_tab),this.getString(R.string.player_tab)};
        // Adding Tabs
        for (String tab_name : tabs) 
        {
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));        
        }
        
    	 /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    
    public void downloadEpisode(int feedId, int episodeId)
    {
    	this.helper.downloadEpisode(feedId, episodeId);
    }
    
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		// on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
		// TODO Auto-generated method stub
		
	}
	
	public PodHoarderService getPodService()
	{
		return podService;
	}


	public boolean isMusicBound()
	{
		return musicBound;
	}

}
