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

import com.podhoarder.object.Episode;
import com.podhoarder.util.Constants;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ToastMessages;
import com.podhoarder.view.FloatingPlayPauseButton;

public class PodHoarderService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener  
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.PodHoarderService";
	
	
	private 	MediaPlayer 	mPlayer;								//Media player
	private 	List<Episode> 	mPlayList;							//Episode list
	public 		Episode 		mCurrentEpisode;						//The Episode object that's currently being played.
	private 	final IBinder 	mMusicBinder = new PodHoarderBinder();	//Binder object
	public 		PodcastHelper 	mHelper;								//Podcast helper.
	private 	boolean 		mStreaming = false;					//Boolean to keep track of whether the player is streaming or playing a local file.
	private		boolean			mLoading = false;					//Boolean that keeps track of whether the player is loading a track or not.
	private		boolean			mCurrentTrackLoaded = false;		//Boolean to keep track of whether the current track is loaded and can be resumed, or if it needs to be reloaded.
	private 	Handler 		mHandler;							//Handler object (for threading)
	private		ServiceNotification	mNotification;
	private FloatingPlayPauseButton mFAB;
	
	

	@Override
	public IBinder onBind(Intent arg0) 
	{
		return mMusicBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent)
	{
		if (isPlaying()) mPlayer.stop();
		mPlayer.release();
		return false;
	}

	@Override
	public void onCreate()
	{
		 //create the service
		super.onCreate();
		//create player
		this.mPlayer = new MediaPlayer();
		//create Handler
		this.mHandler = new Handler();
		//Init player
		initMusicPlayer();
	}
	
	@Override
	public void onCompletion(MediaPlayer arg0)
	{
		this.mCurrentTrackLoaded = false;
		if (this.mCurrentEpisode != null)
		{
			this.mCurrentEpisode.setElapsedTime(this.mCurrentEpisode.getTotalTime());	//Set elapsed time to total time (100% of the Episode)
			this.mCurrentEpisode = mHelper.updateEpisodeNoRefresh(this.mCurrentEpisode);	//Update the db object.
			final Episode lastEp = this.mCurrentEpisode;
			int indexToDelete = this.mHelper.mPlaylistAdapter.findEpisodeInPlaylist(this.mCurrentEpisode);
			boolean wasStreaming = this.mStreaming;
			
			playNext();	//Play
			
			if (!wasStreaming)
			{
				this.mHelper.deleteEpisodeFile(lastEp);
			}
			if (indexToDelete != -1)	
			{
				this.mHelper.mPlaylistAdapter.removeFromPlaylist(this.mPlayList.get(indexToDelete));
			}
			this.mHelper.refreshContent();
		}
		else	ToastMessages.PlaybackFailed(getApplicationContext()).show();
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2)
	{
		this.mPlayer.reset();
		this.mCurrentTrackLoaded = false;
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer player)
	{
		this.mLoading = false;
		mFAB.setPlaying(true);
		this.mCurrentTrackLoaded = true;
		if (this.mCurrentEpisode.getElapsedTime() < this.mCurrentEpisode.getTotalTime())	
			player.seekTo(this.mCurrentEpisode.getElapsedTime());	//If we haven't listened to the complete Episode, seek to the elapsed time stored in the db.
		else 
		{
			player.seekTo(0);	//If we have listened to the entire Episode, the player should start over.
		}
		player.start();
		this.setLastPlayedEpisode(this.mCurrentEpisode.getEpisodeId());
		setupNotification();
		this.mNotification.showNotify(this);
		if (this.mCurrentEpisode.getTotalTime() == 0 || this.mCurrentEpisode.getTotalTime() == 100) 
		{
			this.mCurrentEpisode.setTotalTime(player.getDuration());
			this.mCurrentEpisode = this.mHelper.updateEpisode(this.mCurrentEpisode);
		}
		this.mHandler.post(UpdateRunnable);
	}
	
	private void setupNotification()
	{
		this.mNotification = new ServiceNotification(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		
		if (this != null && intent != null && intent.getAction() != null)	//Make sure the Intent contains any data.
		{
			if (intent.getAction().equals("play"))	//The play button has been pressed.
			{
				this.mNotification.showNotify(this);
				this.resume();
			}
			else if (intent.getAction().equals("pause")) //The pause button has been pressed.
			{
				this.mNotification.pauseNotify(this);
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
			else if (intent.getAction().equals("close"))
			{
				this.pause();
				this.stopForeground(true);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() 
	{
		stopForeground(true);
	}
	
	public void play()
	{
		if (mCurrentTrackLoaded)
		{
			resume();
		}
		else
		{
			try
			{
				playEpisode(this.mCurrentEpisode);
			}
			catch (NullPointerException e)
			{
				this.loadLastPlayedEpisode();
				play();
			}
		}
	}
	
	public void playEpisode(int epPos)
	{
		playEpisode(mPlayList.get(epPos));
	}
	
	public void playEpisode(Episode ep)
	{
		
		if (ep.isDownloaded())
		{
			this.mCurrentEpisode = ep;
			this.startEpisode(ep);
		}
		else
		{
			if (!NetworkUtils.isOnline(getApplicationContext()))
			{
				this.mCurrentEpisode = ep;
				playNext();
			}
				
			else
			{
				this.mCurrentEpisode = ep;
				this.streamEpisode(ep);
			}
		}
	}
	
	private void startEpisode(Episode ep)
	{
		this.mStreaming = false;
		this.mPlayer.reset();
		this.mCurrentEpisode = ep;
		try
		{
			this.mPlayer.setDataSource(getApplicationContext(), Uri.parse(mCurrentEpisode.getLocalLink()));
		}
		catch(Exception e)
		{
			Log.e(LOG_TAG, "Error setting data source", e);
		}
		Log.i(LOG_TAG, "Playing " + this.mCurrentEpisode.getTitle() + " from file!");
		this.mPlayer.prepareAsync();
	}
	
	private void streamEpisode(Episode ep)
	{
		this.mStreaming = true;
		this.mLoading = true;
		this.mPlayer.reset();
		this.mCurrentEpisode = ep;
		try
		{
			this.mPlayer.setDataSource(getApplicationContext(), Uri.parse(this.mCurrentEpisode.getLink()));
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error setting data source", e);
		}
		Log.i(LOG_TAG, "Playing " + this.mCurrentEpisode.getTitle() + " from URL!");
		this.mPlayer.prepareAsync();
	}
	
	public void loadLastPlayedEpisode()
	{
		int lastEpisodeId = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.SETTINGS_KEY_LASTEPISODE,"-1"));
		if (lastEpisodeId != -1)
		{
			this.mCurrentEpisode = this.mHelper.getEpisode(lastEpisodeId);
		}
	}
	
	/**
	 * Saves the Episode Id to Preferences, so the episode can be loaded on app restart.
	 * @param episodeId Id of the last played Episode, or -1 if no Episode.
	 */
	public void setLastPlayedEpisode(int episodeId)
	{
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(Constants.SETTINGS_KEY_LASTEPISODE, ""+episodeId).apply();
	}
	
	/**
     * Post a Runnable that updates the seekbar position in relation to player progress continuously.
     */
    private Runnable UpdateRunnable = new Runnable() {
        @Override
        public void run() 
        {
        	if (isPlaying())
        	{
	        	mCurrentEpisode.setElapsedTime(getPosn());
	    		mHelper.updateEpisode(mCurrentEpisode); //Update the db.
	        	mHandler.postDelayed(UpdateRunnable, 20000);
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
        	mCurrentEpisode.setElapsedTime(getPosn());
    		mHelper.updateEpisode(mCurrentEpisode); //Update the db.
        }
    };
	
	public void pause()
	{
		mFAB.setPlaying(false);
		mPlayer.pause();
		this.mNotification.pauseNotify(this);
		mHandler.post(SingleUpdateRunnable);
	}
	
	public void resume()
	{
		mFAB.setPlaying(true);
		mPlayer.start();
		this.mNotification.showNotify(this);
		mHandler.post(UpdateRunnable);
	}
	
	public void stop()
	{
		mFAB.setPlaying(false);
		mPlayer.stop();
		this.mCurrentEpisode = null;
		this.stopForeground(true);
	}
	
	public void setEpisode(int episodeIndex)
	{
		this.mCurrentEpisode = this.mPlayList.get(episodeIndex);
	}
	
	public void setMembers(PodcastHelper helper, FloatingPlayPauseButton fab)
	{
		mFAB = fab;
		mFAB.setPlaying(isPlaying());
		mHelper = helper;
	}
	
	public void setList(List<Episode> playList){
		this.mPlayList = playList;
	}
	
	public int getPosn()
	{
		return mPlayer.getCurrentPosition();
	}
	 
	public int getDur()
	{
		return mPlayer.getDuration();
	}
	
	public int getPlaylistSize()
	{
		return this.mPlayList.size();
	}
	 
	public boolean isPlaying()
	{
		return mPlayer.isPlaying();
	}
	
	public boolean isStreaming()
	{
		return this.mStreaming;
	}
	
	public boolean isLoading()
	{
		return this.mLoading;
	}

	public boolean isCurrentTrackLoaded()
	{
		return mCurrentTrackLoaded;
	}

	public void seek(int posn)
	{
		if (this.mCurrentEpisode != null)
		{
			mPlayer.seekTo(posn);
			mHandler.post(SingleUpdateRunnable);	
		}
	}

	public void playPrev()
	{
		for (int i=0; i<this.mPlayList.size(); i++)
		{
			if (this.mCurrentEpisode.getEpisodeId() == this.mPlayList.get(i).getEpisodeId())
			{
				if (i > 0 && this.mPlayList.size() > 1)	//Only change Episode when we're not at the start of the playlist or the current Episode isn't the only one in the playlist.
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
		int index = this.mHelper.mPlaylistAdapter.findEpisodeInPlaylist(this.mCurrentEpisode);
		if(index < (this.mPlayList.size()-1) && this.mPlayList.size() > 1)	//Only change Episode when we're not at the end of the playlist or the current Episode isn't the only one in the playlist.
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

	public void skipForward()
	{
		if (this.mCurrentEpisode != null)
		{
			mPlayer.seekTo(mPlayer.getCurrentPosition() + 10000);
			mHandler.post(SingleUpdateRunnable);
		}		
	}
	
	public void skipBackward()
	{
		if (this.mCurrentEpisode != null)
		{	
			if (mPlayer.getCurrentPosition() < 10000) mPlayer.seekTo(0);	//If we're not 10 seconds into the track, just rewind to the start.
			else mPlayer.seekTo(mPlayer.getCurrentPosition() - 10000);	//Else just go back 10 seconds
			mHandler.post(SingleUpdateRunnable);
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
		this.mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		this.mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		//set listeners
		this.mPlayer.setOnPreparedListener(this);
		this.mPlayer.setOnCompletionListener(this);
		this.mPlayer.setOnErrorListener(this);
		
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
		if (this.mCurrentEpisode != null)
		{
			if (this.mCurrentEpisode.getEpisodeId() == episodeId && this.mPlayList.size() > 1) 	//If the deleted Episode is the one currently playing we set the current Episode to current - 1
			{
				this.stop();
			}
		}
	}
	
	
}
