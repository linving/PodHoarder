package com.podhoarder.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.ColorFilter;
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
import com.podhoarder.object.CheckableRelativeLayout;
import com.podhoarder.object.Feed;
import com.podhoarder.object.GridActionModeCallback;
import com.podhoarder.object.FeedImage.ImageDownloadListener;
import com.podhoarder.util.Constants;
import com.podhoarderproject.podhoarder.R;

public class GridListAdapter extends BaseAdapter implements ImageDownloadListener
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.GridListAdapter";
	
	public 	List<Feed> feeds;
	
    private Context mContext;
    
    private View footerView;
    
    private View loadingView;
    private boolean loading;
    
    private GridActionModeCallback mActionModeCallback;  //This comes from the parent fragment and is used to keep track of whether the ActionMode context bar is enabled.
	private ActionMode mActionMode;  //This comes from the parent fragment and is used to keep track of whether the ActionMode context bar is enabled.
    
    private int screenWidth, screenHeight; 
    public int gridItemSize;

    public GridListAdapter(List<Feed> feeds, Context c) 
    {
    	this.feeds = feeds;
    	this.mContext = c;
    	
    	this.loading = false;
    	
    	DisplayMetrics metrics = new DisplayMetrics();
		((MainActivity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);

		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		gridItemSize = (screenWidth/2)-10;
    }
    
    /**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Feed> newItemCollection)
	{
		this.feeds.clear();
		this.feeds.addAll(newItemCollection);
		//this.notifyDataSetChanged();
	}

    public int getCount() 
    {
    	int additionalItems = 0;
    	
    	if (this.loading || this.footerView != null)
    		additionalItems++;
    	
    	return this.feeds.size() + additionalItems;
    }

    public Object getItem(int position) {
        return this.feeds.get(position);
    }

    public long getItemId(int position) {
        return this.feeds.get(position).getFeedId();
    }
    
    public void setFooterView(View v) {
    	v.findViewById(R.id.feeds_grid_item_image).setMinimumHeight(gridItemSize);
    	v.findViewById(R.id.feeds_grid_item_image).setMinimumWidth(gridItemSize);
    	v.setMinimumHeight(gridItemSize);
    	v.setMinimumWidth(gridItemSize);
    	this.footerView = v;
    }
    
    public void setLoadingView(View v)
    {
    	v.findViewById(R.id.feeds_grid_loadingBar).setMinimumHeight(gridItemSize);
    	v.findViewById(R.id.feeds_grid_loadingBar).setMinimumWidth(gridItemSize);
    	v.setMinimumHeight(gridItemSize);
    	v.setMinimumWidth(gridItemSize);
    	this.loadingView = v;
    }
    
    public boolean isLoading()
    {
    	return this.loading;
    }
    
    public void setLoading(boolean isLoading)
    {
    	this.loading = isLoading;
    	this.notifyDataSetChanged();
    }

    public void setActionModeVars(ActionMode mode, GridActionModeCallback callback)
    {
    	this.mActionMode = mode;
    	this.mActionModeCallback = callback;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	FeedViewHolderItem viewHolder;
    	
    	if (position == 0)
    	{
    		((GridView) parent).setColumnWidth(gridItemSize);
    	}
    	
    	if (position == getCount()-1)
    	{
    		if (this.loading)
    			return loadingView;
    		else
    			return footerView;
    	}
    	
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
			LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_feeds_grid_item, null);
			convertView.setMinimumHeight(gridItemSize);
			convertView.setMinimumWidth(gridItemSize);
			
			
			// Set up the ViewHolder
	        viewHolder = new FeedViewHolderItem();
	        viewHolder.feedTitle = (TextView) convertView.findViewById(R.id.feeds_grid_item_text);
	        viewHolder.feedNumberOfEpisodes = (TextView) convertView.findViewById(R.id.feeds_grid_item_notification);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.feeds_grid_item_image);
	        viewHolder.feedImage.setMinimumHeight(gridItemSize);
	        viewHolder.feedImage.setMinimumWidth(gridItemSize);
	        viewHolder.checkableLayout = (CheckableRelativeLayout) convertView.findViewById(R.id.feeds_grid_item_checkableLayout);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (FeedViewHolderItem) convertView.getTag();
		}
		
		
		final Feed currentFeed = this.feeds.get(position);
		
		if(currentFeed != null) 
		{
			try
			{
    			
				if (PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean(SettingsActivity.SETTINGS_KEY_GRIDSHOWTITLE, true))
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
    			
    			viewHolder.feedImage.setImageBitmap(currentFeed.getFeedImage().imageObject());
    			viewHolder.feedImage.setColorFilter(mContext.getResources().getColor(R.color.fragment_feeds_grid_item_image_tint));
    			
    			final int pos = position;
    			
    			convertView.setOnClickListener(new OnClickListener()
    			{
    				
    				@Override
    				public void onClick(View v)
    				{
    					if (mActionMode != null && mActionModeCallback != null)
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
    
    static class FeedViewHolderItem {	
	    TextView feedTitle;
	    TextView feedNumberOfEpisodes;
	    ImageView feedImage;
	    CheckableRelativeLayout checkableLayout;
	}

	@Override
	public void downloadFinished(int feedId)
	{
		setLoading(false);
		((MainActivity)this.mContext).helper.refreshLists();
	}
}