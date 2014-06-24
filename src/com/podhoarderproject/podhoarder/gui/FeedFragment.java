package com.podhoarderproject.podhoarder.gui;

import com.podhoarderproject.podhoarder.PodcastHelper;
import com.podhoarderproject.podhoarder.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
 
/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class FeedFragment extends Fragment
{
	private static final 	String 				LOG_TAG = "com.podhoarderproject.podhoarder.FeedFragment";
	
	public 					ExpandableListView 	mainListView;
	public 					TextView 			feedTitle;
	
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
    
    @Override
    public void onStart()
    {
    	super.onStart();
    }
    
    private void setupListView()
    {
    	this.mainListView = (ExpandableListView) view.findViewById(R.id.mainListView);
    	if (!this.helper.listAdapter.isEmpty())
    	{
    		this.mainListView.addFooterView(setupAddFeed());
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
    		View emptyView = (setupAddFeed());
    		((LinearLayout)this.mainListView.getParent()).addView(emptyView);
    		//getActivity().addContentView(emptyView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    		this.mainListView.setEmptyView(emptyView);
    		//TODO: Show some kind of "list is empty" text instead of the mainlistview here.
    	}
    	
    }
    
    private View setupAddFeed()
    {
    	//We add the footer view (last item) for adding new Feeds here.
    	LayoutInflater inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);	//get the inflater service.
    	View addFeedRow = inflater.inflate(R.layout.fragment_feeds_expandable_list_add_feed_row, this.mainListView, false);	//Inflate the custom row layout for the footer.
    	addFeedRow.setOnClickListener(new View.OnClickListener() {	//Add Click Listener for the footer. It shouldn't behave like the other listrows.
			@Override
			public void onClick(View v)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    	builder.setTitle(getString(R.string.popup_window_title));
		    	
		    	// Set up the input
		    	final EditText input = new EditText(getActivity());
		    	// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		    	input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		    	builder.setView(input);

		    	// Set up the buttons
		    	builder.setPositiveButton(R.string.popup_window_ok_button, new DialogInterface.OnClickListener() { 
		    	    @Override
		    	    public void onClick(DialogInterface dialog, int which) {
		    	        helper.addFeed(input.getText().toString());
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
    	return addFeedRow;
    }
    
    private void setupHelper()
    {
    	this.helper = ((com.podhoarderproject.podhoarder.MainActivity)this.getActivity()).helper;
    }
}
