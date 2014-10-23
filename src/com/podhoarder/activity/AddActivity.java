package com.podhoarder.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.listener.SearchResultMultiChoiceModeListener;
import com.podhoarder.object.Feed;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.util.SearchManager;
import com.podhoarder.view.ButteryProgressBar;
import com.podhoarderproject.podhoarder.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddActivity extends BaseActivity implements SearchView.OnQueryTextListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarder.activity.AddActivity";

	private FeedDBHelper mFeedHelper;
	
	private ListView mListView;
	private SearchResultsAdapter mListAdapter;

	private SearchManager mSearchManager;

	private ButteryProgressBar mProgressBar;

	private SearchResultMultiChoiceModeListener mListSelectionListener;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		mFeedHelper = new FeedDBHelper(AddActivity.this);
		setupListView();
		mProgressBar = (ButteryProgressBar) findViewById(R.id.search_progressBar);
		mSearchManager = new SearchManager(AddActivity.this, mListAdapter, mProgressBar);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
        android.app.SearchManager searchManager = (android.app.SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (null != searchView )
        {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }
        searchView.setOnQueryTextListener(this);
		onSearchRequested();
		return true;
	}

	public void finish(List<SearchResultRow> selectedResults)
	{
		 Intent databackIntent = new Intent(); 
		 databackIntent.putExtra("Results", (Serializable)selectedResults);
		 setResult(Activity.RESULT_OK, databackIntent);
		 finish();
	}
	
	private void setupListView()
	{
		this.mListAdapter = new SearchResultsAdapter(AddActivity.this);

		this.mListView = (ListView) findViewById(R.id.mainListView);
		this.mListView.setAdapter(this.mListAdapter);
		this.mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View v,
					int position, long id)
			{
				List<SearchResultRow> selectedItem = new ArrayList<SearchResultRow>();
				selectedItem.add((SearchResultRow) mListView
						.getItemAtPosition(position));
				addFeedsDialog(AddActivity.this, selectedItem);
			}
		});
		this.mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		this.mListSelectionListener = new SearchResultMultiChoiceModeListener(
				AddActivity.this, this.mListView);
		this.mListView.setMultiChoiceModeListener(this.mListSelectionListener);
	}

	public SearchResultMultiChoiceModeListener getListSelectionListener()
	{
		return mListSelectionListener;
	}

	public SearchManager getSearchManager()
	{
		return mSearchManager;
	}

	@Override
	public boolean onQueryTextChange(String arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String text)
	{
		mSearchManager.doSearch(text);
		hideKeyboard();
		return false;
	}

	public void addFeedsDialog(final Context ctx, final List<SearchResultRow> selectedResults)
	{
		if (selectedResults.size() > 1)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
	    	builder.setTitle(ctx.getString(R.string.popup_window_title_multiple));
	    	builder.setMessage(ctx.getString(R.string.popup_areyousure) + " " + selectedResults.size() + " " + ctx.getString(R.string.popup_podcasts) + "?");

	    	// Set up the buttons
	    	builder.setPositiveButton(R.string.popup_window_ok_button, new DialogInterface.OnClickListener() { 
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) 
	    	    {
	    	    	finish(selectedResults);
	    	    	//TODO: Start mainactivity
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
		else if (selectedResults.size() == 1)
		{
			final SearchResultRow currentItem = selectedResults.get(0);
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
	    	builder.setTitle(ctx.getString(R.string.popup_window_title_single));
	    	builder.setMessage(ctx.getString(R.string.popup_areyousure) + " " + currentItem.getTitle() + "?");

	    	// Set up the buttons
	    	builder.setPositiveButton(R.string.popup_window_ok_button, new DialogInterface.OnClickListener() { 
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) 
	    	    {
	    	    	finish(selectedResults);
	    	    	//TODO: Start mainactivity
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
	}
	
	public void hideKeyboard()
	{
		InputMethodManager inputManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		// check if no view has focus:
		View view = this.getCurrentFocus();
		if (view != null)
		{
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	public boolean feedExists(String feedURL)
	{
		List<Feed> feeds = mFeedHelper.getAllFeeds(false);
		for (int r = 0; r < feeds.size(); r++)
		{
			if (feedURL.equals(feeds.get(r).getLink())) return true;
		}
		return false;
	}
}
