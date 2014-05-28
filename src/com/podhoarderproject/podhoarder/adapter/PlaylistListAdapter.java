/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarderproject.podhoarder.adapter;

import java.util.List;

import com.podhoarderproject.podhoarder.Episode;
import com.podhoarderproject.podhoarder.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;



public class PlaylistListAdapter extends BaseAdapter implements ListAdapter
{

	public 	List<Episode> playList;
	private Context context;

	/**
	 * Creates a LatestEpisodesListAdapter (Constructor).
	 * 
	 * @param playList
	 *            A List<Episode> containing the downloaded episodes ordered by the user.
	 * @param context
	 *            A Context object from the parent Activity.
	 *            
	 */
	public PlaylistListAdapter(List<Episode> playList, Context context)
	{
		this.playList = playList;
		this.context = context;
	}

	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Episode> newItemCollection)
	{
		this.playList.clear();
		this.playList.addAll(newItemCollection);
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount()
	{
		return this.playList.size();
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
		return this.playList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return this.playList.get(position).getEpisodeId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolderItem viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.fragment_player_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new ViewHolderItem();
	        viewHolder.episodeTitle = (TextView) convertView.findViewById(R.id.player_list_row_episodeName);
	        viewHolder.episodeListened = (TextView) convertView.findViewById(R.id.player_list_row_listened);
	        viewHolder.grabberImage = (ImageView) convertView.findViewById(R.id.player_list_row_grabber);
	        
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		
		Episode currentEpisode = this.playList.get(position);
		
		if(currentEpisode != null) {
			
			viewHolder.episodeTitle.setText(currentEpisode.getTitle());	//Set Episode Title			
			//TODO: Insert string resource below.
			viewHolder.episodeListened.setText(currentEpisode.getMinutesListened() + " listened."); //Set time listened.
			
			final int feedId = currentEpisode.getFeedId();
			final int episodeId = currentEpisode.getEpisodeId();
			//Do drag n drop reordering here.
//			viewHolder.downloadButton.setOnClickListener(new OnClickListener() 
//			   { 
//			       @Override
//			       public void onClick(View v) 
//			       {
//			           // Your code that you want to execute on this button click
//			    	   ((MainActivity)context).downloadEpisode(feedId, episodeId);
//			    	   v.setVisibility(View.GONE);
//			       }
//
//			   });
		}
		return convertView;
	}

	//This is to improve ListView performance. See link for details.
	//http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	static class ViewHolderItem {	
	    TextView episodeTitle;
	    TextView episodeListened;
	    ImageView grabberImage;
	}
}
