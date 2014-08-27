package com.podhoarder.object;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;

import com.podhoarder.activity.MainActivity;
import com.podhoarderproject.podhoarder.R;

public class GridActionModeCallback implements ActionMode.Callback
{
	private AbsListView mParentListView;
	private Context mContext;
	private ActionMode mActionMode;
	private List<Integer> mSelectedItems;
	private boolean mActive = false;
	
	public ActionMode getActionMode()
	{
		return mActionMode;
	}

	public GridActionModeCallback(Context context, AbsListView parent)
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
        inflater.inflate(R.menu.feed_menu, menu);
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
    	for (int i : this.mSelectedItems)
    	{
    		Feed feed = (Feed) this.mParentListView.getItemAtPosition(i);
    		((MainActivity)this.mContext).helper.deleteFeed(feed.getFeedId());
    	}
    	((MainActivity)this.mContext).helper.refreshListsAsync();
    }
    
    private void markSelectedItemsAsListened()
    {
    	for (int i : this.mSelectedItems)
    	{
    		Feed feed = (Feed) this.mParentListView.getItemAtPosition(i);
    		((MainActivity)this.mContext).helper.markAsListenedAsync(feed);
    	}
		
    }

    private int getSelectedItemColor()
    {
    	int color = mContext.getResources().getColor(android.R.color.holo_blue_light);
    	int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(115, red, green, blue);
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
