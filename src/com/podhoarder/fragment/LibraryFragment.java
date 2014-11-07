package com.podhoarder.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.service.PodHoarderService;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-03.
 */
public class LibraryFragment extends BaseFragment implements PodHoarderService.StateChangedListener {

    //App bar
    protected Toolbar mToolbar;
    protected int mToolbarSize;

    //Floating Action Button
    protected ImageButton mFAB;

    //Service
    protected PodHoarderService mPlaybackService;

    //Searching
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;
    protected boolean mSearchEnabled;
    protected ListFilter mCurrentFilter;

    //Scroll Listener
    protected AbsListView.OnScrollListener mScrollListener;

    public LibraryFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        mDataManager = ((LibraryActivity)getActivity()).getDataManager();
        mPlaybackService = ((LibraryActivity)getActivity()).getPlaybackService();

        return mContentView;
    }

    @Override
    public void onResume() {
        if (mPlaybackService != null) {
            onServiceConnected();
        }
        super.onResume();
    }

    @Override
    public boolean onBackPressed() {
        if (mSearchEnabled) {
            cancelSearch();
        }
        return false;
    }

    @Override
    public void onServiceConnected() {
        mPlaybackService = ((LibraryActivity) getActivity()).getPlaybackService();
        mPlaybackService.setStateChangedListener(LibraryFragment.this);

        setupFAB();
    }

    @Override
    public void onFragmentResumed() {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        if (null != mSearchView) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        }

        mSearchView.setOnQueryTextListener((LibraryActivity)getActivity());
        mSearchView.setOnCloseListener((LibraryActivity)getActivity());
        super.onCreateOptionsMenu(menu, inflater);
    }

    //SEARCHING
    public void doSearch(String searchString) {
        mSearchEnabled = true;
        mCurrentFilter.setSearchString(searchString.trim());
        setFilter(mCurrentFilter);
    }

    public void cancelSearch() {
        mSearchView.onActionViewCollapsed();
        mCurrentFilter.setSearchString("");
        mSearchEnabled = false;
    }

    protected void setupFAB() {

    }



    @Override
    public void onStateChanged(PodHoarderService.PlayerState newPlayerState) {
        Log.i(LOG_TAG, "New player state: " + newPlayerState);

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

    public void setFilter(ListFilter filterToSet) {
        mCurrentFilter = filterToSet;
        if (mCurrentFilter != ListFilter.ALL || !mCurrentFilter.getSearchString().isEmpty()) {
            if (!mCurrentFilter.getSearchString().isEmpty())
                mSearchEnabled = true;
            ((BaseActivity) getActivity()).startListActivity(mCurrentFilter);
        }
        else {
            ((BaseActivity) getActivity()).startGridActivity();
        }
    }

}
