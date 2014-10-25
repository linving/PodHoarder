package com.podhoarder.adapter;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.activity.MainActivity.ListFilter;
import com.podhoarder.listener.GridActionModeCallback;
import com.podhoarder.object.Feed;
import com.podhoarder.object.FeedImage.ImageDownloadListener;
import com.podhoarder.util.Constants;
import com.podhoarder.util.ViewHolders.FeedsAdapterViewHolder;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

public class GridAdapter extends BaseAdapter implements ImageDownloadListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.GridListAdapter";
	
	private LayoutInflater mInflater;
	
	public 	List<Feed> mItems;
	
    private Context mContext;
    
    private List<View> mLoadingViews;
    private int mLoadingItemsCount;
    
    private GridActionModeCallback mActionModeCallback;  //This comes from the parent fragment and is used to keep track of whether the ActionMode context bar is enabled.
	private ActionMode mActionMode;  //This comes from the parent fragment and is used to keep track of whether the ActionMode context bar is enabled.
    
    public int mGridItemSize;
    
    private boolean mSelectionEnabled;

    private Palette mPalette;

    public GridAdapter(List<Feed> feeds, Context c) 
    {
    	this.mItems = feeds;
    	this.mContext = c;
    	
    	this.mLoadingItemsCount = 0;
    	this.mLoadingViews = new ArrayList<View>();
    	
    	this.mSelectionEnabled = false;
    	
    	this.mInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	
    	setupScreenVars();
    }
    
    /**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Feed> newItemCollection)
	{
		//this.mItems.clear();
		//this.mItems.addAll(newItemCollection);
		this.mItems = newItemCollection;
		this.notifyDataSetChanged();
	}

    public int getCount() 
    {    	
    	if (isLoading())
    		return (this.mItems.size() + this.getLoadingItemsCount());	//We return the items size plus the amount of loading views for the total
    	else
    		return this.mItems.size();	//We return the items size.
    }

    public Object getItem(int position) {
        return this.mItems.get(position);
    }

    public long getItemId(int position) {
        return this.mItems.get(position).getFeedId();
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
    
    public boolean isSelectionEnabled()
	{
		return mSelectionEnabled;
	}

	public void setSelectionEnabled(boolean mSelectionEnabled)
	{
		this.mSelectionEnabled = mSelectionEnabled;
		this.notifyDataSetChanged();
	}

	/**
     * Resets the loading status of this list to not loading.
     */
    public void resetLoading()
    {
    	this.mLoadingViews.clear();
        this.notifyDataSetChanged();
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
    	if (this.mLoadingItemsCount > 0) {
            this.mLoadingItemsCount--;    //We can never have a negative amount of loading items.
        }
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
    	
    	if (position >= mItems.size())
    		return null;

    	if (convertView != null && convertView.findViewById(R.id.fragment_feeds_grid_loading_item) != null) 
    	{ 
    		convertView = null; 
    	}

    	
		if (convertView == null)
		{
			//Inflate
			convertView = this.mInflater.inflate(R.layout.feeds_grid_item, null);
			
			// Set up the ViewHolder
	        viewHolder = new FeedsAdapterViewHolder();
	        viewHolder.feedTitle = (TextView) convertView.findViewById(R.id.feeds_grid_item_text);
	        viewHolder.feedNumberOfEpisodes = (TextView) convertView.findViewById(R.id.feeds_grid_item_notification);
			viewHolder.checkmark = (CheckBox) convertView.findViewById(R.id.feeds_grid_item_checkmark);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.feeds_grid_item_image);
	        viewHolder.checked = false;
	        
	        viewHolder.feedImage.setMinimumHeight(mGridItemSize);
	        viewHolder.feedImage.setMinimumWidth(mGridItemSize);
	        viewHolder.feedImage.setMaxHeight(mGridItemSize);
	        viewHolder.feedImage.setMaxWidth(mGridItemSize);
	        
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

				if (PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(Constants.SETTINGS_KEY_GRIDSHOWTITLE, true) || this.mSelectionEnabled)
				{
                    viewHolder.feedTitle.setBackgroundColor(currentFeed.getFeedImage().palette().getDarkVibrantColor(Color.parseColor("#80000000")));
                    viewHolder.feedTitle.setTextColor(currentFeed.getFeedImage().palette().getDarkVibrantSwatch().getTitleTextColor());
					viewHolder.feedTitle.setText(currentFeed.getTitle());	//Set Feed Title
                    viewHolder.feedTitle.setVisibility(View.VISIBLE);
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

    			viewHolder.feedImage.setImageBitmap(currentFeed.getFeedImage().imageObject());
    			viewHolder.feedImage.setSelected(viewHolder.checked);

    			if (this.mSelectionEnabled)
    				viewHolder.checkmark.setVisibility(View.VISIBLE);
    			else
    				viewHolder.checkmark.setVisibility(View.GONE);

    			viewHolder.checkmark.setChecked(viewHolder.checked);
    			
    			final int pos = position;
    			
    			convertView.setOnClickListener(new OnClickListener()
    			{
    				
    				@Override
    				public void onClick(View v)
    				{
    					if (mActionMode != null && mActionModeCallback != null)
    					{
    						if (v.getId() != R.id.fragment_feeds_grid_loading_item)	//We prevent the user from selecting the loading and add feed grid items
    						{
    							if (mActionModeCallback.isActive())
        						{
        							mActionModeCallback.onItemCheckedStateChanged(pos, !((CheckBox)v.findViewById(R.id.feeds_grid_item_checkmark)).isChecked()); 
        						}
        						else
        						{
        							ListFilter filter = ListFilter.FEED;
            						filter.setFeedId(currentFeed.getFeedId());
        							((MainActivity)mContext).setFilter(filter);
        						}
    						}
    					}
    					else
    					{
    						ListFilter filter = ListFilter.FEED;
    						filter.setFeedId(currentFeed.getFeedId());
    						((MainActivity)mContext).setFilter(filter);
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

        float mScreenWidth = displayMetrics.widthPixels / displayMetrics.density;
        this.mGridItemSize = Math.round(((mScreenWidth/2) - 6f) * displayMetrics.density);	//The 4f is added padding.
    }

	@Override
	public void downloadFinished(int feedId)
	{
		removeLoadingItem();
		if (!isLoading())
		{
			((MainActivity)this.mContext).mPodcastHelper.forceRefreshContent();
		}
	}
}
