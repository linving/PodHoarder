/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarderproject.podhoarder;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
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

	@Override
	public Object getChild(int groupPos, int childPos)
	{
		return this.feeds.get(groupPos).getEpisodes().get(childPos);
	}

	@Override
	public long getChildId(int groupPos, int childPos)
	{
		return this.feeds.get(groupPos).getEpisodes().get(childPos)
				.getEpisodeId();
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
	public long getGroupId(int groupPos)
	{
		return this.feeds.get(groupPos).getFeedId();
	}

	@Override
	public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// TODO: Finish this.
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_feed_row, null);
		}

		TextView tvFeedName = (TextView) convertView.findViewById(R.id.feedName);
		tvFeedName.setText(this.feeds.get(groupPos).getTitle());
		return convertView;
	}

	@Override
	public View getChildView(int groupPos, int childPos, boolean isLastChild, View convertView, ViewGroup parent)
	{
		// TODO: Finish this.
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_episode_row, null);
		}

		TextView tvFeedName = (TextView) convertView.findViewById(R.id.episodeName);
		tvFeedName.setText(this.feeds.get(groupPos).getEpisodes().get(childPos).getTitle());

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
}
