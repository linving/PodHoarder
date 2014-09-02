package com.podhoarder.activity;
 
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.podhoarder.adapter.TabFragmentsAdapter;
import com.podhoarder.component.PullRefreshLayout;
import com.podhoarder.fragment.FeedDetailsFragment;
import com.podhoarder.fragment.FeedFragment;
import com.podhoarder.fragment.LatestEpisodesFragment;
import com.podhoarder.fragment.SearchFragment;
import com.podhoarder.object.Episode;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.service.PodHoarderService.PodHoarderBinder;
import com.podhoarder.util.Constants;
import com.podhoarder.util.HardwareIntentReceiver;
import com.podhoarder.util.PodcastHelper;
import com.podhoarderproject.podhoarder.R;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener, OnRefreshListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.MainActivity";
	//UI Elements
	private CirclePageIndicator mTabIndicator;
    private ViewPager mPager;
    public TabFragmentsAdapter mAdapter;
    public ActionBar actionBar;
    
    //Podcast Helper
    public PodcastHelper helper;
    
    //Refresh Layout
    private PullRefreshLayout swipeRefreshLayout;
    //Playback service
    public PodHoarderService podService;
	
	private Intent playIntent;
	private boolean musicBound = false;
	
	//Receiver for music channel intents (headphones unplugged etc.)
	private HardwareIntentReceiver hardwareIntentReceiver;
    
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
		    IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);  
	        hardwareIntentReceiver = new HardwareIntentReceiver(podService);
	        registerReceiver(hardwareIntentReceiver, headsetFilter);
	        registerReceiver(hardwareIntentReceiver, callStateFilter);
	        registerReceiver(hardwareIntentReceiver, connectivityFilter);
	        fragmentSetup();
	    }
	    
	    @Override
	    public void onServiceDisconnected(ComponentName name) 
	    {
	    	unregisterReceiver(hardwareIntentReceiver);
	    	musicBound = false;
	    }
    };   
    

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {	
    	super.onCreate(savedInstanceState);
        // Initialisation
        setupActionBar();
        setContentView(R.layout.activity_main);
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
    
    private void fragmentSetup()
    {
        doTabSetup();
        setupRefreshControls();
	    setInitialTab();
    }
    
    private void setupActionBar()
    {
    	int titleId = 0;

        titleId = getResources().getIdentifier("action_bar_title", "id", "android");

        // Final check for non-zero invalid id
        if (titleId > 0)
        {
            TextView titleTextView = (TextView) findViewById(titleId);

            DisplayMetrics metrics = getResources().getDisplayMetrics();

            // Fetch layout parameters of titleTextView (LinearLayout.LayoutParams : Info from HierarchyViewer)
            LinearLayout.LayoutParams txvPars = (LayoutParams) titleTextView.getLayoutParams();
            txvPars.gravity = Gravity.CENTER_HORIZONTAL;
            txvPars.width = metrics.widthPixels;
            titleTextView.setLayoutParams(txvPars);

            titleTextView.setGravity(Gravity.CENTER);
        }
//    	this.getActionBar().setDisplayShowCustomEnabled(true);
//    	this.getActionBar().setDisplayShowTitleEnabled(false);
//    	
//    	LayoutInflater inflater = LayoutInflater.from(this);
//    	View v = inflater.inflate(R.layout.actionbar, null);
//    	
//    	TextView titleTextView = ((TextView)v.findViewById(R.id.actionbar_title));
//        
//        titleTextView.setAllCaps(true);
//        titleTextView.setTextColor(getResources().getColor(R.color.actionbar_title));
//        titleTextView.setTextSize(getResources().getDimension(R.dimen.default_title_indicator_text_size));
//        titleTextView.setText(getString(R.string.app_name));
//
//        // Fetch layout parameters of titleTextView (LinearLayout.LayoutParams : Info from HierarchyViewer)
//        RelativeLayout.LayoutParams txvPars = (LayoutParams) titleTextView.getLayoutParams();
//        txvPars.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        titleTextView.setLayoutParams(txvPars);        
//        this.getActionBar().setCustomView(v);
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
    	unregisterReceiver(hardwareIntentReceiver);
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
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        //Bind the title indicator to the adapter
        mTabIndicator = (CirclePageIndicator)findViewById(R.id.tabIndicator);
        mTabIndicator.setViewPager(mPager);
        
        // Adding Tabs
        for (int i=0; i<Constants.TAB_COUNT; i++)
        {
        	switch (i)
        	{
	        	//1. Player tab    
	    		case Constants.PLAYER_TAB_POSITION:
	    	        Tab playerTab = actionBar.newTab();
	    	        playerTab.setTabListener(this);
	    	        playerTab.setCustomView(null);
	    	        actionBar.addTab(playerTab);
	    	        break;
	            //2. Latest Episodes tab    
        		case Constants.LATEST_TAB_POSITION:   
        	        Tab latestTab = actionBar.newTab();
        	        latestTab.setTabListener(this);
        	        latestTab.setCustomView(null);
        	        actionBar.addTab(latestTab);
        	        break;
        	    //3. Feed tab
        		case Constants.FEEDS_TAB_POSITION:
	                Tab feedTab = actionBar.newTab();
	                feedTab.setTabListener(this);
	                feedTab.setCustomView(null);
	                actionBar.addTab(feedTab);
	                break;
        		case Constants.BONUS_TAB_POSITION:
        			//4. Feed Details tab
	                Tab feedDetailsTab = actionBar.newTab();
	                feedDetailsTab.setTabListener(this);
	                feedDetailsTab.setCustomView(null);
	                actionBar.addTab(feedDetailsTab);
	                
	                //5. Search tab
	                Tab searchTab = actionBar.newTab();
	                searchTab.setTabListener(this);
	                searchTab.setCustomView(null);
	                actionBar.addTab(searchTab);
	                break;     
	           
        		
        	}
        }
        
        mTabIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() 
        {

            @Override
            public void onPageSelected(int position) 
            {
                for (int i : adjacentFragmentIndexes(position))
                {
                	finishActionModeIfActive(i);
                }
                if (position < 3)	//Disables the rightmost page once the user leaves it. 	
                {
                	mAdapter.setDetailsPageEnabled(false);
                	mAdapter.setSearchPageEnabled(false);
                }
                setTab(position);
            }

            
            
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) { }

            @Override
            public void onPageScrollStateChanged(int arg0) {  }
        });
    }
    
    private void setupRefreshControls()
    {
    	this.swipeRefreshLayout = (PullRefreshLayout) this.findViewById(R.id.swipe_container);
    	this.swipeRefreshLayout.setOnRefreshListener(this);
    	this.swipeRefreshLayout.setColorScheme(R.color.app_blue_accent, R.color.app_background, R.color.app_blue_accent, R.color.app_background);
    }


    public void downloadEpisode(Episode ep)
    {
    	this.helper.downloadEpisode(ep);
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
	
	public void setInitialTab()
	{
		switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.SETTINGS_KEY_STARTTAB, ""+Constants.FEEDS_TAB_POSITION)))
		{
			case Constants.FEEDS_TAB_POSITION:
                setTab(Constants.FEEDS_TAB_POSITION);
				break;
			case Constants.LATEST_TAB_POSITION:
                setTab(Constants.LATEST_TAB_POSITION);
				break;
			case Constants.PLAYER_TAB_POSITION:
                setTab(Constants.PLAYER_TAB_POSITION);
				break;
		}
	}
	
	@Override
	public void onNewIntent(Intent intent)
	{
	    super.onNewIntent(intent);
	    if (intent.getAction() != null && this != null)	//Make sure the Intent contains any data.
		{
		    if (intent.getAction().equals("navigate_feeds"))
		    {
		    	setTab(Constants.FEEDS_TAB_POSITION);	//Navigate to the Player tab.
		    }
		    else if (intent.getAction().equals("navigate_latest"))
		    {
		    	setTab(Constants.LATEST_TAB_POSITION);	//Navigate to the Latest Episodes tab.
		    }
		    else if (intent.getAction().equals("navigate_player"))
		    {
		    	setTab(Constants.PLAYER_TAB_POSITION);	//Navigate to the Latest Episodes tab.
		    }
		}
	}
	
	/**
	 * Convenience method for finding adjacent fragments around a given index. 
	 * @param currentIndex Index of the Fragment you want to find adjacent indexes for.
	 * @return	A List<Integer> containing the adjacent fragment positions.
	 */
	private List<Integer> adjacentFragmentIndexes(int currentIndex)
	{
		List<Integer> indexes = new ArrayList<Integer>();
		int before = currentIndex - 1;
		int after = currentIndex + 1;
		if (before > -1)
		{
			indexes.add(before);
		}
		if (after < Constants.TAB_COUNT)
		{
			indexes.add(after);
		}
		return indexes;
	}
	
	/**
	 * A really bad solution for checking if the Fragment at fragmentPos currently has an active ActionMode going, and finishing it if true.
	 * @param fragmentPos Position of the fragment to check for ActionMode.
	 */
	private void finishActionModeIfActive(int fragmentPos)
	{
		switch (fragmentPos)
		{
			case Constants.LATEST_TAB_POSITION:
				LatestEpisodesFragment latestEpisodesFragment = ((LatestEpisodesFragment)mAdapter.getItem(Constants.LATEST_TAB_POSITION));
            	if (latestEpisodesFragment != null && latestEpisodesFragment.getListSelectionListener() != null && latestEpisodesFragment.getListSelectionListener().getActionMode() != null)
            	{
            		if (latestEpisodesFragment.getListSelectionListener().isActive())
                	{
            			latestEpisodesFragment.getListSelectionListener().getActionMode().finish();
                	}
            	}
            	break;
			case Constants.FEEDS_TAB_POSITION:
				FeedFragment feedsFragment = ((FeedFragment)mAdapter.getItem(Constants.FEEDS_TAB_POSITION));
            	if (feedsFragment != null && feedsFragment.getActionModeCallback() != null && feedsFragment.getActionMode() != null)
            	{
            		if (feedsFragment.getActionModeCallback().isActive())
                	{
                		feedsFragment.getActionModeCallback().getActionMode().finish();
                	}
            	}
            	break;
			case Constants.BONUS_TAB_POSITION:
				Fragment fragment = mAdapter.getItem(Constants.BONUS_TAB_POSITION);
				if(fragment instanceof FeedDetailsFragment )
				{
					FeedDetailsFragment feedDetailsFragment = (FeedDetailsFragment) fragment;
					if (feedDetailsFragment != null && feedDetailsFragment.getListSelectionListener() != null && feedDetailsFragment.getListSelectionListener().getActionMode() != null)
	            	{
	            		if (feedDetailsFragment.getListSelectionListener().isActive())
	                	{
	            			feedDetailsFragment.getListSelectionListener().getActionMode().finish();
	                	}
	            	}
				}
				else if ( fragment instanceof SearchFragment) 
				{
					SearchFragment searchFragment = (SearchFragment) fragment;
					if (searchFragment != null && searchFragment.getListSelectionListener() != null && searchFragment.getListSelectionListener().getActionMode() != null)
	            	{
	            		if (searchFragment.getListSelectionListener().isActive())
	                	{
	            			searchFragment.getListSelectionListener().getActionMode().finish();
	                	}
	            	}
				}
            	
            	break;
		}
	}

	public void onBackPressed() 
	{
        if(mPager.getCurrentItem() == Constants.BONUS_TAB_POSITION) 
        {
        	setTab(Constants.FEEDS_TAB_POSITION);
        }
        else 
        {
        	Intent startMain = new Intent(Intent.ACTION_MAIN);
        	startMain.addCategory(Intent.CATEGORY_HOME);
        	startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(startMain);
        }
    }
	
	/**
     * Navigates to the desired tab position. Use Constants for reliable values.
     * @param pos Position of the tab to navigate to.
     */
    public void setTab(int pos) 
    {
    	this.mPager.setCurrentItem(pos);
    }
    
    @Override
	public void onRefresh()
	{
		this.helper.setRefreshLayout(this.swipeRefreshLayout);	//Set the layout that should be updated once the Refresh task is done executing.
		this.helper.refreshFeeds();	//Start the refresh process.
	}
    
    public void disableRefresh()
    {
    	this.swipeRefreshLayout.setEnabled(false);
    }
    
    public void enableRefresh()
    {
    	this.swipeRefreshLayout.setEnabled(true);
    }
}
