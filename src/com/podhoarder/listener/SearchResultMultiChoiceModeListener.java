package com.podhoarder.listener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.OnScrollListener;

import com.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarder.object.SearchResultRow;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

import static com.podhoarder.adapter.SearchResultsAdapter.SearchResultsAdapterViewHolder;

public class SearchResultMultiChoiceModeListener implements MultiChoiceModeListener
{
	private AbsListView mParentListView;
	private Context mContext;
	private List<Integer> mSelectedItems;
	private ActionMode mActionMode;
	private boolean mActive = false;
    private onDialogResultListener mOnDialogResultListener;
	
	public SearchResultMultiChoiceModeListener(Context context, AbsListView parent)
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
                            ((SearchResultsAdapterViewHolder) view.getChildAt(i).getTag()).checkbox.setChecked(true);
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
        // Inflate the secondaryAction for the CAB
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu_search, menu);
        this.mActionMode = mode;
        this.mSelectedItems = new ArrayList<Integer>();
        ((SearchResultsAdapter)mParentListView.getAdapter()).setSelectionEnabled(true);
        this.mActive = true;
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
            case R.id.menu_search_add:
            	addSelectedPodcasts();
                mode.finish(); // Action picked, so close the CAB
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
            SearchResultsAdapterViewHolder viewHolder = ((SearchResultsAdapterViewHolder) mParentListView.getChildAt(i).getTag());
            viewHolder.checkbox.setChecked(checked);//Update the selected status of the View object if it is visible and not recycled.
            if (checked)
                this.mSelectedItems.add(position);	//save the list position of the selected view.
            else
                this.mSelectedItems.remove((Object)position);	//remove the list position of the unselected view.
        }
//    	this.updateTitle();
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
                ((SearchResultsAdapterViewHolder) mParentListView.getChildAt(i).getTag()).checkbox.setChecked(false);	//Deselect the view if it's not recycled.
    	}
    	this.mSelectedItems.clear();
    	this.mSelectedItems = null;
    	this.mActive = false;
    	this.mActionMode = null;
        ((SearchResultsAdapter)mParentListView.getAdapter()).setSelectionEnabled(false);
    }
	
	private void addSelectedPodcasts()
    {
		List<SearchResultRow> selectedItems = new ArrayList<SearchResultRow>();
    	for (int i : this.mSelectedItems)
    	{
    		selectedItems.add(((SearchResultRow) this.mParentListView.getItemAtPosition(i)));
    	}
    	addFeedsDialog(mContext, selectedItems);
    }

    public void addFeedsDialog(final Context ctx, final List<SearchResultRow> selectedResults) {
        if (selectedResults.size() > 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(ctx.getString(R.string.popup_window_title_multiple));
            builder.setMessage(ctx.getString(R.string.popup_areyousure) + " " + selectedResults.size() + " " + ctx.getString(R.string.popup_podcasts) + "?");

            // Set up the buttons
            builder.setPositiveButton(R.string.popup_window_ok_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mOnDialogResultListener.onDialogResultReceived(1, selectedResults);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.popup_window_cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mOnDialogResultListener.onDialogResultReceived(0, null);
                    dialog.dismiss();
                }
            });
            builder.show();
        } else if (selectedResults.size() == 1) {
            final SearchResultRow currentItem = selectedResults.get(0);
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(ctx.getString(R.string.popup_window_title_single));
            builder.setMessage(ctx.getString(R.string.popup_areyousure) + " " + currentItem.getTitle() + "?");

            // Set up the buttons
            builder.setPositiveButton(R.string.popup_window_ok_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mOnDialogResultListener.onDialogResultReceived(1, selectedResults);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.popup_window_cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mOnDialogResultListener.onDialogResultReceived(0, null);
                    dialog.dismiss();
                }
            });
            builder.show();
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

    public void setmOnDialogResultListener(onDialogResultListener dialogResultListener) {
        this.mOnDialogResultListener = dialogResultListener;
    }
    public interface onDialogResultListener {
        public void onDialogResultReceived(int result, List<SearchResultRow> selectedResults);
    }
}
