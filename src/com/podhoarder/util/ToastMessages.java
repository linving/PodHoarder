package com.podhoarder.util;

import android.content.Context;
import android.widget.Toast;

import com.podhoarderproject.podhoarder.R;

/**
 * Provides static convenience methods for showing various toast messages from one place.
 * @author Emil Almrot
 *
 */
public class ToastMessages
{
	/**
	 * Shows a "Downloading Podcast" message.
	 * @param ctx Application Context.
	 * @return A Toast message object. Add .show() to show.
	 */
	public static Toast DownloadingPodcast(Context ctx)
	{
		return Toast.makeText(ctx, ctx.getString(R.string.toast_downloading_podcast), Toast.LENGTH_SHORT);
	}
	
	/**
	 * Shows a "Download Failed" message.
	 * @param ctx Application Context.
	 * @return A Toast message object. Add .show() to show.
	 */
	public static Toast DownloadFailed(Context ctx)
	{
		return Toast.makeText(ctx, ctx.getString(R.string.toast_download_failed), Toast.LENGTH_SHORT);
	}
	
	/**
	 * Shows a "Episode Added To Playlist" message.
	 * @param ctx Application Context.
	 * @return A Toast message object. Add .show() to show.
	 */
	public static Toast EpisodeAddedToPlaylist(Context ctx)
	{
		return Toast.makeText(ctx, ctx.getString(R.string.toast_added_to_playlist), Toast.LENGTH_SHORT);
	}
	
	/**
	 * Shows a "Playback Failed" message.
	 * @param ctx Application Context.
	 * @return A Toast message object. Add .show() to show.
	 */
	public static Toast PlaybackFailed(Context ctx)
	{
		return Toast.makeText(ctx, ctx.getString(R.string.toast_player_playback_failed), Toast.LENGTH_SHORT);
	}

    /**
     * Shows a "Subscribed to..." message.
     * @param ctx Application context.
     * @return A Toast message object. Add .show() to show.
     */
    public static Toast Subscribed(Context ctx)
    {
        return Toast.makeText(ctx, ctx.getString(R.string.toast_subscribed), Toast.LENGTH_SHORT);
    }

	/**
	 * Shows a "Failed To Add Podcast" message.
	 * @param ctx Application Context.
	 * @return A Toast message object. Add .show() to show.
	 */
	public static Toast SubscribeFailed(Context ctx)
	{
		return Toast.makeText(ctx, ctx.getString(R.string.toast_subscribe_failed), Toast.LENGTH_SHORT);
	}

	/**
	 * Shows a "Refresh Successful" message.
	 * @param ctx Application Context.
	 * @return A Toast message object. Add .show() to show.
	 */
	public static Toast RefreshSuccessful(Context ctx)
	{
		return Toast.makeText(ctx, ctx.getString(R.string.toast_feeds_refreshed_successful), Toast.LENGTH_SHORT);
	}
	
	/**
	 * Shows a "Refresh Failed" message.
	 * @param ctx Application Context.
	 * @return A Toast message object. Add .show() to show.
	 */
	public static Toast RefreshFailed(Context ctx)
	{
		return Toast.makeText(ctx, ctx.getString(R.string.toast_feeds_refreshed_failed), Toast.LENGTH_SHORT);
	}

    /**
     * Shows a "No network available" message.
     * @param ctx Application Context.
     * @return A Toast message object. Add .show() to show.
     */
    public static Toast NoNetworkAvailable(Context ctx)
    {
        return Toast.makeText(ctx, ctx.getString(R.string.toast_no_network), Toast.LENGTH_SHORT);
    }

    /**
     * Shows a "Sleep timer set for HH:MM" message.
     * @param ctx Application Context.
     * @param hourOfDay HH
     * @param minute MM
     * @return A Toast message object. Add .show() to show.
     */
    public static Toast TimerSet(Context ctx, int hourOfDay, int minute) {
        String timeStamp = "";
        if (hourOfDay < 10)
            timeStamp += "0" + hourOfDay;
        else
            timeStamp += hourOfDay;

        timeStamp += ":";

        if (minute < 10)
            timeStamp += "0" + minute;
        else
            timeStamp += minute;

        return Toast.makeText(ctx, ctx.getString(R.string.toast_timer_set) + " " + timeStamp, Toast.LENGTH_SHORT);
    }

    /**
     * Shows a "Sleep timer cancelled." message.
     * @param ctx Application Context.
     * @return A Toast message object. Add .show() to show.
     */
    public static Toast TimerCancelled(Context ctx) {
        return Toast.makeText(ctx, ctx.getString(R.string.toast_timer_cancelled), Toast.LENGTH_SHORT);
    }
}
