/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarderproject.podhoarder;

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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
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
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PodcastHelper";

	private FeedDBHelper fDbH;
	private EpisodeDBHelper eph;
	public FeedListAdapter listAdapter;
	private Context context;
	private String storagePath;
	private String podcastDir;

	public PodcastHelper(Context ctx)
	{
		this.context = ctx;
		this.storagePath = Environment.DIRECTORY_PODCASTS;
		this.podcastDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).getAbsolutePath();
		this.fDbH = new FeedDBHelper(this.context);
		this.eph = new EpisodeDBHelper(this.context);
		this.listAdapter = new FeedListAdapter(this.fDbH.getAllFeeds(),
				this.context);
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

		this.listAdapter.feeds.remove(i);
		this.listAdapter.notifyDataSetChanged();
	}

	/**
	 * Sets the time listened property of a specific episode.
	 * 
	 * @param feedId
	 *            Id of the Feed to update.
	 * @param episodeId
	 *            unique identifier of the Episode that is to be updated.
	 * @param minutesListened
	 *            number of minutes listened.
	 */
	public void updateEpisodeListened(int feedId, int episodeId, int minutesListened)
	{
		int i = 0;
		int r = 0;
		while (this.listAdapter.feeds.get(i).getFeedId() != feedId
				&& i < this.listAdapter.feeds.size())
		{
			i++;
		}
		while (this.listAdapter.feeds.get(i).getEpisodes().get(r)
				.getEpisodeId() != episodeId
				&& r < this.listAdapter.feeds.get(i).getEpisodes().size())
		{
			r++;
		}
		this.listAdapter.feeds.get(i).getEpisodes().get(r)
				.setMinutesListened(minutesListened);
		this.eph.updateEpisode(this.listAdapter.feeds.get(i).getEpisodes()
				.get(r));
		this.listAdapter.notifyDataSetChanged();
	}

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
		int feedPos = getFeedPositionWithId(feedId);
		int epPos = getEpisodePositionWithId(feedPos, episodeId);
		if (isDownloadManagerAvailable(this.context) && !new File(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getLocalLink()).exists())
		{
			String url = this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getLink();
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setDescription(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle());
			request.setTitle("Podhoarder Download");
			// in order for this if to run, you must use the android 3.2 to compile your app
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
			    request.allowScanningByMediaScanner();
			    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}
			request.setDestinationInExternalPublicDir(this.storagePath, this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle().replace(":", " -")+".mp3");
			// get download service and enqueue file
			DownloadManager manager = (DownloadManager) this.context.getSystemService(Context.DOWNLOAD_SERVICE);
			manager.enqueue(request);
			//update list adapter object
			this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).setLocalLink(	this.podcastDir + "/" +
																						this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle().replace(":", " -")+".mp3");
			//update db entry
			this.eph.updateEpisode(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos));
			
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
	 * Deletes the physical mp3-file associated with an Episode, not the Episod eobject itself.
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
			Log.i(LOG_TAG, file.getAbsolutePath() + " deleted successfully!");
		}
		else
		{
			Log.e(LOG_TAG, file.getAbsolutePath() + " not deleted. Make sure it exists.");
		}
	}
	
	/**
	 * Checks all stored links to make sure that the references files actually exist on startup.
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
			this.listAdapter.notifyDataSetChanged();
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
		for (Feed newFeed : feeds)
		{
			int i=0;
			//Get the Feed that's stored locally with the same Id.
			Feed oldFeed = this.fDbH.getFeedByURL(newFeed.getLink());
			//As long as a duplicate Episode isn't encountered, insert the new Episodes at the start of the list. (Newer episodes come first.)
			while (newFeed.getEpisodes().get(i).getLink().equals(oldFeed.getEpisodes().get(i).getLink()))
			{
				oldFeed.getEpisodes().add(0, newFeed.getEpisodes().get(i));
				i++;
			}
			//Update the Feed with the new Episodes in the db.
			Feed updatedFeed = this.fDbH.updateFeed(oldFeed);
			int pos = this.getFeedPositionWithId(updatedFeed.getFeedId());
			//Update the Feed object in the listAdapter.
			this.listAdapter.feeds.set(pos, updatedFeed);
		}
		Toast notification = Toast.makeText(context, "Feeds refreshed!",
				Toast.LENGTH_SHORT);
		notification.show();
		this.listAdapter.notifyDataSetChanged();
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

							// This section gets the elements from the XML
							// that we want to use you will need to add
							// and remove elements that you want / don't want
							NodeList title = ielem
									.getElementsByTagName("title");
							NodeList link = ielem
									.getElementsByTagName("enclosure");
							NodeList pubDate = ielem
									.getElementsByTagName("pubDate");
							NodeList content = ielem
									.getElementsByTagName("content:encoded");

							// This section adds an entry to the arrays with the
							// data retrieved from above. I have surrounded each
							// with try/catch just incase the element does not
							// exist
							try
							{
								ep.setTitle(title.item(0).getChildNodes()
										.item(0).getNodeValue());
							} catch (NullPointerException e)
							{
								e.printStackTrace();
							}

							try
							{
								ep.setLink(link.item(0).getAttributes().item(0)
										.getNodeValue());
							} catch (NullPointerException e)
							{
								e.printStackTrace();
							}

							try
							{
								ep.setPubDate(pubDate.item(0).getChildNodes()
										.item(0).getNodeValue());
							} catch (NullPointerException e)
							{
								e.printStackTrace();
							}

							try
							{
								ep.setDescription(content.item(0)
										.getChildNodes().item(0).getNodeValue());
							} catch (NullPointerException e)
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
					this.link, this.category, this.img, eps, context);
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
	 * @param String URL of the Feed to be refreshed.
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

						// This is the root node of each section you want to parse
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
						this.img = ((Element) itemLst2.item(0))
								.getElementsByTagName("itunes:image").item(0)
								.getAttributes().item(0).getNodeValue();
						percentIncrement = 10.0;
						publishProgress((int) percentIncrement);
						
						
						// Loop through the XML passing the data to the arrays
						percentIncrement = ((100/urls[0].size())/itemLst.getLength());
						for (int i = 0; i < itemLst.getLength(); i++)
						{
							Episode ep = new Episode();
							Node item = itemLst.item(i);
							if (item.getNodeType() == Node.ELEMENT_NODE)
							{
								Element ielem = (Element) item;

								// This section gets the elements from the XML
								// that we want to use you will need to add
								// and remove elements that you want / don't want
								NodeList title = ielem
										.getElementsByTagName("title");
								NodeList link = ielem
										.getElementsByTagName("enclosure");
								NodeList pubDate = ielem
										.getElementsByTagName("pubDate");
								NodeList content = ielem
										.getElementsByTagName("content:encoded");

								// This section adds an entry to the arrays with the
								// data retrieved from above. I have surrounded each
								// with try/catch just incase the element does not
								// exist
								try
								{
									ep.setTitle(title.item(0).getChildNodes()
											.item(0).getNodeValue());
								} catch (NullPointerException e)
								{
									e.printStackTrace();
								}

								try
								{
									ep.setLink(link.item(0).getAttributes().item(0)
											.getNodeValue());
								} catch (NullPointerException e)
								{
									e.printStackTrace();
								}

								try
								{
									ep.setPubDate(pubDate.item(0).getChildNodes()
											.item(0).getNodeValue());
								} catch (NullPointerException e)
								{
									e.printStackTrace();
								}

								try
								{
									ep.setDescription(content.item(0)
											.getChildNodes().item(0).getNodeValue());
								} catch (NullPointerException e)
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
				feeds.add(new Feed(this.title, this.author, this.description,
						this.link, this.category, this.img, eps, context));
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
				cancel(true);
			} 
			catch (SQLiteConstraintException e)
			{
				Log.e(LOG_TAG,"SQLiteConstraintException: Refresh failed.");
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
}
