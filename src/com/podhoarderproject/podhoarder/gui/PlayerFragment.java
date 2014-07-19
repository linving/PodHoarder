package com.podhoarderproject.podhoarder.gui;

import com.podhoarderproject.ericharlow.DragNDrop.DragListener;
import com.podhoarderproject.ericharlow.DragNDrop.DragNDropListView;
import com.podhoarderproject.ericharlow.DragNDrop.DropListener;
import com.podhoarderproject.ericharlow.DragNDrop.RemoveListener;
import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.adapter.DragNDropAdapter;
import com.podhoarderproject.podhoarder.service.PodHoarderService;
import com.podhoarderproject.podhoarder.util.PodcastHelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * 
 * @author Emil Almrot
 * 2014-05-25
 */
public class PlayerFragment extends Fragment
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PlayFragment";
	
	public ListView mainListView;
	
	private View view;
	private PodcastHelper helper;
	
	private PodHoarderService podService;
	
	//UI Elements
	public ToggleButton playPauseButton;
	public ToggleButton doubleSpeedButton;
	public ImageButton forwardButton;
	public ImageButton backwardButton;
	public TextView episodeTitle;
	public TextView elapsedTime;
	public TextView totalTime;
	public SeekBar seekBar;
	public ProgressBar elapsedTimeBar;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_player, container, false);
    	
    	setupHelper();
    	setupListView();
    	setupMediaControls();
    	
    	if (this.podService != null && this.podService.isPng())	playPauseButton.setChecked(false);
    	return this.view;
    }
    
    private void setServiceVars()
    {
    	this.podService = ((com.podhoarderproject.podhoarder.activity.MainActivity)this.getActivity()).getPodService();
    }
    
    private void setupMediaControls()
    {
    	this.playPauseButton = (ToggleButton)view.findViewById(R.id.player_controls_button_playpause);
    	this.playPauseButton.setOnClickListener(mPlayPauseClickListener);
    	    	
    	this.forwardButton = (ImageButton)view.findViewById(R.id.player_controls_button_skip_forward);
    	this.forwardButton.setOnClickListener(mForwardClickListener);
    	
    	this.backwardButton = (ImageButton)view.findViewById(R.id.player_controls_button_skip_backwards);
    	this.backwardButton.setOnClickListener(mBackwardClickListener);
    	
    	this.episodeTitle = (TextView)view.findViewById(R.id.player_controls_episode_title);
    	
    	this.elapsedTime = (TextView)view.findViewById(R.id.player_controls_elapsed_time);
    	    	
    	this.totalTime = (TextView)view.findViewById(R.id.player_controls_total_time);
    	
    	this.seekBar = (SeekBar)view.findViewById(R.id.player_controls_seekbar);
    	this.seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
    }
    
	@Override
	public void onStart()
	{
		super.onStart();
		setServiceVars();
		if (this.podService != null)
		{
			this.podService.setUIElements(playPauseButton, episodeTitle, elapsedTime, totalTime, seekBar, helper);
		}
		if (this.podService != null && this.podService.currentEpisode != null) this.podService.updateUI();
		else if (this.podService != null && this.podService.currentEpisode == null) this.podService.resetUI();
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
       
    private void setupHelper()
    {
    	this.helper = ((com.podhoarderproject.podhoarder.activity.MainActivity)this.getActivity()).helper;
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

	public void pause()
	{
		this.podService.pause();
	}
	
	public void resume()
	{
		this.podService.resume();
	}

	public void seekTo(int pos)
	{
		this.podService.seek(pos);
	}

	public void start(int epPos)
	{
		//Toggle the play button to a pause button, since the track has started.
		this.playPauseButton.setChecked(true);
		//The service should start the episode.
		this.podService.startEpisode(epPos);
	}
	
	//UI Logic
	//List Listeners
	private DropListener mDropListener = new DropListener() 
    {
        public void onDrop(int from, int to) 
        {
        	ListAdapter adapter = mainListView.getAdapter();
        	if (adapter instanceof DragNDropAdapter) 
        	{
        		podService.setEpisode(to);
        		((DragNDropAdapter)adapter).onDrop(from, to);
        		mainListView.invalidateViews();
        	}
        }
    };
        
    private RemoveListener mRemoveListener = new RemoveListener() 
    {
        public void onRemove(int which) 
        {
        	ListAdapter adapter = mainListView.getAdapter();
        	if (adapter instanceof DragNDropAdapter) 
        	{
        		((DragNDropAdapter)adapter).onRemove(which);
        		mainListView.invalidateViews();
        	}
        }
    };
        
    private DragListener mDragListener = new DragListener() 
    {
    	int defaultBackgroundColor;
    	
		public void onDrag(int x, int y, ListView listView) 
		{
			// TODO Auto-generated method stub
		}

		public void onStartDrag(View itemView) 
		{
			itemView.setVisibility(View.INVISIBLE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(getResources().getColor(R.color.fragment_player_playerlist_row_background_ondrag));
			ImageView iv = (ImageView)itemView.findViewById(R.id.player_list_row_grabber);
			if (iv != null) iv.setVisibility(View.INVISIBLE);
		}

		public void onStopDrag(View itemView) 
		{
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);
			ImageView iv = (ImageView)itemView.findViewById(R.id.player_list_row_grabber);
			if (iv != null) iv.setVisibility(View.VISIBLE);
		}
    };
    
    private OnItemClickListener mOnClickListener = new OnItemClickListener()
    {
		@Override
		public void onItemClick(AdapterView<?> arg0, View listRow, int position, long id)
		{
			start(position);
		}
    };
    
    //Button Listeners
    private OnClickListener mPlayPauseClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if(((ToggleButton)view).isChecked())
            {
                //Button is ON
                // Do Something 
            	if (podService.getPlaylistSize() > 0)	//First we have to make sure that there are any episodes in the list.
            	{
            		if (podService.currentEpisode == null)	//If there is no current episode assigned, we do it manually and set it to the first item in the playlist.
                	{
                		podService.startEpisode(0);
                	}
                	else resume();
            	}
            }
            else
            {
            	//Button is OFF
                // Do Something
            	if (podService.currentEpisode != null)
            	{
            		pause();	
            	}
            }
            
        }
    };

    private OnClickListener mForwardClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
        	if (podService.isPng())
        	{
        		podService.skipForward();
        	}
        }
    };
       
    private OnClickListener mBackwardClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
        	if (podService.isPng())
        	{
        		podService.skipBackward();
        	}
        }
    };

    //Seekbar Listener
    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {    

        @Override       
        public void onStopTrackingTouch(SeekBar seekBar) {
        	//Let the service go back to updating the seekBar position.
        	podService.setUpdateBlocked(false);
        }       

        @Override       
        public void onStartTrackingTouch(SeekBar seekBar) {  
        	//This prevents the PodService tasks to update and set the seekBar position while the user has "grabbed" it.
            podService.setUpdateBlocked(true);    
        }       

        @Override       
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
        	//fromUser makes sure that it's user input that triggers the seekBar position change.
        	if (fromUser)
        	{
        		podService.seek(progress);
        	}
        }       
    };           
}
