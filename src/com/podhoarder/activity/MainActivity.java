package com.podhoarder.activity;

import android.app.ActionBar.OnNavigationListener;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.faizmalkani.FloatingActionButton;
import com.podhoarder.listener.EpisodeMultiChoiceModeListener;
import com.podhoarder.listener.GridActionModeCallback;
import com.podhoarder.object.Episode;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.service.PodHoarderService.PlayerState;
import com.podhoarder.service.PodHoarderService.PodHoarderBinder;
import com.podhoarder.util.Constants;
import com.podhoarder.util.HardwareIntentReceiver;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ToastMessages;
import com.podhoarder.view.FloatingPlayPauseButton;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements OnNavigationListener, OnRefreshListener, PodHoarderService.StateChangedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarder.activity.MainActivity";
   
    //PODCAST HELPER
    public PodcastHelper mPodcastHelper;
    
    //FILTER
    private ArrayList<CharSequence> mFilters;
    private ArrayAdapter<CharSequence> mFiltersAdapter;
    private ListFilter mFilter;
    private ListFilter mPreviousFilter;
    
    //SEARCH
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;
    private boolean mSearchEnabled = false;

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
	private SwipeRefreshLayout mSwipeRefreshLayout;
	
	//ACTIVITY RESULT
	static final int ADD_PODCAST_REQUEST = 1;
    static final int SETTINGS_REQUEST = 2;
	
	//HARDWARE INTENT RECEIVER
	private HardwareIntentReceiver hardwareIntentReceiver;
    
	//PLAYBACK SERVICE
    private PodHoarderService mPlaybackService;
    private Intent mPlayIntent;
	private boolean mIsMusicBound = false;


    //ANON CLASSES
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
		            boolean topOfFirstItemVisible = mGridView.getChildAt(0).getTop() > 0;
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
		            boolean topOfFirstItemVisible = mListView.getChildAt(0).getTop() > 0;
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
		    //mPlaybackService.setList(mPodcastHelper.mPlaylistAdapter.mPlayList);
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
	        mPlaybackService.setHelper(mPodcastHelper);
	        mPlaybackService.setStateChangedListener(MainActivity.this);
	        //TODO: Show player button here. The Service is now completely bound and safe to use.
	        setupFAB();
	    }
	    
	    @Override
	    public void onServiceDisconnected(ComponentName name) 
	    {
	    	unregisterReceiver(hardwareIntentReceiver);
	    	mIsMusicBound = false;
	    }
    };   


    //ACTIVITY OVERRIDES
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {	
    	super.onCreate(savedInstanceState);

        // Initialisation
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.bringToFront();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.windowBackground, R.color.colorAccent, R.color.windowBackground);
        loadFilter();
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
        handleIntent(getIntent());
    }
    @Override
    protected void onStart()
    {
    	super.onStart();

    }
    @Override
    public void onResume()
    {
    	if (mPlaybackService != null && mFAB != null)
    	{
    		mFAB.setPlaying(mPlaybackService.isPlaying());
            mPlaybackService.setStateChangedListener(MainActivity.this);
    	}
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
	public void onStateChanged(PlayerState newPlayerState)
	{
    	Log.i(LOG_TAG, "New player state: " + newPlayerState);
		switch (newPlayerState)
		{
			case PLAYING:
				mFAB.setPlaying(true);
				break;
			case PAUSED:
				mFAB.setPlaying(false);
				break;
			case LOADING:
				break;
		}
	}
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        if (null != mSearchView )
        {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        setupActionBar();
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        if (!super.onOptionsItemSelected(item)) {   //If BaseActivity.onOptionsItemSelected returns true, that means the event was handled there and this activity shouldn't do anything.
            switch (item.getItemId())
            {
                case android.R.id.home:
                    setFilter(ListFilter.ALL);
                    return true;
                case R.id.action_settings:
                    startSettingsActivity();
                    return true;
                case R.id.action_add:
                    startAddActivity();
                    return true;
                case R.id.action_search:
                    //Search
                    return true;
                default:
                    return false;
            }
        }
        else
            return false;

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
        if (mSearchEnabled)
            cancelSearch();
        else {
            if (mFilter == ListFilter.FEED)
                setFilter(ListFilter.ALL);
            else
                super.onBackPressed();
        }
    }
    @Override
	public void onNewIntent(Intent intent)
	{
        setIntent(intent);
        handleIntent(intent);
	}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        switch (requestCode) {
            case ADD_PODCAST_REQUEST:
                // Make sure the request was successful
                if (resultCode == RESULT_OK)
                {
                    // The user picked a contact.
                    // The Intent's data Uri identifies which contact was selected.
                    @SuppressWarnings("unchecked")
                    List<SearchResultRow> results = (List<SearchResultRow>) data.getExtras().getSerializable(AddActivity.INTENT_RESULTS_ID);
                    for (SearchResultRow row : results) //Load all the XML files back into memory from the cache directory.
                        row.loadXML();
                    mPodcastHelper.addSearchResults(results);
                }
                break;
            case SETTINGS_REQUEST:
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    if (data.getExtras().getBoolean(SettingsActivity.INTENT_RESULTS_ID)) {  //If any of the preferences were changed we'll redraw the Grid. (to reflect the UI changes)
                        mPodcastHelper.mFeedsGridAdapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }
    @Override
	public void onRefresh()
	{
		mPodcastHelper.refreshFeeds(mSwipeRefreshLayout);
	}
    @Override
    public boolean onQueryTextChange(String str)
    {
        return false;
    }
    @Override
    public boolean onQueryTextSubmit(String str)
    {
        doSearch(str);
        return false;
    }
    @Override
    public boolean onClose() {
        cancelSearch();
        return true;
    }

    //INTENT HANDLING
    private void handleIntent(Intent intent) {
        Log.i(LOG_TAG,"Received intent!");
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }


    //VIEW SETUPS
    private void setupActionBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_library));
