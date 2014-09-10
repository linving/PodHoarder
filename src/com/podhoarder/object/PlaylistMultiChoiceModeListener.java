package com.podhoarder.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.OnScrollListener;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.util.NetworkUtils;
import com.podhoarderproject.podhoarder.R;
import com.podhoarder.adapter.DragNDropAdapter;

public class PlaylistMultiChoiceModeListener implements MultiChoiceModeListener
{
	private AbsListView mParentListView;
	private Context mContext;
	private List<Integer> mSelectedItems;
	private ActionMode mActionMode;
	private boolean mActive = false;
	
	public PlaylistMultiChoiceModeListener(Context context, AbsListView parent)
	{
		this.mContext = context;
		this.mParentListView = parent;
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
							view.getChildAt(i).setSelected(true);
						}
					}
				}
			}
		});
	}

	public ActionMode getActionMode()
	{
		return mActionMode;
	}

	public boolean isActive()
	{
		return mActive;
	}

	@Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate the menu for the CAB
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu_playlist_row, menu);
        this.mActionMode = mode;
        this.mSelectedItems = new ArrayList<Integer>();
        this.mActive = true;
        ((DragNDropAdapter)this.mParentListView.getAdapter()).setReorderingEnabled(false);
        
        return true;
    }

	@Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // Here you can perform updates to the CAB due to
        // an invalidate() request
        return false;
    }
	
	@Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) 
    {
        // Respond to clicks on the actions in the CAB
        switch (item.getItemId()) 
        {
            case R.id.menu_playlist_delete:
            	removeSelectedItemsFromPlaylist();
            	mode.finish();
            	return true;
            case R.id.menu_playlist_available_offline:
            	downloadSelectedItems();
            	mode.finish();
            	return true;
            default:
                return false;
        }
    }
	
	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
	{
		int i = getViewPosition(position);
		if (i != -1)
			mParentListView.getChildAt(i).setSelected(checked);	//Update the selected status of the View object if it is visible and not recycled.
    	if (checked)
    		this.mSelectedItems.add(position);	//save the list position of the selected view.
    	else
    		this.mSelectedItems.remove((Object)position);	//remove the list position of the unselected view.

    	boolean canDownload = false;
    	for (int it : this.mSelectedItems)
    	{
    		if (!((Episode)this.mParentListView.getItemAtPosition(it)).isDownloaded())
    			canDownload = true;
    	}
    	
    	if (!NetworkUtils.isOnline(mContext))
    		canDownload = false;
    	
    	this.mActionMode.getMenu().findItem(R.id.menu_playlist_available_offline).setVisible(canDownload);
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
    			this.mParentListView.getChildAt(i).setSelected(false);	//Deselect the view if it's not recycled.
    	}
    	this.mSelectedItems.clear();
    	this.mSelectedItems = null;
    	this.mActive = false;
    	this.mActionMode = null;
        ((DragNDropAdapter)this.mParentListView.getAdapter()).setReorderingEnabled(true);
    }
    
    private void removeSelectedItemsFromPlaylist()
    {
		Collections.sort(this.mSelectedItems);
		Collections.reverse(this.mSelectedItems);
		for (int i : this.mSelectedItems)
		{
			Episode ep = (Episode) this.mParentListView.getItemAtPosition(i);
			((MainActivity)this.mContext).podService.deletingEpisode(ep.getEpisodeId());
        	if (ep.isDownloaded())
        		((MainActivity)this.mContext).helper.deleteEpisodeFile(ep);
        	((MainActivity)this.mContext).helper.playlistAdapter.removeFromPlaylist(ep);
		}
    	((MainActivity)this.mContext).helper.refreshPlayList();
    }
    
    private void downloadSelectedItems()
    {
    	List<Episode> eps = new ArrayList<Episode>();
    	for (int i : this.mSelectedItems)
    	{
    		Episode ep = (Episode) this.mParentListView.getItemAtPosition(i);
    		if (!ep.isDownloaded())
    			eps.add(ep);
    	}
    	for (Episode ep : eps)
    	{
    		((MainActivity)this.mContext).helper.downloadEpisode(ep);
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
