package com.podhoarder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.podhoarder.object.Episode;
import com.podhoarder.util.EpisodeRowUtils;
import com.podhoarder.util.ViewHolders;
import com.podhoarderproject.podhoarder.R;

import java.util.LinkedList;

/**
 * Created by Emil on 2014-11-12.
 */
public class QueueAdapter extends ArrayAdapter<Episode> {

    Context mContext;
    int layoutResourceId;
    LinkedList<Episode> data;

    public QueueAdapter(LinkedList<Episode> data, int layoutResourceId, Context context) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolders.EpisodeRowViewHolder holder;

        if (convertView == null) {
            //Inflate
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceId, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolders.EpisodeRowViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.list_episode_row_title);
            holder.subtitle = (TextView) convertView.findViewById(R.id.list_episode_row_subtitle);
            holder.icon = (ImageView) convertView.findViewById(R.id.list_episode_row_icon);
            holder.secondaryAction = (ImageView) convertView.findViewById(R.id.list_episode_row_secondary_action);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolders.EpisodeRowViewHolder) convertView.getTag();
        }

        Episode currentEpisode = data.get(position);

        if(currentEpisode != null) {
            holder.secondaryAction.setVisibility(View.INVISIBLE);

            holder.title.setText(currentEpisode.getTitle());	//Set Episode Title

            EpisodeRowUtils.setRowIndicator(this.mContext, holder, currentEpisode);
        }

        return convertView;
    }

    public void replaceItems(LinkedList<Episode> newCollection) {
        data.clear();
        data = newCollection;
        notifyDataSetChanged();
    }
}
