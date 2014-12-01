package com.podhoarder.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.view.AnimatedSearchView;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-03.
 */
public class CollectionFragment extends BaseFragment implements PodHoarderService.StateChangedListener ,SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    //Floating Action Button
    protected ImageButton mFAB;

    //Searching
    private MenuItem mSearchMenuItem;
    protected AnimatedSearchView mSearchView;
    protected boolean mSearchEnabled;
    protected ListFilter mCurrentFilter;

    //Scroll Listener
    protected AbsListView.OnScrollListener mScrollListener;

    public CollectionFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!isDrawerIconEnabled())
            setDrawerIconEnabled(true, 300);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        if (!isDrawerIconEnabled())
            setDrawerIconEnabled(true, 300);
        if (mPlaybackService != null)
            onServiceConnected();
        super.onResume();
    }

    @Override
    public boolean onBackPressed() {
        if (mSearchEnabled) {

            return onClose();
        }
        return false;
    }

    @Override
    public void onFragmentResumed() {

    }

    protected void onFragmentRedrawn() {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);

        if (android.os.Build.VERSION.SDK_INT >=  Build.VERSION_CODES.LOLLIPOP)
            menu.findItem(R.id.action_add).setVisible(false);

        final SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (AnimatedSearchView) mSearchMenuItem.getActionView();
        if (null != mSearchView) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        }

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEnabled = true;
                mSearchView.onActionViewExpanded();
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    //SEARCHING
    public void doSearch(String searchString) {
        mSearchEnabled = true;
        mCurrentFilter.setSearchString(searchString.trim());
        setFilter(mCurrentFilter);
    }

    @Override
    public void onStateChanged(PodHoarderService.PlayerState newPlayerState) {
        super.onStateChanged(newPlayerState);
    }

    //SearchView onClose event.
    @Override
    public boolean onClose() {
        mSearchView.onActionViewCollapsed();
        mCurrentFilter.setSearchString("");
        mSearchEnabled = false;
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        doSearch(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
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
