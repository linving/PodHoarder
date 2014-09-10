/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarder.adapter;

import java.text.ParseException;
import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.EpisodeRowUtils;
import com.podhoarder.util.ExpandAnimation;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ViewHolders.FeedDetailsRowViewHolder;
import com.podhoarderproject.podhoarder.R;

public class FeedDetailsListAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.FeedDetailsListAdapter";
	public 	Feed mFeed;
	private View mExpanded;
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
		this.mExpanded = null;
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
		FeedDetailsRowViewHolder viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_feed_details_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new FeedDetailsRowViewHolder();
	        viewHolder.episodeTitle = (TextView) convertView.findViewById(R.id.list_episode_row_episodeName);
	        viewHolder.episodeAge = (TextView) convertView.findViewById(R.id.list_episode_row_episodeAge);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.list_episode_row_feed_image);
	        viewHolder.indicator = (View) convertView.findViewById(R.id.row_indicator);
	        viewHolder.indicatorExtension = (View) convertView.findViewById(R.id.row_indicator_extension);
	        viewHolder.episodeDescription = (TextView) convertView.findViewById(R.id.list_episode_row_expandableTextView);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (FeedDetailsRowViewHolder) convertView.getTag();
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

			viewHolder.feedImage.setImageBitmap(mFeed.getFeedImage().thumbnail());
			
			EpisodeRowUtils.setRowIndicator(this.mContext, viewHolder, currentEpisode);
			
			EpisodeRowUtils.setRowListened(viewHolder, currentEpisode.isListened());			
		}
		
		return convertView;
	}


	public void toggleRowExpanded(View v)
	{
		if (mExpanded == null)	//This means there is no row that's currently expanded, so we can just expand the one that was clicked.
		{
			expand(v);
	        mExpanded = v;
		}
		else if (mExpanded == v)	//If we click the one view that has been expanded already, we simply collapse it.
		{
			expand(mExpanded);
	        mExpanded = null;
		}
		else	//This means there is a row that is expanded. Collapse that one and then call this method again, to expand the new one.
		{
			expand(mExpanded);
	        mExpanded = null;
	        toggleRowExpanded(v);
		}
	}
	
	private void expand(View v)
	{
		LinearLayout episodeDescription = (LinearLayout)v.findViewById(R.id.list_episode_row_expandable_container); 
        ExpandAnimation expandAni = new ExpandAnimation(episodeDescription, 100);	// Creating the expand animation for the item
        episodeDescription.startAnimation(expandAni);
	}
}
