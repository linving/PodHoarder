/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarder.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.activity.MainActivity.ListFilter;
import com.podhoarder.adapter.DragNDropAdapter;
import com.podhoarder.adapter.EpisodesListAdapter;
import com.podhoarder.adapter.GridAdapter;
import com.podhoarder.async.AddFeedFromSearchTask;
import com.podhoarder.async.AddFeedFromSearchTaskNoUI;
import com.podhoarder.async.FeedRefreshTask;
import com.podhoarder.async.MarkEpisodesAsListenedTask;
import com.podhoarder.async.MarkFeedsAsListenedTask;
import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.db.PlaylistDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.object.SearchResultRow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that acts as an interface between GUI and SQLite etc.
 * Recommended use is to bind listAdapter to an ExpandableListView. The class
 * ensures that everything is updated automatically.
 */
public class PodcastHelper {
    private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PodcastHelper";


    private com.podhoarder.util.DownloadManager mDownloadManager;
    private FeedDBHelper fDbH;    //Handles saving the Feed objects to a database for persistence.
    private EpisodeDBHelper eph;    //Handles saving the Episode objects to a database for persistence.
    public PlaylistDBHelper plDbH;    //Handles saving the current playlist to a database for persistence.
    public GridAdapter mFeedsGridAdapter;    //An expandable list containing all the Feed and their respective Episodes.
    public EpisodesListAdapter mEpisodesListAdapter;    //A list containing the newest X episodes of all the feeds.
    public DragNDropAdapter mPlaylistAdapter;    //A list containing all the downloaded episodes.
    private Context mContext;
    private boolean mRefreshing, mEmpty;
    private String mLastSearchString = "";

    private List<Feed> mFeeds;
    private List<Episode>  mDownloaded, mNew, mFavorites, mSearch;

