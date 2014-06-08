package com.podhoarderproject.podhoarder.gui;

import android.widget.MediaController.MediaPlayerControl;

import com.podhoarderproject.ericharlow.DragNDrop.DragListener;
import com.podhoarderproject.ericharlow.DragNDrop.DragNDropAdapter;
import com.podhoarderproject.ericharlow.DragNDrop.DragNDropListView;
import com.podhoarderproject.ericharlow.DragNDrop.DropListener;
import com.podhoarderproject.ericharlow.DragNDrop.RemoveListener;
import com.podhoarderproject.podhoarder.Episode;
import com.podhoarderproject.podhoarder.PlaybackController;
import com.podhoarderproject.podhoarder.PodcastHelper;
import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.service.PodHoarderService;
import com.podhoarderproject.podhoarder.service.PodHoarderService.PodHoarderBinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * 
 * @author Emil Almrot
 * 2014-05-25
 */
public class PlayerFragment extends Fragment implements MediaPlayerControl
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PlayFragment";
	
	public ListView mainListView;
	
	private View view;
	private PodcastHelper helper;
	
	private PodHoarderService podService;
	private boolean musicBound = false;
	
	private PlaybackController controller;
	
	private boolean paused=false, playbackPaused=false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_player, container, false);
    	
    	setupHelper();
    	setupListView();
    	//setupMediaControls();
    	setController();
    	setServiceVars();
    	return this.view;
    }
    
    private void setServiceVars()
    {
    	this.podService = ((com.podhoarderproject.podhoarder.MainActivity)this.getActivity()).getPodService();
    	this.musicBound = ((com.podhoarderproject.podhoarder.MainActivity)this.getActivity()).isMusicBound();
    }
    
	@Override
	public void onStart()
	{
		super.onStart();
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
        	((DragNDropListView) this.mainListView).setOnItemClickListener(mOnClickListener);
        	
    	}
    	else
    	{
    		//TODO: Show some kind of "list is empty" text instead of the mainlistview here.
    	}
    }
   
    
    private OnItemClickListener mOnClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long id)
		{
			
			if(playbackPaused)
			{
			    setController();
			    playbackPaused=false;
			}
			podService.setEpisode(position);
			podService.startEpisode();
			controller.show(0);
			
		}
    	
    };
    
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
    
    @Override
	public void onDestroy() 
    {
	    super.onDestroy();
    }
    
    private void setController()
    {
    	if (this.controller == null)
    	{
    		this.controller = new PlaybackController(getActivity());
        	this.controller.setPrevNextListeners(new View.OnClickListener() {
        		  @Override
        		  public void onClick(View v) {
        		    playNext();
        		  }
        		}, new View.OnClickListener() {
        		  @Override
        		  public void onClick(View v) {
        		    playPrev();
        		  }
        		});
        	this.controller.setMediaPlayer(this);
        	this.controller.setAnchorView(view.findViewById(R.id.player_controls_container));
        	this.controller.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        	this.controller.setEnabled(true);
    	}
    }
    
    //MediaController interface methods.
	@Override
	public boolean canPause()
	{
		// Pausing playback is enabled.
		return true;
	}

	@Override
	public boolean canSeekBackward()
	{
		// Seeking should be enabled.
		return true;
	}

	@Override
	public boolean canSeekForward()
	{
		// Seeking should be enabled.
		return true;
	}

	@Override
	public int getAudioSessionId()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBufferPercentage()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrentPosition()
	{
		if(this.podService != null && musicBound && this.podService.isPng())
			return this.podService.getPosn();
		else return 0;
	}

	@Override
	public int getDuration()
	{
		if(this.podService != null && musicBound && this.podService.isPng())
			return this.podService.getDur();
		else return 0;
	}

	@Override
	public boolean isPlaying()
	{
		if(this.podService != null && musicBound)
		    return this.podService.isPng();
		return false;
	}

	@Override
	public void pause()
	{
		this.playbackPaused=true;
		this.podService.pausePlayer();
	}

	@Override
	public void seekTo(int pos)
	{
		this.podService.seek(pos);
	}

	@Override
	public void start()
	{
		this.podService.go();
	}
	
	//play next
	private void playNext()
	{
		this.podService.playNext();
		if(this.playbackPaused)
		{
		    setController();
		    this.playbackPaused=false;
		}
		this.controller.show(0);
	}
	 
	//play previous
	private void playPrev()
	{
		this.podService.playPrev();
		if(this.playbackPaused)
		{
		    setController();
		    this.playbackPaused=false;
		}
		this.controller.show(0);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		this.paused=true;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if(this.paused)
		{
			setController();
			this.paused=false;
		}
	}
	
	@Override
	public void onStop() {
		this.controller.hide();
		super.onStop();
	}
    
}
