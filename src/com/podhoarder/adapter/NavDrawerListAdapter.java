package com.podhoarder.adapter;

/**
 * Created by Emil on 2014-10-26.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.podhoarder.object.NavDrawerItem;
import com.podhoarder.view.CheckableImageView;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;

public class NavDrawerListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_navigation_item, null);
        }

        CheckableImageView imgIcon = (CheckableImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
        txtTitle.setText(navDrawerItems.get(position).getTitle());

        imgIcon.setChecked(convertView.isSelected());
        if (convertView.isSelected())
            txtTitle.setTextColor(context.getResources().getColor(R.color.colorFavorite));
        else
            txtTitle.setTextColor(context.getResources().getColor(R.color.colorNotFavorite));

        return convertView;
    }

    public void setSelectedItem(int pos) {

    }
}