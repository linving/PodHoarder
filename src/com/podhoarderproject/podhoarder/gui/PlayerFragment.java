package com.podhoarderproject.podhoarder.gui;

import com.podhoarderproject.ericharlow.DragNDrop.DragListener;
import com.podhoarderproject.ericharlow.DragNDrop.DragNDropAdapter;
import com.podhoarderproject.ericharlow.DragNDrop.DragNDropListView;
import com.podhoarderproject.ericharlow.DragNDrop.DropListener;
import com.podhoarderproject.ericharlow.DragNDrop.RemoveListener;
import com.podhoarderproject.podhoarder.Episode;
import com.podhoarderproject.podhoarder.PodcastHelper;
import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.service.PodHoarderService;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.renderscript.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
	private boolean musicBound = false;
	
	//Renderscript
	private RenderScript rs;
	
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
    	//define this only once if blurring multiple times
    	this.rs = RenderScript.create(getActivity());
    	
    	setupHelper();
    	setupListView();
    	setupMediaControls();
    	
    	if (this.podService != null && this.podService.isPng()) this.podService.updateUI();
    	if (this.podService != null && this.podService.isPng())	playPauseButton.setChecked(false);
    	return this.view;
    }
    
    private void setServiceVars()
    {
    	this.podService = ((com.podhoarderproject.podhoarder.MainActivity)this.getActivity()).getPodService();
    	this.musicBound = ((com.podhoarderproject.podhoarder.MainActivity)this.getActivity()).isMusicBound();
    }
    
    private void setupMediaControls()
    {
    	this.playPauseButton = (ToggleButton)view.findViewById(R.id.player_controls_button_playpause);
    	this.playPauseButton.setOnClickListener(mPlayPauseClickListener);
    	
    	this.doubleSpeedButton = (ToggleButton)view.findViewById(R.id.player_controls_button_2x);
    	
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
	
	//play next
	private void playNext()
	{
		this.podService.playNext();
	}
	 
	//play previous
	private void playPrev()
	{
		this.podService.playPrev();
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
    	int backgroundColor = 0xe0103010;
    	int defaultBackgroundColor;
    	
		public void onDrag(int x, int y, ListView listView) 
		{
			// TODO Auto-generated method stub
		}

		public void onStartDrag(View itemView) 
		{
			itemView.setVisibility(View.INVISIBLE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);
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
//			Episode currentEpisode = (Episode)mainListView.getAdapter().getItem(position);
//			LinearLayout v = (LinearLayout)view.findViewById(R.id.player_controls_container);
//			Bitmap blurredOriginal = helper.getFeedImage(currentEpisode.getFeedId()).getBitmap();
//			//this will blur the bitmapOriginal with a radius of 8 and save it in bitmapOriginal
//			final Allocation input = Allocation.createFromBitmap(rs, blurredOriginal); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
//			final Allocation output = Allocation.createTyped(rs, input.getType());
//			final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//			script.setRadius(8f);
//			script.setInput(input);
//			script.forEach(output);
//			output.copyTo(blurredOriginal);
//			
//			v.setBackground(new BitmapDrawable(blurredOriginal));
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
            	resume();
            }
            else
            {
            	//Button is OFF
                // Do Something
            	pause();
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
