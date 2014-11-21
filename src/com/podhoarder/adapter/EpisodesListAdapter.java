/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarder.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.podhoarder.object.Episode;
import com.podhoarder.util.DataParser;
import com.podhoarder.util.EpisodeRowUtils;
import com.podhoarder.view.PieProgressDrawable;
import com.podhoarderproject.podhoarder.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class EpisodesListAdapter extends BaseAdapter implements ListAdapter
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.EpisodesListAdapter";
	public 	List<Episode> mEpisodes;
	protected Context mContext;
	private boolean mSelectionEnabled;

	/**
	 * Creates a LatestEpisodesListAdapter (Constructor).
	 * 
	 * @param episodes
	 *            A List<Episode> containing the episodes you want to display.
	 * @param context
	 *            A Context object from the parent Activity.
	 *            
	 */
	public EpisodesListAdapter(List<Episode> episodes, Context context)
	{
        this.mEpisodes = new ArrayList<Episode>();
		this.mEpisodes.addAll(episodes);
		this.mContext = context;
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<Episode> newItemCollection)
	{
        this.mEpisodes.clear();
        this.mEpisodes.addAll(newItemCollection);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return this.mEpisodes.size();
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
		final EpisodeRowViewHolder viewHolder;
		
		if (convertView == null)
		{
			//Inflate
			LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.episode_list_row, null);
			
			// Set up the ViewHolder
	        viewHolder = new EpisodeRowViewHolder();
	        viewHolder.title = (TextView) convertView.findViewById(R.id.list_episode_row_title);
	        viewHolder.subtitle = (TextView) convertView.findViewById(R.id.list_episode_row_subtitle);
	        viewHolder.icon = (ImageView) convertView.findViewById(R.id.list_episode_row_icon);
	        viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.list_episode_row_checkbox);
	        viewHolder.secondaryAction = (ImageView) convertView.findViewById(R.id.list_episode_row_secondary_action);
	        // Store the holder with the view.
	        convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (EpisodeRowViewHolder) convertView.getTag();
		}
		
		final Episode currentEpisode = this.mEpisodes.get(position);
		
		if(currentEpisode != null) {
			//Set Episode Title
			viewHolder.title.setText(currentEpisode.getTitle());	
			//Set Episode Timestamp.
			try
			{
					viewHolder.subtitle.setText(DateUtils.getRelativeTimeSpanString(
									DataParser.correctFormat.parse(
											currentEpisode.getPubDate()).getTime()));	//Set a time stamp since Episode publication.
			} 
			catch (ParseException e)
			{
				e.printStackTrace();
			}
			
			viewHolder.secondaryAction.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					PopupMenu menu = EpisodeRowUtils.getContextMenu(mContext,viewHolder.secondaryAction,currentEpisode);
                    menu.show();
				}
			});
			
			if (mSelectionEnabled)
			{
				viewHolder.secondaryAction.setVisibility(View.GONE);
                viewHolder.checkbox.setVisibility(View.VISIBLE);
			}
			else
			{
				viewHolder.checkbox.setVisibility(View.GONE);
				viewHolder.secondaryAction.setVisibility(View.VISIBLE);
			}

            viewHolder.checkbox.setChecked(false);
			
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

    public static class EpisodeRowViewHolder
    {
        public TextView title;
        public TextView subtitle;
        public ImageView secondaryAction;
        public ImageView icon;
        public PieProgressDrawable progressDrawable;
        public CheckBox checkbox;
    }

}
