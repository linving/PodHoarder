package com.podhoarderproject.podhoarder.activity;
 
import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.adapter.TabFragmentsAdapter;
import com.podhoarderproject.podhoarder.gui.*;
import com.podhoarderproject.podhoarder.service.PodHoarderService;
import com.podhoarderproject.podhoarder.service.PodHoarderService.PodHoarderBinder;
import com.podhoarderproject.podhoarder.util.MusicIntentReceiver;
import com.podhoarderproject.podhoarder.util.PodcastHelper;

import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.MainActivity";
	//UI Elements
    private ViewPager mPager;
    private TabFragmentsAdapter mAdapter;
    public ActionBar actionBar;
    
    //Podcast Helper
    public PodcastHelper helper;
    
    //Playback service
    public PodHoarderService podService;
	
	private Intent playIntent;
	private boolean musicBound = false;
	
	//Receiver for music channel intents (headphones unplugged etc.)
	private MusicIntentReceiver musicIntentReceiver;
    
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
		    
		    //Initialise the headphone jack listener / intent receiver.
		    IntentFilter headsetFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		    IntentFilter callStateFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
	        musicIntentReceiver = new MusicIntentReceiver(podService);
	        registerReceiver(musicIntentReceiver, headsetFilter);
	        registerReceiver(musicIntentReceiver, callStateFilter);
	        
		    setTab();
	    }
	    
	    @Override
	    public void onServiceDisconnected(ComponentName name) 
	    {
	    	unregisterReceiver(musicIntentReceiver);
	    	musicBound = false;
	    }
    };   
    

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        super.onCreate(savedInstanceState);
        
        // Initialisation
        setContentView(R.layout.activity_main);
        getActionBar().show();
        doTabSetup();
        this.helper = new PodcastHelper(this);  
        
        if (!this.musicBound)
    	{
    		if(playIntent==null)
    		{
    			playIntent = new Intent(this, PodHoarderService.class);
    			this.musicBound = this.bindService(playIntent, podConnection, Context.BIND_AUTO_CREATE);
    			this.startService(playIntent);
    		}
    	} 
    }
    
    
    @Override
    protected void onStart()
    {
    	super.onStart();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();    	
    }
    
    @Override
    protected void onDestroy()
    {
    	this.unbindService(this.podConnection);
    	this.stopService(playIntent);
	    this.podService=null;
	    this.musicBound = false;
	    super.onDestroy();
    }
    
    @Override
    protected void onPause()
    {    	
    	super.onPause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) 
    	{
	        case R.id.action_settings:
	        	startActivity(new Intent(this, SettingsActivity.class));
	        	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
	    }
    }
    
    private void doTabSetup()
    {
    	mPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabFragmentsAdapter(getSupportFragmentManager());

        mPager.setAdapter(mAdapter);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        
        View view = null;
        ImageView tabImg = null;
        TextView tabText = null;
        // Adding Tabs
        //1. Feed tab
        Tab feedTab = actionBar.newTab();
        view = LayoutInflater.from(this).inflate(R.layout.tabs_layout, null);
        tabImg = (ImageView) view.findViewById(R.id.tabIcon);
        tabText = (TextView) view.findViewById(R.id.tabText);
        
        tabText.setText(R.string.feed_tab);
        tabImg.setBackgroundResource(R.drawable.tab_icon_feeds_dark);
        feedTab.setCustomView(view);
        feedTab.setTabListener(this);
        actionBar.addTab(feedTab);
        
        //2. Latest Episodes tab        
        Tab latestTab = actionBar.newTab();
        
        view = LayoutInflater.from(this).inflate(R.layout.tabs_layout, null);
        tabImg = (ImageView) view.findViewById(R.id.tabIcon);
        tabText = (TextView) view.findViewById(R.id.tabText);
        
        tabText.setText(R.string.episodes_tab);
        tabImg.setBackgroundResource(R.drawable.tab_icon_latest_dark);
        latestTab.setCustomView(view);
        latestTab.setTabListener(this);
        actionBar.addTab(latestTab);
        
        //3. Player tab
        Tab playerTab = actionBar.newTab();
        
        view = LayoutInflater.from(this).inflate(R.layout.tabs_layout, null);
        tabImg = (ImageView) view.findViewById(R.id.tabIcon);
        tabText = (TextView) view.findViewById(R.id.tabText);
        
        tabText.setText(R.string.player_tab);
        tabImg.setBackgroundResource(R.drawable.tab_icon_player_dark);
        playerTab.setCustomView(view);
        playerTab.setTabListener(this);
        actionBar.addTab(playerTab);
        
        
    	 /**
         * on swiping the viewpager make respective tab selected
         * */
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

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
        mPager.setCurrentItem(tab.getPosition());
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
	
	public void setTab()
	{
		switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.SETTINGS_KEY_STARTTAB, "0")))
		{
		case 0:
			this.actionBar.setSelectedNavigationItem(0);
			break;
		case 1:
			this.actionBar.setSelectedNavigationItem(1);
			break;
		case 2:
			this.actionBar.setSelectedNavigationItem(2);
			break;
		}
	}
	
	@Override
	public void onNewIntent(Intent intent)
	{
	    super.onNewIntent(intent);
	    if (intent.getAction() != null && this != null)	//Make sure the Intent contains any data.
		{
		    if (intent.getAction().equals("navigate_player"))
		    {
		    	this.actionBar.setSelectedNavigationItem(2);	//Navigate to the Player tab.
		    }
		}
	}

	public void onBackPressed() 
	{
        if(mPager.getCurrentItem() == 0) 
        {
            if (mAdapter.getItem(0) instanceof FeedDetailsFragment) 
            {
            	((FeedDetailsFragment) mAdapter.getItem(0)).backPressed();
            }
            else 
            {
            	Intent startMain = new Intent(Intent.ACTION_MAIN);
            	startMain.addCategory(Intent.CATEGORY_HOME);
            	startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(startMain);
            }
        }
        else 
        {
        	Intent startMain = new Intent(Intent.ACTION_MAIN);
        	startMain.addCategory(Intent.CATEGORY_HOME);
        	startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(startMain);
        }
    }
}
