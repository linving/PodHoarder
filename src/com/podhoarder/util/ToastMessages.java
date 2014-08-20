package com.podhoarder.util;

import android.content.Context;
import android.widget.Toast;

import com.podhoarderproject.podhoarder.R;

/**
 * Provides static convenience methods for showing various toast messages from one place.
 * @author Emil
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
		return Toast.makeText(ctx, ctx.getString(R.string.toast_episode_added_to_playlist), Toast.LENGTH_SHORT);
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
	 * Shows a "Failed To Add Podcast" message.
	 * @param ctx Application Context.
	 * @return A Toast message object. Add .show() to show.
	 */
	public static Toast AddFeedFailed(Context ctx)
	{
		return Toast.makeText(ctx, ctx.getString(R.string.toast_add_feed_failed), Toast.LENGTH_SHORT);
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
}
