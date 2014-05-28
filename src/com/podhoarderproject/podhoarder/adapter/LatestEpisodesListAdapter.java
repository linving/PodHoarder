/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarderproject.podhoarder.adapter;

import java.text.ParseException;
import java.util.List;

import com.podhoarderproject.podhoarder.Episode;
import com.podhoarderproject.podhoarder.MainActivity;
import com.podhoarderproject.podhoarder.PodcastHelper;
import com.podhoarderproject.podhoarder.R;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

public class LatestEpisodesListAdapter extends BaseAdapter implements ListAdapter
{

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
			convertView = inflater.inflate(R.layout.fragment_latest_list_episode_row, null);
			
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
		
		
		Episode currentEpisode = this.latestEpisodes.get(position);
		
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

	//This is to improve ListView performance. See link for details.
	//http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	static class ViewHolderItem {	
	    TextView episodeTitle;
	    TextView episodeAge;
	    TextView episodeDescription;
	    ImageButton downloadButton;
	}
}
