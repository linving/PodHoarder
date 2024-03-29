package com.podhoarder.listener;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.adapter.EpisodesListAdapter;
import com.podhoarder.adapter.EpisodesListAdapter.EpisodeRowViewHolder;
import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.object.Episode;
import com.podhoarder.util.EpisodeRowUtils;
import com.podhoarder.util.ToastMessages;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

public class EpisodeMultiChoiceModeListener implements MultiChoiceModeListener
{
	private AbsListView mParentListView;
	private Context mContext;
	private List<Integer> mSelectedItems;
	private ActionMode mActionMode;
	private boolean mActive = false;
	
	public EpisodeMultiChoiceModeListener(Context context, AbsListView parent)
	{
		this.mContext = context;
		this.mParentListView = parent;

	}

    public void onScroll(AbsListView parentListView, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (mSelectedItems != null)
        {
            for (int i : mSelectedItems)
            {
                i = getViewPosition(i);
                if (i != -1)
                {
                    ((EpisodeRowViewHolder) parentListView.getChildAt(i).getTag()).checkbox.setChecked(true);
                }
            }
        }
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
        // Inflate the secondaryAction for the CAB
        EpisodeRowUtils.getMultiSelectionMenu(mContext,mode,menu);

        this.mActionMode = mode;
        this.mSelectedItems = new ArrayList<Integer>();
        this.mActive = true;
        ((EpisodesListAdapter)mParentListView.getAdapter()).setSelectionEnabled(true);
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
        switch (item.getItemId()) {
            case R.id.menu_episode_add_playlist:
                addSelectedItemsToPlaylist();
                mode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.menu_episode_markAsListened:
            	markSelectedItemsAsListened();
            	mode.finish();
            	return true;
            case R.id.menu_episode_available_offline:
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
		if (i != -1) {
            EpisodeRowViewHolder viewHolder = ((EpisodeRowViewHolder) mParentListView.getChildAt(i).getTag());
            viewHolder.checkbox.setChecked(checked);
            //mParentListView.getChildAt(i).invalidate();
            if (checked)
                this.mSelectedItems.add(position);    //save the list position of the selected view.
            else
                this.mSelectedItems.remove((Object) position);    //remove the list position of the unselected view.
        }
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
    			((EpisodeRowViewHolder)mParentListView.getChildAt(i).getTag()).checkbox.setChecked(false);	//Deselect the view if it's not recycled.
    	}
    	this.mSelectedItems.clear();
    	this.mSelectedItems = null;
    	this.mActive = false;
    	this.mActionMode = null;
    	((EpisodesListAdapter)mParentListView.getAdapter()).setSelectionEnabled(false);
    }

	private void addSelectedItemsToPlaylist()
    {
    	for (int i : this.mSelectedItems)
    	{
    		Episode ep = (Episode) this.mParentListView.getItemAtPosition(i);
    		((LibraryActivity)this.mContext).mDataManager.addToPlaylist(ep);
    	}
    	ToastMessages.EpisodeAddedToPlaylist(this.mContext).show();
    }
    
    private void markSelectedItemsAsListened()
    {
		List<Episode> eps = new ArrayList<Episode>();
    	for (int i : this.mSelectedItems)
    	{
    		Episode ep = (Episode) this.mParentListView.getItemAtPosition(i);
    		if (!ep.isListened())	//We only need to mark unlistened Episodes as listened. So we only add those that aren't already listened.
    			eps.add(ep);
    	}
        ((LibraryActivityManager)((LibraryActivity)this.mContext).mDataManager).markAsListened(eps);
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
    		((LibraryActivity)this.mContext).mDataManager.DownloadManager().downloadEpisode(ep);
    	}
    }

    /**
     * Finds the position of a View, relative to the currently visible subset.
     * @param pos Position of the view to find.
     * @return	An index that enables you to access the View at pos. -1 if the View is not visible.
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
