package com.podhoarder.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.component.CheckableRelativeLayout;
import com.podhoarder.object.GridActionModeCallback;
import com.podhoarder.util.Constants;
import com.podhoarder.util.PodcastHelper;
import com.podhoarderproject.podhoarder.R;
 
/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class FeedFragment extends Fragment
{

	@SuppressWarnings("unused")
	private static final String 				LOG_TAG = "com.podhoarderproject.podhoarder.FeedFragment";
	
	public 	GridView 			mainGridView;
	
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
    		this.helper.feedsListAdapter.setLoadingViews(setupLoadingViews());
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
    		emptyView.setMinimumHeight(this.helper.feedsListAdapter.mGridItemSize);
    		emptyView.setMinimumWidth(this.helper.feedsListAdapter.mGridItemSize);
    		emptyView.setLayoutParams(new LayoutParams(this.helper.feedsListAdapter.mGridItemSize, this.helper.feedsListAdapter.mGridItemSize));
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
				if (!mActionModeCallback.isActive())
				{
					((MainActivity)getActivity()).mAdapter.setSearchPageEnabled(true);
					((MainActivity)getActivity()).setTab(Constants.BONUS_TAB_POSITION);
				}
			}
			
		});
    	return addFeedRow;
    }

    private List<View> setupLoadingViews()
    {
    	List<View> views = new ArrayList<View>();	//This is an ugly solution but in order to use the GridViews LayoutParams the loading views must be inflated here.
    	LayoutInflater inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);	//get the inflater service.
    	for (int i=0; i<Constants.SEARCH_RESULT_LIMIT; i++)	//Inflate a collection of Loading views, same size as the maximum amount Search Results.
    	{
        	views.add(inflater.inflate(R.layout.fragment_feeds_grid_loading_feed_item, this.mainGridView, false));	//Inflate the "loading" grid item to show while data is downloaded
    	}
    	return views;
    }
    
}
