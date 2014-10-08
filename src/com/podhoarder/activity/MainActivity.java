package com.podhoarder.activity;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.telephony.TelephonyManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;

import com.faizmalkani.FloatingActionButton;
import com.podhoarder.listener.EpisodeMultiChoiceModeListener;
import com.podhoarder.listener.GridActionModeCallback;
import com.podhoarder.object.Episode;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.service.PodHoarderService.PodHoarderBinder;
import com.podhoarder.util.Constants;
import com.podhoarder.util.HardwareIntentReceiver;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ToastMessages;
import com.podhoarder.view.CustomSwipeRefreshLayout;
import com.podhoarder.view.FloatingPlayPauseButton;
import com.podhoarderproject.podhoarder.R;


public class MainActivity extends Activity implements OnNavigationListener, OnRefreshListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.MainActivity";
   
    //PODCAST HELPER
    public PodcastHelper mPodcastHelper;
    
    //FILTER
    private ArrayList<CharSequence> mFilters;
    private ArrayAdapter<CharSequence> mFiltersAdapter;
    private ListFilter mFilter;
    private ListFilter mPreviousFilter;
    
    //SEARCH
    private String mSearchString;

	//UI ELEMENTS
    private FloatingPlayPauseButton mFAB;
	//Episodes List
    private ListView mListView;
    private EpisodeMultiChoiceModeListener mListSelectionListener;
    //Feeds Grid
    private GridView mGridView;
    private GridActionModeCallback mActionModeCallback;  
	private ActionMode mActionMode;  
	//SwipeRefreshLayout
	private CustomSwipeRefreshLayout mSwipeRefreshLayout;
	
	//HARDWARE INTENT RECEIVER
	private HardwareIntentReceiver hardwareIntentReceiver;
    
	//PLAYBACK SERVICE
    private PodHoarderService mPlaybackService;
    private Intent mPlayIntent;
	private boolean mIsMusicBound = false;
	
	private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener ()
	{  
		  @Override
		  public void onScrollStateChanged(AbsListView view, int scrollState) {

		  }

		  @Override
		  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) 
		  {
			boolean enable = false;
			if (mFilter == ListFilter.ALL && mFilter.mFeedId == 0)
			{
				if(mGridView != null && mGridView.getChildCount() > 0){
		            // check if the first item of the list is visible
		            boolean firstItemVisible = mGridView.getFirstVisiblePosition() == 0;
		            // check if the top of the first item is visible
		            boolean topOfFirstItemVisible = mGridView.getChildAt(0).getTop() <= 6;	//We set 6 instead of 0 here because of grid padding etc.
		            // enabling or disabling the refresh layout
		            enable = firstItemVisible && topOfFirstItemVisible;
		        }
			} 
			else
			{
				if(mListView != null && mListView.getChildCount() > 0){
		            // check if the first item of the list is visible
		            boolean firstItemVisible = mListView.getFirstVisiblePosition() == 0;
		            // check if the top of the first item is visible
		            boolean topOfFirstItemVisible = mListView.getChildAt(0).getTop() <= 12;	//We set 12 instead of 0 here because of list padding etc.
		            // enabling or disabling the refresh layout
		            enable = firstItemVisible && topOfFirstItemVisible;
		        }
			}

			mSwipeRefreshLayout.setEnabled(enable);
		  }

	};
	
	private ServiceConnection podConnection = new ServiceConnection()	//connect to the service
    { 
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service) 
	    {
		    PodHoarderBinder binder = (PodHoarderBinder)service;
		    //get service
		    mPlaybackService = binder.getService();
		    //pass list
		    mPlaybackService.setList(mPodcastHelper.mPlaylistAdapter.mPlayList);
		    mIsMusicBound = true;
		    
		    //Initialise the headphone jack listener / intent receiver.
		    IntentFilter headsetFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		    IntentFilter callStateFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		    IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);  
	        hardwareIntentReceiver = new HardwareIntentReceiver(mPlaybackService, mPodcastHelper);
	        registerReceiver(hardwareIntentReceiver, headsetFilter);
	        registerReceiver(hardwareIntentReceiver, callStateFilter);
	        registerReceiver(hardwareIntentReceiver, connectivityFilter);
	        populate();
	        
	        //TODO: Show player button here. The Service is now completely bound and safe to use.
	        mFAB = (FloatingPlayPauseButton) findViewById(R.id.fabbutton);
	        mPlaybackService.setMembers(mPodcastHelper, mFAB);
	        mFAB.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					if (mPlaybackService.isPlaying())
						mPlaybackService.pause();
					else
						mPlaybackService.play();
				}
			});
	    }
	    
	    @Override
	    public void onServiceDisconnected(ComponentName name) 
	    {
	    	unregisterReceiver(hardwareIntentReceiver);
	    	mIsMusicBound = false;
	    }
    };   
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {	
    	super.onCreate(savedInstanceState);
        // Initialisation
        setContentView(R.layout.activity_main);
        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        setupActionBar();
        loadFilter();
        mSearchString = "";
        mPodcastHelper = new PodcastHelper(this);
        
        if (!this.mIsMusicBound)	//If the service isn't bound, we need to start and bind it.
    	{
    		if(mPlayIntent==null)
    		{
    			mPlayIntent = new Intent(this, PodHoarderService.class);
    			this.mIsMusicBound = this.bindService(mPlayIntent, podConnection, Context.BIND_AUTO_CREATE);
    			this.startService(mPlayIntent);
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
    	this.stopService(mPlayIntent);
    	unregisterReceiver(hardwareIntentReceiver);
	    this.mPlaybackService=null;
	    this.mIsMusicBound = false;
	    mPodcastHelper.closeDbIfOpen();
	    super.onDestroy();
    }
    
    @Override
    protected void onPause()
    {    	
    	super.onPause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch (item.getItemId()) 
    	{
    		case android.R.id.home:
    			setFilter(ListFilter.ALL);
    			return true;
	        case R.id.action_settings:
	        	startActivity(new Intent(this, SettingsActivity.class));
	        	return true;
	        case R.id.action_add:
				//TODO: Go to Add Feed Activity
				return true;
	        case R.id.action_search:
	        	//Search
	        	onSearchRequested();
	        	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
	    }
    }

    @Override
	public boolean onNavigationItemSelected(int pos, long itemId)
	{
		setFilter(ListFilter.values()[pos]);
		return true;
	}
    
    @Override
    public void onBackPressed()
    {
    	if (mFilter == ListFilter.FEED)
    		setFilter(ListFilter.ALL);
    	else if (mFilter == ListFilter.SEARCH)
    		if (mPreviousFilter != null)
    			setFilter(mPreviousFilter);
    		else
    			setFilter(ListFilter.ALL);
    	else
    		super.onBackPressed();
    }
    
    @Override
	public void onNewIntent(Intent intent)
	{
	    super.onNewIntent(intent);
	    if (intent.getAction() != null && this != null)	//Make sure the Intent contains any data.
		{
		    if (intent.getAction().equals("navigate_player"))
		    {
		    	//TODO: Open player activity.
		    }
		    else if (intent.getAction().equals(Intent.ACTION_SEARCH))
		    {
		    	mSearchString = intent.getStringExtra(SearchManager.QUERY);
		    	mPreviousFilter = mFilter;
		    	setFilter(ListFilter.SEARCH);
		    }
		}
	}
    
    @Override
	public void onRefresh()
	{
		mPodcastHelper.refreshFeeds(mSwipeRefreshLayout);
	}
    
	public PodHoarderService getPlaybackService()
	{
		return mPlaybackService;
	}

	public FloatingActionButton getFAB()
	{
		return mFAB;
	}

	public boolean isMusicBound()
	{
		return mIsMusicBound;
	}
	
	private void populate()
	{
		if (mFilter == ListFilter.ALL)
		{
			setupGridView();
			setupListView();
			mGridView.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}
		else
		{
			setupListView();
			setupGridView();
			mGridView.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
	}
	
	private void setupActionBar()
	{
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		String[] mFilterStrings = getResources().getStringArray(R.array.filters);
		mFilters = new ArrayList<CharSequence>();
		mFilters.addAll(Arrays.asList(mFilterStrings));
		mFiltersAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, mFilters);
		//ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.filters, android.R.layout.simple_spinner_item);
		mFiltersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		getActionBar().setListNavigationCallbacks(mFiltersAdapter, this);
	}
	
	private void setupListView()
	{
		mListView = (ListView) findViewById(R.id.episodesListView);
		if (!this.mPodcastHelper.mEpisodesListAdapter.isEmpty())
		{
			this.mListView.setAdapter(this.mPodcastHelper.mEpisodesListAdapter);
			this.mListView.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> adapterview, View v, int pos, long id)
				{
					Episode currentEp = (Episode) mListView.getItemAtPosition(pos);
					if (currentEp.isDownloaded() || NetworkUtils.isOnline(getApplication()))
						mPlaybackService.playEpisode((Episode) mListView.getItemAtPosition(pos));
					else
						ToastMessages.PlaybackFailed(getApplication());
				}

			});

			this.mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			this.mListSelectionListener = new EpisodeMultiChoiceModeListener(this, this.mListView);
			this.mListView.setMultiChoiceModeListener(this.mListSelectionListener);
			this.mListView.setOnScrollListener(mScrollListener);
		} 
		else
		{
			// TODO: Show some kind of "list is empty" text instead of the mainlistview here.
		}
	}

	private void setupGridView()
    {
		mGridView = (GridView) findViewById(R.id.feedsGridView);
    	if (!this.mPodcastHelper.mFeedsGridAdapter.isEmpty())
    	{
    		mPodcastHelper.mFeedsGridAdapter.setLoadingViews(setupLoadingViews());
    		mGridView.setAdapter(mPodcastHelper.mFeedsGridAdapter);
    		
    		mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        	mActionModeCallback = new GridActionModeCallback(this, mGridView);
        	mGridView.setOnItemLongClickListener(new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id)
				{
					if (mActionMode == null || !mActionModeCallback.isActive())
					{
						mActionMode = startActionMode(mActionModeCallback);
						mPodcastHelper.mFeedsGridAdapter.setActionModeVars(mActionMode, mActionModeCallback);
					}
					mActionModeCallback.onItemCheckedStateChanged(pos, !((CheckBox)v.findViewById(R.id.feeds_grid_item_checkmark)).isChecked());
			        return true;  
				}
			});
        	mGridView.setOnScrollListener(mScrollListener);
    	}
    	else
    	{
    		//Grid is empty.
    		//TODO: Add a hint and a link to the add feed fragment.
    		
    		
    	}
    	
    }
	
	private List<View> setupLoadingViews()
    {
    	List<View> views = new ArrayList<View>();	//This is an ugly solution but in order to use the GridViews LayoutParams the loading views must be inflated here.
    	LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);	//get the inflater service.
    	for (int i=0; i<Constants.NEW_SEARCH_RESULT_LIMIT; i++)	//Inflate a collection of Loading views, same size as the maximum amount Search Results.
    	{
        	views.add(inflater.inflate(R.layout.fragment_feeds_grid_loading_feed_item, mGridView, false));	//Inflate the "loading" grid item to show while data is downloaded
    	}
    	return views;
    }

	public PodcastHelper getPodcastHelper()
	{
		return mPodcastHelper;
	}

	public String getSearchString()
	{
		return mSearchString;
	}
	
	//SEARCHING

	
	//FILTERING
	public enum ListFilter 
	{ 
		ALL, NEW, LATEST, DOWNLOADED, FAVORITES, FEED, SEARCH;
		
		public int getFeedId()
		{
			return mFeedId;
		}
		public void setFeedId(int feedId)
		{
			this.mFeedId = feedId;
		}
		private int mFeedId = 0;
	}
	private void loadFilter()
	{
		int lastFilter = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.SETTINGS_KEY_LASTFILTER,"0"));
		mFilter = ListFilter.values()[lastFilter];
	}
	public ListFilter getFilter()
	{
		return mFilter;
	}
	public void setFilter(ListFilter filterToSet)
	{
		if (mFilter == ListFilter.ALL && filterToSet != ListFilter.ALL)
		{
			mGridView.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
		else if (mFilter != ListFilter.ALL && filterToSet == ListFilter.ALL)
		{		
			mListView.setVisibility(View.GONE);
			mGridView.setVisibility(View.VISIBLE);
		}
		if (filterToSet == ListFilter.FEED)
			getActionBar().setDisplayHomeAsUpEnabled(true);
		else
			getActionBar().setDisplayHomeAsUpEnabled(false);
		
		if (mFilter != ListFilter.SEARCH && filterToSet == ListFilter.SEARCH)
		{
	    	//Set the title to Search Results in the action bar when presenting the results from a search query.
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getActionBar().setDisplayShowTitleEnabled(true);
			setTitle(getString(R.string.search_results));
		}
		else if (mFilter == ListFilter.SEARCH && filterToSet != ListFilter.SEARCH)
		{
			//Reset the actionbar to show a navigation list.
			setTitle(getString(R.string.app_name));
			getActionBar().setDisplayShowTitleEnabled(false);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		}
		
		this.mFilter = filterToSet;
		mPodcastHelper.switchLists();
	}	


	//HELPER METHODS
	public void deletingEpisode(int episodeId)
	{
		this.mPlaybackService.deletingEpisode(episodeId);
	}
	public void downloadEpisode(Episode ep)
    {
    	this.mPodcastHelper.downloadEpisode(ep);
    }

	public void startEpisodeDetailsActivity(int episodeId)
	{
		Intent intent = new Intent(MainActivity.this, EpisodeActivity.class);
		Bundle b = new Bundle();
		b.putInt("id", episodeId); //Your id
		intent.putExtras(b); //Put your id to your next Intent
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
	}


}
