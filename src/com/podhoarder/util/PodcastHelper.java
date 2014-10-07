/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarder.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.activity.MainActivity.ListFilter;
import com.podhoarder.adapter.DragNDropAdapter;
import com.podhoarder.adapter.EpisodesListAdapter;
import com.podhoarder.adapter.GridAdapter;
import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.db.PlaylistDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.view.CustomSwipeRefreshLayout;
import com.podhoarderproject.podhoarder.R;

/**
 * Helper class that acts as an interface between GUI and SQLite etc.
 * Recommended use is to bind listAdapter to an ExpandableListView. The class
 * ensures that everything is updated automatically.
 * 
 * @see Feed.java
 * @see FeedListAdapter.java
 * @see FeedDBHelper.java
 * 
 */
public class PodcastHelper
{
	private static final 	String 						LOG_TAG = "com.podhoarderproject.podhoarder.PodcastHelper";

	private 				DownloadManager 			downloadManager;
	private					List<BroadcastReceiver>		broadcastReceivers;

	private 				FeedDBHelper 				fDbH;	//Handles saving the Feed objects to a database for persistence.
	private 				EpisodeDBHelper 			eph;	//Handles saving the Episode objects to a database for persistence.
	public 					PlaylistDBHelper 			plDbH;	//Handles saving the current playlist to a database for persistence.
	public 					GridAdapter 				mFeedsGridAdapter;	//An expandable list containing all the Feed and their respective Episodes.	
	public 					EpisodesListAdapter 		mEpisodesListAdapter;	//A list containing the newest X episodes of all the feeds.
	public 					DragNDropAdapter 			mPlaylistAdapter;	//A list containing all the downloaded episodes.
	private 				Context	 					mContext;
	private 				String 						storagePath;
	private 				String 						podcastDir;
	private 				boolean						mRefreshing;
	
	private List<Feed> mFeeds;
	private List<Episode> mLatest, mDownloaded, mNew, mFavorites, mSearch;

	public PodcastHelper(Context ctx)
	{
		mContext = ctx;
		mRefreshing = false;
		storagePath = Environment.DIRECTORY_PODCASTS;
		podcastDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath();
		fDbH = new FeedDBHelper(this.mContext);
		eph = new EpisodeDBHelper(this.mContext);
		plDbH = new PlaylistDBHelper(this.mContext);
		downloadManager = (DownloadManager) this.mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		broadcastReceivers = new ArrayList<BroadcastReceiver>();
		
		mFeeds = new ArrayList<Feed>();
		mLatest = new ArrayList<Episode>();
		mDownloaded = new ArrayList<Episode>();
		mNew = new ArrayList<Episode>();
		mFavorites = new ArrayList<Episode>();
		mSearch = new ArrayList<Episode>();
		
		checkLocalLinks();
	}

	/**
	 * Adds a feed with the specified url to the database. Automatically
	 * downloads all data(except individual episodes) & an image.
	 * 
	 * @param urls
	 *            Should point to XML-formatted podcast feeds.
	 */
	public void addFeed(List<String> urls)
	{
		if (NetworkUtils.isOnline(mContext))
		{
			for (String url : urls)
			{
				this.mFeedsGridAdapter.addLoadingItem();
				new AddFeedFromURLTask().execute(url);
			}
		}
			
		else
			ToastMessages.AddFeedFailed(this.mContext).show();
	}
	
	/**
	 * Adds a Feed from a Search Result Row. Automatically
	 * downloads all data(except individual episode files) & an image.
	 * 
	 * @param itemsToAdd
	 *            A list of the search result objects that you want to add.
	 */
	public void addSearchResults(List<SearchResultRow> itemsToAdd)
	{
		if (NetworkUtils.isOnline(mContext))
		{
			for (SearchResultRow row : itemsToAdd)
			{
				this.mFeedsGridAdapter.addLoadingItem();
				new AddFeedFromSearchTask().execute(row);
			}
		}
			
		else
			ToastMessages.AddFeedFailed(this.mContext).show();
	}

	/**
	 * Deletes a Feed and all associated Episodes from storage.
	 * 
	 * @param feedId
	 *            Id of the Feed to delete.
	 */
	public void deleteFeeds(List<Integer> feedIds)
	{
		this.fDbH.deleteFeeds(feedIds);
		for (int feedId : feedIds)
		{
			// Delete Feed Image
			String fName = feedId + ".jpg";
			File file = new File(this.mContext.getFilesDir(), fName);
			boolean check = file.delete();
			if (check)
			{
				Log.w(LOG_TAG, "Feed Image deleted successfully!");
			} else
			{
				Log.w(LOG_TAG, "Feed Image not found! No delete necessary.");
			}
		}
		this.refreshContent();
	}

