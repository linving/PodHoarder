package com.podhoarder.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarder.component.ButteryProgressBar;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.util.DialogUtils;
import com.podhoarder.util.SearchManager;
import com.podhoarder.util.SearchResultMultiChoiceModeListener;
import com.podhoarderproject.podhoarder.R;

public class SearchFragment extends Fragment implements OnQueryTextListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarder.fragment.SearchFragment";
	
	private ListView 				mListView;
	private View 					mView;
	private SearchResultsAdapter	mListAdapter;
	
	private SearchManager mSearchManager;

    private SearchView mSearchView;
	
	private ButteryProgressBar	mProgressBar;
	
	private SearchResultMultiChoiceModeListener mListSelectionListener;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.mView = inflater.inflate(R.layout.fragment_search, container, false);

		setupListView();
		setHasOptionsMenu(true);
		mSearchManager = new SearchManager(getActivity(),mListAdapter);
		mProgressBar = (ButteryProgressBar)this.mView.findViewById(R.id.search_progressBar);
		return this.mView;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		
		inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
		mSearchView =
	            (SearchView) menu.findItem(R.id.action_search).getActionView();
	    mSearchView.setQueryHint(getString(R.string.search_hint));
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
	    searchIcon.setImageResource(R.drawable.ic_action_search);
	    
	    mSearchView.setIconifiedByDefault(false);	//Automatically expand the Search View instead of waiting for the user to expand it manually.
	    mSearchView.requestFocus();
	    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); //Toggle the soft keyboard to let the user search instantly.
	    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
	    
		super.onCreateOptionsMenu(menu, inflater);
	}

	private void setupListView()
	{
		this.mListAdapter = new SearchResultsAdapter(getActivity());
		
		this.mListView = (ListView) this.mView.findViewById(R.id.mainListView);
		this.mListView.setAdapter(this.mListAdapter);
		this.mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
			{
				List<SearchResultRow> selectedItem = new ArrayList<SearchResultRow>();
				selectedItem.add((SearchResultRow)mListView.getItemAtPosition(position));
				DialogUtils.addFeedsDialog(getActivity(), selectedItem);
			}
		});
		this.mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		this.mListSelectionListener = new SearchResultMultiChoiceModeListener(getActivity(), this.mListView);
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
		mSearchManager.doSearch(text, this.mProgressBar);
		((MainActivity)getActivity()).hideKeyboard();
		mSearchView.clearFocus();
		return false;
	}
}
