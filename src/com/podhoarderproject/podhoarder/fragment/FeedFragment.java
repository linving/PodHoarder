package com.podhoarderproject.podhoarder.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.util.Feed;
import com.podhoarderproject.podhoarder.util.PodcastHelper;
import com.podhoarderproject.podhoarder.util.PopupMenuUtils;
 
/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class FeedFragment extends Fragment implements OnRefreshListener
{

	@SuppressWarnings("unused")
	private static final 	String 				LOG_TAG = "com.podhoarderproject.podhoarder.FeedFragment";
	
	public 					GridView 			mainGridView;
	
	private 				SwipeRefreshLayout 	swipeLayout;
	
	private 				View 				view;
	private 				PodcastHelper 		helper;
	
	public FeedFragment() { }
	
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_feeds, container, false);
		
		setupHelper();
		setupGridView();
		setupRefreshControls();
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
 
    private void setupHelper()
    {
    	this.helper = ((com.podhoarderproject.podhoarder.activity.MainActivity)this.getActivity()).helper;
    }

    private void setupGridView()
    {
    	this.mainGridView = (GridView) view.findViewById(R.id.mainGridView);
    	if (!this.helper.feedsListAdapter.isEmpty())
    	{
    		this.helper.feedsListAdapter.setFooterView(setupAddFeed());
    		this.helper.feedsListAdapter.setLoadingView(setupLoadingFeed());
    		this.mainGridView.setAdapter(this.helper.feedsListAdapter);
    		
    		this.mainGridView.setOnItemLongClickListener(new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id)
				{
					final Feed currentFeed = helper.feedsListAdapter.feeds.get(pos);
					PopupMenuUtils.buildFeedContextMenu(getActivity(), v, currentFeed, true).show();
					return true;
				}
			});
    	}
    	else
    	{
    		View emptyView = (setupAddFeed());
    		((LinearLayout)this.mainGridView.getParent()).addView(emptyView);
    		this.mainGridView.setEmptyView(emptyView);
    		//TODO: Show some kind of "list is empty" text instead of the mainlistview here.
    	}
    	
    }
    
    private View setupAddFeed()
    {
    	//We add the footer view (last item) for adding new Feeds here.
    	LayoutInflater inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);	//get the inflater service.
    	View addFeedRow = inflater.inflate(R.layout.fragment_feeds_grid_add_feed_item, this.mainGridView, false);	//Inflate the custom row layout for the footer.
    	
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
		    	    public void onClick(DialogInterface dialog, int which) 
		    	    {
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

    private View setupLoadingFeed()
    {
    	LayoutInflater inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);	//get the inflater service.
    	View loadingFeedItem = inflater.inflate(R.layout.fragment_feeds_grid_loading_feed_item, this.mainGridView, false);	//Inflate the "loading" grid item to show while data is downloaded
    	return loadingFeedItem;
    }
    
    private void setupRefreshControls()
    {
    	swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(R.color.refresh_bar_blue, 
        		R.color.refresh_bar_green, 
        		R.color.refresh_bar_orange, 
                R.color.refresh_bar_red);
    }
    
    @Override
	public void onRefresh()
	{
		this.helper.setRefreshLayout(swipeLayout);	//Set the layout that should be updated once the Refresh task is done executing.
		this.helper.refreshFeeds();	//Start the refresh process.
	}
    
}
