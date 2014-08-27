package com.podhoarder.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarder.json.SearchResultItem;
import com.podhoarder.util.ImageUtils;
import com.podhoarderproject.podhoarder.R;

public class SearchResultsAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final 	String 								LOG_TAG = "com.podhoarderproject.podhoarder.SearchResultsAdapter";
	public 	static final 	SimpleDateFormat 					itFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public 					List<SearchResultItem> 				results;
	private 				Context 							context;

	/**
	 * Creates a LatestEpisodesListAdapter (Constructor).
	 * 
	 * @param latestEpisodes
	 *            A List<Episode> containing the X latest episodes from all feeds.
	 * @param context
	 *            A Context object from the parent Activity.
	 *            
	 */
	public SearchResultsAdapter(Context context)
	{
		this.results = new ArrayList<SearchResultItem>();
		this.context = context;
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<SearchResultItem> newItemCollection)
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
		return position;
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
	        viewHolder.feedAuthor = (TextView) convertView.findViewById(R.id.search_list_feed_author);
	        viewHolder.lastUpdated = (TextView) convertView.findViewById(R.id.search_list_feed_last_updated);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.search_list_feed_image);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		
		SearchResultItem currentResult = this.results.get(position);
		
		if(currentResult != null) 
		{
			//Set Feed Title
			viewHolder.feedTitle.setText(currentResult.getCollectionName());
			//Set Feed Description
			viewHolder.feedAuthor.setText(this.context.getString(R.string.notification_by) + " " + currentResult.getArtistName());
			//Set Last Updated string
			try
			{
					viewHolder.lastUpdated.setText(context.getString(R.string.search_list_feed_last_updated) + " " + 
							DateUtils.getRelativeTimeSpanString(itFormat.parse(currentResult.getReleaseDate()).getTime()));	//Set a time stamp since Episode publication.
			} 
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (NullPointerException ex)
			{
				viewHolder.lastUpdated.setText(context.getString(R.string.search_list_feed_last_updated) + " " + context.getString(R.string.search_list_feed_last_updated_unknown));	//Set a time stamp since Episode publication.
			}
			//Set Bitmap Image
			new ImageUtils.DownloadImageTask(viewHolder.feedImage).execute(currentResult.getArtworkUrl60());
		}
		
		return convertView;
	}
	
	//This is to improve ListView performance. See link for details.
	//http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	static class ViewHolderItem {	
	    TextView 			feedTitle;
	    TextView 			feedAuthor;
	    TextView 			lastUpdated;
	    ImageView 			explicitIndicator;
	    ImageView 	feedImage;
	}
}