	/**
	 * Calls a background thread that refreshes all the Feed objects in the db.
	 * * @param swipeRefreshLayout SwipeRefreshLayout to indicate when the refresh is done.
	 */
	@SuppressWarnings("unchecked")
	public void refreshFeeds(CustomSwipeRefreshLayout swipeRefreshLayout)
	{
		if (NetworkUtils.isOnline(mContext))
		{
			List<String> urls = new ArrayList<String>();
			for (Feed f:this.mFeedsGridAdapter.mItems)
			{
				urls.add(f.getLink());
			}
			new FeedRefreshTask(swipeRefreshLayout).execute(urls);
		}
		else
		{
			//TODO: Cancel refresh view if it started to refresh.
			ToastMessages.RefreshFailed(this.mContext).show();
		}
	}
	
	/**
	 * Calls a background thread that refreshes a particular Feed object in the db.
	 * @param swipeRefreshLayout SwipeRefreshLayout to indicate when the refresh is done.
	 * @param id ID of the Feed to be refreshed.
	 */
	@SuppressWarnings("unchecked")
	public void refreshFeed(CustomSwipeRefreshLayout swipeRefreshLayout, int id)
	{
		if (NetworkUtils.isOnline(mContext))
		{
			List<String> urls = new ArrayList<String>();
			urls.add(this.getFeed(id).getLink());
			new FeedRefreshTask(swipeRefreshLayout).execute(urls);
		}
		else
		{
			//TODO: Cancel refresh view if it started to refresh.
			ToastMessages.RefreshFailed(this.mContext).show();
		}
	}
	
	/**
	 * Downloads a Podcast using the a stored URL in the db.
	 * Podcasts are placed in the public Podcasts-directory.
	 * @param feedId Id of the Feed that the Podcast belongs to.
	 * @param episodeId Id of the Episode within the specified feed.
	 * @author Emil
	 */
	public void downloadEpisode(Episode ep)
	{
		if (NetworkUtils.isDownloadManagerAvailable(this.mContext) && !new File(ep.getLocalLink()).exists())
		{
			String url = ep.getLink();
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setDescription(this.mContext.getString(R.string.notification_download_in_progress));
			request.setTitle(ep.getTitle());
			// in order for this if to run, you must use the android 3.2 to compile your app
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
			    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
			}
			//TODO: If there is no sdcard, DownloadManager will throw an exception because it cannot save to a non-existing directory. Make sure the directory is valid or something.
			request.setDestinationInExternalPublicDir(this.storagePath, FileUtils.sanitizeFileName(ep.getTitle())  + ".mp3");
			
			// register broadcast receiver for when the download is done.
			final Episode epTemp = ep;
			this.broadcastReceivers.add(new BroadcastReceiver() {
			    public void onReceive(Context ctxt, Intent intent) {
			        // .mp3 files was successfully downloaded. We should update db and list objects to reflect this.
			    	Long dwnId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			    	checkDownloadStatus(dwnId, epTemp, this);
			    }
			});
			this.mContext.registerReceiver(this.broadcastReceivers.get(this.broadcastReceivers.size()-1), new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			this.downloadManager.enqueue(request);
			ToastMessages.DownloadingPodcast(this.mContext).show();
		}
		else
		{
			Log.w(LOG_TAG, "Podcast already exists locally. No need to download.");
		}
	}
	
	/**
	 * A function that is called when a Podcast download is completed.
	 * @param feedId Id of the Feed that the Podcast belongs to.
	 * @param episodeId Id of the Episode within the specified feed.
	 * @author Emil
	 */
	private void downloadCompleted(Episode ep, BroadcastReceiver receiver)
	{
		//update list adapter object
		
		ep.setLocalLink(this.podcastDir + "/" + FileUtils.sanitizeFileName(ep.getTitle()) + ".mp3");
		
		//update db entry
		this.eph.updateEpisode(ep);
		Log.i(LOG_TAG, "download completed: " + ep.getTitle());
		this.mContext.unregisterReceiver(receiver);
		this.broadcastReceivers.remove(receiver);
		
		this.forceRefreshContent();
	}
		
	/**
	 * Saves an updated Episode object in the db.
	 * @param ep An already existing Episode object with new values.
	 */
	public Episode updateEpisode(Episode ep)
	{
		Episode temp = this.eph.updateEpisode(ep);
		this.refreshContent();
		return temp;
	}
	
	/**
	 * Saves an updated Episode object in the db. (Does not refresh list adapters afterwards)
	 * @param ep An already existing Episode object with new values.
	 */
	public Episode updateEpisodeNoRefresh(Episode ep)
	{
		Episode temp = this.eph.updateEpisode(ep);
		return temp;
	}
	
	/**
	 * Marks an Episode as listened.
	 * @param ep Episode to mark as listened.
	 */
	public void markAsListened(Episode ep)
	{
		if (ep.getElapsedTime() != ep.getTotalTime() && ep.getTotalTime() != 0)	//Only update Episodes that aren't already listened to.
		{
			if (ep.getTotalTime() > 100) ep.setElapsedTime(ep.getTotalTime());	//If totalTime is more than 100 (ms) that means there's a "real" time stored already. So we set elapsedTime to totalTime.
			else																//Otherwise we make up the value 100 (easy when dealing with percent)
			{
				ep.setTotalTime(100);
				ep.setElapsedTime(100);
			}
			this.eph.updateEpisode(ep);
			this.refreshContent();
		}
	}
	
