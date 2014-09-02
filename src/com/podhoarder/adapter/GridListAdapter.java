package com.podhoarder.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.activity.SettingsActivity;
import com.podhoarder.component.CheckableRelativeLayout;
import com.podhoarder.object.Feed;
import com.podhoarder.object.FeedImage.ImageDownloadListener;
import com.podhoarder.object.GridActionModeCallback;
import com.podhoarder.util.Constants;
import com.podhoarder.util.ImageUtils;
import com.podhoarder.util.ViewHolders.FeedsAdapterViewHolder;
import com.podhoarderproject.podhoarder.R;

public class GridListAdapter extends BaseAdapter implements ImageDownloadListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.GridListAdapter";
	
	private LayoutInflater mInflater;
	
	public 	List<Feed> mItems;
	
    private Context mContext;
    
    private View mFooterView;
    
    private List<View> mLoadingViews;
    private int mLoadingItemsCount;
    
    private GridActionModeCallback mActionModeCallback;  //This comes from the parent fragment and is used to keep track of whether the ActionMode context bar is enabled.
	private ActionMode mActionMode;  //This comes from the parent fragment and is used to keep track of whether the ActionMode context bar is enabled.
    
    private float mScreenWidth, mScreenHeight; 
    public int mGridItemSize;

    public GridListAdapter(List<Feed> feeds, Context c) 
    {
    	this.mItems = feeds;
    	this.mContext = c;
    	
    	this.mLoadingItemsCount = 0;
    	this.mLoadingViews = new ArrayList<View>();
    	
    	this.mInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	
    	setupScreenVars();
    }
    
    /**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Feed> newItemCollection)
	{
		this.mItems.clear();
		this.mItems.addAll(newItemCollection);
		//this.notifyDataSetChanged();
	}

    public int getCount() 
    {    	
    	if (isLoading())
    		return (this.mItems.size() + this.getLoadingItemsCount());	//We return the items size plus the amount of loading views for the total
    	else
    		return this.mItems.size() + 1;	//We return the items size plus one (for the "Add Podcast" view.
    }

    public Object getItem(int position) {
        return this.mItems.get(position);
    }

    public long getItemId(int position) {
        return this.mItems.get(position).getFeedId();
    }
   
    public void setFooterView(View v) {
    	v.findViewById(R.id.feeds_grid_item_image).setMinimumHeight(mGridItemSize);
    	v.findViewById(R.id.feeds_grid_item_image).setMinimumWidth(mGridItemSize);
    	v.setMinimumHeight(mGridItemSize);
    	v.setMinimumWidth(mGridItemSize);
    	this.mFooterView = v;
    }
    
    public void setLoadingViews(List<View> views)
    {
    	for (View v : views)
    	{
        	v.findViewById(R.id.feeds_grid_loadingBar).setMinimumHeight(mGridItemSize);
        	v.findViewById(R.id.feeds_grid_loadingBar).setMinimumWidth(mGridItemSize);
        	v.setMinimumHeight(mGridItemSize);
        	v.setMinimumWidth(mGridItemSize);
        	this.mLoadingViews.add(v);
    	}
    }
    
    public boolean isLoading()
    {
    	return (this.getLoadingItemsCount() > 0);
    }
    
    /**
     * Resets the loading status of this list to not loading.
     */
    public void resetLoading()
    {
    	this.mLoadingViews.clear();
    }
    
    public int getLoadingItemsCount()
    {
    	return this.mLoadingItemsCount;
    }
    
    public void addLoadingItem()
    {
    	this.mLoadingItemsCount++;
    }
    
    public void removeLoadingItem()
    {
    	if (this.mLoadingItemsCount > 0) this.mLoadingItemsCount--;	//We can never have a negative amount of loading items.
    }

    public void setActionModeVars(ActionMode mode, GridActionModeCallback callback)
    {
    	this.mActionMode = mode;
    	this.mActionModeCallback = callback;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	FeedsAdapterViewHolder viewHolder;
    	
    	if (position == 0)
    	{
    		((GridView) parent).setColumnWidth(mGridItemSize);
    	}
    	
    	if (isLoading())
    	{
    		if (position >= (getCount() - this.mLoadingItemsCount))
    			return mLoadingViews.get(position - (this.mItems.size() - 1));
    	}
    	else
    	{
    		if (position == getCount()-1)
    			return mFooterView;
    	}
    	
    	if (position >= mItems.size())
    		return null;

    	if (convertView != null && convertView.findViewById(R.id.fragment_feeds_grid_loading_item) != null) 
    	{ 
    		convertView = null; 
    	}
    	
    	if (convertView != null && convertView.findViewById(R.id.fragment_feeds_grid_add_item) != null) 
    	{ 
    		convertView = null; 
    	}
    	
		if (convertView == null)
		{
			//Inflate
			convertView = this.mInflater.inflate(R.layout.fragment_feeds_grid_item, null);
//			convertView.setMinimumHeight(mGridItemSize);
//			convertView.setMinimumWidth(mGridItemSize);
			
			
			// Set up the ViewHolder
	        viewHolder = new FeedsAdapterViewHolder();
	        viewHolder.feedTitle = (TextView) convertView.findViewById(R.id.feeds_grid_item_text);
	        viewHolder.feedNumberOfEpisodes = (TextView) convertView.findViewById(R.id.feeds_grid_item_notification);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.feeds_grid_item_image);
	        
	        viewHolder.feedImage.setMinimumHeight(mGridItemSize);
	        viewHolder.feedImage.setMinimumWidth(mGridItemSize);
	        viewHolder.feedImage.setMaxHeight(mGridItemSize);
	        viewHolder.feedImage.setMaxWidth(mGridItemSize);
	        viewHolder.checkableLayout = (CheckableRelativeLayout) convertView.findViewById(R.id.feeds_grid_item_checkableLayout);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (FeedsAdapterViewHolder) convertView.getTag();
		}
		
		
		final Feed currentFeed = this.mItems.get(position);
		
		if(currentFeed != null) 
		{
			try
			{
				if (PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(Constants.SETTINGS_KEY_GRIDSHOWTITLE, true))
				{
					viewHolder.feedTitle.setText(currentFeed.getTitle());	//Set Feed Title
				}
				else viewHolder.feedTitle.setVisibility(View.GONE);
				
				int newEpisodesCount = currentFeed.getNewEpisodesCount();
				if (newEpisodesCount > 0) 
				{
					viewHolder.feedNumberOfEpisodes.setVisibility(View.VISIBLE);
					viewHolder.feedNumberOfEpisodes.setText("" + newEpisodesCount);	//Set number of Episodes
				}
				else
				{
					viewHolder.feedNumberOfEpisodes.setVisibility(View.GONE);
				}
    			
    			viewHolder.feedImage.setImageBitmap(currentFeed.getFeedImage().loadScaledImage(mGridItemSize, mGridItemSize));
    			ImageUtils.scaleImage(mContext, viewHolder.feedImage, mGridItemSize);
    			viewHolder.feedImage.setColorFilter(mContext.getResources().getColor(R.color.fragment_feeds_grid_item_image_tint));
    			
    			final int pos = position;
    			
    			convertView.setOnClickListener(new OnClickListener()
    			{
    				
    				@Override
    				public void onClick(View v)
    				{
    					if (mActionMode != null && mActionModeCallback != null)
    					{
    						if (v.getId() != R.id.fragment_feeds_grid_add_item && v.getId() != R.id.fragment_feeds_grid_loading_item)	//We prevent the user from selecting the loading and add feed grid items
    						{
    							if (mActionModeCallback.isActive())
        						{
        							((CheckableRelativeLayout)v).toggle();
        							mActionModeCallback.onItemCheckedStateChanged(pos, ((CheckableRelativeLayout)v).isChecked()); 
        						}
        						else
        						{
        							((MainActivity)mContext).helper.feedDetailsListAdapter.setFeed(currentFeed);
        	    					((MainActivity)mContext).mAdapter.setDetailsPageEnabled(true);
        	    					((MainActivity)mContext).setTab(Constants.BONUS_TAB_POSITION);
        						}
    						}
    					}
    					else
    					{
    						((MainActivity)mContext).helper.feedDetailsListAdapter.setFeed(currentFeed);
	    					((MainActivity)mContext).mAdapter.setDetailsPageEnabled(true);
	    					((MainActivity)mContext).setTab(Constants.BONUS_TAB_POSITION);
    					}
    				}
    			});
    			
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}
		return convertView;
    }

    private void setupScreenVars()
    {
    	DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();

        this.mScreenHeight = displayMetrics.heightPixels / displayMetrics.density;
        this.mScreenWidth = displayMetrics.widthPixels / displayMetrics.density;
        this.mGridItemSize = Math.round(((mScreenWidth/2) - 4f) * displayMetrics.density);	//The 4f is added padding.
    }
    

	@Override
	public void downloadFinished(int feedId)
	{
		removeLoadingItem();
		if (isLoading())
		{
			((MainActivity)this.mContext).helper.refreshListsAsync();
		}
	}
}
