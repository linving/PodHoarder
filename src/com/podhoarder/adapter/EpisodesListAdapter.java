/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarder.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.DataParser;
import com.podhoarder.util.EpisodeRowUtils;
import com.podhoarder.util.ImageUtils;
import com.podhoarder.util.ViewHolders.EpisodeRowViewHolder;
import com.podhoarderproject.podhoarder.R;

import java.text.ParseException;
import java.util.List;

public class EpisodesListAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.EpisodesListAdapter";
	public 	List<Episode> mEpisodes;
	protected Context mContext;
	private boolean mSelectionEnabled;

	/**
	 * Creates a LatestEpisodesListAdapter (Constructor).
	 * 
	 * @param latestEpisodes
	 *            A List<Episode> containing the X latest episodes from all feeds.
	 * @param context
	 *            A Context object from the parent Activity.
	 *            
	 */
	public EpisodesListAdapter(List<Episode> episodes, Context context)
	{
		this.mEpisodes = episodes;
		this.mContext = context;
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Episode> newItemCollection)
	{
		this.mEpisodes = newItemCollection;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return this.mEpisodes.size();
	}


	
	@Override
	public Object getItem(int position)
	{
		return this.mEpisodes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return this.mEpisodes.get(position).getEpisodeId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		EpisodeRowViewHolder viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.episode_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new EpisodeRowViewHolder();
	        viewHolder.title = (TextView) convertView.findViewById(R.id.list_episode_row_episodeName);
	        viewHolder.age = (TextView) convertView.findViewById(R.id.list_episode_row_episodeAge);
	        viewHolder.feedImage = (ImageView) convertView.findViewById(R.id.list_episode_row_feed_image);
	        viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.list_episode_row_checkbox);
	        viewHolder.arrow = (ImageView) convertView.findViewById(R.id.list_episode_row_info);
	        viewHolder.checked = false;
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (EpisodeRowViewHolder) convertView.getTag();
		}
		
		
		final Episode currentEpisode = this.mEpisodes.get(position);
		Feed currentFeed = ((LibraryActivity)mContext).mDataManager.getFeed(currentEpisode.getFeedId());
		
		if(currentEpisode != null) {
			//Set Episode Title
			viewHolder.title.setText(currentEpisode.getTitle());	
			//Set Episode Timestamp.
			try
			{
					viewHolder.age.setText(DateUtils.getRelativeTimeSpanString(
									DataParser.correctFormat.parse(
											currentEpisode.getPubDate()).getTime()));	//Set a time stamp since Episode publication.
			} 
			catch (ParseException e)
			{
				e.printStackTrace();
			}
			
			viewHolder.arrow.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					((LibraryActivity)mContext).startEpisodeDetailsActivity(currentEpisode);
				}
			});
			
			if (mSelectionEnabled)
			{
				viewHolder.checkbox.setVisibility(View.VISIBLE);
				viewHolder.feedImage.setVisibility(View.GONE);
				viewHolder.arrow.setVisibility(View.GONE);
			}
			else
			{
				viewHolder.checkbox.setVisibility(View.GONE);
				viewHolder.feedImage.setVisibility(View.VISIBLE);
				viewHolder.arrow.setVisibility(View.VISIBLE);
				viewHolder.feedImage.setImageBitmap(ImageUtils.getCircularBitmapWithBorder(currentFeed.getFeedImage().thumbnail(), 1f));
			}
			
			viewHolder.checkbox.setChecked(viewHolder.checked);				
			
			EpisodeRowUtils.setRowIndicator(this.mContext, viewHolder, currentEpisode);
			EpisodeRowUtils.setRowListened(viewHolder, currentEpisode.isListened());
		}
		
		return convertView;
	}

	public boolean isSelectionEnabled()
	{
		return mSelectionEnabled;
	}

	public void setSelectionEnabled(boolean mSelectionEnabled)
	{
		this.mSelectionEnabled = mSelectionEnabled;
	}

}
