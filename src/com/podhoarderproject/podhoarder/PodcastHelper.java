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
import java.util.Collections;
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
 * @author Emil Almrot
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

	public PodcastHelper(Context ctx)
	{
		this.context = ctx;
		this.fDbH = new FeedDBHelper(this.context);
		this.eph = new EpisodeDBHelper(this.context);
		this.listAdapter = new FeedListAdapter(this.fDbH.getAllFeeds(),
				this.context);
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

	// TODO: Add refreshFeeds

	// TODO: Add downloadEpisode
	public void downloadEpisode(int feedId, int episodeId)
	{
		int feedPos = getFeedPositionWithId(feedId);
		int epPos = getEpisodePositionWithId(feedPos, episodeId);
		if (isDownloadManagerAvailable(this.context) && this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getLocalLink().equals(""))
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
			//request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle().trim()+".mp3");
			//TODO: Why does it fail when specifying an "advanced" filename?
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, "test.mp3");
			// get download service and enqueue file
			DownloadManager manager = (DownloadManager) this.context.getSystemService(Context.DOWNLOAD_SERVICE);
			manager.enqueue(request);
			//update list adapter object
			this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).setLocalLink(Environment.DIRECTORY_PODCASTS + "/" +  this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos).getTitle()+".mp3");
			//update db entry
			this.eph.updateEpisode(this.listAdapter.feeds.get(feedPos).getEpisodes().get(epPos));
		}
		else
		{
			Log.w(LOG_TAG, "Podcast already exists locally. No need to download.");
		}
	}

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
					percentIncrement = (itemLst.getLength() / 100);
					// Loop through the XML passing the data to the arrays
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
			// Reverse list to get the correct ordering in the DB.
			Collections.reverse(eps);
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
			} catch (CursorIndexOutOfBoundsException e)
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
			} catch (SQLiteConstraintException e)
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
