package com.podhoarderproject.podhoarder.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.podhoarderproject.podhoarder.*;

public class PodHoarderService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener  
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PodHoarderService";
	
	private static final int NOTIFY_ID=1;
	
	
	private 	MediaPlayer 	player;								//media player
	private 	List<Episode> 	playList;							//song list
	private 	int 			epPos;								//current position in the playList.
	public 		Episode 		currentEpisode;						//The Episode object that's currently being played.
	private 	final IBinder 	musicBind = new PodHoarderBinder();	//Binder object
	private 	PodcastHelper 	helper;								//Podcast helper.
	private 	int 			timeTracker = 0;					//Integer for keeping track of when to save elapsedTime to db.
	private 	boolean 		streaming = false;					//Boolean to keep track of whether the player is streaming or playing a local file.
	private 	boolean 		updateBlocked = false;				//Boolean to keep track of whether the UI should be updated or not.
	private 	Handler 		handler;							//Handler object (for threading)
	
	//Fragment UI Elements
	private 	ToggleButton	playPauseButton;
	private 	TextView 		episodeTitle;
	private 	TextView 		elapsedTime;
	private 	TextView 		totalTime;
	private 	SeekBar 		seekBar;
	

	@Override
	public IBinder onBind(Intent arg0) 
	{
		return musicBind;
	}
	
	@Override
	public boolean onUnbind(Intent intent)
	{
		player.stop();
		player.release();
		return false;
	}

	@Override
	public void onCreate()
	{
		 //create the service
		super.onCreate();
		//initialize position
		this.epPos=0;
		//create player
		this.player = new MediaPlayer();
		//create Handler
		this.handler = new Handler();
		//Init player
		initMusicPlayer();
	}
	
	@Override
	public void onCompletion(MediaPlayer arg0)
	{
		handler.post(SingleUpdateRunnable);
		if(player.getCurrentPosition()>0){
			this.player.reset();
			this.currentEpisode.setElapsedTime(this.currentEpisode.getTotalTime());	//Set elapsed time to total time (100% of the Episode)
			helper.updateEpisode(currentEpisode);	//Update the db object.
			//TODO: Add an option that let's the user decide if the player should proceed to the next track, or stop once an Episode is finished.
		    playNext();
		}
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2)
	{
		this.player.reset();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer player)
	{
		player.seekTo(this.currentEpisode.getElapsedTime());
		player.start();
		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0,
		  notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		Notification.Builder builder = new Notification.Builder(this);
		 
		builder.setContentIntent(pendInt)
		  .setSmallIcon(R.drawable.ic_launcher)
		  .setTicker(this.currentEpisode.getTitle())
		  .setOngoing(true)
		  .setContentTitle(this.getString(R.string.app_name) + " " + this.getString(R.string.notification_playback))
		  .setContentText(this.currentEpisode.getTitle());
		Notification not = builder.build();
		startForeground(NOTIFY_ID, not);
		
		if (streaming && this.currentEpisode.getTotalTime() == 0) 
		{
			this.currentEpisode.setTotalTime(player.getDuration());
			this.currentEpisode = this.helper.updateEpisode(this.currentEpisode);
		}
		if (this.episodeTitle != null && 
				this.totalTime != null && 
				this.seekBar != null &&
				this.elapsedTime != null)
		{
			updateUI();
		}
		
	}
	
	/**
	 * Updates the UI elements in the Player Fragment (title, runtime etc)
	 */
	public void updateUI()
	{
		//Update the UI Elements in the Player Fragment.
		this.episodeTitle.setText(this.currentEpisode.getTitle());
		this.totalTime.setText(millisToTime(this.currentEpisode.getTotalTime()));
		this.seekBar.setMax(this.currentEpisode.getTotalTime());
		if (isPng()) playPauseButton.setChecked(true);
		this.handler.post(UpdateRunnable);
	}
	
	@Override
	public void onDestroy() 
	{
		stopForeground(true);
	}
	
	public void startEpisode(int epPos)
	{
		this.streaming = false;
		this.player.reset();
		this.epPos = epPos;
		this.currentEpisode = playList.get(epPos);
		try
		{
			this.player.setDataSource(getApplicationContext(), Uri.parse(currentEpisode.getLocalLink()));
		}
		catch(Exception e)
		{
			Log.e(LOG_TAG, "Error setting data source", e);
		}
		this.player.prepareAsync();
	}
	
	public void startEpisode(Episode ep)
	{
		this.streaming = false;
		this.player.reset();
		findEpisodeInPlaylist(ep);
		try
		{
			this.player.setDataSource(getApplicationContext(), Uri.parse(currentEpisode.getLocalLink()));
		}
		catch(Exception e)
		{
			Log.e(LOG_TAG, "Error setting data source", e);
		}
		this.player.prepareAsync();
	}
	
	public void streamEpisode(Episode ep)
	{
		this.streaming = true;
		this.player.reset();
		this.currentEpisode = ep;
		this.epPos = -1;
		try
		{
			this.player.setDataSource(getApplicationContext(), Uri.parse(this.currentEpisode.getLink()));
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error setting data source", e);
		}
//		this.episodeTitle.setText("Loading...");	//TODO: Replace with string resource.
		this.player.prepareAsync();
	}
	
	public void setUIElements(ToggleButton playPauseButton, TextView episodeTitle, TextView elapsedTime, TextView totalTime, SeekBar seekBar, PodcastHelper helper)
	{
		this.playPauseButton = playPauseButton;
		this.episodeTitle = episodeTitle;
		this.totalTime = totalTime;
		this.elapsedTime = elapsedTime;
		this.seekBar = seekBar;
		this.helper = helper;
	}
	
	/**
     * Post a Runnable that updates the seekbar position in relation to player progress continuously.
     */
    private Runnable UpdateRunnable = new Runnable() {
        @Override
        public void run() 
        {
        	elapsedTime.setText(millisToTime(getPosn()));	// update progress bar using getCurrentPosition()
        	if (shouldSaveElapsedTime())
        	{
        		currentEpisode.setElapsedTime(getPosn());
        		helper.updateEpisode(currentEpisode); //Update the db.
        		if (!streaming) playList.get(epPos).setElapsedTime(getPosn());	//Update the object in the list manually instead of reloading the entire list. (Only if we aren't streaming, because if we are then the object is not in the playlist)
        	}
        	if (!updateBlocked) seekBar.setProgress(getPosn());
            if (isPng()) handler.postDelayed(UpdateRunnable, 500);
        }
    };
    
    /**
     * Post a Runnable that updates the seekbar position in relation to player progress once.
     */
    private Runnable SingleUpdateRunnable = new Runnable() 
    {
        @Override
        public void run() 
        {
        	elapsedTime.setText(millisToTime(getPosn()));	// update progress bar using getCurrentPosition()
        	if (shouldSaveElapsedTime())
        	{
        		currentEpisode.setElapsedTime(getPosn());
        		helper.updateEpisode(currentEpisode); //Update the db.
        		if (!streaming)
        		{
        			playList.get(epPos).setElapsedTime(getPosn()); 	//Update the object in the list manually instead of reloading the entire list. (Only if we aren't streaming, because if we are then the object is not in the playlist)
        			
        		}
        	}
        	if (!updateBlocked) seekBar.setProgress(getPosn());
        }
    };
	
	public void pause()
	{
		player.pause();
		playPauseButton.setChecked(false);
		handler.post(SingleUpdateRunnable);
	}
	
	public void resume()
	{
		player.start();
		playPauseButton.setChecked(true);
		handler.post(UpdateRunnable);
	}
	
	public void setEpisode(int episodeIndex)
	{
		epPos = episodeIndex;
		this.currentEpisode = this.playList.get(epPos);
	}
	
	public void setList(List<Episode> playList){
		this.playList = playList;
	}
	
	public int getPosn()
	{
		return player.getCurrentPosition();
	}
	 
	public int getDur()
	{
		return player.getDuration();
	}
	 
	public boolean isPng()
	{
		return player.isPlaying();
	}

	public void setUpdateBlocked(boolean updateBlocked)
	{
		this.updateBlocked = updateBlocked;
	}

	public void seek(int posn)
	{
		player.seekTo(posn);
		handler.post(SingleUpdateRunnable);
	}

	public void playPrev()
	{
		this.epPos--;
		if(this.epPos < 0) this.epPos = this.playList.size()-1;
		startEpisode(this.epPos);
	}
	
	public void playNext()
	{
		this.epPos++;
		if(this.epPos >= this.playList.size()) this.epPos=0;
		startEpisode(this.epPos);
	}
	
	public boolean shouldSaveElapsedTime()
	{
		//A limit of 20 is set because the Runnable generally runs twice per second and we want to save the elapsed time every ~10 seconds.
		if (this.timeTracker >= 20)
		{
			//Reset the tracker if we are about to save.
			this.timeTracker = 0;
			return true;
		}
		else
		{
			this.timeTracker++;
			return false;
		}
	}

	public void skipForward()
	{
		player.seekTo(player.getCurrentPosition() + 10000);
		handler.post(SingleUpdateRunnable);
	}
	
	public void skipBackward()
	{
		player.seekTo(player.getCurrentPosition() - 10000);
		handler.post(SingleUpdateRunnable);
	}
	
	public class PodHoarderBinder extends Binder 
	{
		public PodHoarderService getService() 
		{
			return PodHoarderService.this;
		}
	}
	
	public void initMusicPlayer()
	{
		//set player properties
		this.player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//set listeners
		this.player.setOnPreparedListener(this);
		this.player.setOnCompletionListener(this);
		this.player.setOnErrorListener(this);
	}

	/**
	 * A function that converts a millisecond value to a properly formatted timestamp (HH:MM:SS)
	 * @param time The time value in milliseconds.
	 * @return	A string object with the millisecond value formatted accordingly.
	 */
	public static String millisToTime(int time)
	{
		String retString = "";
		int seconds = (int) (time / 1000) % 60;
		int minutes = (int) ((time / (1000*60)) % 60);
		int hours   = (int) ((time / (1000*60*60)) % 24);
		
		//Add a 0 before hour count if it's less than 10. (05 looks better than 5 in a timestamp)
		if (hours>0 && hours<10) retString += "0" + hours + ":";
		else if (hours>=10) retString += hours + ":";
		else retString += "00:";
		
		//Add a 0 before minute count if it's less than 10. (05 looks better than 5 in a timestamp)
		if (minutes>0 && minutes<10) retString += "0" + minutes + ":";
		else if (minutes>=10) retString += minutes + ":";
		else retString += "00:";
		
		//Add a 0 before second count if it's less than 10. (05 looks better than 5 in a timestamp)
		if (seconds>0 && seconds<10) retString += "0" + seconds;
		else if (seconds>=10) retString += seconds;
		else retString += "00";
		
		return retString;
	}
	
	private void findEpisodeInPlaylist(Episode ep)
	{
		for (int i=0; i<this.playList.size(); i++)
		{
			if (ep.getEpisodeId() == this.playList.get(i).getEpisodeId())
			{
				this.currentEpisode = this.playList.get(i);
				this.epPos = i;
			}
			
		}
	}
}
