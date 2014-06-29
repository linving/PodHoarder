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
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

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
			convertView = inflater.inflate(R.layout.fragment_latest_list_episode_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new ViewHolderItem();
	        viewHolder.episodeTitle = (TextView) convertView.findViewById(R.id.list_episode_row_episodeName);
	        viewHolder.episodeAge = (TextView) convertView.findViewById(R.id.list_episode_row_episodeAge);
	        viewHolder.episodeDescription = (TextView) convertView.findViewById(R.id.list_episode_row_expandableTextView);
	        viewHolder.elapsedTimeBar = (ProgressBar) convertView.findViewById(R.id.list_episode_row_elapsed_progressBar);
	        viewHolder.downloadButton = (Button) convertView.findViewById(R.id.list_episode_row_downloadBtn);
	        viewHolder.deleteButton = (Button) convertView.findViewById(R.id.list_episode_row_deleteBtn);
	        viewHolder.playButton = (Button) convertView.findViewById(R.id.list_episode_row_playBtn);
	        viewHolder.streamButton = (Button) convertView.findViewById(R.id.list_episode_row_streamBtn);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		
		Episode currentEpisode = this.latestEpisodes.get(position);
		
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
			//Set Episode Elapsed Time.
			if (currentEpisode.getTotalTime() != 0)	//We can only set the progressbar if we have downloaded the file to find out the total runtime.
			{
				viewHolder.elapsedTimeBar.setMax(currentEpisode.getTotalTime());
				viewHolder.elapsedTimeBar.setProgress(currentEpisode.getElapsedTime());
			}
			else	//If we have never downloaded the file then we haven't listened to it so just set it to 0 progress out of a 100.
			{
				viewHolder.elapsedTimeBar.setMax(100);
				viewHolder.elapsedTimeBar.setProgress(0);
			}
			
			final int feedId = currentEpisode.getFeedId();
			final int episodeId = currentEpisode.getEpisodeId();
			final Episode currentEp = currentEpisode;
			
			if(!currentEpisode.getLocalLink().isEmpty())	//The Episode.localLink property has a value, which means that the Episode is downloaded. We should show the Play button instead of the Download button.
			{
				viewHolder.downloadButton.setVisibility(View.GONE); //Hide Download Button.
				viewHolder.playButton.setVisibility(View.VISIBLE);	//Show the Play button.
				viewHolder.playButton.setOnClickListener(new OnClickListener() 
				   { 
				       @Override
				       public void onClick(View v) 
				       {
				    	   ((MainActivity)context).podService.startEpisode(currentEp);
				    	   ((MainActivity)context).getActionBar().setSelectedNavigationItem(2);	//Navigate to the Player Fragment automatically.
				    	   v.setEnabled(false);
				       }

				   });
				viewHolder.deleteButton.setVisibility(View.VISIBLE);	//Show the Delete button.
				viewHolder.deleteButton.setOnClickListener(new OnClickListener() 
				   { 
				       @Override
				       public void onClick(View v) 
				       {
				    	   //TODO: Make sure this isn't the file that is currently playing in Service.
				    	   ((MainActivity)context).helper.deleteEpisode(currentEp.getFeedId(), currentEp.getEpisodeId());
				    	   v.setEnabled(false);
				       }

				   });
			}
			else 	//The Episode has not been downloaded, so we can't show the Play button. The Download button should be there instead.
			{
				viewHolder.downloadButton.setVisibility(View.VISIBLE); //Show Download Button.
				viewHolder.playButton.setVisibility(View.GONE);	//Hide the Play button.
				viewHolder.deleteButton.setVisibility(View.GONE);	//Hide the Delete button.
				viewHolder.downloadButton.setOnClickListener(new OnClickListener() 
				   { 
				       @Override
				       public void onClick(View v) 
				       {
				    	   ((MainActivity)context).downloadEpisode(feedId, episodeId);
				    	   v.setEnabled(false);
				       }

				   });
			}
			
			viewHolder.streamButton.setOnClickListener(new OnClickListener() 	//We always want the option to stream an Episode.
			   { 
			       @Override
			       public void onClick(View v) 
			       {
			    	   ((MainActivity)context).podService.streamEpisode(currentEp);
			    	   ((MainActivity)context).getActionBar().setSelectedNavigationItem(2);	//Navigate to the Player Fragment automatically.
			    	   v.setEnabled(false);
			       }

			   });
		}
		return convertView;
	}

	//This is to improve ListView performance. See link for details.
	//http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	static class ViewHolderItem {	
	    TextView episodeTitle;
	    TextView episodeAge;
	    TextView episodeDescription;
	    ProgressBar elapsedTimeBar;
	    Button downloadButton;
	    Button deleteButton;
	    Button playButton;
	    Button streamButton;
	}
}
