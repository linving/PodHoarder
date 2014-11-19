package com.podhoarder.datamanager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.adapter.QueueAdapter;
import com.podhoarder.adapter.QuickListAdapter;
import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.db.PlaylistDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.DownloadManager;
import com.podhoarderproject.podhoarder.R;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Emil on 2014-10-27.
 */
public class DataManager {
    //Log Tag
    private static final String LOG_TAG = "com.podhoarderproject.datamanager.DataManager";
    //Context object
    protected Context mContext;
    //Download Manager
    protected com.podhoarder.util.DownloadManager mDownloadManager;
    //Database Interfaces
    protected FeedDBHelper mFeedDBHelper;          //Handles saving the Feed objects to a database for persistence.
    protected EpisodeDBHelper mEpisodeDBHelper;       //Handles saving the Episode objects to a database for persistence.
    protected PlaylistDBHelper mPlaylistDBHelper;      //Handles saving the Playlist entries to a database for persistence.
    //Quick List Adapters
    public QueueAdapter mPlaylistAdapter;
    public QuickListAdapter mQuicklistAdapter;

    //List Objects.
    protected List<Feed> mFeeds;
    protected List<Episode> mFavorites,
            mNew;
    protected LinkedList<Episode> mPlaylist;
    //Refresh check
    protected boolean mRefreshing;

    public DataManager(Context context) {
        mContext = context;
        mRefreshing = true;

        mFeedDBHelper = new FeedDBHelper(this.mContext);
        mEpisodeDBHelper = new EpisodeDBHelper(this.mContext);
        mPlaylistDBHelper = new PlaylistDBHelper(this.mContext);

        mFeeds = loadFeeds();
        checkFileLinks();
        mFavorites = loadFavorites();
        mNew = loadNew();
        mPlaylist = new LinkedList<Episode>(loadPlaylist());

        if (hasPodcasts()) {
            mQuicklistAdapter = new QuickListAdapter(mFavorites, mContext);
            mPlaylistAdapter = new QueueAdapter(mPlaylist, R.layout.queue_episode_list_row, mContext);
        }
        else {
            mQuicklistAdapter = new QuickListAdapter(new ArrayList<Episode>(), mContext);
            mPlaylistAdapter = new QueueAdapter(new LinkedList<Episode>(), R.layout.episode_list_row,  mContext);
        }

        mDownloadManager = new com.podhoarder.util.DownloadManager(context, this, mEpisodeDBHelper);

        mRefreshing = false;
    }

