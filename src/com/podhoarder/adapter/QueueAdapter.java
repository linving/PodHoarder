package com.podhoarder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.podhoarder.object.Episode;
import com.podhoarder.util.ViewHolders;
import com.podhoarderproject.podhoarder.R;

import java.util.LinkedList;

/**
 * Created by Emil on 2014-11-12.
 */
public class QueueAdapter extends BaseAdapter implements ListAdapter {

    private Context mContext;
    private int layoutResourceId;
    private LinkedList<Episode> data;
    private OnItemSecondaryActionClickedListener mOnItemSecondaryActionClickedListener;

    public QueueAdapter(LinkedList<Episode> data, int layoutResourceId, Context context) {
        this.layoutResourceId = layoutResourceId;
        this.mContext = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getEpisodeId();
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
            holder.secondaryAction = (ImageView) convertView.findViewById(R.id.list_episode_row_secondary_action);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolders.EpisodeRowViewHolder) convertView.getTag();
        }

        Episode currentEpisode = data.get(position);

        if(currentEpisode != null) {
            holder.secondaryAction.setImageResource(R.drawable.ic_close_black_24dp);
            final int pos = position;
            holder.secondaryAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemSecondaryActionClickedListener != null)
                        mOnItemSecondaryActionClickedListener.onItemSecondaryActionClicked(v,pos);
                }
            });
            holder.title.setText(currentEpisode.getTitle());	//Set Episode Title
            holder.subtitle.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void replaceItems(LinkedList<Episode> newCollection) {
        data.clear();
        data = newCollection;
        notifyDataSetChanged();
    }

    public interface OnItemSecondaryActionClickedListener {
        public void onItemSecondaryActionClicked(View v, int pos);
    }

    public OnItemSecondaryActionClickedListener getOnItemSecondaryActionClickedListener() {
        return mOnItemSecondaryActionClickedListener;
    }

    public void setOnItemSecondaryActionClickListener(OnItemSecondaryActionClickedListener listener) {
        mOnItemSecondaryActionClickedListener = listener;
    }
}
