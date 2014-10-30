package com.podhoarder.activity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.fragment.BaseFragment;
import com.podhoarder.fragment.EpisodeFragment;
import com.podhoarder.fragment.LibraryFragment;
import com.podhoarder.fragment.PlayerFragment;
import com.podhoarder.object.Episode;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.service.PodHoarderService.PodHoarderBinder;
import com.podhoarder.util.Constants;
import com.podhoarder.util.HardwareIntentReceiver;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.ToastMessages;
import com.podhoarder.view.FloatingPlayPauseButton;
import com.podhoarderproject.podhoarder.R;

import java.util.List;


public class LibraryActivity extends BaseActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener, BaseActivity.QuicklistItemClickListener {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = "com.podhoarder.activity.MainActivity";
    //FRAGMENTS
    public BaseFragment mCurrentFragment;

    //Floating Action Button
    private FloatingPlayPauseButton mFAB;

    //SEARCH
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;

    //ACTIVITY RESULT
    static final int ADD_PODCAST_REQUEST = 1;
    static final int SETTINGS_REQUEST = 2;

    //HARDWARE INTENT RECEIVER
    private HardwareIntentReceiver hardwareIntentReceiver;

    //PLAYBACK SERVICE
    private PodHoarderService mPlaybackService;
    private Intent mPlayIntent;
    private boolean mIsMusicBound = false;

    private ServiceConnection podConnection = new ServiceConnection()    //connect to the service
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PodHoarderBinder binder = (PodHoarderBinder) service;
            //get service
            mPlaybackService = binder.getService();
            //pass list
            //mPlaybackService.setList(mPodcastHelper.mPlaylistAdapter.mPlayList);
            mIsMusicBound = true;