	/**
	 * Marks all the Episodes in a Feed as listened.
	 * @param feed Feed to mark as listened.
	 */
	public void markAsListened(Feed feed)
	{
		for (Episode ep : feed.getEpisodes())
		{
			if (ep.getTotalTime() > 100) ep.setElapsedTime(ep.getTotalTime());	//If totalTime is more than 100 (ms) that means there's a "real" time stored already. So we set elapsedTime to totalTime.
			else																//Otherwise we make up the value 100 (easy when dealing with percent)
			{
				ep.setTotalTime(100);
				ep.setElapsedTime(100);
			}
		}
		this.eph.bulkUpdateEpisodes(feed.getEpisodes());
		this.refreshContent();
	}
	
	/**
	 * Marks all the Episodes of a Feed as listened asynchronously.
	 * @param feed Feed that should be marked as listened.
	 */
	public void markAsListenedAsync(Feed feed)
	{
		new MarkFeedAsListenedTask().execute(feed);
	}
	
	/**
	 * Marks the Episode as listened asynchronously.
	 * @param ep Episode that should be marked as listened.
	 */
	@SuppressWarnings("unchecked")
	public void markAsListenedAsync(Episode ep)
	{
		List<Episode> eps = new ArrayList<Episode>();
		eps.add(ep);
		new MarkEpisodeAsListenedTask().execute(eps);
	}
	
	/**
	 * Marks Episodes as listened asynchronously.
	 * @param eps List<Episode> containing the Episode objects that should be marked as listened.
	 */
	@SuppressWarnings("unchecked")
	public void markAsListenedAsync(List<Episode> eps)
	{
		new MarkEpisodeAsListenedTask().execute(eps);
	}
	
	/**
     * AsyncTask for marking all the Episodes of a Feed as listened.
     */
    class MarkFeedAsListenedTask extends AsyncTask<Feed, Void, Void> 
    {

        public MarkFeedAsListenedTask() 
        {	}

        @Override
        protected void onPostExecute(Void v) 
        {
        	refreshContent();
        }

		@Override
		protected Void doInBackground(Feed... params)
		{
			for (Episode ep : params[0].getEpisodes())
			{
				if (ep.getTotalTime() > 100) ep.setElapsedTime(ep.getTotalTime());	//If totalTime is more than 100 (ms) that means there's a "real" time stored already. So we set elapsedTime to totalTime.
				else																//Otherwise we make up the value 100 (easy when dealing with percent)
				{
					ep.setTotalTime(100);
					ep.setElapsedTime(100);
				}
			}
			eph.bulkUpdateEpisodes(params[0].getEpisodes());
			return null;
		}
    }
    
    /**
     * AsyncTask for marking the chosen Episodes as listened.
     */
    class MarkEpisodeAsListenedTask extends AsyncTask<List<Episode>, Void, Void> 
    {

        public MarkEpisodeAsListenedTask() 
        {	}

        @Override
        protected void onPostExecute(Void v) 
        {
        	refreshContent();
        }

		@Override
		protected Void doInBackground(List<Episode>... params)
		{
			List<Episode> eps = params[0];
			for (Episode ep : eps)
			{
				if (ep.getTotalTime() > 100) ep.setElapsedTime(ep.getTotalTime());	//If totalTime is more than 100 (ms) that means there's a "real" time stored already. So we set elapsedTime to totalTime.
				else																//Otherwise we make up the value 100 (easy when dealing with percent)
				{
					ep.setTotalTime(100);
					ep.setElapsedTime(100);
				}
				eph.updateEpisode(ep);
			}
			return null;
		}
    }
    
	/**
	 * Deletes the physical mp3-file associated with an Episode, not the Episode object itself.
	 * @param feedId Id of the Feed that the Podcast belongs to.
	 * @param episodeId Id of the Episode within the specified feed.
	 * @author Emil
	 */
	public void deleteEpisodeFile(Episode ep)
	{
		if (ep != null)
		{
			File file = new File(ep.getLocalLink());
			if (file.delete())
			{
				ep.setLocalLink("");
				this.eph.updateEpisode(ep);
				Log.i(LOG_TAG, file.getAbsolutePath() + " deleted successfully!");
			}
			else
			{
				Log.e(LOG_TAG, file.getAbsolutePath() + " not deleted. Make sure it exists.");
			}
		}
	}
	
