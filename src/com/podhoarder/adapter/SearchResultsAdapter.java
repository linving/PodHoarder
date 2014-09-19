package com.podhoarder.adapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarder.object.SearchResultRow;
import com.podhoarder.util.BitmapManager;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ViewHolders.SearchResultsAdapterViewHolder;
import com.podhoarderproject.podhoarder.R;

public class SearchResultsAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final 	String 								LOG_TAG = "com.podhoarder.adapter.SearchResultsAdapter";
	private 				List<SearchResultRow> 				results;
	private 				Context 							context;
	private					BitmapManager						mBitmapManager;

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
		this.results = new ArrayList<SearchResultRow>();
		this.context = context;
		this.mBitmapManager = new BitmapManager();
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<SearchResultRow> newItemCollection)
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
		SearchResultsAdapterViewHolder viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_search_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new SearchResultsAdapterViewHolder();
	        viewHolder.feedTitle = (TextView) convertView.findViewById(R.id.search_list_feed_title);
	        viewHolder.feedAuthor = (TextView) convertView.findViewById(R.id.search_list_feed_author);
	        viewHolder.lastUpdated = (TextView) convertView.findViewById(R.id.search_list_feed_last_updated);
	        viewHolder.feedDescription = (TextView) convertView.findViewById(R.id.search_list_feed_description);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.search_list_feed_image);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (SearchResultsAdapterViewHolder) convertView.getTag();
		}
		
		
		SearchResultRow currentResult = this.results.get(position);
		
		if(currentResult != null) 
		{
			//Set Feed Title
			viewHolder.feedTitle.setText(currentResult.getTitle());
			//Set Feed Author
			viewHolder.feedAuthor.setText(this.context.getString(R.string.notification_by) + " " + currentResult.getAuthor());
			//Set Feed Description
			if (currentResult == null || !currentResult.getDescription().isEmpty())
				viewHolder.feedDescription.setText(Html.fromHtml(currentResult.getDescription()).toString());
			else
				viewHolder.feedDescription.setText(this.context.getString(R.string.search_list_feed_no_description));
			//Set Last Updated string
			try
			{
					viewHolder.lastUpdated.setText(context.getString(R.string.search_list_feed_last_updated) + " " + 
							DateUtils.getRelativeTimeSpanString(
									PodcastHelper.correctFormat.parse(currentResult.getLastUpdated()).getTime()));	//Set a time stamp since Episode publication.
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
			if (mBitmapManager.isCached(currentResult.getImageUrl()))
				viewHolder.feedImage.setImageBitmap(mBitmapManager.fetchBitmap(currentResult.getImageUrl(), viewHolder.feedImage.getMaxWidth()));
			else
				mBitmapManager.fetchBitmapOnThread(currentResult.getImageUrl(), viewHolder.feedImage);
			
			viewHolder.feedImage.invalidate();
		}
		
		return convertView;
	}
	
	public void add(SearchResultRow row)
	{
		this.results.add(row);
	}
	
	public void clear()
	{
		this.results.clear();
	}
}
