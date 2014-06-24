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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

import com.podhoarderproject.podhoarder.PodcastHelper;
import com.podhoarderproject.podhoarder.R;
 
/**
 * 
 * @author Emil Almrot
 * 2014-05-21
 */
public class LatestEpisodesFragment extends Fragment implements OnRefreshListener
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.LatestEpisodesFragment";
	
	public ListView mainListView;
	public TextView episodesTitle;
	
	private SwipeRefreshLayout swipeLayout;
	
	private View view;
	private PodcastHelper helper;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_latest, container, false);
		this.episodesTitle = (TextView) view.findViewById(R.id.episodesTitle);
		
		setupHelper();
		setupListView();
		setupRefreshControls();
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
    
    private void setupRefreshControls()
    {
    	swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);
    }

	@Override
	public void onRefresh()
	{
		this.helper.setRefreshLayout(swipeLayout);	//Set the layout that should be updated once the Refresh task is done executing.
		this.helper.refreshFeeds();	//Start the refresh process.
	}
}
