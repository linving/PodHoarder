/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarderproject.podhoarder.adapter;

import java.text.ParseException;
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

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.activity.MainActivity;
import com.podhoarderproject.podhoarder.util.Episode;
import com.podhoarderproject.podhoarder.util.Feed;
import com.podhoarderproject.podhoarder.util.PodcastHelper;

public class LatestEpisodesListAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.LatestEpisodesListAdapter";
	public 	List<Episode> latestEpisodes;
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
	public LatestEpisodesListAdapter(List<Episode> latestEpisodes, Context context)
	{
		this.latestEpisodes = latestEpisodes;
		this.context = context;
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Episode> newItemCollection)
	{
		this.latestEpisodes.clear();
		this.latestEpisodes.addAll(newItemCollection);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return this.latestEpisodes.size();
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
		return this.latestEpisodes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return this.latestEpisodes.get(position).getEpisodeId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolderItem viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_latest_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new ViewHolderItem();
	        viewHolder.episodeTitle = (TextView) convertView.findViewById(R.id.list_episode_row_episodeName);
	        viewHolder.feedTitle = (TextView) convertView.findViewById(R.id.list_episode_row_feedName);
	        viewHolder.episodeAge = (TextView) convertView.findViewById(R.id.list_episode_row_episodeAge);
	        viewHolder.episodeDescription = (TextView) convertView.findViewById(R.id.list_episode_row_expandableTextView);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.list_episode_row_feed_image);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		
		Episode currentEpisode = this.latestEpisodes.get(position);
		Feed currentFeed = ((MainActivity)context).helper.getFeed(currentEpisode.getFeedId());
		
		if(currentEpisode != null) {
			//Set Episode Title
			viewHolder.episodeTitle.setText(currentEpisode.getTitle());
			//Set Feed Title
			viewHolder.feedTitle.setText(currentFeed.getTitle());
			//Set Episode Description
			viewHolder.episodeDescription.setText(currentEpisode.getDescription());	
			//Set Episode Timestamp.
			try
			{
					viewHolder.episodeAge.setText(
							DateUtils.getRelativeTimeSpanString(
									PodcastHelper.correctFormat.parse(
											currentEpisode.getPubDate()).getTime()));	//Set a time stamp since Episode publication.
			} 
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			viewHolder.feedImage.setImageBitmap(currentFeed.getFeedImage().thumbnail());
			
			if (currentEpisode.isListened())
			{
				convertView.setAlpha(.5f);
			}
			else
			{
				convertView.setAlpha(1f);
			}
			
		}
		
		return convertView;
	}

	//This is to improve ListView performance. See link for details.
	//http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	static class ViewHolderItem {	
	    TextView episodeTitle;
	    TextView feedTitle;
	    TextView episodeAge;
	    TextView episodeDescription;
	    ImageView feedImage;
	}
}
