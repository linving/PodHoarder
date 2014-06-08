/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarderproject.podhoarder.adapter;

import java.text.ParseException;
import java.util.List;

import com.podhoarderproject.podhoarder.Episode;
import com.podhoarderproject.podhoarder.Feed;
import com.podhoarderproject.podhoarder.MainActivity;
import com.podhoarderproject.podhoarder.PodcastHelper;
import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.adapter.LatestEpisodesListAdapter.ViewHolderItem;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedListAdapter extends BaseExpandableListAdapter
{

	public 	List<Feed> feeds;
	private Context context;

	public FeedListAdapter(List<Feed> feeds, Context context)
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
	public Object getChild(int groupPos, int childPos)
	{
		return this.feeds.get(groupPos).getEpisodes().get(childPos);
	}

	@Override
	public long getChildId(int groupPos, int childPos)
	{
		return ((Integer)childPos).longValue();
	}

	@Override
	public int getChildrenCount(int groupPos)
	{
		return this.feeds.get(groupPos).getEpisodes().size();
	}

	@Override
	public Object getGroup(int groupPos)
	{
		return this.feeds.get(groupPos);
	}

	@Override
	public int getGroupCount()
	{
		return this.feeds.size();
	}
	
	@Override
	public long getCombinedChildId(long groupId, long childId) {
        return groupId * 10000L + childId;
    }

	@Override
    public long getCombinedGroupId(long groupId) {
        return groupId * 10000L;
    }

	@Override
	public long getGroupId(int groupPos)
	{
		return ((Integer)groupPos).longValue();
	}

	@Override
	public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent)
	{
		FeedViewHolderItem viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_feeds_expandable_list_feed_row, null);
			
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
		
		
		Feed currentFeed = this.feeds.get(groupPos);
		
		if(currentFeed != null) 
		{
			viewHolder.feedTitle.setText(currentFeed.getTitle());	//Set Feed Title
			//TODO: Replace with String resource.
			viewHolder.feedNumberOfEpisodes.setText(currentFeed.getEpisodes().size() + " episodes");	//Set number of Episodes
			viewHolder.feedImage.setBackground(currentFeed.getFeedImage().imageObject());
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPos, int childPos, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewHolderItem viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_feeds_expandable_list_episode_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new ViewHolderItem();
	        viewHolder.episodeTitle = (TextView) convertView.findViewById(R.id.list_episode_row_episodeName);
	        viewHolder.episodeAge = (TextView) convertView.findViewById(R.id.list_episode_row_episodeAge);
	        viewHolder.episodeDescription = (TextView) convertView.findViewById(R.id.list_episode_row_expandableTextView);
	        viewHolder.downloadButton = (ImageButton) convertView.findViewById(R.id.list_episode_row_downloadBtn);
	        
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		
		Episode currentEpisode = this.feeds.get(groupPos).getEpisodes().get(childPos);
		
		if(currentEpisode != null) {
			
			viewHolder.episodeTitle.setText(currentEpisode.getTitle());	//Set Episode Title
			viewHolder.episodeDescription.setText(currentEpisode.getDescription());	//Set Episode Description
			
			try
			{
					viewHolder.episodeAge.setText(
							DateUtils.getRelativeTimeSpanString(
									PodcastHelper.correctFormat.parse(
											currentEpisode.getPubDate()).getTime()));
			} 
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(!currentEpisode.getLocalLink().isEmpty()) viewHolder.downloadButton.setVisibility(View.GONE); //Hide Download Button if applicable.
			else {
				viewHolder.downloadButton.setVisibility(View.VISIBLE);
				final int feedId = currentEpisode.getFeedId();
				final int episodeId = currentEpisode.getEpisodeId();
				viewHolder.downloadButton.setOnClickListener(new OnClickListener() 
				   { 
				       @Override
				       public void onClick(View v) 
				       {
				           // Your code that you want to execute on this button click
				    	   ((MainActivity)context).downloadEpisode(feedId, episodeId);
				    	   v.setEnabled(false);
				       }

				   });
			}
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPos, int childPos)
	{
		if (groupPos < this.feeds.size())
		{
			if (childPos < this.feeds.get(groupPos).getEpisodes().size())
			{
				return true;
			}
		}
		return false;
	}

	//This is to improve ListView performance. See link for details.
	//http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	static class FeedViewHolderItem {	
	    TextView feedTitle;
	    TextView feedNumberOfEpisodes;
	    ImageView feedImage;
	}
}
