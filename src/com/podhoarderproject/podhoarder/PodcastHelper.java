/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarderproject.podhoarder;

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

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class PodcastHelper
{
	private FeedDBHelper fDbH;
	public FeedListAdapter listAdapter;
	private Context context;
	
	public PodcastHelper(Context ctx)
	{
		this.context = ctx;
		this.fDbH = new FeedDBHelper(this.context);
		this.listAdapter = new FeedListAdapter(this.fDbH.getAllFeeds(),this.context);
	}
	
	public void addFeed(String urlOfFeedToAdd)
	{
		new FeedReaderTask().execute(urlOfFeedToAdd);
	}
	
	
	
	private void insertFeedObject(Feed feed)
	{
		feed = this.fDbH.insertFeed(feed);
		this.listAdapter.feeds.add(feed);
		this.listAdapter.notifyDataSetChanged();
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

					// This is the root node of each section you want to parse
					NodeList itemLst = doc.getElementsByTagName("item");
					NodeList itemLst2 = doc.getElementsByTagName("channel");

					this.title = ((Element) itemLst2.item(0))
							.getElementsByTagName("title").item(0).getChildNodes()
							.item(0).getNodeValue();
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
					publishProgress((int)percentIncrement);
					percentIncrement = (itemLst.getLength()/100);
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
							NodeList title = ielem.getElementsByTagName("title");
							NodeList link = ielem.getElementsByTagName("enclosure");
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
								ep.setTitle(title.item(0).getChildNodes().item(0)
										.getNodeValue());
							} catch (NullPointerException e)
							{
								e.printStackTrace();
							}

							try
							{
								ep.setLink(link.item(0).getAttributes().item(0).getNodeValue());
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
								ep.setDescription(content.item(0).getChildNodes()
										.item(0).getNodeValue());
							} catch (NullPointerException e)
							{
								e.printStackTrace();
							}
						}
						publishProgress((int)percentIncrement);
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
			}
			this.newFeed = new Feed(this.title, this.author, this.description, this.link, this.category, this.img, eps, context);
			return newFeed;
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			setProgressPercent(progress[0]);
		}
		
		protected void onPostExecute(Feed result)
		{
			insertFeedObject(this.newFeed);
			Toast notification = Toast.makeText(context, "Feed added!", Toast.LENGTH_LONG);
			notification.show();
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
}
