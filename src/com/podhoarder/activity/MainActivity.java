package com.podhoarder.activity;
 
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
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


public class MainActivity extends Activity implements OnNavigationListener, OnRefreshListener, OnQueryTextListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.MainActivity";
   
    //PODCAST HELPER
    public PodcastHelper mPodcastHelper;
    
    //FILTER
    private ListFilter mFilter;
    private ListFilter mPreviousFilter;
    
    //SEARCH
    private String mSearchString;
    private SearchView mSearchView;

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
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        
        final Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);
        

        // Create LayoutParams with width set to screen's width
        LayoutParams params = new LayoutParams(p.x, getActionBar().getHeight());
        mSearchView.setLayoutParams(params);
        mSearchView.setMaxWidth(p.x);
        mSearchView.setMinimumHeight(getActionBar().getHeight());
        
        mSearchView.setQueryHint(getString(R.string.search_hint));
	    mSearchView.setOnQueryTextListener(this);
	    
	    mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
	        @Override
	        public void onFocusChange(View view, boolean queryTextFocused) {
	            if(!queryTextFocused) 
	            {
	            	menu.findItem(R.id.action_search).collapseActionView();
	                mSearchString = "";
	                if (mPreviousFilter != null) 
	        		{
	        			setFilter(mPreviousFilter);
	        			mPreviousFilter = null;
	        		}
	            }
	        }
	    });
	    
	    SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)mSearchView.findViewById(R.id.search_src_text);
	    searchAutoComplete.setHintTextColor(Color.WHITE);
	    searchAutoComplete.setTextColor(Color.WHITE);
	    
	    View searchplate = (View)mSearchView.findViewById(R.id.search_plate);
	    searchplate.setBackgroundResource(R.drawable.abc_textfield_search_default_holo_dark);

	    ImageView searchCloseIcon = (ImageView)mSearchView.findViewById(R.id.search_close_btn);
	    searchCloseIcon.setImageResource(R.drawable.ic_action_remove);

	    ImageView voiceIcon = (ImageView)mSearchView.findViewById(R.id.search_voice_btn);
	    voiceIcon.setImageResource(R.drawable.abc_ic_voice_search);

	    ImageView searchIcon = (ImageView)mSearchView.findViewById(R.id.search_mag_icon);
	    searchIcon.setScaleType(ScaleType.CENTER_INSIDE);
	    searchIcon.setImageResource(R.drawable.ic_action_search);
	    
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
	        	openSearch();
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
    	{
    		closeSearch();
    		if (mPreviousFilter != null) 
    		{
    			setFilter(mPreviousFilter);
    			mPreviousFilter = null;
    		}
    	}
    	else
    		super.onBackPressed();
    }
    
    @Override
	public void onRefresh()
	{
		mPodcastHelper.refreshFeeds(mSwipeRefreshLayout);
    	//mSwipeLayout.setRefreshing(false);
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
		}
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
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.filters, android.R.layout.simple_spinner_item);
		list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		getActionBar().setListNavigationCallbacks(list, this);
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
	@Override
	public boolean onQueryTextChange(String arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onQueryTextSubmit(String arg0)
	{
		mPreviousFilter = mFilter;
		Log.i(LOG_TAG,"Search for: " + arg0);
		mSearchString = arg0;
		setFilter(ListFilter.SEARCH);
		
		
		return false;
	}
	
	private void openSearch()
	{
    	mSearchView.setIconifiedByDefault(false);
    	mSearchView.requestFocus();
	    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //Toggle the soft keyboard to let the user search instantly.
	    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
	}
	
	private void closeSearch()
	{
		InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

	    // check if no view has focus:
	    View view = this.getCurrentFocus();
	    if (view != null) {
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	
	//FILTERING
	public enum ListFilter 
	{ 
		ALL, NEW, LATEST, DOWNLOADED, FAVORITES, SEARCH, FEED, ;
		
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
