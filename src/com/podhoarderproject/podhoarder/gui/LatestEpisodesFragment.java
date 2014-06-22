package com.podhoarderproject.podhoarder.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.podhoarderproject.podhoarder.PodcastHelper;
import com.podhoarderproject.podhoarder.R;
 
/**
 * 
 * @author Emil Almrot
 * 2014-05-21
 */
public class LatestEpisodesFragment extends Fragment
{
	public ListView mainListView;
	public TextView episodesTitle;
	
	private View view;
	private PodcastHelper helper;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_latest, container, false);
		this.episodesTitle = (TextView) view.findViewById(R.id.episodesTitle);
		
		setupHelper();
		setupListView();
		return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
        
    private void setupListView()
    {
    	this.mainListView = (ListView) view.findViewById(R.id.mainListView);
    	if (!this.helper.latestEpisodesListAdapter.isEmpty())
    	{
    		this.mainListView.setAdapter(this.helper.latestEpisodesListAdapter);
    		this.mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  
     		   
    			@Override
    			public void onItemClick(AdapterView<?> parent, View view, int position,
    					long id)
    			{
    				LinearLayout episodeDescription = (LinearLayout)view.findViewById(R.id.list_episode_row_expandable_container);
    				 
    		        // Creating the expand animation for the item
    		        ExpandAnimation expandAni = new ExpandAnimation(episodeDescription, 100);
    		 
    		        // Start the animation on the toolbar
    		        episodeDescription.startAnimation(expandAni);
    			}
        	});
    	}
    	else
    	{
    		//TODO: Show some kind of "list is empty" text instead of the mainlistview here.
    	}
    }
    
    private void setupHelper()
    {
    	this.helper = ((com.podhoarderproject.podhoarder.MainActivity)this.getActivity()).helper;
    }
    
    
}
