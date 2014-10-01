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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.EpisodeRowUtils;
import com.podhoarder.util.ExpandAnimation;
import com.podhoarder.util.ImageUtils;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ViewHolders.EpisodeRowViewHolder;
import com.podhoarderproject.podhoarder.R;

public class EpisodesListAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.LatestEpisodesListAdapter";
	public 	List<Episode> mEpisodes;
	private View mExpanded;
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
	public EpisodesListAdapter(List<Episode> latestEpisodes, Context context)
	{
		this.mEpisodes = latestEpisodes;
		this.mContext = context;
		this.mExpanded = null;
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Episode> newItemCollection)
	{
		this.mEpisodes.clear();
		this.mEpisodes.addAll(newItemCollection);
	}

	@Override
	public int getCount()
	{
		return this.mEpisodes.size();
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
	        viewHolder.indicator = (ImageView) convertView.findViewById(R.id.list_episode_row_indicator);
	        viewHolder.description = (TextView) convertView.findViewById(R.id.list_episode_row_expandableTextView);
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (EpisodeRowViewHolder) convertView.getTag();
		}
		
		
		Episode currentEpisode = this.mEpisodes.get(position);
		Feed currentFeed = ((MainActivity)mContext).helper.getFeed(currentEpisode.getFeedId());
		
		if(currentEpisode != null) {
			//Set Episode Title
			viewHolder.title.setText(currentEpisode.getTitle());
			//Set Feed Title
			//viewHolder.feedTitle.setText(currentFeed.getTitle());
			//Set Episode Description
			viewHolder.description.setText(currentEpisode.getDescription());	
			//Set Episode Timestamp.
			try
			{
					viewHolder.age.setText(DateUtils.getRelativeTimeSpanString(
									PodcastHelper.correctFormat.parse(
											currentEpisode.getPubDate()).getTime()));	//Set a time stamp since Episode publication.
			} 
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (mSelectionEnabled)
			{
				viewHolder.checkbox.setVisibility(View.VISIBLE);
				viewHolder.feedImage.setVisibility(View.GONE);
			}
			else
			{
				viewHolder.checkbox.setVisibility(View.GONE);
				viewHolder.feedImage.setVisibility(View.VISIBLE);
				viewHolder.feedImage.setImageBitmap(ImageUtils.getCircularBitmapWithBorder(currentFeed.getFeedImage().thumbnail(), 1f));
			}
			
			viewHolder.checkbox.setChecked(convertView.isSelected());				
			
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
