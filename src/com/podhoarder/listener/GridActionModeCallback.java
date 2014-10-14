package com.podhoarder.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.GridAdapter;
import com.podhoarder.object.Feed;
import com.podhoarder.util.AnimUtils;
import com.podhoarder.util.ViewHolders;
import com.podhoarderproject.podhoarder.R;

public class GridActionModeCallback implements ActionMode.Callback
{
	private GridView mParentListView;
	private Context mContext;
	private ActionMode mActionMode;
	private List<Integer> mSelectedItems;
	private boolean mActive = false;
	
	public ActionMode getActionMode()
	{
		return mActionMode;
	}

	public GridActionModeCallback(Context context, GridView parent)
	{
		this.mContext = context;
		this.mParentListView = parent;
		this.mSelectedItems = new ArrayList<Integer>();
		
		this.mParentListView.setOnScrollListener(new OnScrollListener()
		{
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount)
			{
				if (mSelectedItems != null)
				{
					for (int i : mSelectedItems)
					{
						i = getViewPosition(i);
						if (i != -1)
						{
							RelativeLayout item = (RelativeLayout)view.getChildAt(i);
							CheckBox checkmark = ((CheckBox)item.findViewById(R.id.feeds_grid_item_checkmark));
							checkmark.setChecked(true);
						}
					}
				}
			}
		});
	}

	@Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate the menu for the CAB
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu_feed, menu);
        this.mActionMode = mode;
		this.mActive = true;
		((GridAdapter)this.mParentListView.getAdapter()).setSelectionEnabled(true);
        return true;
    }

	@Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // Here you can perform updates to the CAB due to
        // an invalidate() request
		this.mActive = true;
        this.mActionMode = mode;
        return true;
    }
	
	@Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) 
    {
        // Respond to clicks on the actions in the CAB
        switch (item.getItemId()) {
            case R.id.menu_feed_markAsListened:
            	markSelectedItemsAsListened();
                mode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.menu_feed_delete:
            	deleteSelectedItems();
            	mode.finish();
            	return true;
            default:
                return false;
        }
    }
	
	public void onItemCheckedStateChanged(int position, boolean checked)
	{
		int i = getViewPosition(position);
		if (i != -1)
		{
			RelativeLayout view = (RelativeLayout)this.mParentListView.getChildAt(i);
			((ViewHolders.FeedsAdapterViewHolder)view.getTag()).checked = checked;
			CheckBox checkmark = ((CheckBox)view.findViewById(R.id.feeds_grid_item_checkmark));
			checkmark.setChecked(checked);
			AnimUtils.gridSelectionAnimation(view);
		}
    	if (checked)
    		this.mSelectedItems.add(position);	//save the list position of the selected view.
    	else
    		this.mSelectedItems.remove((Object)position);	//remove the list position of the unselected view.
    	
    	if (this.mSelectedItems.size() == 0)	//If the last item was deselected we finish the actionMode.
    		this.mActionMode.finish();
	}
	

	@Override
    public void onDestroyActionMode(ActionMode mode) 
	{
        // Here you can make any necessary updates to the activity when
        // the CAB is removed. By default, selected items are deselected/unchecked.
    	for (int i : this.mSelectedItems)
    	{
    		i = getViewPosition(i);
    		if (i != -1)
    		{
    			((ViewHolders.FeedsAdapterViewHolder)this.mParentListView.getChildAt(i).getTag()).checked = false;
    			((CheckBox)this.mParentListView.getChildAt(i).findViewById(R.id.feeds_grid_item_checkmark)).setChecked(false);
    		}
    	}
    	this.mSelectedItems.clear();
    	((GridAdapter)this.mParentListView.getAdapter()).setSelectionEnabled(false);
    	this.mActive = false;
        this.mActionMode = null;
        this.mParentListView.invalidateViews();
    }
	
	public boolean isActive()
	{
		return this.mActive;
	}

	private void deleteSelectedItems()
    {
		List<Integer> ids = new ArrayList<Integer>();
		Collections.sort(this.mSelectedItems);	//Since we are removing the entries from the adapter manually, using saved indexes we need to sort the indexes.
		Collections.reverse(this.mSelectedItems); //They are sorted and reversed so they highest index comes first. This prevents later indexes from pointing at the wrong item.
    	for (int i : this.mSelectedItems)
    	{
    		ids.add(((Feed) this.mParentListView.getItemAtPosition(i)).getFeedId());	//Save the Feed ID for the db operation.
    		((GridAdapter)this.mParentListView.getAdapter()).mItems.remove(i);	//Remove the item from the adapter to quickly reflect changes.
    	}
		((MainActivity)this.mContext).mPodcastHelper.deleteFeeds(ids);	//Perform the background operation on the DB.
    }
    
    private void markSelectedItemsAsListened()
    {
    	for (int i : this.mSelectedItems)
    	{
    		Feed feed = (Feed) this.mParentListView.getItemAtPosition(i);
    		((MainActivity)this.mContext).mPodcastHelper.markAsListenedAsync(feed);
    	}
		
    }

    /**
     * Finds the position of a View, relative to the currently visible subset.
     * @param pos Position of the view to find.
     * @return	An index that enables you to access the View at pos.
     */
    private int getViewPosition(int pos)
    {
    	final int numVisibleChildren = this.mParentListView.getChildCount();
    	final int firstVisiblePosition = this.mParentListView.getFirstVisiblePosition();

    	for ( int i = 0; i < numVisibleChildren; i++ ) {
    	    int positionOfView = firstVisiblePosition + i;

    	    if (positionOfView == pos) {
    	        return i;
    	    }
    	}
    	
    	return -1;
    }
}
