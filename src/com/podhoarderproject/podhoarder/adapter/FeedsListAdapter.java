/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarderproject.podhoarder.adapter;

import java.util.List;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.util.Feed;
import com.podhoarderproject.podhoarder.util.ImageUtils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class FeedsListAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.FeedsListAdapter";
	public 	List<Feed> feeds;
	private Context context;

	/**
	 * Creates a FeedsListAdapter (Constructor).
	 * 
	 * @param latestEpisodes
	 *            A List<Episode> containing the X latest episodes from all feeds.
	 * @param context
	 *            A Context object from the parent Activity.
	 *            
	 */
	public FeedsListAdapter(List<Feed> feeds, Context context)
	{
		this.feeds = feeds;
		this.context = context;
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Feed> newItemCollection)
	{
		this.feeds.clear();
		this.feeds.addAll(newItemCollection);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return this.feeds.size();
	}

	@Override
	public int getViewTypeCount() 
	{
	    return getCount();
	}

	@Override
	public int getItemViewType(int position) 
	{
	    return position;
	}
	
	@Override
	public Object getItem(int position)
	{
		return this.feeds.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return this.feeds.get(position).getFeedId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		FeedViewHolderItem viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_feeds_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new FeedViewHolderItem();
	        viewHolder.feedTitle = (TextView) convertView.findViewById(R.id.list_feed_row_title);
	        viewHolder.feedNumberOfEpisodes = (TextView) convertView.findViewById(R.id.list_feed_row_numberOfEpisodes);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.list_feed_row_image);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (FeedViewHolderItem) convertView.getTag();
		}
		
		
		Feed currentFeed = this.feeds.get(position);
		
		if(currentFeed != null) 
		{
			viewHolder.feedTitle.setText(currentFeed.getTitle());	//Set Feed Title
			viewHolder.feedNumberOfEpisodes.setText(currentFeed.getEpisodes().size() + " " + context.getString(R.string.fragment_feeds_list_feed_row_episodes));	//Set number of Episodes
			viewHolder.feedImage.setBackground(new BitmapDrawable(context.getResources(),ImageUtils.getCircularBitmap(currentFeed.getFeedImage().imageObject().getBitmap())));

		}
		return convertView;
	}
	
	//This is to improve ListView performance. See link for details.
	//http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	static class FeedViewHolderItem {	
	    TextView feedTitle;
	    TextView feedNumberOfEpisodes;
	    ImageView feedImage;
	    ImageView groupIndicator;
	}
}
