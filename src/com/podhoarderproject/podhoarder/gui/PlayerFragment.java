package com.podhoarderproject.podhoarder.gui;

import com.podhoarderproject.ericharlow.DragNDrop.DragListener;
import com.podhoarderproject.ericharlow.DragNDrop.DragNDropAdapter;
import com.podhoarderproject.ericharlow.DragNDrop.DragNDropListView;
import com.podhoarderproject.ericharlow.DragNDrop.DropListener;
import com.podhoarderproject.ericharlow.DragNDrop.RemoveListener;
import com.podhoarderproject.podhoarder.PodcastHelper;
import com.podhoarderproject.podhoarder.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class PlayerFragment extends Fragment
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PlayFragment";
	
	public ListView mainListView;
	
	private View view;
	private PodcastHelper helper;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_player, container, false);
    	
    	setupHelper();
    	setupListView();
    	
    	
    	
    	return this.view;
    }
    
    private void setupListView()
    {
    	this.mainListView = (ListView) view.findViewById(R.id.playerList);
    	if (!this.helper.playlistAdapter.isEmpty())
    	{
    		this.mainListView.setAdapter(this.helper.playlistAdapter);
    		((DragNDropListView) this.mainListView).setDropListener(mDropListener);
        	((DragNDropListView) this.mainListView).setRemoveListener(mRemoveListener);
        	((DragNDropListView) this.mainListView).setDragListener(mDragListener);
    	}
    	else
    	{
    		//TODO: Show some kind of "list is empty" text instead of the mainlistview here.
    	}
    }
    
    private DropListener mDropListener = 
    		new DropListener() {
            public void onDrop(int from, int to) {
            	ListAdapter adapter = mainListView.getAdapter();
            	if (adapter instanceof DragNDropAdapter) {
            		((DragNDropAdapter)adapter).onDrop(from, to);
            		mainListView.invalidateViews();
            	}
            }
        };
        
        private RemoveListener mRemoveListener =
            new RemoveListener() {
            public void onRemove(int which) {
            	ListAdapter adapter = mainListView.getAdapter();
            	if (adapter instanceof DragNDropAdapter) {
            		((DragNDropAdapter)adapter).onRemove(which);
            		mainListView.invalidateViews();
            	}
            }
        };
        
        private DragListener mDragListener =
        	new DragListener() {

        	int backgroundColor = 0xe0103010;
        	int defaultBackgroundColor;
        	
    			public void onDrag(int x, int y, ListView listView) {
    				// TODO Auto-generated method stub
    			}

    			public void onStartDrag(View itemView) {
    				itemView.setVisibility(View.INVISIBLE);
    				defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
    				itemView.setBackgroundColor(backgroundColor);
    				ImageView iv = (ImageView)itemView.findViewById(R.id.player_list_row_grabber);
    				if (iv != null) iv.setVisibility(View.INVISIBLE);
    			}

    			public void onStopDrag(View itemView) {
    				itemView.setVisibility(View.VISIBLE);
    				itemView.setBackgroundColor(defaultBackgroundColor);
    				ImageView iv = (ImageView)itemView.findViewById(R.id.player_list_row_grabber);
    				if (iv != null) iv.setVisibility(View.VISIBLE);
    			}
        	
        };
    
    private void setupHelper()
    {
    	this.helper = ((com.podhoarderproject.podhoarder.MainActivity)this.getActivity()).helper;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
    
    
}
