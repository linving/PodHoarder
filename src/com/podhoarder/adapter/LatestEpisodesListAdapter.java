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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.ExpandAnimation;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ViewHolders.LatestEpisodesAdapterViewHolder;
import com.podhoarderproject.podhoarder.R;

public class LatestEpisodesListAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.LatestEpisodesListAdapter";
	public 	List<Episode> mLatestEpisodes;
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
	public LatestEpisodesListAdapter(List<Episode> latestEpisodes, Context context)
	{
		this.mLatestEpisodes = latestEpisodes;
		this.mContext = context;
		this.mExpanded = null;
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Episode> newItemCollection)
	{
		this.mLatestEpisodes.clear();
		this.mLatestEpisodes.addAll(newItemCollection);
	}

	@Override
	public int getCount()
	{
		return this.mLatestEpisodes.size();
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
		return this.mLatestEpisodes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return this.mLatestEpisodes.get(position).getEpisodeId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LatestEpisodesAdapterViewHolder viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_latest_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new LatestEpisodesAdapterViewHolder();
	        viewHolder.episodeTitle = (TextView) convertView.findViewById(R.id.list_episode_row_episodeName);
	        viewHolder.feedTitle = (TextView) convertView.findViewById(R.id.list_episode_row_feedName);
	        viewHolder.episodeAge = (TextView) convertView.findViewById(R.id.list_episode_row_episodeAge);
	        viewHolder.newNotification = (TextView) convertView.findViewById(R.id.list_episode_row_new);
	        viewHolder.episodeDescription = (TextView) convertView.findViewById(R.id.list_episode_row_expandableTextView);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.list_episode_row_feed_image);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (LatestEpisodesAdapterViewHolder) convertView.getTag();
		}
		
		
		Episode currentEpisode = this.mLatestEpisodes.get(position);
		Feed currentFeed = ((MainActivity)mContext).helper.getFeed(currentEpisode.getFeedId());
		
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
					viewHolder.episodeAge.setText(DateUtils.getRelativeTimeSpanString(
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
			
			viewHolder.feedImage.setImageBitmap(currentFeed.getFeedImage().thumbnail());
			
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
			((TextView)row.findViewById(R.id.list_episode_row_feedName)).setTextColor(res.getColor(R.color.episode_list_row_title_listened));
			((TextView)row.findViewById(R.id.list_episode_row_episodeAge)).setTextColor(res.getColor(R.color.episode_list_row_subtitle_listened));
			if (((TextView)row.findViewById(R.id.list_episode_row_new)).getVisibility() == View.VISIBLE) ((TextView)row.findViewById(R.id.list_episode_row_new)).setVisibility(View.GONE);
		}
		else
		{
			row.findViewById(R.id.list_episode_row_feed_image).setAlpha(1f);
			((TextView)row.findViewById(R.id.list_episode_row_episodeName)).setTextColor(res.getColor(R.color.episode_list_row_title));
			((TextView)row.findViewById(R.id.list_episode_row_feedName)).setTextColor(res.getColor(R.color.episode_list_row_title));
			((TextView)row.findViewById(R.id.list_episode_row_episodeAge)).setTextColor(res.getColor(R.color.episode_list_row_subtitle));
		}
		
	}
	
	public static void setRowListened(Context ctx, LatestEpisodesAdapterViewHolder row, boolean listened)
	{
		Resources res = ctx.getResources();
		if (listened)
		{
			row.feedImage.setAlpha(.5f);
			row.episodeTitle.setTextColor(res.getColor(R.color.episode_list_row_title_listened));
			row.feedTitle.setTextColor(res.getColor(R.color.episode_list_row_title_listened));
			row.episodeAge.setTextColor(res.getColor(R.color.episode_list_row_subtitle_listened));
			if (row.newNotification.getVisibility() == View.VISIBLE) row.newNotification.setVisibility(View.GONE);
		}
		else
		{
			row.feedImage.setAlpha(1f);
			row.episodeTitle.setTextColor(res.getColor(R.color.episode_list_row_title));
			row.feedTitle.setTextColor(res.getColor(R.color.episode_list_row_title));
			row.episodeAge.setTextColor(res.getColor(R.color.episode_list_row_subtitle));
		}
		
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
