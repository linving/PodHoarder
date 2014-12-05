/**
 * @author Emil Almrot
 * 2013-03-20
 */
package com.podhoarder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.ImageUtils;
import com.podhoarderproject.podhoarder.R;

import java.util.List;

public class QuickListAdapter extends BaseAdapter implements ListAdapter
{
    @SuppressWarnings("unused")
    private static final String LOG_TAG = "com.podhoarderproject.podhoarder.QuicklistAdapter";
    public 	List<Episode> mEpisodes;
    protected Context mContext;

    /**
     * Creates a LatestQuicklistAdapter (Constructor).
     *
     * @param episodes
     *            A List<Episode> containing the episodes to display.
     * @param context
     *            A Context object from the parent Activity.
     *
     */
    public QuickListAdapter(List<Episode> episodes, Context context)
    {
        this.mEpisodes = episodes;
        this.mContext = context;
    }

    /**
     * Replaces the item collection behind the adapter to force an update.
     * @param newItemCollection The new collection.
     */
    public void replaceItems(List<Episode> newItemCollection)
    {
        this.mEpisodes = newItemCollection;
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
        QuickListRowViewHolder viewHolder;

        if (convertView == null)
        {
            //Inflate
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_quicklist_item, null);

            // Set up the ViewHolder
            viewHolder = new QuickListRowViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            // Store the holder with the view.
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (QuickListRowViewHolder) convertView.getTag();
        }


        final Episode currentEpisode = this.mEpisodes.get(position);
        Feed currentFeed = ((BaseActivity)mContext).mDataManager.getFeed(currentEpisode.getFeedId());

        if(currentEpisode != null) {
            //Set Episode Title
            viewHolder.title.setText(currentEpisode.getTitle());
            //Set Episode Icon
            viewHolder.icon.setImageBitmap(ImageUtils.getCircularBitmapWithBorder(currentFeed.getFeedImage().thumbnail(), 1f));
        }

        return convertView;
    }

    public static class QuickListRowViewHolder
    {
        public TextView title;
        public ImageView icon;
    }

}
