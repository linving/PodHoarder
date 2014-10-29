package com.podhoarder.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.adapter.GridAdapter;
import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.listener.EpisodeMultiChoiceModeListener;
import com.podhoarder.listener.GridActionModeCallback;
import com.podhoarder.object.Episode;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.util.Constants;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.ToastMessages;
import com.podhoarder.view.FloatingPlayPauseButton;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Emil on 2014-10-28.
 */
public class LibraryFragment extends BaseFragment implements PodHoarderService.StateChangedListener, SwipeRefreshLayout.OnRefreshListener, GridAdapter.GridItemClickListener {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = "com.podhoarder.fragment.LibraryFragment";
    //SERVICE
    private PodHoarderService mPlaybackService;
    private LibraryActivityManager mDataManager;

    //FILTER
    private ListFilter mFilter;
    //SEARCHING
    private boolean mSearchEnabled = false;

    //UI ELEMENTS
    //Fragment View
    private View mContentView;
    //Episodes List
    private ListView mListView;
    private EpisodeMultiChoiceModeListener mListSelectionListener;
    //Feeds Grid
    private GridView mGridView;
    private GridActionModeCallback mActionModeCallback;
    private ActionMode mActionMode;
    //SwipeRefreshLayout
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //Floating Action Button
    private FloatingPlayPauseButton mFAB;