    /**
     * An Asynchronous Task for loading items in the background. This should be done after the data you want to display has loaded to minimise waiting.
     */
    protected class loadListDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (mFeeds.isEmpty())
                mFeeds = mFeedDBHelper.getAllFeeds(true);
            if (mFavorites.isEmpty())
                mFavorites = mEpisodeDBHelper.getFavoriteEpisodes();
            if (mNew.isEmpty())
                mNew = mEpisodeDBHelper.getNewEpisodes();
            if (mPlaylist.isEmpty())
                mPlaylist = new LinkedList<Episode>(mEpisodeDBHelper.getPlaylistEpisodes());
            return null;
        }
    }

    private List<Feed> loadFeeds() {
        mFeeds = mFeedDBHelper.getAllFeeds();
        return mFeeds;
    }

    protected class loadFeedsDataAsync extends AsyncTask<Void, Void, Void> {
        private boolean mLoadBitmaps;

        public loadFeedsDataAsync(boolean loadBitmaps) {
            mLoadBitmaps = loadBitmaps;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mFeeds = mFeedDBHelper.refreshFeedData(mFeeds, mLoadBitmaps);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onFeedsDataReloaded();
        }
    }

    private List<Episode> loadFavorites() {
        mFavorites = mEpisodeDBHelper.getFavoriteEpisodes();
        return mFavorites;
    }

    protected class loadFavoritesDataAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mFavorites = mEpisodeDBHelper.getFavoriteEpisodes();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onFavoritesDataReloaded();
        }
    }

    private List<Episode> loadNew() {
        mNew = mEpisodeDBHelper.getNewEpisodes();
        return mNew;
    }

    protected class loadNewDataAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mNew = mEpisodeDBHelper.getNewEpisodes();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onNewDataReloaded();
        }
    }

    private Queue<Episode> loadPlaylist() {
        mPlaylist = new LinkedList<Episode>(mEpisodeDBHelper.getPlaylistEpisodes());
        return mPlaylist;
    }

    protected class loadPlaylistDataAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mPlaylist = new LinkedList<Episode>(mEpisodeDBHelper.getPlaylistEpisodes());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onPlaylistDataReloaded();
        }
    }

    public List<Feed> Feeds() {
        return mFeeds;
    }

    public List<Episode> Favorites() {
        return mFavorites;
    }

    public List<Episode> New() {
        return mNew;
    }

    public Queue<Episode> Playlist() {
        return mPlaylist;
    }

    public DownloadManager DownloadManager() {
        return mDownloadManager;
    }

    /**
     * Retrieves a Feed object using a Feed ID as parameter.
     *
     * @param feedId ID of the Feed to retrieve.
     * @return A Feed object.
     */
    public Feed getFeed(int feedId) {
        for (Feed feed : mFeeds) {
            if (feed.getFeedId() == feedId)
                return feed;
        }

        return mFeedDBHelper.getFeed(feedId);
    }

    /**
     * Saves an updated Feed object in the db.
     *
     * @param feed An already existing Feed object with new values.
     */
    public Feed updateFeed(Feed feed) {
        return mFeedDBHelper.updateFeed(feed);
    }

    /**
     * Deletes all the specified Feeds and all associated Episodes from storage.
     *
     * @param feedIds List of feed IDs to be deleted.
     */
    public void deleteFeeds(List<Integer> feedIds) {
        mFeedDBHelper.deleteFeeds(feedIds);
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
    }

    /**
     * Retrieves an Episode object using Episode ID as parameter.
     *
     * @param episodeId ID of the Episode to retrieve.
     * @return An Episode object.
     */
    public Episode getEpisode(int episodeId) {
        return mEpisodeDBHelper.getEpisode(episodeId);
    }

    /**
     * Saves an updated Episode object in the db.
     *
     * @param ep An already existing Episode object with new values.
     */
    public Episode updateEpisode(Episode ep) {
        return mEpisodeDBHelper.updateEpisode(ep);
    }

    /**
     * Deletes the physical mp3-file associated with an Episode, not the Episode object itself.
     *
     * @param ep Episode object with the file you want to delete.
     */
    public void deleteEpisodeFile(Episode ep) {
        if (ep != null) {
            File file = new File(ep.getLocalLink());
            if (file.delete()) {
                ep.setLocalLink("");
                updateEpisode(ep);
                Log.i(LOG_TAG, file.getAbsolutePath() + " deleted successfully!");
            } else {
                Log.e(LOG_TAG, file.getAbsolutePath() + " not deleted. Make sure it exists.");
            }
        }
    }

    public void updatePlaylist(Queue<Episode> playlist) {
        mPlaylistDBHelper.savePlaylist(playlist);
    }


    public void addToPlaylist(Episode ep) {
        mPlaylist.add(ep);
        mPlaylistAdapter.notifyDataSetChanged();
        updatePlaylist(mPlaylist);
    }

    public void addToPlaylistAsNext(Episode ep) {
        mPlaylist.addFirst(ep);
        mPlaylistAdapter.notifyDataSetChanged();
        updatePlaylist(mPlaylist);
    }

    public void removeFromPlaylist(int pos) {
        mPlaylist.remove(pos);
        mPlaylistAdapter.notifyDataSetChanged();
        updatePlaylist(mPlaylist);
    }

    public void removeFromPlaylist(Episode ep) {
        removeFromPlaylist(mPlaylist.indexOf(ep));
    }

    /**
     * A simple check to see if there are any podcasts or if the library is empty.
     *
     * @return True if the library is empty. False if it contains anything.
     */
    public boolean hasPodcasts() {
        return (mFeeds.size() > 0);
    }

    /**
     * A simple check to see it a Podcast feed is already stored by supplying the URL of said Feed.
     * @param url URL of the Feed.
     * @return True if the podcast is already stored. False otherwise.
     */
    public boolean hasPodcast(String url) {
        for (Feed feed : this.Feeds()) {
            if (url.equals(feed.getLink())) return true;
        }
        return false;
    }

    /**
     * Refreshes the list or grid view.
     */
    public void reloadListData(boolean reloadAsync) {
        if (!mRefreshing)
            if (reloadAsync)
                new ListDataReloadTask().execute();
            else
                loadListData();
        else
            Log.d(LOG_TAG, "Refresh currently in progress. Not running RefreshListsAsync()!");
    }

    /**
     * Forces a refresh on the list or grid view.
     */
    public void forceReloadListData(boolean reloadAsync) {
        if (hasPodcasts()) {
            if (reloadAsync)
                new ListDataReloadTask().execute();
            else
                loadListData();
        }
    }

    protected void loadListData() {
        mFeeds = mFeedDBHelper.getAllFeeds();
        mFavorites = mEpisodeDBHelper.getFavoriteEpisodes();
        mNew = mEpisodeDBHelper.getNewEpisodes();
        mPlaylist = new LinkedList<Episode>(mEpisodeDBHelper.getPlaylistEpisodes());
    }

    /**
     * AsyncTask for refreshing all list content.
     */
    class ListDataReloadTask extends AsyncTask<Void, Void, Void> {
        private boolean cancelled = false;

        @Override
        protected void onPreExecute() {
            if (!cancelled) {
                mRefreshing = true;
            }
        }

        /**
         * Actual refresh method.
         * This function does all the work "in the background". (In this case it gets all the new data from the db)
         */
        @Override
        protected Void doInBackground(Void... params) {
            if (!cancelled) {
                mFeeds = mFeedDBHelper.refreshFeedData(mFeeds, false);
                mFavorites = mEpisodeDBHelper.getFavoriteEpisodes();
                mNew = mEpisodeDBHelper.getNewEpisodes();
                mPlaylist = new LinkedList<Episode>(mEpisodeDBHelper.getPlaylistEpisodes());
            }
            return null;
        }

        /**
         * Fires when Task has been executed.
         * This function replaces all the collections and calls for a redraw.
         */
        @Override
        protected void onPostExecute(Void param) {
            onFeedsDataReloaded();
            onFavoritesDataReloaded();
            onNewDataReloaded();
            onPlaylistDataReloaded();
            mRefreshing = false;
        }

        @Override
        protected void onCancelled(Void result) {
            this.cancelled = true;
            mRefreshing = false;
        }
    }

    /**
     * Should be used to notify the active list that the data has been invalidated.
     * To be overriden.
     */
    protected void invalidate() {
    }


    /**
     * Called when the "Feeds" data gets reloaded. Should notify any adapters.
     */
    protected void onFeedsDataReloaded() {
    }

    /**
     * Called when the "Playlist" data gets reloaded. Should notify any adapters.
     */
    protected void onPlaylistDataReloaded() {
        mPlaylistAdapter.replaceItems(mPlaylist);
        mPlaylistAdapter.notifyDataSetChanged();
    }

    /**
     * Called when the "New" list data gets reloaded. Should notify any adapters.
     */
    protected void onNewDataReloaded() {
        if (((BaseActivity)mContext).currentQuicklistFilter() == BaseActivity.QuicklistFilter.NEW) {
            mQuicklistAdapter.replaceItems(mNew);
        }
        mQuicklistAdapter.notifyDataSetChanged();
    }

    /**
     * Called when the "Favorited" list data gets reloaded. Should notify any adapters.
     */
    protected void onFavoritesDataReloaded() {
        Log.i(LOG_TAG,"Favorites notified!");
        if (((BaseActivity)mContext).currentQuicklistFilter() == BaseActivity.QuicklistFilter.FAVORITES) {
            mQuicklistAdapter.replaceItems(mFavorites);
        }
        mQuicklistAdapter.notifyDataSetChanged();
    }


    /**
     * Checks all stored links to make sure that the referenced files actually exist.
     * The function resets local links of files that can't be found, so that they may be downloaded again.
     * Files can be manually removed from the public external directories, thus this is necessary.
     */
    private void checkFileLinks() {
        for (Feed feed : mFeeds) {
            for (Episode ep : feed.getEpisodes()) {
                if (!ep.getLocalLink().isEmpty()) { //If localLink isn't empty, check that the file exists in external storage.
                    File file = new File(ep.getLocalLink());
                    if (!file.exists()) {
                        Log.w(LOG_TAG, "Couldn't find " + file.getName() + ". Resetting local link for entry: " + ep.getEpisodeId());
                        ep.setLocalLink("");
                        updateEpisode(ep);
                    }
                }
            }
        }
    }

    public void closeDbIfOpen() {
        mEpisodeDBHelper.closeDatabaseIfOpen();
        mFeedDBHelper.closeDatabaseIfOpen();
    }

}
