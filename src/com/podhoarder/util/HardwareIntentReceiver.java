package com.podhoarder.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.service.PodHoarderService;

/**
 * A Broadcast Receiver class for intercepting and handling hardware events that disrupt playback somehow.
 * <p>Pauses playback when headset is unplugged.</p>
 * Pauses and Resumes playback when a call is received and ended.
 */
public class HardwareIntentReceiver extends BroadcastReceiver 
{
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.HardwareIntentReceiver";
	
	private PodHoarderService mPlaybackService;
	
	private LibraryActivityManager mLibraryActivityManager;
	
	private boolean mWasPlaying = false;
	
	public HardwareIntentReceiver(PodHoarderService playbackService, LibraryActivityManager podcastHelper)
	{
		mPlaybackService = playbackService;
		mLibraryActivityManager = podcastHelper;
	}
	
    @Override public void onReceive(Context context, Intent intent) 
    {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) //We receive this Intent if the headset is plugged in or out.
        {
            int state = intent.getIntExtra("state", -1);
            switch (state) 
            {
	            case 0:
	            	//Fires when the headphones are unplugged.
	            	if (this.mPlaybackService != null && this.mPlaybackService.isPlaying()) this.mPlaybackService.pause();
	                break;
	            case 1:
	            	//Fires when the headphones are plugged in.
	                break;
	            default:
	                break;
            }
        }
        
        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) //We receive this Intent if the phone starts ringing.
        {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING))
            {
            	if (this.mPlaybackService != null && this.mPlaybackService.isPlaying()) 
            	{
            		this.mWasPlaying = true;
            		this.mPlaybackService.pause();	//For when it starts ringing.
            	}
            }
            else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE))
            {
            	if (this.mPlaybackService != null && this.mWasPlaying) 
            	{
            		this.mWasPlaying = false;
            		mPlaybackService.resume(); //For when the phone returns back to normal after a call.
            	}
            }
        }
        
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
        {
        	Log.d(LOG_TAG, "Network connectivity change");

            if (intent.getExtras() != null) 
            {
                final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                if (ni != null && ni.isConnectedOrConnecting()) 
                {
                    Log.i(LOG_TAG, "Network " + ni.getTypeName() + " connected");
                    mLibraryActivityManager.invalidate();
                } 
                else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) 
                {
                	if (mPlaybackService.isPlaying() && mPlaybackService.isStreaming())
                	{
                		mPlaybackService.stop();
                		mLibraryActivityManager.invalidate();
                	}
                	else
                	{
                		mLibraryActivityManager.invalidate();
                	}
                    Log.d(LOG_TAG, "There's no network connectivity");
                }
            }
        }
    }
}


