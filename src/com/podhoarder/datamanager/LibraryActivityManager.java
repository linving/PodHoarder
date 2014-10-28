/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarder.datamanager;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.activity.LibraryActivity.ListFilter;
import com.podhoarder.adapter.EpisodesListAdapter;
import com.podhoarder.adapter.GridAdapter;
import com.podhoarder.async.AddFeedFromSearchTask;
import com.podhoarder.async.AddFeedFromSearchTaskNoUI;
import com.podhoarder.async.FeedRefreshTask;
import com.podhoarder.async.MarkEpisodesAsListenedTask;
import com.podhoarder.async.MarkFeedsAsListenedTask;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.ToastMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that acts as an interface between GUI and SQLite etc.
 * Recommended use is to bind listAdapter to an ExpandableListView. The class
 * ensures that everything is updated automatically.
 */
public class LibraryActivityManager extends DataManager {
    private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PodcastHelper";

    public GridAdapter mFeedsGridAdapter;    //An expandable list containing all the Feed and their respective Episodes.
    public EpisodesListAdapter mEpisodesListAdapter;    //A list containing the newest X episodes of all the feeds.

    private List<Episode>  mSearch;

    public LibraryActivityManager(Context context) {
        super(context);

        mSearch = new ArrayList<Episode>();

        setupAdapters();
    }

    //DATA FETCHING
    /**
     * Adds a Feed from a Search Result Row. Automatically
     * downloads all data(except individual episode files) & an image.
     *
     * @param itemsToAdd A list of the search result objects that you want to add.
     */
    public void addSearchResults(List<SearchResultRow> itemsToAdd) {
        if (NetworkUtils.isOnline(mContext)) {
            if (hasPodcasts())
            {
                for (SearchResultRow row : itemsToAdd) {
                    this.mFeedsGridAdapter.addLoadingItem();
                    this.mFeedsGridAdapter.notifyDataSetChanged();
                    new AddFeedFromSearchTask(mContext, mFeedDBHelper, mFeedsGridAdapter).execute(row);
                }
            }
            else {
                for (SearchResultRow row : itemsToAdd) {
                    new AddFeedFromSearchTaskNoUI(mContext, mFeedDBHelper).execute(row);
                }
            }


        } else
            ToastMessages.AddFeedFailed(this.mContext).show();
    }

    /**
     * Calls a background thread that refreshes all the Feed objects in the db.
     * * @param swipeRefreshLayout SwipeRefreshLayout to indicate when the refresh is done.
     */
    @SuppressWarnings("unchecked")
    public void refreshFeeds(SwipeRefreshLayout swipeRefreshLayout) {
        if (NetworkUtils.isOnline(mContext)) {
            new FeedRefreshTask(mContext, mFeedDBHelper, swipeRefreshLayout).execute(this.mFeedsGridAdapter.mItems);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            ToastMessages.NoNetworkAvailable(this.mContext).show();
        }
    }

    //DATABASE OPERATIONS
    /**
     * Deletes all the specified Feeds and all associated Episodes from storage.
     *
     * @param feedIds List of feed IDs to be deleted.
     */
    @Override
    public void deleteFeeds(List<Integer> feedIds) {
        super.deleteFeeds(feedIds);
        this.reloadListData();
    }

    /**
     * Saves an updated Episode object in the db.
     *
     * @param ep An already existing Episode object with new values.
     */
    @Override
    public Episode updateEpisode(Episode ep) {
        Episode temp = super.updateEpisode(ep);
        this.reloadListData();
        return temp;
    }

    /**
     * Saves an updated Episode object in the db. (Does not refresh list adapters afterwards)
     *
     * @param ep An already existing Episode object with new values.
     */
    public Episode updateEpisodeNoRefresh(Episode ep) {
        return super.updateEpisode(ep);
    }

