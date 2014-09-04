package com.podhoarder.fragment;

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

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarder.json.SearchResult;
import com.podhoarder.json.SearchResultItem;
import com.podhoarder.object.SearchResultMultiChoiceModeListener;
import com.podhoarder.util.Constants;
import com.podhoarder.util.DialogUtils;
import com.podhoarderproject.podhoarder.R;

public class SearchFragment extends Fragment implements OnQueryTextListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.SearchFragment";
	
	private ListView 				mainListView;
	private View 					view;
	private SearchResultsAdapter	listAdapter;
	
	private String baseURL = "http://itunes.apple.com/search?media=podcast&entity=podcast&attribute=titleTerm&limit=" + Constants.SEARCH_RESULT_LIMIT + "&term=";	//Just append the search term to this string and you will receive the 25 most relevant results.
	private String searchTerm = "";
    private SearchView mSearchView;
	
	private ProgressBar	progressBar;
	
	private AsyncTask<String, Void, Void> searchTask;
	
	private SearchResultMultiChoiceModeListener mListSelectionListener;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.view = inflater.inflate(R.layout.fragment_search, container, false);

		setupListView();
		setHasOptionsMenu(true);
		return this.view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		
		inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
		mSearchView =
	            (SearchView) menu.findItem(R.id.action_search).getActionView();
	    mSearchView.setQueryHint(getString(R.string.search_hint));

	    mSearchView.setEnabled(true);
	    
	    mSearchView.setOnQueryTextListener(this);

	    SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)mSearchView.findViewById(R.id.search_src_text);
	    searchAutoComplete.setHintTextColor(Color.WHITE);
	    searchAutoComplete.setTextColor(Color.WHITE);

	    View searchplate = (View)mSearchView.findViewById(R.id.search_plate);
	    searchplate.setBackgroundResource(R.drawable.abc_textfield_search_default_holo_dark);

	    ImageView searchCloseIcon = (ImageView)mSearchView.findViewById(R.id.search_close_btn);
	    searchCloseIcon.setImageResource(R.drawable.ic_action_remove);

	    ImageView voiceIcon = (ImageView)mSearchView.findViewById(R.id.search_voice_btn);
	    voiceIcon.setImageResource(R.drawable.abc_ic_voice_search);

	    ImageView searchIcon = (ImageView)mSearchView.findViewById(R.id.search_mag_icon);
	    searchIcon.setImageResource(R.drawable.abc_ic_search);
	    
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	

	private void doSearch(String searchString)
	{
		this.searchTerm = Normalizer.normalize(searchString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		Log.d(LOG_TAG,"Search for: " + this.searchTerm);
		
		//Clear results and notify list adapter.
		listAdapter.results.clear();
		listAdapter.notifyDataSetChanged();
		//Start the asynchronous search task.
		if (this.searchTask != null)
		{
			this.searchTask.cancel(true);
			this.searchTask = new SearchTask().execute(this.baseURL + this.searchTerm.replace(" ", "%20"));
			
		}
		else
		{
			this.searchTask = new SearchTask().execute(this.baseURL + this.searchTerm.replace(" ", "%20"));
		}
	}

	private void setupListView()
	{
		this.listAdapter = new SearchResultsAdapter(getActivity());
		
		this.mainListView = (ListView) this.view.findViewById(R.id.mainListView);
		this.mainListView.setAdapter(this.listAdapter);
		this.mainListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
			{
				List<SearchResultItem> selectedItem = new ArrayList<SearchResultItem>();
				selectedItem.add((SearchResultItem)mainListView.getItemAtPosition(position));
				DialogUtils.addFeedsDialog(getActivity(), selectedItem);
			}
		});
		this.mainListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		this.mListSelectionListener = new SearchResultMultiChoiceModeListener(getActivity(), this.mainListView);
		this.mainListView.setMultiChoiceModeListener(this.mListSelectionListener);
	}

	private class SearchTask extends AsyncTask<String, Void, Void >
	{
		private SearchResult		results;
		private int					timeOutInc = 0;
		
		@Override
		protected void onPreExecute()
		{
			//progressBar.setVisibility(View.VISIBLE);		//Show progressbar
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
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
				{
					InputStream input = url.openStream();
					Reader reader = new InputStreamReader(input, "UTF-8");
					this.results = new Gson().fromJson(reader, SearchResult.class);
					while (results == null && timeOutInc < Constants.SEARCH_TIMEOUT)
					{
						Thread.sleep(100);
						this.timeOutInc++;
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
			
			if (this.results != null)
			{
				for (int i = 0; i<this.results.getResultCount(); i++)
				{
					//Add results and notify list adapter.
					if (!((MainActivity)getActivity()).helper
							.feedExists(
									this.results.getResults().get(i).getFeedUrl()))	//If the feed already exists we don't add it to the results. Can't add the same feed twice.
					{
						listAdapter.results.add(this.results.getResults().get(i));
						publishProgress();
					}
					
				}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... nothing)
		{
			listAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Void nothing) 
		{
			//progressBar.setVisibility(View.INVISIBLE);	//Hide progressbar
		};
		
		@Override
		protected void onCancelled(Void nothing)
		{
			//Clear results and notify list adapter.
			this.results = null;
			listAdapter.results.clear();
			listAdapter.notifyDataSetChanged();
		}
	}		

	public SearchResultMultiChoiceModeListener getListSelectionListener()
	{
		return mListSelectionListener;
	}

	@Override
	public boolean onQueryTextChange(String arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0)
	{
		doSearch(arg0);
		return false;
	}
}
