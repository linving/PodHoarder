/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarder.adapter;

import java.text.ParseException;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ViewHolders.FeedDetailsAdapterViewHolder;
import com.podhoarderproject.podhoarder.R;

public class FeedDetailsListAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.FeedDetailsListAdapter";
	public 	Feed mFeed;
	private Context mContext;

	/**
	 * Creates a LatestEpisodesListAdapter (Constructor).
	 * 
	 * @param latestEpisodes
	 *            A List<Episode> containing the X latest episodes from all feeds.
	 * @param context
	 *            A Context object from the parent Activity.
	 *            
	 */
	public FeedDetailsListAdapter(Context context)
	{
		this.mContext = context;
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Episode> newItemCollection)
	{
		this.mFeed.getEpisodes().clear();
		this.mFeed.getEpisodes().addAll(newItemCollection);
	}
	
	public void setFeed(Feed feedToSet)
	{
		this.mFeed = feedToSet;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return this.mFeed.getEpisodes().size();
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
		return this.mFeed.getEpisodes().get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return this.mFeed.getEpisodes().get(position).getEpisodeId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		FeedDetailsAdapterViewHolder viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_feed_details_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new FeedDetailsAdapterViewHolder();
	        viewHolder.episodeTitle = (TextView) convertView.findViewById(R.id.list_episode_row_episodeName);
	        viewHolder.episodeAge = (TextView) convertView.findViewById(R.id.list_episode_row_episodeAge);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.list_episode_row_feed_image);
	        viewHolder.newNotification = (TextView) convertView.findViewById(R.id.list_episode_row_new);
	        viewHolder.episodeDescription = (TextView) convertView.findViewById(R.id.list_episode_row_expandableTextView);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (FeedDetailsAdapterViewHolder) convertView.getTag();
		}
		
		
		Episode currentEpisode = this.mFeed.getEpisodes().get(position);
		
		if(currentEpisode != null) {
			//Set Episode Title
			viewHolder.episodeTitle.setText(currentEpisode.getTitle());	
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
			
			if (currentEpisode.isNew()) 
				viewHolder.newNotification.setVisibility(View.VISIBLE);
			else
				viewHolder.newNotification.setVisibility(View.GONE);

			viewHolder.feedImage.setImageBitmap(mFeed.getFeedImage().thumbnail());
			
			setRowListened(mContext, viewHolder, currentEpisode.isListened());			
		}
		
		return convertView;
	}

	public static void setRowListened(Context ctx, View row, boolean listened)
	{
		Resources res = ctx.getResources();
		if (listened)
		{
			row.findViewById(R.id.list_episode_row_feed_image).setAlpha(.5f);
			((TextView)row.findViewById(R.id.list_episode_row_episodeName)).setTextColor(res.getColor(R.color.episode_list_row_title_listened));
			((TextView)row.findViewById(R.id.list_episode_row_expandableTextView)).setTextColor(res.getColor(R.color.episode_list_row_title_listened));
			((TextView)row.findViewById(R.id.list_episode_row_episodeAge)).setTextColor(res.getColor(R.color.episode_list_row_subtitle_listened));
			if (((TextView)row.findViewById(R.id.list_episode_row_new)).getVisibility() == View.VISIBLE) ((TextView)row.findViewById(R.id.list_episode_row_new)).setVisibility(View.GONE);
		}
		else
		{
			row.findViewById(R.id.list_episode_row_feed_image).setAlpha(1f);
			((TextView)row.findViewById(R.id.list_episode_row_episodeName)).setTextColor(res.getColor(R.color.episode_list_row_title));
			((TextView)row.findViewById(R.id.list_episode_row_expandableTextView)).setTextColor(res.getColor(R.color.episode_list_row_title));
			((TextView)row.findViewById(R.id.list_episode_row_episodeAge)).setTextColor(res.getColor(R.color.episode_list_row_subtitle));
		}
		
	}
	
	public static void setRowListened(Context ctx, FeedDetailsAdapterViewHolder row, boolean listened)
	{
		Resources res = ctx.getResources();
		if (listened)
		{
			row.feedImage.setAlpha(.5f);
			row.episodeTitle.setTextColor(res.getColor(R.color.episode_list_row_title_listened));
			row.episodeAge.setTextColor(res.getColor(R.color.episode_list_row_subtitle_listened));
			row.episodeDescription.setTextColor(res.getColor(R.color.episode_list_row_subtitle_listened));
			if (row.newNotification.getVisibility() == View.VISIBLE) row.newNotification.setVisibility(View.GONE);
		}
		else
		{
			row.feedImage.setAlpha(1f);
			row.episodeTitle.setTextColor(res.getColor(R.color.episode_list_row_title));
			row.episodeAge.setTextColor(res.getColor(R.color.episode_list_row_subtitle));
			row.episodeDescription.setTextColor(res.getColor(R.color.episode_list_row_subtitle));
		}
		
	}
}