/*        getSupportActionBar().setDisplayShowTitleEnabled(true);
        //getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        String[] mFilterStrings = getResources().getStringArray(R.array.filters);
        mFilters = new ArrayList<CharSequence>();
        mFilters.addAll(Arrays.asList(mFilterStrings));
        mFiltersAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, mFilters);
        //ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.filters, android.R.layout.simple_spinner_item);
        mFiltersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getActionBar().setListNavigationCallbacks(mFiltersAdapter, this);*/
    }
	private void setupListView()
	{
		mListView = (ListView) findViewById(R.id.episodesListView);
		if (this.mPodcastHelper.hasPodcasts())
		{
			Animation animation = AnimationUtils.loadAnimation( this, R.anim.grid_fade_in);
    		LayoutAnimationController animationController = new LayoutAnimationController(animation, 0.2f);
    		
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

			this.mListView.setLayoutAnimation(animationController);
			
			this.mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			this.mListSelectionListener = new EpisodeMultiChoiceModeListener(this, this.mListView);
			this.mListView.setMultiChoiceModeListener(this.mListSelectionListener);
			this.mListView.setOnScrollListener(mScrollListener);
		} 
		else
		{
            //List is empty. So we show the "Click to add your first podcast" string.
            setupEmptyText();
		}
	}
	private void setupGridView()
    {
		
		mGridView = (GridView) findViewById(R.id.feedsGridView);
    	if (this.mPodcastHelper.hasPodcasts())
    	{
    		
    		Animation animation = AnimationUtils.loadAnimation( this, R.anim.grid_fade_in);
    		GridLayoutAnimationController animationController = new GridLayoutAnimationController(animation, 0.15f, 0.45f);
    		
    		mPodcastHelper.mFeedsGridAdapter.setLoadingViews(setupLoadingViews());
    		mGridView.setAdapter(mPodcastHelper.mFeedsGridAdapter);
    		
    		mGridView.setLayoutAnimation(animationController);
    		
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
            //Grid is empty. So we show the "Click to add your first podcast" string.
            setupEmptyText();
    	}
    	
    }
    private List<View> setupLoadingViews()
    {
        List<View> views = new ArrayList<View>();	//This is an ugly solution but in order to use the GridViews LayoutParams the loading views must be inflated here.
        LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);	//get the inflater service.
        for (int i=0; i<Constants.NEW_SEARCH_RESULT_LIMIT; i++)	//Inflate a collection of Loading views, same size as the maximum amount Search Results.
        {
            views.add(inflater.inflate(R.layout.feeds_grid_loading_item, mGridView, false));	//Inflate the "loading" grid item to show while data is downloaded
        }
        return views;
    }
    private void setupFAB()
    {
        mFAB = (FloatingPlayPauseButton) findViewById(R.id.fabbutton);

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
        mFAB.setOnLongClickListener( new OnLongClickListener()
        {

            @Override
            public boolean onLongClick(View v)
            {
                startPlayerActivity();
                return false;
            }
        });
        mFAB.setPlaying(mPlaybackService.isPlaying());

        if (!mPodcastHelper.hasPodcasts()) mFAB.setVisibility(View.GONE);
        else mFAB.setVisibility(View.VISIBLE);
    }
    private void setupEmptyText()
    {
        TextView mEmptyText = (TextView) findViewById(R.id.emptyLibraryString);
        if (mEmptyText.getVisibility() != View.VISIBLE) {
            mEmptyText.setVisibility(View.VISIBLE);
        }
    }

	//FILTERING
	public enum ListFilter 
	{ 
		ALL, NEW, DOWNLOADED, FAVORITES, FEED;
		
		public int getFeedId()
		{
			return mFeedId;
		}
		public void setFeedId(int feedId)
		{
			this.mFeedId = feedId;
		}
        public String getSearchString() {return searchString;}
        public void setSearchString(String searchString) {this.searchString = searchString;}
		private int mFeedId = 0;
        private String searchString = "";
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
        if (!filterToSet.getSearchString().isEmpty() || filterToSet != ListFilter.ALL ) {
            if (mFilter == ListFilter.ALL){ //If the current Filter is ALL, then the grid is showing and we need to toggle List/Grid view visibility properties.
                mGridView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                mListView.startLayoutAnimation();
            }
		}
		else {
            if (filterToSet == ListFilter.ALL && filterToSet.getSearchString().isEmpty() && mGridView.getVisibility() != View.VISIBLE) {
                mListView.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
                mGridView.startLayoutAnimation();
            }

		}
		
		this.mFilter = filterToSet;
		mPodcastHelper.switchLists();
	}	

    //SEARCHING
    private void doSearch(String searchString)
    {
        mSearchEnabled = true;
        mFilter.setSearchString(searchString.trim());
        setFilter(mFilter);
    }

    private void cancelSearch()
    {
        mSearchView.onActionViewCollapsed();
        mFilter.setSearchString("");
        setFilter(mFilter);
        mSearchEnabled = false;
    }


    //NAVIGATION
	public void startEpisodeDetailsActivity(Episode currentEp)
	{
		//Animation slideOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
		//findViewById(R.id.root).startAnimation(slideOutAnim);
		Intent intent = new Intent(MainActivity.this, EpisodeActivity.class);
		Bundle b = new Bundle();
		b.putInt("id", currentEp.getEpisodeId()); //Your id
		b.putString("title", currentEp.getTitle());
        b.putString("timeStamp", currentEp.getPubDate());
		b.putString("description", currentEp.getDescription());
		intent.putExtras(b); //Put your id to your next Intent
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
	}
	public void startPlayerActivity()
	{
		Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.player_fade_in , R.anim.activity_stay_transition);
	}
	public void startAddActivity()
	{
		Intent intent = new Intent(MainActivity.this, AddActivity.class);
		startActivityForResult(intent, ADD_PODCAST_REQUEST);
		overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
	}
    public void startSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_REQUEST);
    }


    //MISC HELPER METHODS
    public void deletingEpisode(int episodeId)
    {
        this.mPlaybackService.deletingEpisode(episodeId);
    }
    public void downloadEpisode(Episode ep)
    {
        this.mPodcastHelper.getDownloadManager().downloadEpisode(ep);
    }
    private void populate()
    {
        if (mFilter == ListFilter.ALL)
        {
            setupGridView();
            setupListView();
            mGridView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            mGridView.startLayoutAnimation();
        }
        else
        {
            setupListView();
            setupGridView();
            mGridView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mListView.startLayoutAnimation();
        }


    }
    /**
     * Perform fade out animations on all of the grid items where position != pos
     * @param pos Position of the item that shouldn't be faded.
     */
    public void fadeOtherGridItems(int pos)
    {
        Animation animation = AnimationUtils.loadAnimation( this, R.anim.grid_fade_out);
        animation.setFillAfter(true);
        GridLayoutAnimationController animationController = new GridLayoutAnimationController(animation, 0.3f, 0.3f);

        View v = mGridView.getChildAt(pos);
        for (int i=0; i<mGridView.getChildCount(); i++)
        {
            if (i != pos)
                mGridView.getChildAt(i).startAnimation(animation);
        }

    }

    /**
     * Called when the first Podcast has been added to the library to create List/Grid adapters etc.
     */
    public void firstFeedAdded()
    {
        populate();
        if (!mPodcastHelper.hasPodcasts()) mFAB.setVisibility(View.GONE);
        else mFAB.setVisibility(View.VISIBLE);
    }

    //GETTERS
    public PodcastHelper getPodcastHelper()
    {
        return mPodcastHelper;
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
}
