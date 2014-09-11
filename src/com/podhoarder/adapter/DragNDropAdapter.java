/*
 * Copyright (C) 2010 Eric Harlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.podhoarder.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericharlow.DragNDrop.DropListener;
import com.podhoarder.activity.MainActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.EpisodeRowUtils;
import com.podhoarder.util.ViewHolders.PlaylistRowViewHolder;
import com.podhoarderproject.podhoarder.R;

public final class DragNDropAdapter extends BaseAdapter implements DropListener{

	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.DragNDropAdapter";
	
	public 	List<Episode> mPlayList;
	private Context mContext;

	private boolean mReorderingEnabled;
    
    public DragNDropAdapter(List<Episode> playList, Context context) {
    	this.mContext = context;
    	this.mPlayList = playList;
    	this.mReorderingEnabled = true;
    }
    
    /**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Episode> newItemCollection)
	{
		this.mPlayList.clear();
		this.mPlayList.addAll(newItemCollection);
	}
	
    /**
     * The number of items in the list
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() 
    {
    	if (this.mPlayList.size() < 2)
    		this.mReorderingEnabled = false;
    	else
    		this.mReorderingEnabled = true;
        return this.mPlayList.size();
    }

    /**
     * Since the data comes from an array, just returning the index is
     * sufficient to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
     *
     * @see android.widget.ListAdapter#getItem(int)
     */
    public Episode getItem(int position) {
        return this.mPlayList.get(position);
    }

    /**
     * Use the array index as a unique id.
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Make a view to hold each row.
     *
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
    	PlaylistRowViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) 
        {
        	//Inflate
			LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_player_list_row, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new PlaylistRowViewHolder();
            holder.episodeTitle = (TextView) convertView.findViewById(R.id.player_list_row_episodeName);
            holder.feedTitle = (TextView) convertView.findViewById(R.id.player_list_row_feedName);
            holder.timeListened = (TextView) convertView.findViewById(R.id.player_list_row_timeListened);
            holder.feedImage = (ImageView) convertView.findViewById(R.id.player_list_row_feedImage);
            holder.indicator = (View) convertView.findViewById(R.id.row_indicator);
            holder.handle = (ImageView) convertView.findViewById(R.id.player_list_row_handle);

            convertView.setTag(holder);
        } 
        else 
        {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (PlaylistRowViewHolder) convertView.getTag();
        }

        Episode currentEpisode = this.mPlayList.get(position);
        Feed currentFeed = ((MainActivity)mContext).helper.getFeed(currentEpisode.getFeedId());
        
		if(currentEpisode != null) 
		{ 	
			if (mReorderingEnabled) holder.handle.setVisibility(View.VISIBLE);
			else	holder.handle.setVisibility(View.INVISIBLE);
			holder.episodeTitle.setText(currentEpisode.getTitle());	//Set Episode Title	
			holder.feedTitle.setText(currentFeed.getTitle()); //Set Podcast title.
			int percent = (int) Math.round(((double)currentEpisode.getElapsedTime()/1000) / ((double)currentEpisode.getTotalTime()/1000) * 100.0);
			holder.timeListened.setText(percent + "% " + mContext.getString(R.string.playlist_listened));
			
			if (position == findCurrentEpisodeIndex())
			{
				holder.feedImage.setImageResource(R.drawable.dark_player_play);
				holder.feedImage.setPadding(15, 15, 15, 15);
				holder.feedImage.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
			}
			else
			{
				holder.feedImage.setImageBitmap(currentFeed.getFeedImage().thumbnail());
				holder.feedImage.setPadding(0,0,0,0);
				holder.feedImage.setBackgroundResource(R.drawable.list_image);
			}
			
			EpisodeRowUtils.setRowIndicator(mContext, holder, currentEpisode);
		}		
        return convertView;
    }
    
    public boolean isReorderingEnabled()
	{
		return mReorderingEnabled;
	}

	public void setReorderingEnabled(boolean enabled)
	{
		this.mReorderingEnabled = enabled;
		this.notifyDataSetChanged();
	}

	/**
     * Finds out whether an Episode is in the Playlist.
     * @param ep Episode to find.
     * @return True if ep is in the playlist, false otherwise.
     */
    public boolean hasEpisode(Episode ep)
    {
    	for (int i=0; i<this.mPlayList.size(); i++)
		{
			if (ep.getEpisodeId() == this.mPlayList.get(i).getEpisodeId())
			{
				return true;
			}
		}
		return false;
    }

    
    public void addToPlaylist(Episode ep)
    {
    	this.mPlayList.add(ep);
    	((MainActivity)mContext).helper.plDbH.savePlaylist(this.mPlayList);
    	this.notifyDataSetChanged();
    }
    
    
    
    public void removeFromPlaylist(Episode ep)
    {
    	int index = this.findEpisodeInPlaylist(ep);
    	if (index >= 0)
    	{
    		this.mPlayList.remove(index);
    		((MainActivity)mContext).helper.plDbH.deleteEntry(ep.getEpisodeId());
    	}
    	((MainActivity)mContext).helper.plDbH.savePlaylist(this.mPlayList);
    	this.notifyDataSetChanged();
    }
    
    /**
	 * Returns the index of ep.
	 * @param ep The Episode to find.
	 * @return Index of ep, or -1 if it isn't found.
	 */
	public int findEpisodeInPlaylist(Episode ep)
	{
		for (int i=0; i<this.mPlayList.size(); i++)
		{
			if (ep.getEpisodeId() == this.mPlayList.get(i).getEpisodeId())
			{
				return i;
			}
		}
		return -1;
	}
	
	public int findCurrentEpisodeIndex()
	{
		if (((MainActivity)mContext).podService != null)
		{
			if (((MainActivity)mContext).podService.mCurrentEpisode != null)
			{
				return this.findEpisodeInPlaylist(((MainActivity)mContext).podService.mCurrentEpisode);
			}
		}
		return -1;
	}
    
	public void onRemove(int which) 
	{	}

	public void onDrop(int from, int to) {
		Episode temp = this.mPlayList.get(from);
		this.mPlayList.remove(from);
		this.mPlayList.add(to,temp);
		((MainActivity)mContext).helper.plDbH.savePlaylist(this.mPlayList);
	}
}