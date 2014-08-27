package com.podhoarder.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.object.CheckableRelativeLayout;
import com.podhoarder.object.GridActionModeCallback;
import com.podhoarder.util.Constants;
import com.podhoarder.util.PodcastHelper;
import com.podhoarderproject.podhoarder.R;
 
/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class FeedFragment extends Fragment implements OnRefreshListener
{

	@SuppressWarnings("unused")
	private static final String 				LOG_TAG = "com.podhoarderproject.podhoarder.FeedFragment";
	
	public 	GridView 			mainGridView;
	
	private SwipeRefreshLayout 	swipeLayout;
	
	private View 				view;
	private PodcastHelper 		helper;
	
	private GridActionModeCallback mActionModeCallback;  
	private ActionMode mActionMode;  
	
	public FeedFragment() { }
	
	
    public GridActionModeCallback getActionModeCallback()
	{
		return mActionModeCallback;
	}
    
    public ActionMode getActionMode()
    {
    	return mActionMode;
    }


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
    	this.helper = ((com.podhoarder.activity.MainActivity)this.getActivity()).helper;
    }

    private void setupGridView()
    {
    	this.mainGridView = (GridView) view.findViewById(R.id.mainGridView);
    	if (!this.helper.feedsListAdapter.isEmpty())
    	{
    		this.helper.feedsListAdapter.setFooterView(setupAddFeed());
    		this.helper.feedsListAdapter.setLoadingView(setupLoadingFeed());
    		this.mainGridView.setAdapter(this.helper.feedsListAdapter);
    		
    		this.mainGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        	mActionModeCallback = new GridActionModeCallback(getActivity(), mainGridView);
    		this.mainGridView.setOnItemLongClickListener(new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id)
				{
					if (mActionMode == null || !mActionModeCallback.isActive())
					{
						mActionMode = getActivity().startActionMode(mActionModeCallback);
			        	helper.feedsListAdapter.setActionModeVars(mActionMode, mActionModeCallback);
					}
					((CheckableRelativeLayout)v).toggle();
					mActionModeCallback.onItemCheckedStateChanged(pos, ((CheckableRelativeLayout)v).isChecked());
			        return true;  
				}
			});
    	}
    	else
    	{
    		//TODO: MAKE SURE THIS WORKS CORRECTLY
    		View emptyView = (setupAddFeed());
    		emptyView.setMinimumHeight(this.helper.feedsListAdapter.gridItemSize);
    		emptyView.setMinimumWidth(this.helper.feedsListAdapter.gridItemSize);
    		emptyView.setLayoutParams(new LayoutParams(this.helper.feedsListAdapter.gridItemSize, this.helper.feedsListAdapter.gridItemSize));
    		((RelativeLayout)this.mainGridView.getParent().getParent()).addView(emptyView);
    		this.mainGridView.setEmptyView(emptyView);
    		
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
				((MainActivity)getActivity()).mAdapter.setSearchPageEnabled(true);
				((MainActivity)getActivity()).setTab(Constants.BONUS_TAB_POSITION);
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
