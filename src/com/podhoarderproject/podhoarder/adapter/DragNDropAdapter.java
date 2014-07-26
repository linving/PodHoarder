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

package com.podhoarderproject.podhoarder.adapter;

import java.util.List;

import com.podhoarderproject.ericharlow.DragNDrop.DropListener;
import com.podhoarderproject.ericharlow.DragNDrop.RemoveListener;
import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.activity.MainActivity;
import com.podhoarderproject.podhoarder.util.Episode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public final class DragNDropAdapter extends BaseAdapter implements RemoveListener, DropListener{

	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.DragNDropAdapter";
	
	public 	List<Episode> playList;
	private Context context;

    
    public DragNDropAdapter(List<Episode> playList, Context context) {
    	this.context = context;
    	this.playList = playList;
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
	
    /**
     * The number of items in the list
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
        return this.playList.size();
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
        return this.playList.get(position);
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
        ViewHolderItem holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) 
        {
        	//Inflate
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_player_list_row, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolderItem();
            holder.episodeTitle = (TextView) convertView.findViewById(R.id.player_list_row_episodeName);
            holder.podcastTitle = (TextView) convertView.findViewById(R.id.player_list_row_podcastName);
            holder.elapsedTimeBar = (ProgressBar) convertView.findViewById(R.id.player_list_row_elapsed_progressBar);

            convertView.setTag(holder);
        } 
        else 
        {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolderItem) convertView.getTag();
        }

        Episode currentEpisode = this.playList.get(position);
        
		if(currentEpisode != null) 
		{ 	
			holder.episodeTitle.setText(currentEpisode.getTitle());	//Set Episode Title	
			holder.podcastTitle.setText(((MainActivity)context).helper.getFeed(currentEpisode.getFeedId()).getTitle()); //Set Podcast title.
			
			//Set up Circular progress bar that shows the elapsed time of each episode.
			holder.elapsedTimeBar.setMax(currentEpisode.getTotalTime());	//Set the max value (total runtime of each episode in milliseconds)
			holder.elapsedTimeBar.setProgress(currentEpisode.getElapsedTime());	//Set the elapsed time of each episode (in milliseconds)
		}		
        return convertView;
    }

    //This is to improve ListView performance. See link for details.
  	//http://developer.android.com/training/improving-layouts/smooth-scrolling.html
  	static class ViewHolderItem {	
  	    TextView episodeTitle;
  	    TextView podcastTitle;
  	    ProgressBar elapsedTimeBar;
  	}

	public void onRemove(int which) {
//		if (which < 0 || which > this.playList.size()) return;		
//		this.playList.remove(which);
	}

	public void onDrop(int from, int to) {
		Episode temp = this.playList.get(from);
		this.playList.remove(from);
		this.playList.add(to,temp);
		((MainActivity)context).helper.plDbH.savePlaylist(this.playList);
	}
}