    /**
     * Marks all the Episodes of a Feed as listened asynchronously.
     *
     * @param feed Feed that should be marked as listened.
     */
    public void markAsListened(Feed feed) {
        new MarkFeedsAsListenedTask(this, mEpisodeDBHelper).execute(feed);
    }

    /**
     * Marks Episodes as listened asynchronously.
     *
     * @param eps List<Episode> containing the Episode objects that should be marked as listened.
     */
    @SuppressWarnings("unchecked")
    public void markAsListened(List<Episode> eps) {
        new MarkEpisodesAsListenedTask(this, mEpisodeDBHelper).execute(eps);
    }

    //FEED UTILS
    /**
     * Finds the correct position of a Feed in the listadapter list containing Feed objects.
     *
     * @param feedId ID of the Feed to get the location of.
     * @return The correct position in the list.
     */
    private int getFeedPositionWithId(int feedId) {
        int retVal = -1;
        for (int i = 0; i < this.mFeeds.size(); i++) {
            if (this.mFeeds.get(i).getFeedId() == feedId) {
                retVal = i;
            }
        }
        return retVal;
    }

    /**
     * Get a Feed by supplying an URL. If the URL matches any of the Feeds currently in the db, said Feed is returned.
     *
     * @param url URL of the Feed that should be returned.
     * @return The Feed with a matching URL, or null if no match is found.
     */
    private Feed getFeedWithURL(String url) {
        for (Feed currentFeed : this.mFeeds) {
            if (url.equals(currentFeed.getLink())) return currentFeed;
        }
        return null;
    }

    //LIST UTILS
    /**
     * Initializes adapter objects.
     */
    public void setupAdapters() {
        //TODO: If the filter points to for example favorites, and the favorites list is empty, the listview will not initialize. Have to always start with items in list and gridviews.
        if (hasPodcasts()) {
            mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
            mEpisodesListAdapter = new EpisodesListAdapter(mFeeds.get(0).getEpisodes(), mContext);
            new loadListDataTask().execute();
        }

    }

    /**
     * Performs the List/Grid adapter update which fills it with new or relevant items.
     */
    public void switchLists() {
        ListFilter mCurrentFilter = ((LibraryActivity) mContext).getFilter();
        if (hasPodcasts()) {
            if (!mCurrentFilter.getSearchString().isEmpty()) {  //If a search string has been specified, we conduct a search with the current filter.
                mSearch = mEpisodeDBHelper.search(mCurrentFilter);
                mEpisodesListAdapter.replaceItems(mSearch);
            }
            else { //If search string is empty we should just apply the filter.
                switch (mCurrentFilter) {
                    case ALL:
                        mFeedsGridAdapter.replaceItems(mFeeds);
                        break;
                    case FEED:
                        int feedPos = getFeedPositionWithId(((LibraryActivity) mContext).getFilter().getFeedId());
                        mEpisodesListAdapter.replaceItems(mFeeds.get(feedPos).getEpisodes());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void invalidate() {
        if (hasPodcasts())
        {
            ListFilter mCurrentFilter = ((LibraryActivity) mContext).getFilter();
            //Notify for UI updates.
            if (mCurrentFilter == ListFilter.ALL && mCurrentFilter.getFeedId() == 0)
                mFeedsGridAdapter.notifyDataSetChanged();
            else
                mEpisodesListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFeedsDataReloaded() {
        invalidate();
    }

    /**
     * Refreshes the playlist adapter.
     */
/*    public void reloadPlayList() {
        this.mRefreshing = true;
        //Playlist
        if (this.mPlaylistAdapter != null)
            this.mPlaylistAdapter.replaceItems(this.mEpisodeDBHelper.getPlaylistEpisodes());
        else
            this.mPlaylistAdapter = new DragNDropAdapter(this.mEpisodeDBHelper.getPlaylistEpisodes(), mContext);

        //Notify for UI updates.
        this.mPlaylistAdapter.notifyDataSetChanged();
        this.mRefreshing = false;
    }*/


}
