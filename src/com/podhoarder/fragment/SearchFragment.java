package com.podhoarder.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.gson.Gson;
import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarder.json.SearchResult;
import com.podhoarder.json.SearchResultItem;
import com.podhoarder.util.Constants;
import com.podhoarderproject.podhoarder.R;

public class SearchFragment extends Fragment
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.SearchFragment";
	
	private ListView 				mainListView;
	private View 					view;
	private SearchResultsAdapter	listAdapter;
	
	private String baseURL = "http://itunes.apple.com/search?media=podcast&entity=podcast&attribute=titleTerm&limit=" + Constants.SEARCH_RESULT_LIMIT + "&term=";	//Just append the search term to this string and you will receive the 25 most relevant results.
	private String searchTerm = "";
	
	private ProgressBar	progressBar;
	
	private AsyncTask<String, Void, Void> searchTask;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.view = inflater.inflate(R.layout.fragment_search, container, false);

		setupListView();
		setupSearch();
		
		return this.view;
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
		
	private void setupSearch()
	{
		this.progressBar = (ProgressBar) this.view.findViewById(R.id.search_progressBar);
		
		final InputMethodManager keyboard = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        
		final EditText editText = (EditText) this.view.findViewById(R.id.search_text_input);
		final RelativeLayout container = (RelativeLayout) this.view.findViewById(R.id.searchFragment_container);
		editText.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
		    {
		        boolean handled = false;
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) 
		        {
		            doSearch(v.getText().toString());
		            handled = true;
		            getActivity().getCurrentFocus().clearFocus();
		            getActivity().getCurrentFocus().setSelected(false);
		            container.requestFocus();
		            container.setSelected(true);
		            //editText.clearFocus();
		            //container.requestFocus(View.FOCUS_DOWN);
		        }
		        return handled;
		    }
		});
		
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

		    @Override
		    public void onFocusChange(View v, boolean hasFocus) 
		    {
		    	if (hasFocus) keyboard.showSoftInput(v, 0);
		    	else keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
		    }
		});
		editText.requestFocus();
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
				final SearchResultItem currentItem = (SearchResultItem)mainListView.getItemAtPosition(position);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    	builder.setTitle(getString(R.string.popup_window_title));
		    	builder.setMessage(getActivity().getString(R.string.popup_areyousure) + " " + currentItem.getCollectionName() + "?");

		    	// Set up the buttons
		    	builder.setPositiveButton(R.string.popup_window_ok_button, new DialogInterface.OnClickListener() { 
		    	    @Override
		    	    public void onClick(DialogInterface dialog, int which) 
		    	    {
		    	    	((MainActivity)getActivity()).helper.addFeed(currentItem.getFeedUrl());
						((MainActivity)getActivity()).actionBar.setSelectedNavigationItem(Constants.FEEDS_TAB_POSITION);
		    	    }
		    	});
		    	builder.setNegativeButton(R.string.popup_window_cancel_button, new DialogInterface.OnClickListener() {
		    	    @Override
		    	    public void onClick(DialogInterface dialog, int which) {
		    	        dialog.cancel();
		    	    }
		    	});
				builder.show();
			}
		});
	}

	private class SearchTask extends AsyncTask<String, Void, Void >
	{
		private SearchResult		results;
		
		@Override
		protected void onPreExecute()
		{
			progressBar.setVisibility(View.VISIBLE);		//Show progressbar
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
					while (results == null)
						Thread.sleep(100);
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
					listAdapter.results.add(this.results.getResults().get(i));
					publishProgress();
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
			progressBar.setVisibility(View.INVISIBLE);	//Hide progressbar
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
}
