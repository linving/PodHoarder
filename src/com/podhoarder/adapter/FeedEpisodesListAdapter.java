/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarder.adapter;

import android.content.Context;
import android.widget.ListAdapter;

import com.podhoarder.object.Feed;

public class FeedEpisodesListAdapter extends EpisodesListAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.FeedDetailsListAdapter";
	public 	Feed mFeed;

	/**
	 * Creates a FeedDetailsListAdapter (Constructor).
	 * 
	 * @param context
	 *            A Context object from the parent Activity.
	 *            
	 */
	public FeedEpisodesListAdapter(Context context)
	{
		super(null, context);
	}
	
	public void setFeed(Feed feedToSet)
	{
		this.mFeed = feedToSet;
		this.mEpisodes = this.mFeed.getEpisodes();
		this.notifyDataSetChanged();
	}
}
