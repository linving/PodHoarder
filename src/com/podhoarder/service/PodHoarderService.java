package com.podhoarder.service;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.podhoarder.activity.SettingsActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ToastMessages;

public class PodHoarderService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener  
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PodHoarderService";
	
	
	private 	MediaPlayer 	player;								//Media player
	private 	List<Episode> 	playList;							//Episode list
	public 		Episode 		currentEpisode;						//The Episode object that's currently being played.
	private 	final IBinder 	musicBind = new PodHoarderBinder();	//Binder object
	public 		PodcastHelper 	helper;								//Podcast helper.
	private 	int 			timeTracker = 0;					//Integer for keeping track of when to save elapsedTime to db.
	private 	boolean 		streaming = false;					//Boolean to keep track of whether the player is streaming or playing a local file.
	private		boolean			loading = false;					//Boolean that keeps track of whether the player is loading a track or not.
	private 	boolean 		updateBlocked = false;				//Boolean to keep track of whether the UI should be updated or not.
	private 	Handler 		handler;							//Handler object (for threading)
	private		ServiceNotification	notification;
	
	//Fragment UI Elements
	private 	ToggleButton	playPauseButton;
	private		ProgressBar		loadingCircle;
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
		if (isPlaying()) player.stop();
		player.release();
		return false;
	}

	@Override
	public void onCreate()
	{
		 //create the service
		super.onCreate();
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
		if (this.currentEpisode != null)
		{
			this.currentEpisode.setElapsedTime(this.currentEpisode.getTotalTime());	//Set elapsed time to total time (100% of the Episode)
			this.currentEpisode = helper.updateEpisodeNoRefresh(this.currentEpisode);	//Update the db object.
			final Episode lastEp = this.currentEpisode;
			int indexToDelete = this.helper.playlistAdapter.findEpisodeInPlaylist(this.currentEpisode);
			boolean wasStreaming = this.streaming;
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.SETTINGS_KEY_PLAYNEXTFILE, true))
			{
				playNext();	//Play
			}
			else this.stop();
			
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.SETTINGS_KEY_DELETELISTENED, true) && indexToDelete != -1)	
			{
				if (!wasStreaming)
				{
					
					this.helper.deleteEpisodeFile(lastEp);
					this.helper.playlistAdapter.removeFromPlaylist(this.playList.get(indexToDelete));
				}
				else
				{
					this.helper.playlistAdapter.removeFromPlaylist(this.playList.get(indexToDelete));
				}
			}
			this.helper.refreshListsAsync();
		}
		else	ToastMessages.PlaybackFailed(getApplicationContext()).show();
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
		this.loading = false;
		if (this.currentEpisode.getElapsedTime() < this.currentEpisode.getTotalTime())	
			player.seekTo(this.currentEpisode.getElapsedTime());	//If we haven't listened to the complete Episode, seek to the elapsed time stored in the db.
		else 
		{
			player.seekTo(0);	//If we have listened to the entire Episode, the player should start over.
			int index = this.helper.playlistAdapter.findEpisodeInPlaylist(currentEpisode);
			if (index != -1)
			{
				this.playList.get(index).setElapsedTime(0);
				this.helper.playlistAdapter.notifyDataSetChanged();
			}
		}
		player.start();
		setupNotification();
		this.notification.showNotify(this);
		if (this.currentEpisode.getTotalTime() == 0 || this.currentEpisode.getTotalTime() == 100) 
		{
			this.currentEpisode.setTotalTime(player.getDuration());
			this.currentEpisode = this.helper.updateEpisode(this.currentEpisode);
		}
		updateUI();
	}
	
	private void setupNotification()
	{
		this.notification = new ServiceNotification(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		
		if (this != null && intent != null && intent.getAction() != null)	//Make sure the Intent contains any data.
		{
			if (intent.getAction().equals("play"))	//The play button has been pressed.
			{
				this.notification.showNotify(this);
				this.resume();
			}
			else if (intent.getAction().equals("pause")) //The pause button has been pressed.
			{
				this.notification.pauseNotify(this);
				this.pause();
			}
			else if (intent.getAction().equals("forward"))
			{
				this.skipForward();
			}
			else if (intent.getAction().equals("backward"))
			{
				this.skipBackward();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
		
	/**
	 * Updates the UI elements in the Player Fragment (title, runtime etc)
	 */
	public void updateUI()
	{
		//Update the UI Elements in the Player Fragment.
		if (this.episodeTitle != null) 
		{
			this.episodeTitle.setText(this.currentEpisode.getTitle());
			this.episodeTitle.setSelected(true);
		}
		if (this.totalTime != null)this.totalTime.setText(millisToTime(this.currentEpisode.getTotalTime()));
		if (this.elapsedTime != null) this.elapsedTime.setText(millisToTime(this.currentEpisode.getElapsedTime()));
		if (this.seekBar != null) 
		{
			if (this.currentEpisode != null && this.seekBar.getVisibility() != View.VISIBLE) 
				this.seekBar.setVisibility(View.VISIBLE);
			this.seekBar.setMax(this.currentEpisode.getTotalTime());
		}
		if (isLoading())
		{
			if (this.playPauseButton != null && this.playPauseButton.getVisibility() == View.VISIBLE)	this.playPauseButton.setVisibility(View.GONE);
			if (this.loadingCircle != null && this.loadingCircle.getVisibility() == View.GONE) this.loadingCircle.setVisibility(View.VISIBLE);
		}
		else
		{
			if (this.playPauseButton != null && this.playPauseButton.getVisibility() == View.GONE)	this.playPauseButton.setVisibility(View.VISIBLE);
			if (this.loadingCircle != null && this.loadingCircle.getVisibility() == View.VISIBLE) this.loadingCircle.setVisibility(View.GONE);
			if (isPlaying() && this.playPauseButton != null) playPauseButton.setChecked(true);
		}
		this.helper.playlistAdapter.notifyDataSetChanged();
		this.handler.post(UpdateRunnable);
	}
	
	public void resetUI()
	{
		this.episodeTitle.setText("");
		this.totalTime.setText("");
		this.elapsedTime.setText("");
		this.seekBar.setMax(0);
		this.seekBar.setProgress(0);
		this.seekBar.setVisibility(View.INVISIBLE);
		this.loadingCircle.setVisibility(View.GONE);
		this.playPauseButton.setVisibility(View.VISIBLE);
		this.playPauseButton.setChecked(false);
	}
	
	@Override
	public void onDestroy() 
	{
		stopForeground(true);
	}
	
	public void playEpisode(int epPos)
	{
		playEpisode(playList.get(epPos));
	}
	
	public void playEpisode(Episode ep)
	{
		this.currentEpisode = ep;
		if (ep.isDownloaded())
		{
			this.startEpisode(ep);
		}
		else
		{
			if (!NetworkUtils.isOnline(getApplicationContext()))
				playNext();
			else
				this.streamEpisode(ep);
		}
	}
	
	private void startEpisode(Episode ep)
	{
		this.streaming = false;
		this.player.reset();
		this.currentEpisode = ep;
		try
		{
			this.player.setDataSource(getApplicationContext(), Uri.parse(currentEpisode.getLocalLink()));
		}
		catch(Exception e)
		{
			Log.e(LOG_TAG, "Error setting data source", e);
		}
		this.updateUI();
		Log.i(LOG_TAG, "Playing " + this.currentEpisode.getTitle() + " from file!");
		this.player.prepareAsync();
	}
	
	private void streamEpisode(Episode ep)
	{
		this.streaming = true;
		this.loading = true;
		this.player.reset();
		this.currentEpisode = ep;
		try
		{
			this.player.setDataSource(getApplicationContext(), Uri.parse(this.currentEpisode.getLink()));
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error setting data source", e);
		}
		this.updateUI();
		Log.i(LOG_TAG, "Playing " + this.currentEpisode.getTitle() + " from URL!");
		this.player.prepareAsync();
	}
	
	public void setUIElements(ToggleButton playPauseButton, ProgressBar loadingCircle, TextView episodeTitle, TextView elapsedTime, TextView totalTime, SeekBar seekBar, PodcastHelper helper)
	{
		this.playPauseButton = playPauseButton;
		this.loadingCircle = loadingCircle;
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
        	if (currentEpisode != null)
        	{
        		if (shouldSaveElapsedTime())
            	{
            		currentEpisode.setElapsedTime(getPosn());
            		helper.updateEpisode(currentEpisode); //Update the db.
            	}
            	if (!updateBlocked) seekBar.setProgress(getPosn());
                if (isPlaying()) 
                {
                	elapsedTime.setText(millisToTime(getPosn()));	// update progress bar using getCurrentPosition()
                	handler.postDelayed(UpdateRunnable, 350);
                }
        	}
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
        	if (currentEpisode != null)
        	{
        		if (shouldSaveElapsedTime())
            	{
            		currentEpisode.setElapsedTime(getPosn());
            		helper.updateEpisode(currentEpisode); //Update the db.
            	}
            	if (!updateBlocked) seekBar.setProgress(getPosn());
            	if (isPlaying())elapsedTime.setText(millisToTime(getPosn()));	// update progress bar using getCurrentPosition()
        	}
        }
    };
	
	public void pause()
	{
		player.pause();
		playPauseButton.setChecked(false);
		this.notification.pauseNotify(this);
		handler.post(SingleUpdateRunnable);
	}
	
	public void resume()
	{
		player.start();
		playPauseButton.setChecked(true);
		this.notification.showNotify(this);
		handler.post(UpdateRunnable);
	}
	
	public void stop()
	{
		player.stop();
		this.resetUI();
		this.currentEpisode = null;
		this.stopForeground(true);
	}
	
	public void setEpisode(int episodeIndex)
	{
		this.currentEpisode = this.playList.get(episodeIndex);
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
	
	public int getPlaylistSize()
	{
		return this.playList.size();
	}
	 
	public boolean isPlaying()
	{
		return player.isPlaying();
	}
	
	public boolean isStreaming()
	{
		return this.streaming;
	}
	
	public boolean isLoading()
	{
		return this.loading;
	}

	public void setUpdateBlocked(boolean updateBlocked)
	{
		this.updateBlocked = updateBlocked;
	}

	public void seek(int posn)
	{
		if (this.currentEpisode != null)
		{
			player.seekTo(posn);
			handler.post(SingleUpdateRunnable);	
		}
	}

	public void playPrev()
	{
		for (int i=0; i<this.playList.size(); i++)
		{
			if (this.currentEpisode.getEpisodeId() == this.playList.get(i).getEpisodeId())
			{
				if (i > 0 && this.playList.size() > 1)	//Only change Episode when we're not at the start of the playlist or the current Episode isn't the only one in the playlist.
				{
					playEpisode(i-1);
					break;
				}
				else
				{
					this.stop();
					break;
				}
			}
		}		
	}
	
	public void playNext()
	{
		int index = this.helper.playlistAdapter.findEpisodeInPlaylist(this.currentEpisode);
		if(index < (this.playList.size()-1) && this.playList.size() > 1)	//Only change Episode when we're not at the end of the playlist or the current Episode isn't the only one in the playlist.
		{
			playEpisode(index+1);
			return;
		}
		else
		{
			this.stop();
			return;
		}
	}
	
	public void playNext(int previousIndex)
	{
		
	}
	
	public boolean shouldSaveElapsedTime()
	{
		//A limit of 40 is set because the Runnable generally runs twice per second and we want to save the elapsed time every ~20 seconds.
		if (this.timeTracker >= 40)
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
		if (this.currentEpisode != null)
		{
			player.seekTo(player.getCurrentPosition() + 10000);
			handler.post(SingleUpdateRunnable);
		}		
	}
	
	public void skipBackward()
	{
		if (this.currentEpisode != null)
		{	
			if (player.getCurrentPosition() < 10000) player.seekTo(0);	//If we're not 10 seconds into the track, just rewind to the start.
			else player.seekTo(player.getCurrentPosition() - 10000);	//Else just go back 10 seconds
			handler.post(SingleUpdateRunnable);
		}
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
	
	/**
	 * Should be used to notify the Service that an .mp3-file is being deleted. The service will make sure it isn't the one playing.
	 * @param episodeId ID of the Episode being deleted.
	 */
	public void deletingEpisode(int episodeId)
	{
		if (this.currentEpisode != null)
		{
			if (this.currentEpisode.getEpisodeId() == episodeId && this.playList.size() > 1) 	//If the deleted Episode is the one currently playing we set the current Episode to current - 1
			{
				this.stop();
			}
		}
	}
	
	
}
