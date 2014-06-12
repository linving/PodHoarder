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

import com.podhoarderproject.podhoarder.*;

public class PodHoarderService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener  
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PodHoarderService";
	
	private static final int NOTIFY_ID=1;
	
	//media player
	private MediaPlayer player;
	//song list
	private List<Episode> playList;
	//current position in the playList.
	private int epPos;
	//Binder object
	private final IBinder musicBind = new PodHoarderBinder();
	//Podcast helper.
	private PodcastHelper helper;
	//Integer for keeping track of when to save elapsedTime to db.
	private int timeTracker = 0;
	
	//Handler object (for threading)
	Handler handler;
	
	//Fragment UI Elements
	private TextView episodeTitle;
	private TextView elapsedTime;
	private TextView totalTime;
	private SeekBar seekBar;
	
	private boolean updateBlocked = false;

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
		if(player.getCurrentPosition()>0){
			this.player.reset();
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
		Episode currentEpisode = playList.get(epPos);
		player.seekTo(currentEpisode.getElapsedTime());
		player.start();
		
		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0,
		  notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		Notification.Builder builder = new Notification.Builder(this);
		 
		builder.setContentIntent(pendInt)
		  .setSmallIcon(R.drawable.ic_launcher)
		  .setTicker(currentEpisode.getTitle())
		  .setOngoing(true)
		  .setContentTitle("Playing")	//TODO: Replace with string resource.
		  .setContentText(currentEpisode.getTitle());
		Notification not = builder.build();
		 
		startForeground(NOTIFY_ID, not);
		
		//Update the UI Elements in the Player Fragment.
		this.episodeTitle.setText(currentEpisode.getTitle());
		this.totalTime.setText(millisToTime(currentEpisode.getTotalTime()));
		this.seekBar.setMax(currentEpisode.getTotalTime());
		
		this.handler.post(UpdateRunnable);
	}
	
	@Override
	public void onDestroy() 
	{
		stopForeground(true);
	}
	
	public void startEpisode()
	{
		this.player.reset();
		
		//get song
		Episode currentEpisode = playList.get(epPos);
		
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
	
	public void setUIElements(TextView episodeTitle, TextView elapsedTime, TextView totalTime, SeekBar seekBar, PodcastHelper helper)
	{
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
        public void run() {
            // update progress bar using getCurrentPosition()
        	elapsedTime.setText(millisToTime(getPosn()));
        	if (shouldSaveElapsedTime())
        	{
        		//Update the db.
        		helper.updateEpisodeListened(playList.get(epPos).getFeedId(), playList.get(epPos).getEpisodeId(), getPosn());
        		//Update the object in the list manually instead of reloading the entire list.
        		playList.get(epPos).setElapsedTime(getPosn());
        	}
        	if (!updateBlocked) seekBar.setProgress(getPosn());
            if (isPng())
                handler.postDelayed(UpdateRunnable, 500);
        }
    };
    
    /**
     * Post a Runnable that updates the seekbar position in relation to player progress once.
     */
    private Runnable SingleUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            // update progress bar using getCurrentPosition()
        	elapsedTime.setText(millisToTime(getPosn()));
        	if (shouldSaveElapsedTime())
        	{
        		//Update the db.
        		helper.updateEpisodeListened(playList.get(epPos).getFeedId(), playList.get(epPos).getEpisodeId(), getPosn());
        		//Update the object in the list manually instead of reloading the entire list.
        		playList.get(epPos).setElapsedTime(getPosn());
        	}
        	if (!updateBlocked) seekBar.setProgress(getPosn());
        }
    };
	
	public void pause()
	{
		player.pause();
		handler.post(SingleUpdateRunnable);
	}
	
	public void resume()
	{
		player.start();
		handler.post(UpdateRunnable);
	}
	
	public void setEpisode(int episodeIndex)
	{
		epPos = episodeIndex;
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
		if(this.epPos < 0) this.epPos=this.playList.size()-1;
		startEpisode();
	}
	
	public boolean shouldSaveElapsedTime()
	{
		//A limit of 20 is set because the Runnable generally runs twice per second and we want to save the elapsed time roughly every 10 seconds.
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
	
	//skip to next
	public void playNext()
	{
		this.epPos++;
		if(this.epPos >= this.playList.size()) this.epPos=0;
		startEpisode();
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
}