	/**
	 * Checks all stored links to make sure that the referenced files actually exist.
	 * The function resets local links of files that can't be found, so that they may be downloaded again.
	 * Files can be manually removed from the public external directories, thus this is necessary.
	 * @author Emil 
	 */
	private void checkLocalLinks()
	{
		List<Feed> feeds = this.fDbH.getAllFeeds(false);
		for (int feedNo=0; feedNo<feeds.size(); feedNo++)
		{
			List<Episode> episodes = feeds.get(feedNo).getEpisodes();
			for (int epNo=0; epNo<episodes.size(); epNo++)
			{
				if (!episodes.get(epNo).getLocalLink().equals(""))
				{
					//If localLink isn't empty, check that the file exists in external storage.
					File file = new File(episodes.get(epNo).getLocalLink());
					if (!file.exists())
					{
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
	 * Task used for parsing an XML file that contains podcast data.
	 * @author Emil
	 *
	 */
	private class AddFeedFromURLTask extends AsyncTask<String, Integer, Feed>
	{
		private Feed newFeed;
		private int progressPercent;
		private String title, link, description, category, author, img;

		protected Feed doInBackground(String... urls)
		{
			newFeed = new Feed();
			double percentIncrement;
			List<Episode> eps = new ArrayList<Episode>();
			try
			{
				
				// Set the url
				URL url = new URL(urls[0]);

				// Setup the connection
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				// Connect
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
				{
					// Retreive the XML from the URL
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document doc;
					doc = db.parse(url.openStream());
					doc.getDocumentElement().normalize();

					// This is the root node of each section you want to parse
					NodeList itemLst = doc.getElementsByTagName("item");
					NodeList itemLst2 = doc.getElementsByTagName("channel");

					this.title = 		DataParser.parsePodcastTitle(itemLst2);
					this.link = 		urls[0];
					this.description = 	DataParser.parsePodcastDescription(itemLst2);
					this.author = 		DataParser.parsePodcastAuthor(itemLst2);
					this.category = 	DataParser.parsePodcastCategory(itemLst2);
					this.img = 			DataParser.parsePodcastImageLocation(itemLst2);
					
					percentIncrement = 10.0;
					publishProgress((int) percentIncrement);
					
					// Loop through the XML passing the data to the arrays
					percentIncrement = (itemLst.getLength() / 90);
					for (int i = 0; i < itemLst.getLength(); i++)
					{
						
						Episode ep = DataParser.parseNewEpisode(itemLst.item(i));
						publishProgress((int) percentIncrement);
						eps.add(ep);
					}
				}

			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (DOMException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (ParserConfigurationException e)
			{
				e.printStackTrace();
			} catch (SAXException e)
			{
				e.printStackTrace();
			} catch (SQLiteConstraintException e)
			{
				cancel(true);
			}
			this.newFeed = new Feed(this.title, this.author, this.description,
					this.link, this.category, this.img, false, eps, mContext);
			
			//Insert all the data into the db.
			try
			{
				this.newFeed = fDbH.insertFeed(this.newFeed);
				if (this.newFeed != null)
				{
					this.newFeed.getFeedImage().setImageDownloadListener(mFeedsGridAdapter);	
					return newFeed;
				}
				else
				{
					return null;
				}
			} 
			catch (SQLiteConstraintException e)
			{
				Log.e(LOG_TAG,
						"NOT A UNIQUE LINK. FEED ALREADY EXISTS IN THE DATABASE?");
				throw e;
			}
			
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			setProgressPercent(progress[0]);
		}

		protected void onPostExecute(Feed result)
		{
			if (result != null)
				Log.i(LOG_TAG, "Added Feed: " + result.getTitle());
			else
			{
				mFeedsGridAdapter.resetLoading();
				ToastMessages.AddFeedFailed(mContext);
				Log.e(LOG_TAG, "Couldn't Add Feed!");
			}
		}

		@SuppressWarnings("unused")
		public int getProgressPercent()
		{
			return progressPercent;
		}

		public void setProgressPercent(int progressPercent)
		{
			this.progressPercent = progressPercent;
		}
	}
	
	/**
	 * Task used for parsing an XML file that contains podcast data.
	 * @author Emil
	 *
	 */
	private class AddFeedFromSearchTask extends AsyncTask<SearchResultRow, Integer, Feed>
	{
		private Feed newFeed;
		private int progressPercent;

		protected Feed doInBackground(SearchResultRow... result)
		{
			SearchResultRow data = result[0];
			newFeed = new Feed();
			double percentIncrement;
			List<Episode> eps = new ArrayList<Episode>();
			try
			{
				
					// This is the root node of each section you want to parse
					NodeList itemLst = data.getXml().getElementsByTagName("item");
					
					// Loop through the XML passing the data to the arrays
					percentIncrement = (itemLst.getLength() / 100);
					for (int i = 0; i < itemLst.getLength(); i++)
					{
						
						Episode ep = DataParser.parseNewEpisode(itemLst.item(i));
						publishProgress((int) percentIncrement);
						eps.add(ep);
					}

			} 
			catch (SQLiteConstraintException e)
			{
				cancel(true);
			}
			this.newFeed = new Feed(data.getTitle(), data.getAuthor(), data.getDescription(),
					data.getLink(), data.getCategory(), data.getImageUrl(), false, eps, mContext);
			
			//Insert all the data into the db.
			try
			{
				this.newFeed = fDbH.insertFeed(this.newFeed);
				if (this.newFeed != null)
				{
					this.newFeed.getFeedImage().setImageDownloadListener(mFeedsGridAdapter);	
					return newFeed;
				}
				else
				{
					return null;
				}
			} 
			catch (SQLiteConstraintException e)
			{
				Log.e(LOG_TAG,
						"NOT A UNIQUE LINK. FEED ALREADY EXISTS IN THE DATABASE?");
				throw e;
			}
			
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			setProgressPercent(progress[0]);
		}

		protected void onPostExecute(Feed result)
		{
			if (result != null)
				Log.i(LOG_TAG, "Added Feed: " + result.getTitle());
			else
			{
				mFeedsGridAdapter.resetLoading();
				ToastMessages.AddFeedFailed(mContext);
				Log.e(LOG_TAG, "Couldn't Add Feed!");
			}
		}

		@SuppressWarnings("unused")
		public int getProgressPercent()
		{
			return progressPercent;
		}

		public void setProgressPercent(int progressPercent)
		{
			this.progressPercent = progressPercent;
		}
	}

	/**
	 * Task used for refreshing Feeds.
	 * @param List of Strings containing URLs of the Feeds to be refreshed.
	 * @param Integer Progress Indicator.
	 * @return A Feed object.
	 * @author Emil
	 *
	 */
	private class FeedRefreshTask extends AsyncTask<List<String>, Integer, List<Feed>>
	{
		private int progressPercent;
		private String title, link, description, category, author, img;
		private List<Feed> feeds;
		private List<String> titles;
		private boolean shouldDelete = false;
		private CustomSwipeRefreshLayout swipeRefreshLayout;
		
		public FeedRefreshTask(CustomSwipeRefreshLayout swipeRefreshLayout)
		{
			this.swipeRefreshLayout = swipeRefreshLayout;
		}
		
		protected List<Feed> doInBackground(List<String>... urls)
		{
			this.feeds = new ArrayList<Feed>();
			this.titles = new ArrayList<String>();
			for (String feedLink:urls[0])
			{
				double percentIncrement;
				List<Episode> eps = new ArrayList<Episode>();
				try
				{
					// Set the url (you will need to change this to your RSS URL
					URL url = new URL(feedLink);

					// Setup the connection
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();

					// Connect
					if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
					{
						// Retreive the XML from the URL
						DocumentBuilderFactory dbf = DocumentBuilderFactory
								.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						Document doc;
						doc = db.parse(url.openStream());
						doc.getDocumentElement().normalize();

						// This is the root node.
						NodeList itemLst = doc.getElementsByTagName("item");
						NodeList itemLst2 = doc.getElementsByTagName("channel");

						this.title = DataParser.parsePodcastTitle(itemLst2);
						this.link = feedLink;
						this.description = DataParser.parsePodcastDescription(itemLst2);
						this.author = DataParser.parsePodcastAuthor(itemLst2);
						this.category = DataParser.parsePodcastCategory(itemLst2);
						
						percentIncrement = 10.0;
						publishProgress((int) percentIncrement);
						Feed currentFeed = getFeedWithURL(feedLink);
						
						// Loop through the XML passing the data to the arrays
						percentIncrement = ((100/urls[0].size())/itemLst.getLength());
						for (int i = 0; i < itemLst.getLength(); i++)							//Parse each Episode.
						{
							Episode ep = new Episode();
							Node item = itemLst.item(i);
							if (item.getNodeType() == Node.ELEMENT_NODE)
							{
								
								Element ielem = (Element) item;									//Initialise the base element for an Episode.
								
								ep.setTitle(DataParser.parseEpisodeTitle(ielem));				//Parse Title
								
								if (currentFeed != null)										//Nullcheck
								{
									titles.add(ep.getTitle());
									if (episodeExists(ep.getTitle(), currentFeed.getEpisodes()))//If the current Episode is already in the local list, there's no need to keep processing it.
										continue;												
								}

								ep.setLink(DataParser.parseEpisodeLink(ielem));					//Parse Link

								ep.setPubDate(DataParser.parseEpisodePubDate(ielem));			//Parse Publish date
								
								ep.setDescription(DataParser.parseEpisodeDescription(ielem));	//Parse Description
							}
							publishProgress((int) percentIncrement);							//Update AsyncTask progress
							eps.add(ep);	
						}
						
						if (itemLst.getLength() < (eps.size() + currentFeed.getEpisodes().size()))	//this means that there are fewer Episodes in the XML than in our db. We should remove those that aren't in the XML.
						{
							shouldDelete = true;
						}
						this.img = DataParser.parsePodcastImageLocation(itemLst2);				//We process the image last, because it can potentially take a lot of time and if we discover that we don't need to update anything, this shouldn't be done at all.
					}

				} catch (MalformedURLException e)
				{
					e.printStackTrace();
				} catch (DOMException e)
				{
					e.printStackTrace();
				} catch (IOException e)
				{
					cancel(true);
					Log.e(LOG_TAG, e.getMessage());
				} catch (ParserConfigurationException e)
				{
					e.printStackTrace();
				} catch (SAXException e)
				{
					e.printStackTrace();
				} catch (SQLiteConstraintException e)
				{
					cancel(true);
				} 
				
				if (eps.size() > 0 || shouldDelete)	//If we haven't found any new Episodes, there's no need to add the entire Feed object and process it. 
				{
					feeds.add(new Feed(this.title, this.author, this.description,
							this.link, this.category, this.img, false, eps, mContext));
				}
			}
			
			return feeds;
		}

		@Override
		protected void onCancelled()
		{
			this.swipeRefreshLayout.setRefreshing(false);
			ToastMessages.RefreshFailed(mContext).show();
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			setProgressPercent(progress[0]);
		}

		protected void onPostExecute(List<Feed> feeds)
		{
			try
			{
				for (Feed newFeed : feeds)
				{
					//Get the Feed that's stored locally with the same Id.
					Feed oldFeed = fDbH.getFeedByURL(newFeed.getLink());
					//If the new Episode list has more Episodes, we need to add the new Episodes to the db.
					for (int i = 0; i<newFeed.getEpisodes().size(); i++)
					{
						oldFeed.getEpisodes().add(0, newFeed.getEpisodes().get(i));
					}
					
					if (shouldDelete)	//This is true if we have too many episodes. titles contains all the episode titles from the XML, so we need to remove the Episodes we have that aren't in titles.
					{
						if (titles != null)
						{
							for (int i = 0; i < oldFeed.getEpisodes().size(); i++)
							{
								if (!titles.contains(oldFeed.getEpisodes().get(i).getTitle()))
									oldFeed.getEpisodes().remove(i);
							}
						}
					}
					
					
					oldFeed.getFeedImage().imageObject().recycle();
					//Update the Feed with the new Episodes in the db.
					oldFeed = fDbH.updateFeed(oldFeed);				
				}
				this.swipeRefreshLayout.setRefreshing(false);
				refreshContent();
				ToastMessages.RefreshSuccessful(mContext).show();
			} 
			catch (CursorIndexOutOfBoundsException e)
			{
				Log.e(LOG_TAG,"CursorIndexOutOfBoundsException: Refresh failed.");
				this.swipeRefreshLayout.setRefreshing(false);
				ToastMessages.RefreshFailed(mContext).show();
				cancel(true);
			} 
			catch (SQLiteConstraintException e)
			{
				Log.e(LOG_TAG,"SQLiteConstraintException: Refresh failed.");
				this.swipeRefreshLayout.setRefreshing(false);
				ToastMessages.RefreshFailed(mContext).show();
				cancel(true);
			}
		}

		@SuppressWarnings("unused")
		public int getProgressPercent()
		{
			return progressPercent;
		}

		public void setProgressPercent(int progressPercent)
		{
			this.progressPercent = progressPercent;
		}
		
		
	}
	
	/**
	 * Finds the correct position of a Feed in the listadapter list containing Feed objects.
	 * @param feedId ID of the Feed to get the location of.
	 * @return The correct position in the list.
	 */
	private int getFeedPositionWithId(int feedId)
	{
		int retVal = -1;
		for (int i=0; i<this.mFeeds.size(); i++)
		{
			if (this.mFeeds.get(i).getFeedId() == feedId)
			{
				retVal = i;
			}
		}
		return retVal;
	}
	
	/**
	 * Retrieves a Feed object using a Feed ID as parameter.
	 * @param feedId ID of the Feed to retrieve.
	 * @return A Feed object.
	 */
	public Feed getFeed(int feedId)
	{
		int index = this.getFeedPositionWithId(feedId);
		if (index == -1)
			return null;
		else
			return this.mFeeds.get(index);
	}
	
	/**
	 * Retrieves an Episode object using Episode ID as parameter.
	 * @param episodeId ID of the Episode to retrieve.
	 * @return An Episode object.
	 */
	public Episode getEpisode(int episodeId)
	{
		return this.eph.getEpisode(episodeId);
	}
	
	public BitmapDrawable getFeedImage(int feedId)
	{
		return new BitmapDrawable(this.mContext.getResources(),getFeed(feedId).getFeedImage().imageObject());
	}
	
	@SuppressWarnings("unused")
	private boolean FeedHasEpisode(Feed feed, String episodeTitle)
	{
		for (Episode ep : feed.getEpisodes())
		{
			if (episodeTitle.equals(ep.getTitle())) return true;
		}
		return false;
	}
	
	/**
	 * Get a Feed by supplying an URL. If the URL matches any of the Feeds currently in the db, said Feed is returned.
	 * @param url URL of the Feed that should be returned.
	 * @return The Feed with a matching URL, or null if no match is found.
	 */
	private Feed getFeedWithURL(String url)
	{
		for (Feed currentFeed : this.mFeedsGridAdapter.mItems)
		{
			if (url.equals(currentFeed.getLink())) return currentFeed;
		}
		return null;
	}
	
	/**
	 * Checks the status of the latest download.
	 * @param feedPos Id of the Feed that the Podcast belongs to.
	 * @param epPos Id of the Episode within the specified feed.
	 * @author Emil
	 */
	private void checkDownloadStatus(long dwnId, Episode ep, BroadcastReceiver source)
	{
		 DownloadManager.Query query = new DownloadManager.Query();
		 query.setFilterById(dwnId);
		 Cursor cursor = downloadManager.query(query);
		 if(cursor.moveToFirst())
		 {
			  int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
			  int status = cursor.getInt(columnIndex);
			  int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
			  int reason = cursor.getInt(columnReason);
			 
			  switch(status)
			  {
				  case DownloadManager.STATUS_FAILED:
					   String failedReason = "";
					   switch(reason){
						   case DownloadManager.ERROR_CANNOT_RESUME:
							    failedReason = "ERROR_CANNOT_RESUME";
							    break;
						   case DownloadManager.ERROR_DEVICE_NOT_FOUND:
							    failedReason = "ERROR_DEVICE_NOT_FOUND";
							    break;
						   case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
							    failedReason = "ERROR_FILE_ALREADY_EXISTS";
							    break;
						   case DownloadManager.ERROR_FILE_ERROR:
							    failedReason = "ERROR_FILE_ERROR";
							    break;
						   case DownloadManager.ERROR_HTTP_DATA_ERROR:
							    failedReason = "ERROR_HTTP_DATA_ERROR";
							    break;
						   case DownloadManager.ERROR_INSUFFICIENT_SPACE:
							    failedReason = "ERROR_INSUFFICIENT_SPACE";
							    break;
						   case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
							    failedReason = "ERROR_TOO_MANY_REDIRECTS";
							    break;
						   case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
							    failedReason = "ERROR_UNHANDLED_HTTP_CODE";
							    break;
						   case DownloadManager.ERROR_UNKNOWN:
							    failedReason = "ERROR_UNKNOWN";
							    break;
					   }
					   ToastMessages.DownloadFailed(this.mContext).show();
					   Log.i(LOG_TAG, "FAILED: " + failedReason);
					   break;
					   
				  case DownloadManager.STATUS_PAUSED:
					  String pausedReason = "";
					  switch(reason)
					  {
					  	case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
					  		pausedReason = "PAUSED_QUEUED_FOR_WIFI";
						    break;
					  	case DownloadManager.PAUSED_UNKNOWN:
					  		pausedReason = "PAUSED_UNKNOWN";
						    break;
					  	case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
					  		pausedReason = "PAUSED_WAITING_FOR_NETWORK";
						    break;
					  	case DownloadManager.PAUSED_WAITING_TO_RETRY:
					  		pausedReason = "PAUSED_WAITING_TO_RETRY";
					  		break;
					   }
					   Log.i(LOG_TAG, "DOWNLOAD PAUSED: " + pausedReason);
					   break;
				  case DownloadManager.STATUS_PENDING:
					  Log.i(LOG_TAG, "DOWNLOAD PENDING");
					  break;
				  case DownloadManager.STATUS_RUNNING:
					  Log.i(LOG_TAG, "DOWNLOAD RUNNING");
					  break;
				  case DownloadManager.STATUS_SUCCESSFUL:
					  //Download was successful. We should update db etc.
					  downloadCompleted(ep, source);
					  break;
			 }
		 }
	}
	
	/**
	 * Initializes adapter objects.
	 */
	public void setupAdapters()
	{
		//TODO: If the filter points to for example favorites, and the favorites list is empty, the listview will not initialize. Have to always start with items in list and gridviews.
		mFeeds = fDbH.getAllFeeds(true);
		switch (((MainActivity)mContext).getFilter())
    	{
        	case ALL:
        		
        		mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
        		mEpisodesListAdapter = new EpisodesListAdapter(mFeeds.get(0).getEpisodes(), mContext);
        		break;
        	case NEW:
        		mNew = eph.getNewEpisodes();
        		mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
        		mEpisodesListAdapter = new EpisodesListAdapter(mNew, mContext);
        		break;
        	case LATEST:
        		mLatest = eph.getLatestEpisodes();
        		mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
        		mEpisodesListAdapter = new EpisodesListAdapter(mLatest, mContext);
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
			case SEARCH:
				mSearch = eph.search(((MainActivity)mContext).getSearchString());
				mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
				mEpisodesListAdapter = new EpisodesListAdapter(mSearch, mContext);
				break;
			default:
				mFeedsGridAdapter = new GridAdapter(mFeeds, mContext);
				mEpisodesListAdapter = new EpisodesListAdapter(new ArrayList<Episode>(), mContext);
				break;
    	}
		mPlaylistAdapter = new DragNDropAdapter(eph.getPlaylistEpisodes(), mContext);
		new loadListsTask().execute();
	}

	private class loadListsTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			if (mFeeds.isEmpty())
				mFeeds = fDbH.getAllFeeds(true);
			if (mLatest.isEmpty())
				mLatest = eph.getLatestEpisodes();
			if (mNew.isEmpty())
				mNew = eph.getNewEpisodes();
			if (mDownloaded.isEmpty())
				mDownloaded = eph.getDownloadedEpisodes();
			if (mFavorites.isEmpty())
				mFavorites = eph.getFavoriteEpisodes();
			if (mSearch.isEmpty())
				mSearch = eph.search(((MainActivity)mContext).getSearchString());

			
			return null;
		}
	}
	
	public void switchLists()
	{
		ListFilter mCurrentFilter = ((MainActivity)mContext).getFilter();
		switch (mCurrentFilter)
    	{
        	case ALL:
        		mFeedsGridAdapter.replaceItems(mFeeds);
        		break;
        	case FEED:
        		int feedPos = getFeedPositionWithId(((MainActivity)mContext).getFilter().getFeedId());
        		mEpisodesListAdapter.replaceItems(mFeeds.get(feedPos).getEpisodes());
        		break;
        	case NEW:
        		mEpisodesListAdapter.replaceItems(mNew);
        		break;
        	case LATEST:
        		mEpisodesListAdapter.replaceItems(mLatest);
        		break;
        	case DOWNLOADED:
        		mEpisodesListAdapter.replaceItems(mDownloaded);
        		break;
        	case FAVORITES:
        		mEpisodesListAdapter.replaceItems(mFavorites);
        		break;
			case SEARCH:
				mSearch = eph.search(((MainActivity)mContext).getSearchString());
				mEpisodesListAdapter.replaceItems(mSearch);
				break;
			default:
				break;
    	}
	}
	
	public void invalidate()
	{
		ListFilter mCurrentFilter = ((MainActivity)mContext).getFilter();
		//Notify for UI updates.
    	if (mCurrentFilter == ListFilter.ALL && mCurrentFilter.getFeedId() == 0)
    		mFeedsGridAdapter.notifyDataSetChanged();
    	else
    		mEpisodesListAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Refreshes the list or grid view.
	 */
	public void refreshContent()
	{
		if (!mRefreshing)
			new ListRefreshTask().execute();
		else
			Log.d(LOG_TAG, "Refresh currently in progress. Not running RefreshListsAsync()!");
	}
	
	/**
	 * Forces a refresh on the list or grid view.
	 */
	public void forceRefreshContent()
	{
		new ListRefreshTask().execute();
	}
	
	/**
     * AsyncTask for refreshing list adapters.
     */
    class ListRefreshTask extends AsyncTask<Void, Void, Void> 
    {
    	private ListFilter mCurrentFilter;
    	private boolean cancelled = false;
    	
        public ListRefreshTask()	
        {
        	
        }
        
        @Override
        protected void onPreExecute() 
        {
        	if (!cancelled)
        	{
        		mRefreshing = true;
            	mCurrentFilter = ((MainActivity)mContext).getFilter();
        	}
        }

        /**
         * Actual refresh method.
         * This function does all the work "in the background". (In this case it gets all the new data from the db)
         */
        @Override
        protected Void doInBackground(Void... params) 
        {
        	if (!cancelled)
        	{
        		mFeeds = fDbH.refreshFeedData(mFeedsGridAdapter.mItems, false);
        		mNew = eph.getNewEpisodes();
        		mLatest = eph.getLatestEpisodes();
        		mDownloaded = eph.getDownloadedEpisodes();
        		mFavorites = eph.getFavoriteEpisodes();
				mSearch = eph.search(((MainActivity)mContext).getSearchString());
        	}
    		return null;
        }

        /**
         * Fires when Task has been executed.
         * This function replaces all the collections and calls for a redraw.
         */
        @Override
        protected void onPostExecute(Void param) 
        {
        	switch (mCurrentFilter)
        	{
	        	case ALL:
	        		mFeedsGridAdapter.replaceItems(mFeeds);
	        		break;
	        	case FEED:
	        		int feedPos = getFeedPositionWithId(((MainActivity)mContext).getFilter().getFeedId());
	        		mEpisodesListAdapter.replaceItems(mFeeds.get(feedPos).getEpisodes());
	        		break;
	        	case NEW:
	        		mEpisodesListAdapter.replaceItems(mNew);
	        		break;
	        	case LATEST:
	        		mEpisodesListAdapter.replaceItems(mLatest);
	        		break;
	        	case DOWNLOADED:
	        		mEpisodesListAdapter.replaceItems(mDownloaded);
	        		break;
	        	case FAVORITES:
	        		mEpisodesListAdapter.replaceItems(mFavorites);
	        		break;
				case SEARCH:
					mEpisodesListAdapter.replaceItems(mSearch);
					break;
				default:
					break;
        	}
        	mRefreshing = false;
        }
        
        @Override
        protected void onCancelled(Void result)
        {
        	this.cancelled = true;
        	mRefreshing = false;
        	//Notify for UI updates.
        	invalidate();
        }
    }

    /**
	 * Refreshes the playlist adapter.
	 */
	public void refreshPlayList()
	{
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
    
    public static boolean episodeExists(String episodeTitle, List<Episode> episodes)
	{
		for (int r = 0; r < episodes.size(); r++)
		{
			if (episodeTitle.equals(episodes.get(r).getTitle())) return true;
		}
		return false;
	}
    
    public boolean feedExists(String feedURL)
	{
    	List<Feed> feeds = this.mFeedsGridAdapter.mItems;
		for (int r = 0; r < feeds.size(); r++)
		{
			if (feedURL.equals(feeds.get(r).getLink())) return true;
		}
		return false;
	}

    /**
     * Closes all open db connections to prevent leaks.
     */
    public void closeDbIfOpen()
    {
    	this.eph.closeDatabaseIfOpen();
    	this.fDbH.closeDatabaseIfOpen();
    }
}
