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
        QueueRowViewHolder holder;

        if (convertView == null) {
            //Inflate
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceId, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new QueueRowViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
            holder.secondaryAction = (ImageView) convertView.findViewById(R.id.secondary_action);

            convertView.setTag(holder);
        } else {
            holder = (QueueRowViewHolder) convertView.getTag();
        }

        Episode currentEpisode = data.get(position);
        Feed currentFeed = ((BaseActivity)mContext).mDataManager.getFeed(currentEpisode.getFeedId());

        if(currentEpisode != null) {
            //Set Episode Title
            holder.title.setText(currentEpisode.getTitle());
            //Set subtitle
            holder.subtitle.setText(currentFeed.getTitle());
            //Set secondary action icon
            holder.secondaryAction.setImageResource(R.drawable.ic_close_black_24dp);
            final int pos = position;
            holder.secondaryAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemSecondaryActionClickedListener != null)
                        mOnItemSecondaryActionClickedListener.onItemSecondaryActionClicked(v,pos);
                }
            });

        }

        return convertView;
    }

    public void replaceItems(LinkedList<Episode> newCollection) {
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

    public static class QueueRowViewHolder
    {
        public TextView title, subtitle;
        public ImageView secondaryAction;
    }
}