    public PodcastHelper(Context ctx) {
        mContext = ctx;
        mRefreshing = false;
        fDbH = new FeedDBHelper(this.mContext);
        eph = new EpisodeDBHelper(this.mContext);
        plDbH = new PlaylistDBHelper(this.mContext);
        mDownloadManager = new com.podhoarder.util.DownloadManager(ctx,this, eph);

        mFeeds = new ArrayList<Feed>();
        mDownloaded = new ArrayList<Episode>();
        mNew = new ArrayList<Episode>();
        mFavorites = new ArrayList<Episode>();
        mSearch = new ArrayList<Episode>();

        if (fDbH.getCount() > 0) {
            mEmpty = false;
            checkLocalLinks();
        }
        else
            mEmpty = true;
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
                    new AddFeedFromSearchTask(mContext, fDbH, mFeedsGridAdapter).execute(row);
                }
            }
            else {
                for (SearchResultRow row : itemsToAdd) {
                    new AddFeedFromSearchTaskNoUI(mContext, fDbH).execute(row);
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
            new FeedRefreshTask(mContext, fDbH, swipeRefreshLayout).execute(this.mFeedsGridAdapter.mItems);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            ToastMessages.NoNetworkAvailable(this.mContext).show();
        }
    }

    /**
     * Calls a background thread that refreshes a particular Feed object in the db.
     *
     * @param swipeRefreshLayout SwipeRefreshLayout to indicate when the refresh is done.
     * @param id                 ID of the Feed to be refreshed.
     */
    @SuppressWarnings("unchecked")
    public void refreshFeed(SwipeRefreshLayout swipeRefreshLayout, int id) {
        if (NetworkUtils.isOnline(mContext)) {
            List<Feed> feeds = new ArrayList<Feed>();
            feeds.add(this.getFeed(id));
            new FeedRefreshTask(mContext, fDbH, swipeRefreshLayout).execute(feeds);
        } else {
            //TODO: Cancel refresh view if it started to refresh.
            ToastMessages.RefreshFailed(this.mContext).show();
        }
    }


    //DATABASE OPERATIONS
    /**
     * Deletes a Feed and all associated Episodes from storage.
     *
     * @param feedIds List of feed IDs to be deleted.
     */
    public void deleteFeeds(List<Integer> feedIds) {
        this.fDbH.deleteFeeds(feedIds);
        for (int feedId : feedIds) {
            // Delete Feed Image
            String fName = feedId + ".jpg";
            File file = new File(this.mContext.getFilesDir(), fName);
            boolean check = file.delete();
            if (check) {
                Log.w(LOG_TAG, "Feed Image deleted successfully!");
            } else {
                Log.w(LOG_TAG, "Feed Image not found! No delete necessary.");
            }
        }
        this.refreshContent();
    }

    /**
     * Saves an updated Episode object in the db.
     *
     * @param ep An already existing Episode object with new values.
     */
    public Episode updateEpisode(Episode ep) {
        Episode temp = this.eph.updateEpisode(ep);
        this.refreshContent();
        return temp;
    }

    /**
     * Saves an updated Episode object in the db. (Does not refresh list adapters afterwards)
     *
     * @param ep An already existing Episode object with new values.
     */
    public Episode updateEpisodeNoRefresh(Episode ep) {
        return this.eph.updateEpisode(ep);
    }

    /**
     * Marks all the Episodes of a Feed as listened asynchronously.
     *
     * @param feed Feed that should be marked as listened.
     */
    public void markAsListened(Feed feed) {
        new MarkFeedsAsListenedTask(this, eph).execute(feed);
    }

    /**
     * Marks the Episode as listened asynchronously.
     *
     * @param ep Episode that should be marked as listened.
     */
    @SuppressWarnings("unchecked")
    public void markAsListened(Episode ep) {
        List<Episode> eps = new ArrayList<Episode>();
        eps.add(ep);
        new MarkEpisodesAsListenedTask(this, eph).execute(eps);
    }

    /**
     * Marks Episodes as listened asynchronously.
     *
     * @param eps List<Episode> containing the Episode objects that should be marked as listened.
     */
    @SuppressWarnings("unchecked")
    public void markAsListened(List<Episode> eps) {
        new MarkEpisodesAsListenedTask(this, eph).execute(eps);
    }

    /**
     * Checks all stored links to make sure that the referenced files actually exist.
     * The function resets local links of files that can't be found, so that they may be downloaded again.
     * Files can be manually removed from the public external directories, thus this is necessary.
     *
     * @author Emil
     */
    private void checkLocalLinks() {
        List<Feed> feeds = this.fDbH.getAllFeeds(false);
        for (int feedNo = 0; feedNo < feeds.size(); feedNo++) {
            List<Episode> episodes = feeds.get(feedNo).getEpisodes();
            for (int epNo = 0; epNo < episodes.size(); epNo++) {
                if (!episodes.get(epNo).getLocalLink().equals("")) {
                    //If localLink isn't empty, check that the file exists in external storage.
                    File file = new File(episodes.get(epNo).getLocalLink());
                    if (!file.exists()) {
                        Log.w(LOG_TAG, "Couldn't find " + file.getName() + ". Resetting local link for entry: " + episodes.get(epNo).getEpisodeId());
                        Episode temp = feeds.get(feedNo).getEpisodes().get(epNo);
                        temp.setLocalLink("");
                        this.eph.updateEpisode(temp);
                    }
                }
            }
        }
        setupAdapters();
    }

    /**
     * Closes all open db connections to prevent leaks.
     */
    public void closeDbIfOpen() {
        this.eph.closeDatabaseIfOpen();
        this.fDbH.closeDatabaseIfOpen();
    }


    //EPISODE UTILS
    /**
     * Deletes the physical mp3-file associated with an Episode, not the Episode object itself.
     *
     * @param ep Episode object with the file you want to delete.
     * @author Emil
     */
    public void deleteEpisodeFile(Episode ep) {
        if (ep != null) {
            File file = new File(ep.getLocalLink());
            if (file.delete()) {
                ep.setLocalLink("");
                this.eph.updateEpisode(ep);
                Log.i(LOG_TAG, file.getAbsolutePath() + " deleted successfully!");
            } else {
                Log.e(LOG_TAG, file.getAbsolutePath() + " not deleted. Make sure it exists.");
            }
        }
    }

    /**
     * Retrieves an Episode object using Episode ID as parameter.
     *
     * @param episodeId ID of the Episode to retrieve.
     * @return An Episode object.
     */
    public Episode getEpisode(int episodeId) {
        return this.eph.getEpisode(episodeId);
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
     * Retrieves a Feed object using a Feed ID as parameter.
     *
     * @param feedId ID of the Feed to retrieve.
     * @return A Feed object.
     */
    public Feed getFeed(int feedId) {
        int index = this.getFeedPositionWithId(feedId);
        if (index == -1)
            return null;
        else
            return this.mFeeds.get(index);
    }

    /**
     * Gets the image of a Feed and intializes it.
     * @param feedId ID of the Feed to get the image for.
     * @return A BitmapDrawable object containing the Feed Image.
     */
    public BitmapDrawable getFeedImage(int feedId) {
        return new BitmapDrawable(this.mContext.getResources(), getFeed(feedId).getFeedImage().imageObject());
    }

    /**
     * Get a Feed by supplying an URL. If the URL matches any of the Feeds currently in the db, said Feed is returned.
     *
     * @param url URL of the Feed that should be returned.
     * @return The Feed with a matching URL, or null if no match is found.
     */
    private Feed getFeedWithURL(String url) {
        for (Feed currentFeed : this.mFeedsGridAdapter.mItems) {
            if (url.equals(currentFeed.getLink())) return currentFeed;
        }
        return null;
    }

    public DownloadManager getDownloadManager() {
        return mDownloadManager;
    }

    public boolean hasPodcasts() {
        return !mEmpty;
    }

    //LIST UTILS
    /**
     * Initializes adapter objects.
     */
    public void setupAdapters() {
        //TODO: If the filter points to for example favorites, and the favorites list is empty, the listview will not initialize. Have to always start with items in list and gridviews.
        mFeeds = fDbH.getAllFeeds(true);
        if (mFeeds.size() > 0) {
            switch (((MainActivity) mContext).getFilter()) {
                case ALL:

                    mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
                    mEpisodesListAdapter = new EpisodesListAdapter(mFeeds.get(0).getEpisodes(), mContext);
                    break;
                case NEW:
                    mNew = eph.getNewEpisodes();
                    mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
                    mEpisodesListAdapter = new EpisodesListAdapter(mNew, mContext);
                    break;
                case DOWNLOADED:
                    mDownloaded = eph.getDownloadedEpisodes();
                    mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
                    mEpisodesListAdapter = new EpisodesListAdapter(mDownloaded, mContext);
                    break;
                case FAVORITES:
                    mFavorites = eph.getFavoriteEpisodes();
                    mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
                    mEpisodesListAdapter = new EpisodesListAdapter(mFavorites, mContext);
                    break;
                default:
                    mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
                    mEpisodesListAdapter = new EpisodesListAdapter(new ArrayList<Episode>(), mContext);
                    break;
            }
            mPlaylistAdapter = new DragNDropAdapter(eph.getPlaylistEpisodes(), mContext);
            new loadListsTask().execute();
        }

    }

    /**
     * An Asynchronous Task for loading items in the background. This should be done after the data you want to display has loaded to minimise waiting.
     */
    private class loadListsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (mFeeds.isEmpty())
                mFeeds = fDbH.getAllFeeds(true);
            if (mNew.isEmpty())
                mNew = eph.getNewEpisodes();
            if (mDownloaded.isEmpty())
                mDownloaded = eph.getDownloadedEpisodes();
            if (mFavorites.isEmpty())
                mFavorites = eph.getFavoriteEpisodes();
            return null;
        }
    }

    /**
     * Performs the List/Grid adapter update which fills it with new or relevant items.
     */
    public void switchLists() {
        ListFilter mCurrentFilter = ((MainActivity) mContext).getFilter();
        if (!mEmpty) {
            if (!mCurrentFilter.getSearchString().isEmpty()) {  //If a search string has been specified, we conduct a search with the current filter.
                mSearch = eph.search(mCurrentFilter);
                mEpisodesListAdapter.replaceItems(mSearch);
            }
            else { //If search string is empty we should just apply the filter.
                switch (mCurrentFilter) {
                    case ALL:
                        mFeedsGridAdapter.replaceItems(mFeeds);
                        break;
                    case FEED:
                        int feedPos = getFeedPositionWithId(((MainActivity) mContext).getFilter().getFeedId());
                        mEpisodesListAdapter.replaceItems(mFeeds.get(feedPos).getEpisodes());
                        break;
                    case NEW:
                        mEpisodesListAdapter.replaceItems(mNew);
                        break;
                    case DOWNLOADED:
                        mEpisodesListAdapter.replaceItems(mDownloaded);
                        break;
                    case FAVORITES:
                        mEpisodesListAdapter.replaceItems(mFavorites);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Flags the currently visible List/Grid as invalidated, causing it to redraw the list.
     */
    public void invalidate() {
        if (hasPodcasts())
        {
            ListFilter mCurrentFilter = ((MainActivity) mContext).getFilter();
            //Notify for UI updates.
            if (mCurrentFilter == ListFilter.ALL && mCurrentFilter.getFeedId() == 0)
                mFeedsGridAdapter.notifyDataSetChanged();
            else
                mEpisodesListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Refreshes the list or grid view.
     */
    public void refreshContent() {
        if (!mRefreshing)
            new ListRefreshTask().execute();
        else
            Log.d(LOG_TAG, "Refresh currently in progress. Not running RefreshListsAsync()!");
    }

    /**
     * Forces a refresh on the list or grid view.
     */
    public void forceRefreshContent() {
        if(mEmpty) {
            setupAdapters();
            mEmpty = false;
        }
        else
            new ListRefreshTask().execute();
    }

    /**
     * AsyncTask for refreshing list adapters.
     */
    class ListRefreshTask extends AsyncTask<Void, Void, Void> {
        private ListFilter mCurrentFilter;
        private boolean cancelled = false;

        public ListRefreshTask() {

        }

        @Override
        protected void onPreExecute() {
            if (!cancelled) {
                mRefreshing = true;
                mCurrentFilter = ((MainActivity) mContext).getFilter();
            }
        }

        /**
         * Actual refresh method.
         * This function does all the work "in the background". (In this case it gets all the new data from the db)
         */
        @Override
        protected Void doInBackground(Void... params)
        {
            if (!cancelled) {
                mFeeds = fDbH.refreshFeedData(mFeedsGridAdapter.mItems, false);
                mNew = eph.getNewEpisodes();
                mDownloaded = eph.getDownloadedEpisodes();
                mFavorites = eph.getFavoriteEpisodes();
            }
            return null;
        }

        /**
         * Fires when Task has been executed.
         * This function replaces all the collections and calls for a redraw.
         */
        @Override
        protected void onPostExecute(Void param) {
            switch (mCurrentFilter) {
                case ALL:
                    mFeedsGridAdapter.replaceItems(mFeeds);
                    break;
                case FEED:
                    int feedPos = getFeedPositionWithId(((MainActivity) mContext).getFilter().getFeedId());
                    if (feedPos != -1)
                        mEpisodesListAdapter.replaceItems(mFeeds.get(feedPos).getEpisodes());
                    break;
                case NEW:
                    mEpisodesListAdapter.replaceItems(mNew);
                    break;
                case DOWNLOADED:
                    mEpisodesListAdapter.replaceItems(mDownloaded);
                    break;
                case FAVORITES:
                    mEpisodesListAdapter.replaceItems(mFavorites);
                    break;
                default:
                    break;
            }
            mRefreshing = false;
        }

        @Override
        protected void onCancelled(Void result) {
            this.cancelled = true;
            mRefreshing = false;
            //Notify for UI updates.
            invalidate();
        }
    }

    /**
     * Refreshes the playlist adapter.
     */
    public void refreshPlayList() {
        this.mRefreshing = true;
        //Playlist
        if (this.mPlaylistAdapter != null)
            this.mPlaylistAdapter.replaceItems(this.eph.getPlaylistEpisodes());
        else
            this.mPlaylistAdapter = new DragNDropAdapter(this.eph.getPlaylistEpisodes(), mContext);

        //Notify for UI updates.
        this.mPlaylistAdapter.notifyDataSetChanged();
        this.mRefreshing = false;
    }


}
