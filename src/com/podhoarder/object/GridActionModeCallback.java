package com.podhoarder.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.GridListAdapter;
import com.podhoarder.component.CheckableRelativeLayout;
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
							CheckableRelativeLayout item = (CheckableRelativeLayout)view.getChildAt(i);
							item.setChecked(true);
							ImageView img = ((ImageView)item.findViewById(R.id.feeds_grid_item_image));
							img.setColorFilter(getSelectedItemColor());
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
			CheckableRelativeLayout view = (CheckableRelativeLayout)this.mParentListView.getChildAt(i);
			ImageView img = ((ImageView)view.findViewById(R.id.feeds_grid_item_image));
			if (checked)
			{
				img.setColorFilter(getSelectedItemColor());
			}
			else
			{
				img.setColorFilter(mContext.getResources().getColor(R.color.fragment_feeds_grid_item_image_tint));
			}
		}
    	if (checked)
    		this.mSelectedItems.add(position);	//save the list position of the selected view.
    	else
    		this.mSelectedItems.remove((Object)position);	//remove the list position of the unselected view.
    	
    	if (this.mSelectedItems.size() == 0)	//If the last item was deselected we finish the actionMode.
    		this.mActionMode.finish();
    	else
    		this.updateTitle();
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
    			((CheckableRelativeLayout)this.mParentListView.getChildAt(i).findViewById(R.id.feeds_grid_item_checkableLayout)).setChecked(false);
    	}
    	this.mSelectedItems.clear();
    	this.mParentListView.invalidateViews();
    	this.mActive = false;
        this.mActionMode = null;
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
    		((GridListAdapter)this.mParentListView.getAdapter()).mItems.remove(i);	//Remove the item from the adapter to quickly reflect changes.
    	}
		((MainActivity)this.mContext).helper.deleteFeeds(ids);	//Perform the background operation on the DB.
    }
    
    private void markSelectedItemsAsListened()
    {
    	for (int i : this.mSelectedItems)
    	{
    		Feed feed = (Feed) this.mParentListView.getItemAtPosition(i);
    		((MainActivity)this.mContext).helper.markAsListenedAsync(feed);
    	}
		
    }
    
    private void updateTitle()
    {
    	String titleString = ""+this.mSelectedItems.size();
    	if (this.mSelectedItems.size() == 1)
    	{
    		titleString += " " + this.mContext.getString(R.string.contextual_action_mode_selected);
    	}
    	else
    	{
    		titleString += " " + this.mContext.getString(R.string.contextual_action_mode_selected);
        	
    	}
    	this.mActionMode.setTitle(titleString);
    }

    private int getSelectedItemColor()
    {
    	int color = mContext.getResources().getColor(R.color.app_accent);
    	int red = Color.red(color) + 30;
        int green = Color.green(color) + 30;
        int blue = Color.blue(color) + 30;
        return Color.argb(125, red, green, blue);
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
