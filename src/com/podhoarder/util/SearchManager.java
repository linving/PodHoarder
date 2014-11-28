package com.podhoarder.util;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarder.json.SearchResult;
import com.podhoarder.json.SearchResultItem;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.view.AnimatedSearchView;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * A helper class for doing podcast searches. Divides searches in subtasks and provides simple methods for searching, cancelling etc.
 * @author Emil
 *
 */
public class SearchManager
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarder.util.SearchManager";

    private static final int NEW_SEARCH_RESULT_LIMIT = 10;
    private static final int SEARCH_TIMEOUT = 100;
    private static final int SEARCH_TIMEOUT_MILLIS = 10000;
	
	private String baseURL = "http://itunes.apple.com/search?media=podcast&entity=podcast&limit=" + NEW_SEARCH_RESULT_LIMIT + "&term=";	//Just append the search term to this string and you will receive the 25 most relevant results.

    private SearchResultsAdapter mListAdapter;
	private AnimatedSearchView mSearchView;
	private Context mContext;
	private SearchTask mSearchTask;
    
	/**
	 * Create a new SearchManager object.
	 * @param context	Application context.
	 * @param listAdapter	ListAdapter to show search results in.
	 * @param searchView	The AnimatedSearchView that is used to enter the search query.
	 * @return A new SearchManager object.
	 */
    public SearchManager(Context context, SearchResultsAdapter listAdapter, AnimatedSearchView searchView)
    {
    	this.mContext = context;
    	this.mListAdapter = listAdapter;
    	this.mSearchView = searchView;
    }
    
    /**
     * Perform a podcast search.
     * @param searchString	The string to search for.
     */
    public void doSearch(String searchString)
    {
    	
    	String searchTerm = Normalizer.normalize(searchString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		Log.d(LOG_TAG,"Search for: " + searchTerm);
		
		//Clear results and notify list adapter.
		mListAdapter.clear();
		mListAdapter.notifyDataSetChanged();
		//Start the asynchronous search task.
		if (this.mSearchTask != null)
		{
			this.mSearchTask.cancel();
			this.mSearchTask = (SearchTask) new SearchTask().execute(this.baseURL + searchTerm.replace(" ", "%20"));
			
		}
		else
		{
			this.mSearchTask = (SearchTask) new SearchTask().execute(this.baseURL + searchTerm.replace(" ", "%20"));
		}
    }
    
	/**
	 * Cancel the current Search operation, if one is running.
	 */
    public void cancelSearch()
    {
    	if (this.mSearchTask != null)
    		this.mSearchTask.cancel();
    }
    
    /**
     * AsyncTask for performing podcast searches. Uses subtasks of type FeedParseTask to actually parse channel data.
     * @author Emil
     * @see FeedParseTask
     */
    private class SearchTask extends AsyncTask<String, Void, Void >
	{
    	private List<AsyncTask<String, Void, SearchResultRow >> mSubtasks;
		private SearchResult		mResults;
		private int					mTimeOutInc = 0;
		public int					mFailedTasks = 0;
		
		@Override
		protected void onPreExecute()
		{
			mSearchView.setSearching(true);
			//mProgressBar.setVisibility(View.VISIBLE);		//Show progressbar
			this.mSubtasks = new ArrayList<AsyncTask<String, Void, SearchResultRow >> ();
		}
		
		@Override
		protected Void doInBackground(String... param)
		{
			//Do the HTTP request and parse the data.
			try
			{
				URL url;
				url = new URL(param[0]);
				
				// Setup the connection
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				
				// Connect
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK && !this.isCancelled())
				{
					InputStream input = url.openStream();
					Reader reader = new InputStreamReader(input, "UTF-8");
					this.mResults = new Gson().fromJson(reader, SearchResult.class);
					while (mResults == null && mTimeOutInc < SEARCH_TIMEOUT && !this.isCancelled())
					{
						Thread.sleep(100);
						this.mTimeOutInc++;
					}
				}
			} 
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			} 
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (SearchResultItem result : mResults.getResults())
			{
				
				if (!((LibraryActivity)mContext).mDataManager.hasPodcast(result.getFeedUrl()) && !this.isCancelled())
				{
					AsyncTask<String, Void, SearchResultRow > tempTask = new FeedParseTask();
					tempTask.execute(result.getFeedUrl());
					this.mSubtasks.add(tempTask);
				}
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... nothing)
		{
			mListAdapter.notifyDataSetChanged();
		}
		
		@Override
		protected void onCancelled(Void nothing)
		{
			cancel();
		}
		
		@Override
		protected void onCancelled()
		{
			cancel();
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			if (this.mSubtasks.isEmpty())
	    		cancel();
		}
		
		public void cancel()
		{
			//Clear results and notify list adapter.
	    	Log.i(LOG_TAG, "Search cancelled!");
	    	mSearchView.setSearching(false);
			this.mResults = null;
			this.mFailedTasks = 0;
			mListAdapter.clear();
			mListAdapter.notifyDataSetChanged();
			for (AsyncTask<String, Void, SearchResultRow > task : this.mSubtasks)
			{
				task.cancel(true);
			}
			if (!this.isCancelled()) cancel(true);
		}
	}
	
    /**
     * AsyncTask for actually parsing Feed data. This is used for each individual search result to present channel name, image etc to users. Parses about half of the data needed to actually add a Search Result, which in turn makes adding faster.
     * @author Emil
     *
     */
	private class FeedParseTask extends AsyncTask<String, Void, SearchResultRow >
	{
		private HttpURLConnection conn;
		
		@Override
		protected SearchResultRow doInBackground(String... param)
		{
			try
			{
				SearchResultRow feed = new SearchResultRow();
				// Set the url
				URL url = new URL(param[0]);

				// Setup the connection
                try {
                    this.conn = (HttpURLConnection) url.openConnection();

                    this.conn.setConnectTimeout(SEARCH_TIMEOUT_MILLIS);
                    this.conn.setReadTimeout(SEARCH_TIMEOUT_MILLIS);

                    // Connect
                    if (this.conn.getResponseCode() == HttpURLConnection.HTTP_OK && !this.isCancelled())
                    {
                        // Retreive the XML from the URL
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc;
                        doc = db.parse(url.openStream());
                        doc.getDocumentElement().normalize();

                        // This is the root node of each section you want to parse
                        NodeList channelNodeList = doc.getElementsByTagName("channel");

                        feed.setTitle(DataParser.parsePodcastTitle(channelNodeList));
                        feed.setAuthor(DataParser.parsePodcastAuthor(channelNodeList));
                        feed.setDescription(DataParser.parsePodcastDescription(channelNodeList));
                        feed.setLink(param[0]);
                        feed.setCategory(DataParser.parsePodcastCategory(channelNodeList));
                        feed.setImageUrl(DataParser.parsePodcastImageLocation(channelNodeList));
                        feed.setXml(doc);
                        feed.setLastUpdated(DataParser.parsePodcastPubDate(channelNodeList));
                        return feed;
                    }
                }
                catch (java.net.SocketTimeoutException e) {
                    Log.e(LOG_TAG,"Timeout when trying to connect to " + feed.getLink());
                    e.printStackTrace();
                    this.conn.disconnect();
                    cancel(true);
                } catch (java.io.IOException e) {
                    Log.e(LOG_TAG,"IOException when trying to connect to " + feed.getLink());
                    e.printStackTrace();
                    this.conn.disconnect();
                    cancel(true);
                }
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
				this.conn.disconnect();
				cancel(true);
			} catch (DOMException e)
			{
				e.printStackTrace();
				this.conn.disconnect();
				cancel(true);
			} catch (IOException e)
			{
				e.printStackTrace();
				this.conn.disconnect();
				cancel(true);
			} catch (ParserConfigurationException e)
			{
				e.printStackTrace();
				this.conn.disconnect();
				cancel(true);
			} catch (SAXException e)
			{
				e.printStackTrace();
				this.conn.disconnect();
				cancel(true);
			} catch (SQLiteConstraintException e)
			{
				this.conn.disconnect();
				cancel(true);
			} 
			return null;
		}
		
		@Override
		protected void onPostExecute(SearchResultRow result) 
		{
			if (result != null)
			{
				mListAdapter.add(result);
				publishProgress();
			}
			else
				mSearchTask.mFailedTasks++;
			if ((mSearchTask.mSubtasks.size() - mSearchTask.mFailedTasks) == mListAdapter.getCount())	//The last subtask has been completed.
			{
				mSearchView.setSearching(false);
			}
		};
		
		@Override
		protected void onProgressUpdate(Void... nothing)
		{
			mListAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onCancelled(SearchResultRow result)
		{
			Log.i("ParseFeedTask", "Search Result Parse task cancelled");
			super.onCancelled(result);
		}
	}
	
	
}
