package com.podhoarderproject.podhoarder.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.List;

import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;
import com.podhoarderproject.podhoarder.*;

public class PodHoarderService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener  
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PodHoarderService";
	
	private static final int NOTIFY_ID=1;
	
	//media player
	private MediaPlayer player;
	//song list
	private List<Episode> playList;
	//current position
	private int epPos;
	//Binder object
	private final IBinder musicBind = new PodHoarderBinder();
	 
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
		//Init player
		initMusicPlayer();
	}
	
	@Override
	public void onCompletion(MediaPlayer arg0)
	{
		if(player.getCurrentPosition()>0){
			this.player.reset();
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
	
	public void pausePlayback()
	{
		player.pause();
	}
	
	public void resumePlayback()
	{
		player.start();
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
	 
	public void pausePlayer()
	{
		player.pause();
	}
	 
	public void seek(int posn)
	{
		player.seekTo(posn);
	}
	 
	public void go(){
		player.start();
	}
	
	public void playPrev()
	{
		this.epPos--;
		if(this.epPos < 0) this.epPos=this.playList.size()-1;
		startEpisode();
	}
	
	//skip to next
	public void playNext()
	{
		this.epPos++;
		if(this.epPos >= this.playList.size()) this.epPos=0;
		startEpisode();
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

}
