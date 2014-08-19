/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarderproject.podhoarder.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.adapter.DragNDropAdapter;
import com.podhoarderproject.podhoarder.adapter.FeedDetailsListAdapter;
import com.podhoarderproject.podhoarder.adapter.GridListAdapter;
import com.podhoarderproject.podhoarder.adapter.LatestEpisodesListAdapter;
import com.podhoarderproject.podhoarder.db.EpisodeDBHelper;
import com.podhoarderproject.podhoarder.db.FeedDBHelper;
import com.podhoarderproject.podhoarder.db.PlaylistDBHelper;

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
	
	public 	static final 	SimpleDateFormat 			xmlFormat = new SimpleDateFormat("EEE, d MMM yyy HH:mm:ss Z");	//Used when formatting Timestamps in .xml's
	public 	static final 	SimpleDateFormat 			correctFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");	//Used when formatting timestamps in .xml's
	private 				DownloadManager 			downloadManager;
	private					List<BroadcastReceiver>		broadcastReceivers;

	private 				FeedDBHelper 				fDbH;	//Handles saving the Feed objects to a database for persistence.
	private 				EpisodeDBHelper 			eph;	//Handles saving the Episode objects to a database for persistence.
	public 					PlaylistDBHelper 			plDbH;	//Handles saving the current playlist to a database for persistence.
	public 					GridListAdapter 			feedsListAdapter;	//An expandable list containing all the Feed and their respective Episodes.	
	public					FeedDetailsListAdapter		feedDetailsListAdapter;
	public 					LatestEpisodesListAdapter 	latestEpisodesListAdapter;	//A list containing the newest X episodes of all the feeds.
	public 					DragNDropAdapter 			playlistAdapter;	//A list containing all the downloaded episodes.
	private 				Context	 					context;
	private 				String 						storagePath;
	private 				String 						podcastDir;
	
	private					SwipeRefreshLayout			refreshLayout;

	public PodcastHelper(Context ctx)
	{
		this.context = ctx;
		this.storagePath = Environment.DIRECTORY_PODCASTS;
		this.podcastDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath();
		this.fDbH = new FeedDBHelper(this.context);
		this.eph = new EpisodeDBHelper(this.context);
		this.plDbH = new PlaylistDBHelper(this.context);
		this.downloadManager = (DownloadManager) this.context.getSystemService(Context.DOWNLOAD_SERVICE);
		this.broadcastReceivers = new ArrayList<BroadcastReceiver>();
		checkLocalLinks();
	}

	/**
	 * Adds a feed with the specified url to the database. Automatically
	 * downloads all data(except individual episodes) & an image.
	 * 
	 * @param urlOfFeedToAdd
	 *            Should point to an XML-formatted podcast feed.
	 */
	public void addFeed(String urlOfFeedToAdd)
	{
		if (NetworkUtils.isOnline(context))
		{    	
			this.feedsListAdapter.setLoading(true);
			new FeedReaderTask().execute(urlOfFeedToAdd);
		}
			
		else
			ToastMessages.AddFeedFailed(this.context).show();
	}

	/**
	 * Deletes a Feed and all associated Episodes from storage.
	 * 
	 * @param feedId
	 *            Id of the Feed to delete.
	 */
	public void deleteFeed(int feedId)
	{
		int i = 0;
		this.fDbH.deleteFeed(feedId);
		while (this.feedsListAdapter.feeds.get(i).getFeedId() != feedId
				&& i < this.feedsListAdapter.feeds.size())
		{
			i++;
		}

		// Delete Feed Image
		String fName = this.feedsListAdapter.feeds.get(i).getFeedId() + ".jpg";
		File file = new File(this.context.getFilesDir(), fName);
		boolean check = file.delete();
		if (check)
		{
			Log.w(LOG_TAG, "Feed Image deleted successfully!");
		} else
		{
			Log.w(LOG_TAG, "Feed Image not found! No delete necessary.");
		}
		this.refreshListsAsync();
	}

	/**
	 * Calls a background thread that refreshes all the Feed objects in the db.
	 * (Make sure that before calling this, you have called setRefreshLayout so the Task can update the UI once done.)
	 */
	public void refreshFeeds()
	{
		if (NetworkUtils.isOnline(context))
		{
			List<String> urls = new ArrayList<String>();
			for (Feed f:this.feedsListAdapter.feeds)
			{
				urls.add(f.getLink());
			}
			new FeedRefreshTask().execute(urls);
		}
		else
		{
			if (refreshLayout.isRefreshing())
				refreshLayout.setRefreshing(false);
			ToastMessages.RefreshFailed(this.context).show();
		}
	}
	
	/**
	 * Calls a background thread that refreshes a particular Feed object in the db.
	 * (Make sure that before calling this, you have called setRefreshLayout so the Task can update the UI once done.)
	 * @param id ID of the Feed to be refreshed.
	 */
	public void refreshFeed(int id)
	{
		if (NetworkUtils.isOnline(context))
		{
			List<String> urls = new ArrayList<String>();
			urls.add(this.getFeed(id).getLink());
			new FeedRefreshTask().execute(urls);
		}
		else
		{
			if (refreshLayout.isRefreshing())
				refreshLayout.setRefreshing(false);
			ToastMessages.RefreshFailed(this.context).show();
		}
	}
	
	/**
	 * Downloads a Podcast using the a stored URL in the db.
	 * Podcasts are placed in the public Podcasts-directory.
	 * @param feedId Id of the Feed that the Podcast belongs to.
	 * @param episodeId Id of the Episode within the specified feed.
	 * @author Emil
	 */
	public void downloadEpisode(int feedId, int episodeId)
	{
		final int feedPos = getFeedPositionWithId(feedId);
		final int epPos = getEpisodePositionWithId(feedPos, episodeId);
		
		if (NetworkUtils.isDownloadManagerAvailable(this.context) && !new File(this.feedsListAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getLocalLink()).exists())
		{
			String url = this.feedsListAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getLink();
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setDescription(this.context.getString(R.string.notification_download_in_progress));
			request.setTitle(this.feedsListAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle());
			// in order for this if to run, you must use the android 3.2 to compile your app
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
			    request.allowScanningByMediaScanner();
			    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}
			//TODO: If there is no sdcard, DownloadManager will throw an exception because it cannot save to a non-existing directory. Make sure the directory is valid or something.
			request.setDestinationInExternalPublicDir(this.storagePath, FileUtils.sanitizeFileName(this.feedsListAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle())  + ".mp3");
			
			// register broadcast receiver for when the download is done.
			this.broadcastReceivers.add(new BroadcastReceiver() {
			    public void onReceive(Context ctxt, Intent intent) {
			        // .mp3 files was successfully downloaded. We should update db and list objects to reflect this.
			    	Long dwnId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			    	checkDownloadStatus(dwnId, feedPos, epPos, this);
			    }
			});
			this.context.registerReceiver(this.broadcastReceivers.get(this.broadcastReceivers.size()-1), new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			this.downloadManager.enqueue(request);
			ToastMessages.DownloadingPodcast(this.context).show();
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
	private void downloadCompleted(int feedPos, int epPos, BroadcastReceiver receiver)
	{
		//update list adapter object
		Episode currentEpisode = this.feedsListAdapter.feeds.get(feedPos).getEpisodes().get(epPos);
		
		currentEpisode.setLocalLink(this.podcastDir + "/" + FileUtils.sanitizeFileName(this.feedsListAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle()) + ".mp3");
		
		//update db entry
		this.eph.updateEpisode(currentEpisode);
		Log.i(LOG_TAG, "download completed: " + currentEpisode.getTitle());
		this.context.unregisterReceiver(receiver);
		this.broadcastReceivers.remove(receiver);
		
		this.refreshLists();
	}
		
	/**
	 * Saves an updated Episode object in the db.
	 * @param ep An already existing Episode object with new values.
	 */
	public Episode updateEpisode(Episode ep)
	{
		Episode temp = this.eph.updateEpisode(ep);
		this.refreshListsAsync();
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
			this.refreshListsAsync();
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
		this.refreshListsAsync();
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
	 * Marks all the Episodes of a Feed as listened asynchronously.
	 * @param feed Feed that should be marked as listened.
	 */
	public void markAsListenedAsync(Episode ep)
	{
		new MarkEpisodeAsListenedTask().execute(ep);
	}
	
	/**
     * AsyncTask for marking all the Episodes of a Feed as listened.
     */
    class MarkFeedAsListenedTask extends AsyncTask<Feed, Void, Void> 
    {

        public MarkFeedAsListenedTask() 
        {	}

        /**
         * Fires when Task has been executed.
         * This function associates the downloaded bitmap with the object property.
         */
        @Override
        protected void onPostExecute(Void v) 
        {
        	refreshListsAsync();
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
     * AsyncTask for marking all the Episodes of a Feed as listened.
     */
    class MarkEpisodeAsListenedTask extends AsyncTask<Episode, Void, Void> 
    {

        public MarkEpisodeAsListenedTask() 
        {	}

        /**
         * Fires when Task has been executed.
         * This function associates the downloaded bitmap with the object property.
         */
        @Override
        protected void onPostExecute(Void v) 
        {
        	refreshListsAsync();
        }

		@Override
		protected Void doInBackground(Episode... params)
		{
			Episode ep = params[0];
			if (ep.getTotalTime() > 100) ep.setElapsedTime(ep.getTotalTime());	//If totalTime is more than 100 (ms) that means there's a "real" time stored already. So we set elapsedTime to totalTime.
			else																//Otherwise we make up the value 100 (easy when dealing with percent)
			{
				ep.setTotalTime(100);
				ep.setElapsedTime(100);
			}
			eph.updateEpisode(ep);
			return null;
		}
    }
    
	/**
	 * Deletes the physical mp3-file associated with an Episode, not the Episode object itself.
	 * @param feedId Id of the Feed that the Podcast belongs to.
	 * @param episodeId Id of the Episode within the specified feed.
	 * @author Emil
	 */
	public void deleteEpisodeFile(int feedId, int episodeId)
	{
		int feedPos = getFeedPositionWithId(feedId);
		int epPos = getEpisodePositionWithId(feedPos, episodeId);
		File file = new File(this.feedsListAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getLocalLink());
		if (file.delete())
		{
			this.feedsListAdapter.feeds.get(feedPos).getEpisodes().get(epPos).setLocalLink("");
			this.eph.updateEpisode(this.feedsListAdapter.feeds.get(feedPos).getEpisodes().get(epPos));
			Log.i(LOG_TAG, file.getAbsolutePath() + " deleted successfully!");
		}
		else
		{
			Log.e(LOG_TAG, file.getAbsolutePath() + " not deleted. Make sure it exists.");
		}
		this.refreshListsAsync();
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
		this.refreshLists();
	}
		
	/**
	 * Does the actual sql operations in the insertion process.
	 * @param feed Feed object to insert.
	 * @throws SQLiteConstraintException Is thrown if adding duplicate feeds.
	 */
	private void insertFeedObject(Feed feed) throws SQLiteConstraintException
	{
		try
		{
			feed = this.fDbH.insertFeed(feed);
			Feed newFeed = this.fDbH.getFeed(feed.getFeedId());
			newFeed.getFeedImage().setImageDownloadListener(feedsListAdapter);			
		} 
		catch (SQLiteConstraintException e)
		{
			Log.e(LOG_TAG,
					"NOT A UNIQUE LINK. FEED ALREADY EXISTS IN THE DATABASE?");
			throw e;
		}

	}

	/**
	 * Does the actual sql operations in the refresh process.
	 * @param feed Feed object to insert.
	 * @throws SQLiteConstraintException Is thrown if adding duplicate feeds.
	 */
	private void refreshFeedObjects(List<Feed> feeds) throws SQLiteConstraintException
	{
		try
		{
			for (Feed newFeed : feeds)
			{
				//Get the Feed that's stored locally with the same Id.
				Feed oldFeed = this.fDbH.getFeedByURL(newFeed.getLink());
				//If the new Episode list has more Episodes, we need to add the new Episodes to the db.
				for (int i = 0; i<newFeed.getEpisodes().size(); i++)
				{
					oldFeed.getEpisodes().add(0, newFeed.getEpisodes().get(i));
				}
				
				oldFeed.getFeedImage().imageObject().recycle();
				//Update the Feed with the new Episodes in the db.
				oldFeed = this.fDbH.updateFeed(oldFeed);				
			}
			//Disable the "refreshing" animation.
			this.refreshLayout.setRefreshing(false);
			this.refreshListsAsync();
			ToastMessages.RefreshSuccessful(this.context).show();
		}
		catch (Exception ex)
		{
			Log.e(LOG_TAG, ex.getMessage());
			ToastMessages.RefreshFailed(this.context).show();
			//Disable the "refreshing" animation.
			this.refreshLayout.setRefreshing(false);
		}
	}	
	
	/**
	 * Task used for parsing an XML file that contains podcast data.
	 * @author Emil
	 *
	 */
	private class FeedReaderTask extends AsyncTask<String, Integer, Feed>
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
				// Set the url (you will need to change this to your RSS URL
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
					this.link, this.category, this.img, false, eps, context);
			
			//Insert all the data into the db.
			try
			{
				this.newFeed = fDbH.insertFeed(this.newFeed);
				Feed newFeed2 = fDbH.getFeed(this.newFeed.getFeedId());
				newFeed2.getFeedImage().setImageDownloadListener(feedsListAdapter);	
				return newFeed2;
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
			Log.i(LOG_TAG, "Added Feed: " + result.getTitle());
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
							this.link, this.category, this.img, false, eps, context));
				}
			}
			
			return feeds;
		}

		@Override
		protected void onCancelled()
		{
			if (refreshLayout.isRefreshing()) 
				refreshLayout.setRefreshing(false);
			ToastMessages.RefreshFailed(context).show();
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
				//Disable the "refreshing" animation.
				refreshLayout.setRefreshing(false);
				refreshListsAsync();
				ToastMessages.RefreshSuccessful(context).show();
			} 
			catch (CursorIndexOutOfBoundsException e)
			{
				Log.e(LOG_TAG,"CursorIndexOutOfBoundsException: Refresh failed.");
				refreshLayout.setRefreshing(false);
				ToastMessages.RefreshFailed(context).show();
				cancel(true);
			} 
			catch (SQLiteConstraintException e)
			{
				Log.e(LOG_TAG,"SQLiteConstraintException: Refresh failed.");
				refreshLayout.setRefreshing(false);
				ToastMessages.RefreshFailed(context).show();
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
		for (int i=0; i<this.feedsListAdapter.feeds.size(); i++)
		{
			if (this.feedsListAdapter.feeds.get(i).getFeedId() == feedId)
			{
				retVal = i;
			}
		}
		return retVal;
	}
	
	/**
	 * Retrieves a Feed object using a Feed ID as parameter.
	 * @param feedId ID of the Feed to retrieve.
	 * @return A feed object.
	 */
	public Feed getFeed(int feedId)
	{
		int index = this.getFeedPositionWithId(feedId);
		return this.feedsListAdapter.feeds.get(index);
	}
	
	public BitmapDrawable getFeedImage(int feedId)
	{
		return new BitmapDrawable(this.context.getResources(),getFeed(feedId).getFeedImage().imageObject());
	}
	
	/**
	 * Helper method to find correct location of an Episode object, using it's unique Id.
	 * Remember to use getFeedPositionWithId first, to ensure the correct Feed is used.
	 * @param feedPosition The correct feed position relative to the list, not Id.
	 * @param episodeId ID of the Episode to get the location of.
	 * @return The correct position in the list.
	 */
	private int getEpisodePositionWithId(int feedPosition, int episodeId)
	{
		int retVal = -1;
		for (int i=0; i<this.feedsListAdapter.feeds.get(feedPosition).getEpisodes().size(); i++)
		{
			if (this.feedsListAdapter.feeds.get(feedPosition).getEpisodes().get(i).getEpisodeId() == episodeId)
			{
				retVal = i;
			}
		}
		return retVal;
	}
	
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
		for (Feed currentFeed : this.feedsListAdapter.feeds)
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
	private void checkDownloadStatus(long dwnId, int feedPos, int epPos, BroadcastReceiver source)
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
					   ToastMessages.DownloadFailed(this.context).show();
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
					  downloadCompleted(feedPos, epPos, source);
					  break;
			 }
		 }
	}
	
	/**
	 * Use this to set the current Refresh layout. It will be updated once the FeedRefreshTask is completed.
	 * @param layout	The view to update once the Task is finished.
	 */
	public void setRefreshLayout(SwipeRefreshLayout layout)
	{
		this.refreshLayout = layout;
	}

	/**
	 * Refreshes the lists on each of the fragments simultaneously. This operation is pretty intense because of sorting and selection etc.
	 * You probably want to use refreshListsAsync() instead since it doesn't do all the heavy lifting on the UI thread.
	 * @see refreshListsAsync()
	 */
	public void refreshLists()
	{
		//Update the list adapters to reflect changes.
		//Latest Episodes list
		if (this.latestEpisodesListAdapter != null)	
			this.latestEpisodesListAdapter.replaceItems(this.eph.getLatestEpisodes(Constants.LATEST_EPISODES_COUNT));
		else	
			this.latestEpisodesListAdapter = new LatestEpisodesListAdapter(this.eph.getLatestEpisodes(Constants.LATEST_EPISODES_COUNT), this.context);
		
		//Playlist
		if (this.playlistAdapter != null)	
			//this.playlistAdapter.replaceItems(this.plDbH.sort(this.eph.getDownloadedEpisodes()));
			this.playlistAdapter.replaceItems(this.eph.getPlaylistEpisodes());
		else	
			//this.playlistAdapter = new DragNDropAdapter(this.plDbH.sort(this.eph.getDownloadedEpisodes()), this.context);
			this.playlistAdapter = new DragNDropAdapter(this.eph.getPlaylistEpisodes(), this.context);
		
		//Feeds List
		if (this.feedsListAdapter != null)	
			this.feedsListAdapter.replaceItems(this.fDbH.getAllFeeds());
		else	
			this.feedsListAdapter = new GridListAdapter(this.fDbH.getAllFeeds(), this.context);
		
		//Feed Details List
		if (this.feedDetailsListAdapter != null && this.feedDetailsListAdapter.feed != null)
		{
			this.feedDetailsListAdapter.replaceItems(this.fDbH.getFeed(this.feedDetailsListAdapter.feed.getFeedId()).getEpisodes());
			this.feedDetailsListAdapter.notifyDataSetChanged();
		}
		else if (this.feedDetailsListAdapter == null)
		{
			this.feedDetailsListAdapter = new FeedDetailsListAdapter(this.context);
		}
		
		//Notify for UI updates.
		this.feedsListAdapter.notifyDataSetChanged();
		this.playlistAdapter.notifyDataSetChanged();
		this.latestEpisodesListAdapter.notifyDataSetChanged();
		
	}
	
	/**
	 * Refreshes the lists on each of the fragments asynchronously. This operation is pretty intense because of sorting and selection etc.
	 */
	public void refreshListsAsync()
	{
		new ListRefreshTask().execute();
	}
	
	/**
	 * Refreshes the list on the player fragment.
	 */
	public void refreshPlayList()
	{
		//Playlist
		if (this.playlistAdapter != null)	
			//this.playlistAdapter.replaceItems(this.plDbH.sort(this.eph.getDownloadedEpisodes()));
			this.playlistAdapter.replaceItems(this.eph.getPlaylistEpisodes());
		else	
			//this.playlistAdapter = new DragNDropAdapter(this.plDbH.sort(this.eph.getDownloadedEpisodes()), this.context);
			this.playlistAdapter = new DragNDropAdapter(this.eph.getPlaylistEpisodes(), this.context);
		
		//Notify for UI updates.
		this.playlistAdapter.notifyDataSetChanged();
	}
	
	/**
     * AsyncTask for refreshing list adapters.
     */
    class ListRefreshTask extends AsyncTask<Void, Void, Void> 
    {
    	private List<Episode> latestEpisodes, playlist, feedDetailsEpisodes;
    	private List<Feed> feeds;
    	
    	
        public ListRefreshTask()	
        {
        	
        }

        /**
         * Actual download method.
         * This function does all the work "in the background". (In this case it gets all the new data from the db)
         */
        @Override
        protected Void doInBackground(Void... params) 
        {
        	this.latestEpisodes = eph.getLatestEpisodes(Constants.LATEST_EPISODES_COUNT);
        	this.playlist = eph.getPlaylistEpisodes();
        	this.feeds = fDbH.getAllFeeds();
        	if (feedDetailsListAdapter != null && feedDetailsListAdapter.feed != null)
    		{
    			this.feedDetailsEpisodes = fDbH.getFeed(feedDetailsListAdapter.feed.getFeedId()).getEpisodes();
    		}
        	
        	//Replace and reload the Feeds Grid List.
        	feedsListAdapter.replaceItems(this.feeds);
        	
        	//Replace and reload the Latest Episodes List.
        	latestEpisodesListAdapter.replaceItems(this.latestEpisodes);
        	
        	//Replace and reload the Playlist.
        	playlistAdapter.replaceItems(this.playlist);
        	
        	//Replace and reload the Feed Details List only when a feed is selected.
        	if (feedDetailsListAdapter != null && feedDetailsListAdapter.feed != null)
        	{
        		feedDetailsListAdapter.replaceItems(feedDetailsEpisodes);
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
        	//Notify for UI updates.
        	feedsListAdapter.notifyDataSetChanged();
        	latestEpisodesListAdapter.notifyDataSetChanged();
        	playlistAdapter.notifyDataSetChanged();
        	feedDetailsListAdapter.notifyDataSetChanged();   
        }
    }

    public static boolean episodeExists(String episodeTitle, List<Episode> episodes)
	{
		for (int r = 0; r < episodes.size(); r++)
		{
			if (episodeTitle.equals(episodes.get(r).getTitle())) return true;
		}
		return false;
	}
}
