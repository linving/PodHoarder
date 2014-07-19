package com.podhoarderproject.podhoarder.gui;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.activity.MainActivity;
import com.podhoarderproject.podhoarder.adapter.FirstPageFragmentListener;
import com.podhoarderproject.podhoarder.util.Feed;
import com.podhoarderproject.podhoarder.util.PodcastHelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
 
/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class FeedFragment extends Fragment
{

	@SuppressWarnings("unused")
	private static final 	String 				LOG_TAG = "com.podhoarderproject.podhoarder.FeedFragment";
	
	public 					ListView 			mainListView;
	public 					TextView 			feedTitle;
	
	private 				View view;
	private 				PodcastHelper helper;
	
	private static			FirstPageFragmentListener firstPageListener;
	
	public FeedFragment() { }
	
	public FeedFragment(FirstPageFragmentListener listener) {
        firstPageListener = listener;
    }
	
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
    	this.mainListView = (ListView) view.findViewById(R.id.mainListView);
    	if (!this.helper.feedsListAdapter.isEmpty())
    	{
    		this.mainListView.addFooterView(setupAddFeed());
    		this.mainListView.setAdapter(this.helper.feedsListAdapter);
    		
    		this.mainListView.setOnItemLongClickListener(new OnItemLongClickListener()
			{

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id)
				{
					final Feed currentFeed = helper.feedsListAdapter.feeds.get(pos);
					
					PopupMenu actionMenu = new PopupMenu(getActivity(), v);
					MenuInflater inflater = actionMenu.getMenuInflater();
					inflater.inflate(R.menu.feed_menu, actionMenu.getMenu());
		    	   
					actionMenu.setOnMenuItemClickListener(new OnMenuItemClickListener()
					{
						@Override
						public boolean onMenuItemClick(MenuItem item)
						{
							switch (item.getItemId()) 
							{
						        case R.id.menu_feed_markAsListened:
						        	//TODO: Add a function that marks all Episodes of a Feed as 100% listened.
						            return true;
						        case R.id.menu_feed_delete:
						        	((MainActivity)getActivity()).helper.deleteFeed(currentFeed.getFeedId());
							    	return true;
							}
							return true;
						}
					});
		    	   
		    	   actionMenu.show();
		           return true;
				}
			});
    		
    		this.mainListView.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
				{
					helper.feedDetailsListAdapter.setFeed(helper.feedsListAdapter.feeds.get(pos));
					firstPageListener.onSwitchToNextFragment();
				}

			});
    	}
    	else
    	{
    		View emptyView = (setupAddFeed());
    		((LinearLayout)this.mainListView.getParent()).addView(emptyView);
    		this.mainListView.setEmptyView(emptyView);
    		//TODO: Show some kind of "list is empty" text instead of the mainlistview here.
    	}
    	
    }
    
    private View setupAddFeed()
    {
    	//We add the footer view (last item) for adding new Feeds here.
    	LayoutInflater inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);	//get the inflater service.
    	View addFeedRow = inflater.inflate(R.layout.fragment_feeds_list_add_feed_row, this.mainListView, false);	//Inflate the custom row layout for the footer.
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
    	this.helper = ((com.podhoarderproject.podhoarder.activity.MainActivity)this.getActivity()).helper;
    }

}
