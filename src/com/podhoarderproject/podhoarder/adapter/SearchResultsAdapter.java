package com.podhoarderproject.podhoarder.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.util.Feed;

public class SearchResultsAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.LatestEpisodesListAdapter";
	public 	List<Feed> results;
	private Context context;

	/**
	 * Creates a LatestEpisodesListAdapter (Constructor).
	 * 
	 * @param latestEpisodes
	 *            A List<Episode> containing the X latest episodes from all feeds.
	 * @param context
	 *            A Context object from the parent Activity.
	 *            
	 */
	public SearchResultsAdapter(List<Feed> results, Context context)
	{
		this.results = results;
		this.context = context;
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Feed> newItemCollection)
	{
		this.results.clear();
		this.results.addAll(newItemCollection);
	}

	@Override
	public int getCount()
	{
		return this.results.size();
	}

	@Override
	public Object getItem(int position)
	{
		return this.results.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return this.results.get(position).getFeedId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolderItem viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_search_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new ViewHolderItem();
	        viewHolder.feedTitle = (TextView) convertView.findViewById(R.id.search_list_feed_title);
	        viewHolder.feedDescription = (TextView) convertView.findViewById(R.id.search_list_feed_description);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.search_list_feed_image);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		
		Feed currentFeed = this.results.get(position);
		
		if(currentFeed != null) 
		{
			//Set Feed Title
			viewHolder.feedTitle.setText(currentFeed.getTitle());
			//Set Feed Description
			viewHolder.feedDescription.setText(currentFeed.getDescription());	
			
			viewHolder.feedImage.setImageBitmap(currentFeed.getFeedImage().thumbnail());
			
		}
		
		return convertView;
	}
	
	//This is to improve ListView performance. See link for details.
	//http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	static class ViewHolderItem {	
	    TextView feedTitle;
	    TextView feedDescription;
	    ImageView feedImage;
	}
}
