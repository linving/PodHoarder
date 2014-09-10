package com.podhoarder.fragment;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;

import com.ericharlow.DragNDrop.DragListener;
import com.ericharlow.DragNDrop.DragNDropListView;
import com.ericharlow.DragNDrop.DropListener;
import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.DragNDropAdapter;
import com.podhoarder.component.CircularSeekBar;
import com.podhoarder.component.CircularSeekBar.OnSeekChangeListener;
import com.podhoarder.component.ToggleImageButton;
import com.podhoarder.object.PlaylistMultiChoiceModeListener;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.util.PodcastHelper;
import com.podhoarderproject.podhoarder.R;

/**
 * 
 * @author Emil Almrot
 * 2014-05-25
 */
@SuppressWarnings("deprecation")
public class PlayerFragment extends Fragment
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PlayerFragment";
	
	public DragNDropListView mainListView;
	@SuppressWarnings("deprecation")
	public SlidingDrawer mPlaylistDrawer;
	
	private View view;
	private PodcastHelper helper;
	
	private PlaylistMultiChoiceModeListener mListSelectionListener;
	
	private PodHoarderService podService;
	
	//UI Elements
	public ToggleImageButton playPauseButton;
	public ProgressBar	loadingCircle;
	public ImageButton forwardButton;
	public ImageButton backwardButton;
	public TextView episodeTitle;
	public TextView elapsedTime;
	public TextView totalTime;
	public CircularSeekBar seekBar;
	public ProgressBar elapsedTimeBar;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_player, container, false);
    	
    	setupHelper();
    	setupListView();
    	setupMediaControls();
    	
    	if (this.podService != null && this.podService.isPlaying())	
			this.playPauseButton.setChecked(false);
    	return this.view;
    }
    
    private void setServiceVars()
    {
    	this.podService = ((com.podhoarder.activity.MainActivity)this.getActivity()).getPodService();
    }
    
    public void setService(PodHoarderService service)
    {
    	this.podService = service;
    }
    
    private void setupMediaControls()
    {
    	this.playPauseButton = (ToggleImageButton)view.findViewById(R.id.player_controls_button_playpause);
    	this.playPauseButton.setOnClickListener(mPlayPauseClickListener);
    	
    	this.loadingCircle = (ProgressBar)view.findViewById(R.id.player_controls_loading_circle);
    	    	
    	this.forwardButton = (ImageButton)view.findViewById(R.id.player_controls_button_skip_forward);
    	this.forwardButton.setOnClickListener(mForwardClickListener);
    	
    	this.backwardButton = (ImageButton)view.findViewById(R.id.player_controls_button_skip_backwards);
    	this.backwardButton.setOnClickListener(mBackwardClickListener);
    	
    	this.episodeTitle = (TextView)view.findViewById(R.id.player_controls_episode_title);
    	
    	this.elapsedTime = (TextView)view.findViewById(R.id.player_controls_elapsed_time);
    	    	
    	this.totalTime = (TextView)view.findViewById(R.id.player_controls_total_time);
    	
    	this.seekBar = (CircularSeekBar)view.findViewById(R.id.player_controls_seekbar);
    	this.seekBar.setSeekBarChangeListener(mSeekBarChangeListener);
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
		if (this.podService != null && this.podService.mCurrentEpisode != null) 
		{
			if (this.podService.isPlaying())
				this.podService.updateUI();
			else
				this.podService.setUI();
		}
		else if (this.podService != null && this.podService.mCurrentEpisode == null) this.podService.loadLastPlayedEpisode();
	}
    
    @SuppressWarnings("deprecation")
	private void setupListView()
    {
    	this.mainListView = (DragNDropListView) view.findViewById(R.id.playlist);
    	this.mPlaylistDrawer = (SlidingDrawer) view.findViewById(R.id.drawer);
    	
    	this.mainListView.setAdapter(this.helper.playlistAdapter);
		this.mainListView.setDropListener(mDropListener);
    	this.mainListView.setDragListener(mDragListener);
    	this.mainListView.setOnItemClickListener(mOnClickListener);
    	
    	this.mainListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		this.mListSelectionListener = new PlaylistMultiChoiceModeListener(getActivity(), this.mainListView);
		this.mainListView.setMultiChoiceModeListener(this.mListSelectionListener);
    	
    	this.mPlaylistDrawer.setOnDrawerOpenListener(onDrawerOpenListener);
    	this.mPlaylistDrawer.setOnDrawerCloseListener(onDrawerCloseListener);
    }
       
    private void setupHelper()
    {
    	this.helper = ((com.podhoarder.activity.MainActivity)this.getActivity()).helper;
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
		if (this.podService != null)
		{
			//Toggle the play button to a pause button, since the track has started.
			this.playPauseButton.setChecked(true);
			//The service should start the episode.
			this.podService.playEpisode(epPos);
		}
		else
		{
			setServiceVars();
			start(epPos);
		}
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
    		if (mPlaylistDrawer.isOpened()) mPlaylistDrawer.animateClose();
		}
    };
    
    //Button Listeners
    private OnClickListener mPlayPauseClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if(!((ToggleImageButton)view).isChecked())
            {
                //Button is ON
                // Do Something 
            	if (podService.mCurrentEpisode != null)
            	{
            		if (podService.isCurrentTrackLoaded())
            			resume();
            		else
            			podService.playEpisode(podService.mCurrentEpisode);
            	}
            	else
            	{
            		if (podService.getPlaylistSize() > 0)	//First we have to make sure that there are any episodes in the list.
                	{
            			podService.playEpisode(0);
                	}	
            	}
            }
            else
            {
            	//Button is OFF
                // Do Something
            	if (podService.mCurrentEpisode != null)
            	{
            		pause();	
            	}
            }
            
        }
    };

    private OnClickListener mForwardClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
        	if (podService.isPlaying())
        	{
        		podService.skipForward();
        	}
        }
    };
       
    private OnClickListener mBackwardClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
        	if (podService.isPlaying())
        	{
        		podService.skipBackward();
        	}
        }
    };

    //Seekbar Listener
    private CircularSeekBar.OnSeekChangeListener mSeekBarChangeListener = new OnSeekChangeListener() {    


		@Override
		public void onProgressChange(CircularSeekBar view, int newProgress, boolean fromTouch)
		{
    		//podService.seek(newProgress);
			if (fromTouch)
				podService.seek(newProgress);
		}       
    };

    @SuppressWarnings("deprecation")
	private SlidingDrawer.OnDrawerOpenListener onDrawerOpenListener = new OnDrawerOpenListener()
	{
		
		@Override
		public void onDrawerOpened()
		{
			((MainActivity)getActivity()).disableRefresh();
			((ImageView)mPlaylistDrawer.getHandle()).setImageResource(R.drawable.ic_action_expand);
		}
	};
    
	@SuppressWarnings("deprecation")
	private SlidingDrawer.OnDrawerCloseListener onDrawerCloseListener = new OnDrawerCloseListener()
	{
		
		@Override
		public void onDrawerClosed()
		{
			((MainActivity)getActivity()).enableRefresh();
			((ImageView)mPlaylistDrawer.getHandle()).setImageResource(R.drawable.ic_action_collapse);
			if (mListSelectionListener.isActive())
				mListSelectionListener.getActionMode().finish();
		}
	};
}
