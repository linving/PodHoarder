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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.ImageUtils;
import com.podhoarder.util.ViewHolders.PlaylistRowViewHolder;
import com.podhoarderproject.podhoarder.R;

import java.util.List;

public final class DragNDropAdapter extends BaseAdapter {

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
            convertView = inflater.inflate(R.layout.drawer_quicklist_playlist_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new PlaylistRowViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.handle = (ImageView) convertView.findViewById(R.id.handle);

            convertView.setTag(holder);
        } 
        else 
        {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (PlaylistRowViewHolder) convertView.getTag();
        }

        Episode currentEpisode = this.mPlayList.get(position);
        Feed currentFeed = ((BaseActivity)mContext).mDataManager.getFeed(currentEpisode.getFeedId());
        
		if(currentEpisode != null) 
		{ 	
			if (mReorderingEnabled) 
				holder.handle.setVisibility(View.VISIBLE);
			else	
				holder.handle.setVisibility(View.INVISIBLE);
			 
			holder.title.setText(currentEpisode.getTitle());	//Set Episode Title
			
			if (position == findCurrentEpisodeIndex())
			{
				holder.icon.setImageResource(R.drawable.ic_play_arrow_black_24dp);
			}
			else
			{
                holder.icon.setImageBitmap(ImageUtils.getCircularBitmapWithBorder(currentFeed.getFeedImage().thumbnail(), 1f));
			}

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

	
	public int findCurrentEpisodeIndex()
	{

		return -1;
	}
	
	public void move(int from, int to)
	{
		Episode temp = this.mPlayList.get(from);
		this.mPlayList.remove(from);
		this.mPlayList.add(to,temp);
		this.notifyDataSetChanged();
	}
}