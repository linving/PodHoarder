package com.podhoarderproject.podhoarder.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarderproject.podhoarder.util.Feed;
import com.podhoarderproject.podhoarder.util.PodcastHelper;

public class SearchFragment extends Fragment
{
	private ListView 				mainListView;
	private View 					view;
	private SearchResultsAdapter	listAdapter;
	private PodcastHelper 			helper;
	private List<Feed>				results;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		this.view = inflater.inflate(R.layout.fragment_search, container, false);

		setupHelper();
		setupListView();
		
		return this.view;
	}
	
	private void setupHelper()
	{
		this.helper = new PodcastHelper(getActivity());
	}
	
	private void setupListView()
	{
		this.results = new ArrayList<Feed>();
		this.listAdapter = new SearchResultsAdapter(this.results, getActivity());
		
		this.mainListView = (ListView) this.view.findViewById(R.id.mainListView);
		this.mainListView.setAdapter(this.listAdapter);
	}
}