    //ANON CLASSES
    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            boolean enable = false;
            if (mFilter == ListFilter.ALL && mFilter.mFeedId == 0) {
                if (mGridView != null && mGridView.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = mGridView.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = mGridView.getChildAt(0).getTop() > 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
            } else {
                if (mListView != null && mListView.getChildCount() > 0) {
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

    //OVERRIDES
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.activity_library, container, false);

        mPlaybackService = ((LibraryActivity) getActivity()).getPlaybackService();
        mPlaybackService.setStateChangedListener(LibraryFragment.this);
        mDataManager = ((LibraryActivity) getActivity()).getDataManager();
        mDataManager.mFeedsGridAdapter.setGridItemClickListener(this);

        if (mDataManager.hasPodcasts())
            setupFAB();

        mSwipeRefreshLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.windowBackground, R.color.colorAccent, R.color.windowBackground);

        mFilter = ListFilter.ALL;

        populate();
        //TODO: Make a nicer solution
        mGridView.setColumnWidth(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Constants.SETTINGS_KEY_GRIDITEMSIZE,"-1")));

        ((LibraryActivity)getActivity()).setCurrentFragment(this);

        return mContentView;
    }

    @Override
    public void onResume() {
        if (mPlaybackService != null && mFAB != null) {
            mFAB.setPlaying(mPlaybackService.isPlaying());
            mPlaybackService.setStateChangedListener(this);
        }
        super.onResume();
    }

    @Override
    public void onStateChanged(PodHoarderService.PlayerState newPlayerState) {
        Log.i(LOG_TAG, "New player state: " + newPlayerState);
        switch (newPlayerState) {
            case PLAYING:
                mFAB.setPlaying(true);
                //updateDrawer();
                break;
            case PAUSED:
                mFAB.setPlaying(false);
                break;
            case LOADING:
                break;
        }
    }

    @Override
    public void onRefresh() {
        ((LibraryActivityManager) mDataManager).refreshFeeds(mSwipeRefreshLayout);
    }

    @Override
    public void onGridItemClicked(int pos, int feedId) {
        LibraryFragment.ListFilter filter = LibraryFragment.ListFilter.FEED;
        filter.setFeedId(feedId);
        setFilter(filter);
    }

    /**
     * Method to handle back press.
     *
     * @return True if the backpress was handled. False otherwise.
     */
    public boolean onBackPressed() {
        if (mSearchEnabled) {
            cancelSearch();
            return true;
        } else {
            if (mFilter == ListFilter.FEED) {
                setFilter(ListFilter.ALL);
                return true;
            }
        }
        return false;
    }

    //VIEW SETUPS
    private void setupListView() {
        mListView = (ListView) mContentView.findViewById(R.id.episodesListView);
        if (this.mDataManager.hasPodcasts()) {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_fade_in);
            LayoutAnimationController animationController = new LayoutAnimationController(animation, 0.2f);

            this.mListView.setAdapter(((LibraryActivityManager) mDataManager).mEpisodesListAdapter);
            this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterview, View v, int pos, long id) {
                    Episode currentEp = (Episode) mListView.getItemAtPosition(pos);
                    if (currentEp.isDownloaded() || NetworkUtils.isOnline(getActivity()))
                        mPlaybackService.playEpisode((Episode) mListView.getItemAtPosition(pos));
                    else
                        ToastMessages.PlaybackFailed(getActivity()).show();
                }

            });

            this.mListView.setLayoutAnimation(animationController);

            this.mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            this.mListSelectionListener = new EpisodeMultiChoiceModeListener(getActivity(), this.mListView);
            this.mListView.setMultiChoiceModeListener(this.mListSelectionListener);
            this.mListView.setOnScrollListener(mScrollListener);
        } else {
            //List is empty. So we show the "Click to add your first podcast" string.
            setupEmptyText();
        }
    }

    private void setupGridView() {

        mGridView = (GridView) mContentView.findViewById(R.id.feedsGridView);
        if (this.mDataManager.hasPodcasts()) {

            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_fade_in);
            GridLayoutAnimationController animationController = new GridLayoutAnimationController(animation, 0.15f, 0.45f);

            ((LibraryActivityManager) mDataManager).mFeedsGridAdapter.setLoadingViews(setupLoadingViews());
            mGridView.setAdapter(((LibraryActivityManager) mDataManager).mFeedsGridAdapter);

            mGridView.setLayoutAnimation(animationController);

            mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mActionModeCallback = new GridActionModeCallback(getActivity(), mGridView);
            mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
                    if (mActionMode == null || !mActionModeCallback.isActive()) {
                        mActionMode = getActivity().startActionMode(mActionModeCallback);
                        mDataManager.mFeedsGridAdapter.setActionModeVars(mActionMode, mActionModeCallback);
                    }
                    mActionModeCallback.onItemCheckedStateChanged(pos, !((CheckBox) v.findViewById(R.id.feeds_grid_item_checkmark)).isChecked());
                    return true;
                }
            });
            mGridView.setOnScrollListener(mScrollListener);

        } else {
            //Grid is empty. So we show the "Click to add your first podcast" string.
            setupEmptyText();
        }

    }

    private List<View> setupLoadingViews() {
        List<View> views = new ArrayList<View>();    //This is an ugly solution but in order to use the GridViews LayoutParams the loading views must be inflated here.
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);    //get the inflater service.
        for (int i = 0; i < Constants.NEW_SEARCH_RESULT_LIMIT; i++)    //Inflate a collection of Loading views, same size as the maximum amount Search Results.
        {
            views.add(inflater.inflate(R.layout.feeds_grid_loading_item, mGridView, false));    //Inflate the "loading" grid item to show while data is downloaded
        }
        return views;
    }

    private void setupEmptyText() {
        TextView mEmptyText = (TextView) mContentView.findViewById(R.id.emptyLibraryString);
        if (mEmptyText.getVisibility() != View.VISIBLE) {
            mEmptyText.setVisibility(View.VISIBLE);
        }
    }

    private void setupFAB() {
        mFAB = (FloatingPlayPauseButton) mContentView.findViewById(R.id.fabbutton);

        mFAB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPlaybackService.isPlaying())
                    mPlaybackService.pause();
                else
                    mPlaybackService.play();
            }
        });
        mFAB.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                ((BaseActivity)getActivity()).startPlayerActivity();
                return false;
            }
        });
        mFAB.setPlaying(mPlaybackService.isPlaying());

        if (!mDataManager.hasPodcasts()) mFAB.setVisibility(View.GONE);
        else {
            mFAB.setVisibility(View.VISIBLE);
            mFAB.animateButton();
        }
    }

    private void populate() {
        if (mFilter == ListFilter.ALL) {
            setupGridView();
            setupListView();
            mGridView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            mGridView.startLayoutAnimation();
        } else {
            setupListView();
            setupGridView();
            mGridView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mListView.startLayoutAnimation();
        }
    }

    //ANIMATIONS
    public void fadeOtherGridItems(int pos) {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_fade_out);
        animation.setFillAfter(true);
        GridLayoutAnimationController animationController = new GridLayoutAnimationController(animation, 0.3f, 0.3f);

        View v = mGridView.getChildAt(pos);
        for (int i = 0; i < mGridView.getChildCount(); i++) {
            if (i != pos)
                mGridView.getChildAt(i).startAnimation(animation);
        }

    }


    //SEARCHING
    public void doSearch(String searchString) {
        mSearchEnabled = true;
        mFilter.setSearchString(searchString.trim());
        setFilter(mFilter);
    }

    public void cancelSearch() {
        mFilter.setSearchString("");
        setFilter(mFilter);
        mSearchEnabled = false;
    }

    //FILTERING
    public enum ListFilter {
        ALL, FEED;

        public int getFeedId() {
            return mFeedId;
        }

        public void setFeedId(int feedId) {
            this.mFeedId = feedId;
        }

        public String getSearchString() {
            return searchString;
        }

        public void setSearchString(String searchString) {
            this.searchString = searchString;
        }

        private int mFeedId = 0;
        private String searchString = "";
    }

    public ListFilter getFilter() {
        return mFilter;
    }

    public void setFilter(ListFilter filterToSet) {
        if (!filterToSet.getSearchString().isEmpty() || filterToSet != ListFilter.ALL) {
            if (mFilter == ListFilter.ALL) { //If the current Filter is ALL, then the grid is showing and we need to toggle List/Grid view visibility properties.
                mGridView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                mListView.startLayoutAnimation();
            }
        } else {
            if (filterToSet == ListFilter.ALL && filterToSet.getSearchString().isEmpty() && mGridView.getVisibility() != View.VISIBLE) {
                mListView.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
                mGridView.startLayoutAnimation();
            }

        }

        mFilter = filterToSet;
        mDataManager.switchLists(mFilter);
    }


}
