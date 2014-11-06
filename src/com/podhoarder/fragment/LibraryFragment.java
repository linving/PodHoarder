package com.podhoarder.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.view.FloatingPlayPauseButton;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-03.
 */
public class LibraryFragment extends BaseFragment implements PodHoarderService.StateChangedListener {

    //App bar
    protected Toolbar mToolbar;
    protected int mToolbarSize;

    //Floating Action Button
    protected FloatingPlayPauseButton mFAB;

    //Service
    protected PodHoarderService mPlaybackService;

    //Searching
    protected boolean mSearchEnabled;

    protected ListFilter mCurrentFilter;

    //Scroll Listener
    protected AbsListView.OnScrollListener mScrollListener;

    public LibraryFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = super.onCreateView(inflater, container, savedInstanceState);
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

    //SEARCHING
    public void doSearch(String searchString) {
        mSearchEnabled = true;
        mCurrentFilter.setSearchString(searchString.trim());
        setFilter(mCurrentFilter);
    }

    public void cancelSearch() {
        ((LibraryActivity)getActivity()).cancelSearch();
        mCurrentFilter.setSearchString("");
        mSearchEnabled = false;
    }

    protected void setupFAB() {
        mFAB = (FloatingPlayPauseButton) mContentView.findViewById(R.id.fabbutton);
        if (mDataManager.hasPodcasts()) mFAB.setVisibility(View.VISIBLE);
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
           // mFAB.animateButton();
        }
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
