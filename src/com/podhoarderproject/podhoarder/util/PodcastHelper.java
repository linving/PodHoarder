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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.adapter.DragNDropAdapter;
import com.podhoarderproject.podhoarder.adapter.FeedListAdapter;
import com.podhoarderproject.podhoarder.adapter.LatestEpisodesListAdapter;
import com.podhoarderproject.podhoarder.db.EpisodeDBHelper;
import com.podhoarderproject.podhoarder.db.FeedDBHelper;
import com.podhoarderproject.podhoarder.db.PlaylistDBHelper;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

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
	
	public 	static final 	SimpleDateFormat 			xmlFormat = new SimpleDateFormat("EEE, d MMM yyy HH:mm:ss Z");	//Used when formatting timestamps in .xml's
	public 	static final 	SimpleDateFormat 			correctFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");	//Used when formatting timestamps in .xml's
	
	private final 			String 						strPref_Download_ID = "PREF_DOWNLOAD_ID";	//Used with the DownloadManager to store request ID in SharedPreferences.
	
	private 				SharedPreferences 			preferenceManager;
	private 				DownloadManager 			downloadManager;

	private 				FeedDBHelper 				fDbH;	//Handles saving the Feed objects to a database for persistence.
	private 				EpisodeDBHelper 			eph;	//Handles saving the Episode objects to a database for persistence.
	public 					PlaylistDBHelper 			plDbH;	//Handles saving the current playlist to a database for persistence.
	public 					FeedListAdapter 			listAdapter;	//An expandable list containing all the Feed and their respective Episodes.	
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
		this.listAdapter = new FeedListAdapter(this.fDbH.getAllFeeds(), this.context);
		this.latestEpisodesListAdapter = new LatestEpisodesListAdapter(this.eph.getLatestEpisodes(100), this.context);
		this.playlistAdapter = new DragNDropAdapter(this.plDbH.sort(this.eph.getDownloadedEpisodes()), this.context);
		preferenceManager = PreferenceManager.getDefaultSharedPreferences(this.context);
		// get download service and enqueue file
		this.downloadManager = (DownloadManager) this.context.getSystemService(Context.DOWNLOAD_SERVICE);
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
		new FeedReaderTask().execute(urlOfFeedToAdd);
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
		while (this.listAdapter.feeds.get(i).getFeedId() != feedId
				&& i < this.listAdapter.feeds.size())
		{
			i++;
		}

		// Delete Feed Image
		String fName = this.listAdapter.feeds.get(i).getFeedId() + ".jpg";
		File file = new File(this.context.getFilesDir(), fName);
		boolean check = file.delete();
		if (check)
		{
			Log.w(LOG_TAG, "Feed Image deleted successfully!");
		} else
		{
			Log.w(LOG_TAG, "Feed Image not found! No delete necessary.");
		}
		this.refreshLists();
	}

	/**
	 * Calls a background thread that refreshes all the Feed objects in the db.
	 * (Make sure that before calling this, you have called setRefreshLayout so the Task can update the UI once done.)
	 */
	public void refreshFeeds()
	{
		List<String> urls = new ArrayList<String>();
		for (Feed f:this.listAdapter.feeds)
		{
			urls.add(f.getLink());
		}
		new FeedRefreshTask().execute(urls);
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
		
		if (isDownloadManagerAvailable(this.context) && !new File(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getLocalLink()).exists())
		{
			String url = this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getLink();
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setDescription(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle());
			request.setTitle(this.context.getString(R.string.app_name) + " " + this.context.getString(R.string.notification_download));
			// in order for this if to run, you must use the android 3.2 to compile your app
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
			    request.allowScanningByMediaScanner();
			    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}
			//TODO: If there is no sdcard, DownloadManager will throw an exception because it cannot save to a non-existing directory. Make sure the directory is valid or something.
			request.setDestinationInExternalPublicDir(this.storagePath, sanitizeFileName(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle())  + ".mp3");
			
			// register broadcast receiver for when the download is done.
			BroadcastReceiver onComplete=new BroadcastReceiver() {
			    public void onReceive(Context ctxt, Intent intent) {
			        // .mp3 files was successfully downloaded. We should update db and list objects to reflect this.
			    	checkDownloadStatus(feedPos, epPos);
			    }
			};
			this.context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			long id = this.downloadManager.enqueue(request);
			//Save the request id
			Editor PrefEdit = preferenceManager.edit();
			PrefEdit.putLong(strPref_Download_ID, id);
			PrefEdit.commit();
			
			Toast notification = Toast.makeText(context, "Downloading Podcast.",
					Toast.LENGTH_SHORT);
			notification.show();
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
	private void downloadCompleted(int feedPos, int epPos)
	{
		//update list adapter object
		Episode currentEpisode = this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos);
		
		currentEpisode.setLocalLink(this.podcastDir + "/" + sanitizeFileName(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle()) + ".mp3");
		
		//If the total duration of the .mp3 file isn't already stored, we need to access the file to retrieve it.
		if (currentEpisode.getTotalTime() == 0)
		{
			//A MediaMetadataRetriever is used to extract the duration of an Episode from the downloaded .mp3 file.
			MediaMetadataRetriever r = new MediaMetadataRetriever();
			//Point the MediaMetadataRetriever to our recently downloaded file.
			r.setDataSource(currentEpisode.getLocalLink());
			//Extract the duration in milliseconds.
			currentEpisode.setTotalTime(Integer.parseInt(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
			//Release MediaMetadataRetriever to free up system resources.
			r.release();
		}
		
		//update db entry
		this.eph.updateEpisode(currentEpisode);
		
		this.refreshLists();
	}
	
	private static String sanitizeFileName(String fileName)
	{
		String retString = fileName;
		retString = retString.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
		if (retString.length() > 30) retString = retString.substring(0, 25);
		return retString;
	}
	
	/**
	 * Saves an updated Episode object in the db.
	 * @param ep An already existing Episode object with new values.
	 */
	public Episode updateEpisode(Episode ep)
	{
		Episode temp = this.eph.updateEpisode(ep);
		this.refreshLists();
		return temp;
	}
	
	/**
	 * Deletes the physical mp3-file associated with an Episode, not the Episode object itself.
	 * @param feedId Id of the Feed that the Podcast belongs to.
	 * @param episodeId Id of the Episode within the specified feed.
	 * @author Emil
	 */
	public void deleteEpisode(int feedId, int episodeId)
	{
		int feedPos = getFeedPositionWithId(feedId);
		int epPos = getEpisodePositionWithId(feedPos, episodeId);
		File file = new File(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getLocalLink());
		if (file.delete())
		{
			this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).setLocalLink("");
			this.eph.updateEpisode(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos));
			this.plDbH.deleteEntry(episodeId);
			Log.i(LOG_TAG, file.getAbsolutePath() + " deleted successfully!");
		}
		else
		{
			Log.e(LOG_TAG, file.getAbsolutePath() + " not deleted. Make sure it exists.");
		}
		this.refreshLists();
	}
	
	/**
	 * Checks all stored links to make sure that the referenced files actually exist on startup.
	 * The function resets local links of files that can't be found, so that they may be downloaded again.
	 * Files can be manually removed from the public external directories, thus this is necessary.
	 * @author Emil 
	 */
	private void checkLocalLinks()
	{
		List<Feed> feeds = this.listAdapter.feeds;
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
						this.listAdapter.feeds.get(feedNo).getEpisodes().get(epNo).setLocalLink("");
						Episode temp = this.listAdapter.feeds.get(feedNo).getEpisodes().get(epNo);
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
			this.listAdapter.feeds.add(feed);
			this.refreshLists();
		} catch (SQLiteConstraintException e)
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
		//TODO: Make sure this works correctly.
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
				
				oldFeed.getFeedImage().imageObject().getBitmap().recycle();
				//Update the Feed with the new Episodes in the db.
				this.fDbH.updateFeed(oldFeed);
			}
			//TODO: Replace with String resource.
			Toast.makeText(context, "Feeds refreshed!", Toast.LENGTH_SHORT).show();
			//Disable the "refreshing" animation.
			this.refreshLayout.setRefreshing(false);
			this.refreshLists();
		}
		catch (Exception ex)
		{
			//TODO: Replace with String resource.
			Toast.makeText(context, "Refresh failed!", Toast.LENGTH_SHORT).show();
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
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();

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

					// This is the root node of each section you want to parse
					NodeList itemLst = doc.getElementsByTagName("item");
					NodeList itemLst2 = doc.getElementsByTagName("channel");

					this.title = ((Element) itemLst2.item(0))
							.getElementsByTagName("title").item(0)
							.getChildNodes().item(0).getNodeValue();
					this.link = urls[0];
					this.description = ((Element) itemLst2.item(0))
							.getElementsByTagName("description").item(0)
							.getChildNodes().item(0).getNodeValue();
					this.author = ((Element) itemLst2.item(0))
							.getElementsByTagName("itunes:author").item(0)
							.getChildNodes().item(0).getNodeValue();
					this.category = ((Element) itemLst2.item(0))
							.getElementsByTagName("itunes:category").item(0)
							.getAttributes().item(0).getNodeValue();
					this.img = ((Element) itemLst2.item(0))
							.getElementsByTagName("itunes:image").item(0)
							.getAttributes().item(0).getNodeValue();
					percentIncrement = 10.0;
					publishProgress((int) percentIncrement);
					
					// Loop through the XML passing the data to the arrays
					percentIncrement = (itemLst.getLength() / 100);
					for (int i = 0; i < itemLst.getLength(); i++)
					{
						Episode ep = new Episode();
						Node item = itemLst.item(i);
						if (item.getNodeType() == Node.ELEMENT_NODE)
						{
							Element ielem = (Element) item;

							// This section gets the elements from the XML.
							NodeList title = ielem.getElementsByTagName("title");
							NodeList link = ielem.getElementsByTagName("enclosure");
							NodeList pubDate = ielem.getElementsByTagName("pubDate");
							NodeList description = ielem.getElementsByTagName("description");	//Try to get the description tag first. 
							NodeList content = ielem.getElementsByTagName("content:encoded");	//If the description tag doesn't contain anything, get the content:encoded tag data instead.

							// Extract relevant data from the NodeList objects.
							//EPISODE TITLE
							try
							{
								ep.setTitle(title.item(0).getChildNodes()
										.item(0).getNodeValue());
							} catch (NullPointerException e)
							{
								e.printStackTrace();
							}
							//URL LINK
							try
							{
								ep.setLink(link.item(0).getAttributes().getNamedItem("url").getNodeValue());	//Extract the attributes from the NodeList, and then extract value of the attribute named "url".
							} catch (NullPointerException e)
							{
								e.printStackTrace();
							}
							//PUBLISH DATE
							try
							{
								String val = pubDate.item(0).getChildNodes().item(0).getNodeValue();
								try
								{
									Date date = xmlFormat.parse(val);
									ep.setPubDate(correctFormat.format(date));
								} catch (ParseException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							catch (NullPointerException e)
							{
								e.printStackTrace();
							}
							//DESCRIPTION
							try
							{
								ep.setDescription(description.item(0)
										.getChildNodes().item(0).getNodeValue());
								if (ep.getDescription().isEmpty()){
									ep.setDescription(content.item(0)
											.getChildNodes().item(0).getNodeValue());
								}
								
							}
							catch (NullPointerException e)
							{
								e.printStackTrace();
							}
						}
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
					this.link, this.category, this.img, true, eps, context);
			return newFeed;
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			setProgressPercent(progress[0]);
		}

		protected void onPostExecute(Feed result)
		{
			try
			{
				insertFeedObject(this.newFeed);
				// TODO: Change Strings value instead of hardcoded.
				Toast notification = Toast.makeText(context, "Feed added!",
						Toast.LENGTH_LONG);
				notification.show();
			} 
			catch (CursorIndexOutOfBoundsException e)
			{
				Log.e(LOG_TAG,
						"CursorIndexOutOfBoundsException: Insert failed. Feed link not unique?");
				// TODO: Change Strings value instead of hardcoded.
				Toast notification = Toast
						.makeText(context,
								"You can't add the same feed twice!",
								Toast.LENGTH_LONG);
				notification.show();
				cancel(true);
			} 
			catch (SQLiteConstraintException e)
			{
				Log.e(LOG_TAG,
						"SQLiteConstraintException: Insert failed. Feed link not unique?");
				// TODO: Change Strings value instead of hardcoded.
				Toast notification = Toast
						.makeText(context,
								"You can't add the same feed twice!",
								Toast.LENGTH_LONG);
				notification.show();
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
		protected List<Feed> doInBackground(List<String>... urls)
		{
			this.feeds = new ArrayList<Feed>();
			for (String feedLink:urls[0])
			{
				double percentIncrement;
				List<Episode> eps = new ArrayList<Episode>();
				try
				{
					// Set the url (you will need to change this to your RSS URL
					URL url = new URL(feedLink);

					// Setup the connection
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();

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

						this.title = ((Element) itemLst2.item(0))
								.getElementsByTagName("title").item(0)
								.getChildNodes().item(0).getNodeValue();
						this.link = feedLink;
						this.description = ((Element) itemLst2.item(0))
								.getElementsByTagName("description").item(0)
								.getChildNodes().item(0).getNodeValue();
						this.author = ((Element) itemLst2.item(0))
								.getElementsByTagName("itunes:author").item(0)
								.getChildNodes().item(0).getNodeValue();
						this.category = ((Element) itemLst2.item(0))
								.getElementsByTagName("itunes:category").item(0)
								.getAttributes().item(0).getNodeValue();
						
						percentIncrement = 10.0;
						publishProgress((int) percentIncrement);
						Feed currentFeed = getFeedWithURL(feedLink);
						
						// Loop through the XML passing the data to the arrays
						percentIncrement = ((100/urls[0].size())/itemLst.getLength());
						for (int i = 0; i < itemLst.getLength(); i++)
						{
							Episode ep = new Episode();
							Node item = itemLst.item(i);
							if (item.getNodeType() == Node.ELEMENT_NODE)
							{
								Element ielem = (Element) item;
								
								// This section adds an entry to the arrays with the
								// data retrieved from above. I have surrounded each
								// with try/catch just incase the element does not
								// exist
								try
								{
									NodeList title = ielem.getElementsByTagName("title");
									ep.setTitle(title.item(0).getChildNodes()
											.item(0).getNodeValue());
									
									if (currentFeed != null)
									{
										if (episodeExists(ep.getTitle(), currentFeed.getEpisodes())) continue;	//If the current Episode is already in the local list, there's no need to keep processing it.
									}
								} catch (NullPointerException e)
								{
									e.printStackTrace();
								}

								try
								{
									NodeList link = ielem.getElementsByTagName("enclosure");
									ep.setLink(link.item(0).getAttributes().getNamedItem("url").getNodeValue());	//Extract the attributes from the NodeList, and then extract value of the attribute named "url".
								} catch (NullPointerException e)
								{
									e.printStackTrace();
								}

								try
								{
									NodeList pubDate = ielem.getElementsByTagName("pubDate");	//Extract pubdate data from the XML.
									
									String val = pubDate.item(0).getChildNodes().item(0).getNodeValue();
									try
									{
										Date date = xmlFormat.parse(val);
										ep.setPubDate(correctFormat.format(date));
									} catch (ParseException e)
									{
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								} catch (NullPointerException e)
								{
									e.printStackTrace();
								}

								try
								{
									NodeList description = ielem
											.getElementsByTagName("description");	//Try to get the description tag first. 
									NodeList content = ielem
											.getElementsByTagName("content:encoded");	//If the description tag doesn't contain anything, get the content:encoded tag data instead.

									ep.setDescription(description.item(0)
											.getChildNodes().item(0).getNodeValue());
									if (ep.getDescription().isEmpty()){
										ep.setDescription(content.item(0)
												.getChildNodes().item(0).getNodeValue());
									}
									
								} catch (NullPointerException e)
								{
									e.printStackTrace();
								}
							}
							publishProgress((int) percentIncrement);
							eps.add(ep);	
						}
						//We process the image last, because it can potentially take a lot of time and if we discover that we don't need to update anything, this shouldn't be done at all.
						this.img = ((Element) itemLst2.item(0))
								.getElementsByTagName("itunes:image").item(0)
								.getAttributes().item(0).getNodeValue();
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
				if (eps.size() > 0)	//If we haven't found any new Episodes, there's no need to add the entire Feed object and process it. 
				{
					
					feeds.add(new Feed(this.title, this.author, this.description,
							this.link, this.category, this.img, false, eps, context));
				}
			}
			
			return feeds;
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			setProgressPercent(progress[0]);
		}

		protected void onPostExecute(List<Feed> result)
		{
			try
			{
				refreshFeedObjects(this.feeds);
			} 
			catch (CursorIndexOutOfBoundsException e)
			{
				Log.e(LOG_TAG,"CursorIndexOutOfBoundsException: Refresh failed.");
				refreshLayout.setRefreshing(false);
				//TODO: Replace with String resource
				Toast.makeText(context, "Refresh failed!", Toast.LENGTH_SHORT).show();
				cancel(true);
			} 
			catch (SQLiteConstraintException e)
			{
				Log.e(LOG_TAG,"SQLiteConstraintException: Refresh failed.");
				refreshLayout.setRefreshing(false);
				//TODO: Replace with String resource
				Toast.makeText(context, "Refresh failed!", Toast.LENGTH_SHORT).show();
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
		
		private boolean episodeExists(String episodeTitle, List<Episode> episodes)
		{
			for (int r = 0; r < episodes.size(); r++)
			{
				if (episodeTitle.equals(episodes.get(r).getTitle())) return true;
			}
			return false;
		}
	}
	
	/**
	 *	Used to check the device version and DownloadManager information.
	 * 
	 * @param context Context object.
	 * @return true if the download manager is available
	 */
	private static boolean isDownloadManagerAvailable(Context context)
	{
		try
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
			{
				return false;
			}
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName("com.android.providers.downloads.ui",
					"com.android.providers.downloads.ui.DownloadList");
			List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
		} 
		catch (Exception e)
		{
			return false;
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
		for (int i=0; i<this.listAdapter.feeds.size(); i++)
		{
			if (this.listAdapter.feeds.get(i).getFeedId() == feedId)
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
		return this.listAdapter.feeds.get(index);
	}
	
	public BitmapDrawable getFeedImage(int feedId)
	{
		return getFeed(feedId).getFeedImage().imageObject();
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
		for (int i=0; i<this.listAdapter.feeds.get(feedPosition).getEpisodes().size(); i++)
		{
			if (this.listAdapter.feeds.get(feedPosition).getEpisodes().get(i).getEpisodeId() == episodeId)
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
		for (Feed currentFeed : this.listAdapter.feeds)
		{
			if (url.equals(currentFeed.getLink())) return currentFeed;
		}
		return null;
	}
	
	/**
	 * Checks the status of the latest download.
	 * @param feedId Id of the Feed that the Podcast belongs to.
	 * @param episodeId Id of the Episode within the specified feed.
	 * @author Emil
	 */
	private void checkDownloadStatus(int feedPos, int epPos)
	{
		
		 // TODO Auto-generated method stub
		 DownloadManager.Query query = new DownloadManager.Query();
		 long id = preferenceManager.getLong(strPref_Download_ID, 0);
		 query.setFilterById(id);
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
							 //TODO: Replace with String resource
							    failedReason = "ERROR_CANNOT_RESUME";
							    break;
						   case DownloadManager.ERROR_DEVICE_NOT_FOUND:
							 //TODO: Replace with String resource
							    failedReason = "ERROR_DEVICE_NOT_FOUND";
							    break;
						   case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
							 //TODO: Replace with String resource
							    failedReason = "ERROR_FILE_ALREADY_EXISTS";
							    break;
						   case DownloadManager.ERROR_FILE_ERROR:
							 //TODO: Replace with String resource
							    failedReason = "ERROR_FILE_ERROR";
							    break;
						   case DownloadManager.ERROR_HTTP_DATA_ERROR:
							 //TODO: Replace with String resource
							    failedReason = "ERROR_HTTP_DATA_ERROR";
							    break;
						   case DownloadManager.ERROR_INSUFFICIENT_SPACE:
							 //TODO: Replace with String resource
							    failedReason = "ERROR_INSUFFICIENT_SPACE";
							    break;
						   case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
							 //TODO: Replace with String resource
							    failedReason = "ERROR_TOO_MANY_REDIRECTS";
							    break;
						   case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
							 //TODO: Replace with String resource
							    failedReason = "ERROR_UNHANDLED_HTTP_CODE";
							    break;
						   case DownloadManager.ERROR_UNKNOWN:
							 //TODO: Replace with String resource
							    failedReason = "ERROR_UNKNOWN";
							    break;
					   }
					 //TODO: Replace with String resource
					   Toast.makeText(this.context,"FAILED: " + failedReason, Toast.LENGTH_LONG).show();
					   break;
					   
				  case DownloadManager.STATUS_PAUSED:
					   String pausedReason = "";
					   switch(reason){
						   case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
							 //TODO: Replace with String resource
							    pausedReason = "PAUSED_QUEUED_FOR_WIFI";
							    break;
						   case DownloadManager.PAUSED_UNKNOWN:
							 //TODO: Replace with String resource
							    pausedReason = "PAUSED_UNKNOWN";
							    break;
						   case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
							 //TODO: Replace with String resource
							    pausedReason = "PAUSED_WAITING_FOR_NETWORK";
							    break;
						   case DownloadManager.PAUSED_WAITING_TO_RETRY:
							 //TODO: Replace with String resource
							    pausedReason = "PAUSED_WAITING_TO_RETRY";
							    break;
					   }
					 //TODO: Replace with String resource
					   Toast.makeText(this.context,"PAUSED: " + pausedReason, Toast.LENGTH_LONG).show();
					   break;
				  case DownloadManager.STATUS_PENDING:
					//TODO: Replace with String resource
					   Toast.makeText(this.context, "PENDING", Toast.LENGTH_LONG).show();
					   break;
				  case DownloadManager.STATUS_RUNNING:
					  //TODO: Replace with String resource
					   Toast.makeText(this.context, "RUNNING", Toast.LENGTH_LONG).show();
					   break;
				  case DownloadManager.STATUS_SUCCESSFUL:
					  //Download was successful. We should update db etc.
					   downloadCompleted(feedPos, epPos);
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
	 */
	public void refreshLists()
	{
		//Update the list adapters to reflect changes.
		((LatestEpisodesListAdapter)this.latestEpisodesListAdapter).replaceItems(this.eph.getLatestEpisodes(100));
		((DragNDropAdapter)this.playlistAdapter).replaceItems(this.plDbH.sort(this.eph.getDownloadedEpisodes()));
		this.listAdapter.replaceItems(this.fDbH.getAllFeeds());
		
		//Notify for UI updates.
		this.listAdapter.notifyDataSetChanged();
		this.playlistAdapter.notifyDataSetChanged();
		this.latestEpisodesListAdapter.notifyDataSetChanged();
	}
}