            //Initialise the headphone jack listener / intent receiver.
            IntentFilter headsetFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            IntentFilter callStateFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            hardwareIntentReceiver = new HardwareIntentReceiver(mPlaybackService, ((LibraryActivityManager) mDataManager));
            registerReceiver(hardwareIntentReceiver, headsetFilter);
            registerReceiver(hardwareIntentReceiver, callStateFilter);
            registerReceiver(hardwareIntentReceiver, connectivityFilter);
            mPlaybackService.setManager((LibraryActivityManager) mDataManager);
            updateDrawer();
            mQuicklistItemClickListener = LibraryActivity.this;
            mCurrentFragment.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unregisterReceiver(hardwareIntentReceiver);
            mIsMusicBound = false;
        }
    };

    //ACTIVITY OVERRIDES
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation
        //setContentView(R.layout.activity_base);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.bringToFront();

        if (!this.mIsMusicBound)    //If the service isn't bound, we need to start and bind it.
        {
            if (mPlayIntent == null) {
                mPlayIntent = new Intent(this, PodHoarderService.class);
                this.mIsMusicBound = this.bindService(mPlayIntent, podConnection, Context.BIND_AUTO_CREATE);
                this.startService(mPlayIntent);
            }
        }

        if (mCurrentFragment == null) {
            // Create a new Fragment to be placed in the activity layout
            mCurrentFragment = new LibraryFragment();
            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            mCurrentFragment.setArguments(getIntent().getExtras());
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction().add(R.id.root_container, mCurrentFragment).commit();
        }
        handleIntent(getIntent());

        if (savedInstanceState != null) {
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        this.unbindService(this.podConnection);
        this.stopService(mPlayIntent);
        unregisterReceiver(hardwareIntentReceiver);
        this.mPlaybackService = null;
        this.mIsMusicBound = false;
        mDataManager.closeDbIfOpen();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        if (null != mSearchView) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        setupActionBar();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (!super.onOptionsItemSelected(item)) {   //If BaseActivity.onOptionsItemSelected returns true, that means the event was handled there and this activity shouldn't do anything.
            switch (item.getItemId()) {
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
        } else
            return false;

    }

    @Override
    public void onBackPressed() {
        if (!mCurrentFragment.onBackPressed())
            super.onBackPressed();
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        switch (requestCode) {
            case ADD_PODCAST_REQUEST:
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    // The user picked a contact.
                    // The Intent's data Uri identifies which contact was selected.
                    @SuppressWarnings("unchecked")
                    List<SearchResultRow> results = (List<SearchResultRow>) data.getExtras().getSerializable(AddActivity.INTENT_RESULTS_ID);
                    for (SearchResultRow row : results) //Load all the XML files back into memory from the cache directory.
                        row.loadXML();
                    ((LibraryActivityManager) mDataManager).addSearchResults(results);
                }
                break;
            case SETTINGS_REQUEST:
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    if (data.hasExtra(Constants.SETTINGS_KEY_GRIDSHOWTITLE) && data.getBooleanExtra(Constants.SETTINGS_KEY_GRIDSHOWTITLE,true)) {  //If any of the preferences were changed we'll redraw the Grid. (to reflect the UI changes)
                        ((LibraryActivityManager) mDataManager).mFeedsGridAdapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onQueryTextChange(String str) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String str) {
        ((LibraryFragment)mCurrentFragment).doSearch(str);
        return false;
    }

    @Override
    public void onQuicklistItemClicked(View clickedView, int position, QuicklistFilter currentFilter) {
        switch (currentFilter) {
            case FAVORITES:
                mPlaybackService.playEpisode(mDataManager.Favorites().get(position));
                break;
            case PLAYLIST:
                mPlaybackService.playEpisode(mDataManager.Playlist().get(position));
                break;
            case NEW:
                mPlaybackService.playEpisode(mDataManager.New().get(position));
                break;
        }
    }

    @Override
    public boolean onClose() {
        cancelSearch();
        return true;
    }

    //INTENT HANDLING
    private void handleIntent(Intent intent) {
        Log.i(LOG_TAG, "Received intent with action: " + intent.getAction());
        if (intent.getAction() != null) {
            if (intent.getAction().equals("navigate_player"))
                startPlayerActivity();
        }
    }

    //DATA MANAGER SETUP
    @Override
    protected void setupDataManager() {
        if (mDataManager == null)
            mDataManager = new LibraryActivityManager(this);
    }

    private void setupActionBar() {
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

    //NAVIGATION
    @Override
    public void startLibraryActivity() {
        if (!((Object) mCurrentFragment).getClass().getName().equals(LibraryFragment.class.getName())) { //We check to see if the current fragment is a PlayerFragment. In that case we don't need to create a new one.
            onBackPressed();
            getSupportFragmentManager().popBackStack();
        }
    }
    @Override
    public void startEpisodeActivity(Episode currentEp) {
        if (!((Object) mCurrentFragment).getClass().getName().equals(EpisodeFragment.class.getName())) {
            cancelSearch();
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
            ft.replace(R.id.root_container, EpisodeFragment.newInstance(currentEp.getEpisodeId()));
            ft.addToBackStack(null);
            ft.commit();
        }
    }
    @Override
    public void startPlayerActivity() {
        if (!((Object) mCurrentFragment).getClass().getName().equals(PlayerFragment.class.getName())) { //We check to see if the current fragment is a PlayerFragment. In that case we don't need to create a new one.
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.player_fade_in, R.anim.fade_out, R.anim.activity_stay_transition, R.anim.player_fade_out);
            ft.replace(R.id.root_container, new PlayerFragment());
            ft.addToBackStack(null);
            ft.commit();
        }
    }
    public void startAddActivity() {
        if (NetworkUtils.isOnline(LibraryActivity.this)) {
            Intent intent = new Intent(LibraryActivity.this, AddActivity.class);
            startActivityForResult(intent, ADD_PODCAST_REQUEST);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else
            ToastMessages.NoNetworkAvailable(LibraryActivity.this).show();
    }
    @Override
    public void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_REQUEST);
    }

    //SEARCHING
    public void doSearch(String searchString) {
        ((LibraryFragment)mCurrentFragment).doSearch(searchString);
    }
    public void cancelSearch() {
        mSearchView.onActionViewCollapsed();
        //((LibraryFragment)mCurrentFragment).cancelSearch();
    }

    //MISC HELPER METHODS
    /**
     * Called when the first Podcast has been added to the library to create List/Grid adapters etc.
     */
    public void firstFeedAdded() {
        //populate();
        if (!mDataManager.hasPodcasts()) mFAB.setVisibility(View.GONE);
        else mFAB.setVisibility(View.VISIBLE);
    }
    private void updateDrawer() {
        if (mPlaybackService.mCurrentEpisode != null) {
            mNavDrawerBanner.setImageBitmap(
                    mDataManager.getFeed(mPlaybackService.mCurrentEpisode.getFeedId()).getFeedImage().imageObject()
            );

        }

    }
    public void deletingEpisode(int episodeId) {
        this.mPlaybackService.deletingEpisode(episodeId);
    }
    public void downloadEpisode(Episode ep) {
        this.mDataManager.DownloadManager().downloadEpisode(ep);
    }

    //GETTERS
    public LibraryActivityManager getDataManager() {
        return ((LibraryActivityManager) mDataManager);
    }
    public PodHoarderService getPlaybackService() {
        return mPlaybackService;
    }
    public boolean isMusicBound() {
        return mIsMusicBound;
    }

    public void setCurrentFragment(BaseFragment currentFragment)  {
        this.mCurrentFragment = currentFragment;
    }
}
