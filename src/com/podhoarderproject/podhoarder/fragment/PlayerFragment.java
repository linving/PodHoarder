package com.podhoarderproject.podhoarder.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.podhoarderproject.ericharlow.DragNDrop.DragListener;
import com.podhoarderproject.ericharlow.DragNDrop.DragNDropListView;
import com.podhoarderproject.ericharlow.DragNDrop.DropListener;
import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.adapter.DragNDropAdapter;
import com.podhoarderproject.podhoarder.service.PodHoarderService;
import com.podhoarderproject.podhoarder.util.Episode;
import com.podhoarderproject.podhoarder.util.PodcastHelper;
import com.podhoarderproject.podhoarder.util.PopupMenuUtils;

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
	public ProgressBar	loadingCircle;
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
    	
    	this.loadingCircle = (ProgressBar)view.findViewById(R.id.player_controls_loading_circle);
    	    	
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
			this.podService.setUIElements(playPauseButton, loadingCircle, episodeTitle, elapsedTime, totalTime, seekBar, helper);
		}
		if (this.podService != null && this.podService.currentEpisode != null) this.podService.updateUI();
		else if (this.podService != null && this.podService.currentEpisode == null) this.podService.resetUI();
	}
    
    private void setupListView()
    {
    	this.mainListView = (ListView) view.findViewById(R.id.playlist);
    	if (!this.helper.playlistAdapter.isEmpty())
    	{
    		this.mainListView.setAdapter(this.helper.playlistAdapter);
    		((DragNDropListView) this.mainListView).setDropListener(mDropListener);
        	((DragNDropListView) this.mainListView).setDragListener(mDragListener);
        	((DragNDropListView) this.mainListView).setOnItemClickListener(mOnClickListener);
        	((DragNDropListView) this.mainListView).setOnItemLongClickListener(mOnLongClickListener);
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
		this.podService.playEpisode(epPos);
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
        		//podService.setEpisode(to);
        		((DragNDropAdapter)adapter).onDrop(from, to);
        		mainListView.invalidateViews();
        	}
        }
    };
        
    private DragListener mDragListener = new DragListener() 
    {
    	int index = -1, originalCount = -1;
    	
		public void onDrag(int x, int y, ListView listView) 
		{
			if (listView.getChildCount() < originalCount)												//This means the grabbed row has been removed, and the list now contains 1 row less.
			{
				if (index != listView.pointToPosition(x, y) && listView.pointToPosition(x, y) != -1)	//Only animate if the view is dragged onto a new index in the list.
				{
					resetAnimations();																	//Reset all the rows to their initial locations. (before they were animated)
					
					for (int i = listView.pointToPosition(x, y); i<listView.getChildCount(); i++)		//Iterate through all rows from the one we're hovering to the end.
					{
						try
						{
							slideAnimation(listView.getChildAt(i), R.anim.slide_out_bottom);			//Animate them moving down one row (we don't actually move any objects/indexes, just pixels)
						}
						catch (NullPointerException e)
						{
							Log.e(LOG_TAG, "NullPointerException at index: " + i);
							e.printStackTrace();
						}
					}
				}
				else if (index != listView.pointToPosition(x, y) && listView.pointToPosition(x, y) == -1)//Reset all animations if we are outside the list.
				{
					resetAnimations();
				}
			}
			index = listView.pointToPosition(x, y);														//Store the index of where we're currently hovering.
		}

		public void onStartDrag(View itemView) 
		{
			originalCount = mainListView.getChildCount();
		}

		public void onStopDrag(View itemView) 
		{
			resetAnimations();		//Reset all the rows to their initial locations. (before they were animated)
		}
		
		/**
		 * Does an animation on the selected View object. Animations will persist after they are completed, so you have to manually clear animations afterwards if you want to reset their position.
		 * @param viewToSlide	View to slide.
		 * @param anim	Animation resource identifier.
		 */
		private void slideAnimation(View viewToSlide, int anim)
	    {
	    	Animation animation = AnimationUtils.loadAnimation(getActivity(), anim);
	    	animation.setDuration(100);
	    	animation.setFillEnabled(true);
	    	animation.setFillAfter(true);
	    	viewToSlide.startAnimation(animation);
	    }
		
		/**
		 * Goes through all the rows in the list and resets/clears their animations.
		 */
		private void resetAnimations()
		{
			for (int i=0; i<mainListView.getChildCount(); i++)	//Go through all the rows in the list.	
			{
				mainListView.getChildAt(i).clearAnimation();	//Cancel any animation (reset their location in this case)
			}
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
    
    private OnItemLongClickListener mOnLongClickListener = new OnItemLongClickListener()
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View listRow, int position, long id)
		{
			final Episode currentEp = (Episode)mainListView.getItemAtPosition(position);
			PopupMenuUtils.buildPlaylistContextMenu(getActivity(), listRow, currentEp, true).show();
			return true;
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
                		podService.playEpisode(0);
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
