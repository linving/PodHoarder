package com.podhoarder.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.DragNDropAdapter;
import com.podhoarder.listener.DragListener;
import com.podhoarder.listener.PlaylistMultiChoiceModeListener;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.view.CircularSeekBar;
import com.podhoarder.view.DragNDropListView;
import com.podhoarder.view.ToggleImageButton;
import com.podhoarder.view.CircularSeekBar.OnSeekChangeListener;
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
	
	public DragNDropListView mListView;
	
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
	public CircularSeekBar seekBar;

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
			this.podService.setUIElements(playPauseButton, loadingCircle, seekBar, helper);
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
    	this.mListView = (DragNDropListView) view.findViewById(R.id.playlist);
    	this.mPlaylistDrawer = (SlidingDrawer) view.findViewById(R.id.drawer);
    	
    	this.mListView.setAdapter(this.helper.playlistAdapter);
    	this.mListView.setDragListener(mDragListener);
    	this.mListView.setOnItemClickListener(mOnClickListener);
    	
    	this.mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		this.mListSelectionListener = new PlaylistMultiChoiceModeListener(getActivity(), this.mListView);
		this.mListView.setMultiChoiceModeListener(this.mListSelectionListener);
    	
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

	
	public PlaylistMultiChoiceModeListener getListSelectionListener()
	{
		return mListSelectionListener;
	}


	//UI Logic
	//List Listeners
        
    private DragListener mDragListener = new DragListener() 
    {
    	int index;
    	DragNDropAdapter adapter;
    	
		public void onDrag(int x, int y, DragNDropListView listView) 
		{
			
			int pos = listView.pointToPosition(x, y);
			if (pos == -1)
			{
				if (index == adapter.getCount())
				{
					
				}
				else if (index == 0)
				{
					
				}
			}
			else if (index != pos && pos > -1 && index > -1)	//Only animate if the view is dragged onto a new index in the list.
			{
				listView.animateMove(index, pos);
				adapter.move(index, pos);
				Log.i("View", "moved to: " + pos);
			}
			index = pos;														//Store the index of where we're currently hovering.
			//Log.i(LOG_TAG, "New index: " + index);
		}

		public void onStartDrag() 
		{
			index = -1;
			adapter = (DragNDropAdapter) mListView.getAdapter();
		}

		public void onStopDrag() 
		{
			((MainActivity)getActivity()).helper.plDbH.savePlaylist(adapter.mPlayList);
			index = -1;
			adapter.notifyDataSetChanged();
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
			if (mListView.getAdapter().getCount() < 2)
				((DragNDropAdapter)mListView.getAdapter()).setReorderingEnabled(false);
			else
				((DragNDropAdapter)mListView.getAdapter()).setReorderingEnabled(true);
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
