package com.podhoarderproject.podhoarder.gui;

import com.podhoarderproject.podhoarder.PodcastHelper;
import com.podhoarderproject.podhoarder.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
 
/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class FeedFragment extends Fragment
{
	public ExpandableListView mainListView;
	public TextView feedTitle;
	
	private View view;
	private PodcastHelper helper;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_feeds, container, false);
		
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
    	this.mainListView = (ExpandableListView) view.findViewById(R.id.mainListView);
    	if (!this.helper.listAdapter.isEmpty())
    	{
    		this.mainListView.setAdapter(this.helper.listAdapter);
    		this.mainListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {  
     		   
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id)
				{
					LinearLayout episodeExpandableArea = (LinearLayout)v.findViewById(R.id.list_episode_row_expandable_container);
   				 
    		        // Creating the expand animation for the item
    		        ExpandAnimation expandAni = new ExpandAnimation(episodeExpandableArea, 100);
    		 
    		        // Start the animation on the toolbar
    		        episodeExpandableArea.startAnimation(expandAni);
					return true;
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
