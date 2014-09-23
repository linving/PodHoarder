package com.podhoarder.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.gson.Gson;
import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarder.component.ButteryProgressBar;
import com.podhoarder.json.SearchResult;
import com.podhoarder.json.SearchResultItem;
import com.podhoarder.object.SearchResultRow;
import com.podhoarderproject.podhoarder.R;

public class SearchManager
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarder.util.SearchManager";
	
	private String baseURL = "http://itunes.apple.com/search?media=podcast&entity=podcast&limit=" + Constants.SEARCH_RESULT_LIMIT + "&term=";	//Just append the search term to this string and you will receive the 25 most relevant results.

    private SearchResultsAdapter mListAdapter;
	private ButteryProgressBar mProgressBar;
	private Context mContext;
	private SearchTask mSearchTask;
    
    public SearchManager(Context context, SearchResultsAdapter listAdapter)
    {
    	this.mContext = context;
    	this.mListAdapter = listAdapter;
    }
    
    public void doSearch(String searchString, ButteryProgressBar progressBar)
    {
    	this.mProgressBar = progressBar;
    	
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

    public void cancelSearch()
    {
    	if (this.mSearchTask != null)
    		this.mSearchTask.cancel();
    }
    
    private class SearchTask extends AsyncTask<String, Void, Void >
	{
    	private List<AsyncTask<String, Void, SearchResultRow >> mSubtasks;
		private SearchResult		mResults;
		private int					mTimeOutInc = 0;
		public int					mFailedTasks = 0;
		
		@Override
		protected void onPreExecute()
		{
			fadeInAnimation(mProgressBar, 1000);
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
					while (mResults == null && mTimeOutInc < Constants.SEARCH_TIMEOUT && !this.isCancelled())
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
				if (!((MainActivity)mContext).helper.feedExists(result.getFeedUrl()) && !this.isCancelled())
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
	    	fadeOutAnimation(mProgressBar , 500);
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
				this.conn = (HttpURLConnection) url.openConnection();
				
				this.conn.setConnectTimeout(Constants.SEARCH_TIMEOUT_MILLIS);

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
			catch (MalformedURLException e)
			{
				e.printStackTrace();
				cancel(true);
			} catch (DOMException e)
			{
				e.printStackTrace();
				cancel(true);
			} catch (IOException e)
			{
				e.printStackTrace();
				cancel(true);
			} catch (ParserConfigurationException e)
			{
				e.printStackTrace();
				cancel(true);
			} catch (SAXException e)
			{
				e.printStackTrace();
				cancel(true);
			} catch (SQLiteConstraintException e)
			{
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
				fadeOutAnimation(mProgressBar, 1000);	//Fade out the progress indicator.
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
			if (this.conn != null)
				this.conn.disconnect();
			super.onCancelled(result);
		}
	}

	private void fadeOutAnimation(final View viewToFade, int duration)
    {
    	Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
    	animation.setDuration(duration);
    	animation.setFillEnabled(true);
    	animation.setFillAfter(true);
    	animation.setAnimationListener(new Animation.AnimationListener(){
      
    	    

			@Override
			public void onAnimationRepeat(Animation arg0)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation arg0)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
    	    public void onAnimationEnd(Animation arg0) 
			{
				viewToFade.setVisibility(View.INVISIBLE);
    	    }
    	});
    	viewToFade.startAnimation(animation);
    }
	
	private void fadeInAnimation(final View viewToFade, int duration)
    {
    	Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
    	animation.setDuration(duration);
    	animation.setFillEnabled(true);
    	animation.setFillAfter(true);
    	animation.setAnimationListener(new Animation.AnimationListener(){
      
    	    

			@Override
			public void onAnimationRepeat(Animation arg0)
			{
				
			}

			@Override
			public void onAnimationStart(Animation arg0)
			{
				viewToFade.setVisibility(View.VISIBLE);
			}
			
			@Override
    	    public void onAnimationEnd(Animation arg0) 
			{
				
    	    }
    	});
    	viewToFade.startAnimation(animation);
    }
}
