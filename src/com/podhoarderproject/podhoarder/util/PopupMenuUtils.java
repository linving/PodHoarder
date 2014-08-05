package com.podhoarderproject.podhoarder.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.activity.MainActivity;

public class PopupMenuUtils
{
	
	/**
	 * Show a context menu for a Feed object.
	 * @param context	App context.
	 * @param anchorView	The view that was clicked. The PopupMenu will appear near this View.
	 * @param feed Feed to perform actions on.
	 * @param showIcons	True to show icons on the menu, False otherwise.
	 * @return	A PopupMenu object ready for use. Just need to call show().
	 */
	public static PopupMenu buildFeedContextMenu(final Context context, View anchorView, final Feed feed, boolean showIcons)
	{
		final PopupMenu actionMenu = new PopupMenu(context, anchorView);
		MenuInflater inflater = actionMenu.getMenuInflater();
		inflater.inflate(R.menu.feed_menu, actionMenu.getMenu());
	   
		actionMenu.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				switch (item.getItemId()) 
				{
			        case R.id.menu_feed_markAsListened:
			        	actionMenu.dismiss();
			        	((MainActivity)context).helper.markAsListened(feed);
			            return true;
			        case R.id.menu_feed_delete:
			        	actionMenu.dismiss();
			        	((MainActivity)context).helper.deleteFeed(feed.getFeedId());
				    	return true;
				}
				return true;
			}
		});
		if (showIcons) forceShowIcons(actionMenu);
		return actionMenu;
	}
	
	public static PopupMenu buildPlaylistContextMenu(final Context context, View anchorView, final Episode ep, boolean showIcons)
	{
		final PopupMenu actionMenu = new PopupMenu(context, anchorView);
		MenuInflater inflater = actionMenu.getMenuInflater();
		inflater.inflate(R.menu.playlist_row_menu, actionMenu.getMenu());
		
		if (!NetworkUtils.isOnline(context)) 	//Phone is not connected
		{
			if (ep.isDownloaded())	//Episode is downloaded
			{
				actionMenu.getMenu().findItem(R.id.menu_playlist_available_offline).setEnabled(false);	//Since the file is downloaded,, we show the Delete File row instead.
				actionMenu.getMenu().findItem(R.id.menu_playlist_delete).setVisible(true);
			}
			else					//Episode is not downloaded
			{
				actionMenu.getMenu().findItem(R.id.menu_playlist_available_offline).setEnabled(false);	//If the phone isn't connected to the internet, you can't download anything, so we disable the stream option.
				actionMenu.getMenu().findItem(R.id.menu_playlist_delete).setVisible(true);
			}
		}
		else									//Phone is connected
		{
			if (ep.isDownloaded())	//Episode is downloaded
			{
				actionMenu.getMenu().findItem(R.id.menu_playlist_available_offline).setEnabled(false);	//Since the file is downloaded,, we show the Delete File row instead.
				actionMenu.getMenu().findItem(R.id.menu_playlist_delete).setVisible(true);
			}
			else					//Episode is not downloaded
			{
				actionMenu.getMenu().findItem(R.id.menu_playlist_available_offline).setEnabled(true);
				actionMenu.getMenu().findItem(R.id.menu_playlist_delete).setVisible(true);	//Can't delete a file that doesn't exist, so we remove the Delete File row.
			}
		}
		
		actionMenu.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				switch (item.getItemId()) 
				{
			        case R.id.menu_playlist_available_offline:
			        	actionMenu.dismiss();
			        	((MainActivity)context).downloadEpisode(ep.getFeedId(), ep.getEpisodeId());
			            return true;
			        case R.id.menu_playlist_delete:
			        	actionMenu.dismiss();
			        	((MainActivity)context).podService.deletingEpisode(ep.getEpisodeId());
			        	if (ep.isDownloaded())
			        		((MainActivity)context).helper.deleteEpisodeFile(ep.getFeedId(), ep.getEpisodeId());
			        	((MainActivity)context).helper.playlistAdapter.removeFromPlaylist(ep);
				    	return true;
			        case R.id.menu_playlist_startOver:
			        	actionMenu.dismiss();
			        	ep.setElapsedTime(0);
			        	((MainActivity)context).helper.updateEpisodeNoRefresh(ep);
			        	((MainActivity)context).podService.playEpisode(ep);
			        	return true;
				}
				return true;
			}
		});
		
		if (showIcons) forceShowIcons(actionMenu);
		return actionMenu;
	}
	
	/**
	 * Show a context menu for an Episode object.
	 * @param context	App context.
	 * @param anchorView	The view that was clicked. The PopupMenu will appear near this View.
	 * @param ep Episode to perform actions on.
	 * @param showIcons	True to show icons on the menu, False otherwise.
	 * @return	A PopupMenu object ready for use. Just need to call show().
	 */
	public static PopupMenu buildEpisodeContextMenu(final Context context, View anchorView, final Episode ep, boolean showIcons)
	{
		final PopupMenu actionMenu = new PopupMenu(context, anchorView);
		MenuInflater inflater = actionMenu.getMenuInflater();
		inflater.inflate(R.menu.episode_menu, actionMenu.getMenu());
		
		if (!NetworkUtils.isOnline(context)) 	//Phone is not connected
		{
			if (ep.isDownloaded())	//Episode is downloaded
			{
				actionMenu.getMenu().findItem(R.id.menu_episode_available_offline).setVisible(false);	//Since the file is downloaded,, we show the Delete File row instead.
				actionMenu.getMenu().findItem(R.id.menu_episode_deleteFile).setVisible(true);
			}
			else					//Episode is not downloaded
			{
				actionMenu.getMenu().findItem(R.id.menu_episode_available_offline).setEnabled(false);	//If the phone isn't connected to the internet, you can't download anything, so we disable the stream option.
				actionMenu.getMenu().findItem(R.id.menu_episode_play_now).setEnabled(false);	//If the phone isn't connected to the internet, you can't stream anything, so we disable the stream option.
				actionMenu.getMenu().findItem(R.id.menu_episode_deleteFile).setVisible(false);	//Can't delete a file that doesn't exist, so we remove the Delete File row.
			}
		}
		else									//Phone is connected
		{
			if (ep.isDownloaded())	//Episode is downloaded
			{
				actionMenu.getMenu().findItem(R.id.menu_episode_available_offline).setVisible(false);	//Since the file is downloaded,, we show the Delete File row instead.
				actionMenu.getMenu().findItem(R.id.menu_episode_deleteFile).setVisible(true);
			}
			else					//Episode is not downloaded
			{
				actionMenu.getMenu().findItem(R.id.menu_episode_deleteFile).setVisible(false);	//Can't delete a file that doesn't exist, so we remove the Delete File row.
				actionMenu.getMenu().findItem(R.id.menu_episode_available_offline).setVisible(true);
			}
		}

		if ((((MainActivity)context).helper.playlistAdapter.findEpisodeInPlaylist(ep)) != -1) //If file is already in playlist, we disable the add to playlist row.
			actionMenu.getMenu().findItem(R.id.menu_episode_add_playlist).setEnabled(false);
		else
			actionMenu.getMenu().findItem(R.id.menu_episode_add_playlist).setEnabled(true);
		
		if (ep.isListened()) //File has been listened to already, so we can't mark it as listened.
			actionMenu.getMenu().findItem(R.id.menu_episode_markAsListened).setVisible(false); //No need to show "Mark As Listened" alternative.
		else
			actionMenu.getMenu().findItem(R.id.menu_episode_markAsListened).setVisible(true);
	
		actionMenu.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				switch (item.getItemId()) 
				{
			        case R.id.menu_episode_available_offline:
			        	actionMenu.dismiss();
			        	((MainActivity)context).downloadEpisode(ep.getFeedId(), ep.getEpisodeId());
			            return true;
			        case R.id.menu_episode_play_now:
			        	actionMenu.dismiss();
			        	((MainActivity)context).podService.playEpisode(ep);
				    	((MainActivity)context).actionBar.setSelectedNavigationItem(Constants.PLAYER_TAB_POSITION);	//Navigate to the Player Fragment automatically.
			            return true;
			        case R.id.menu_episode_add_playlist:
			        	actionMenu.dismiss();
			        	((MainActivity)context).helper.playlistAdapter.addToPlaylist(ep);
			        	ToastMessages.EpisodeAddedToPlaylist(context).show();
			        	return true;
			        case R.id.menu_episode_deleteFile:
			        	actionMenu.dismiss();
			        	((MainActivity)context).podService.deletingEpisode(ep.getEpisodeId());
				    	((MainActivity)context).helper.deleteEpisodeFile(ep.getFeedId(), ep.getEpisodeId());
				    	return true;
			        case R.id.menu_episode_markAsListened:
			        	actionMenu.dismiss();
			        	((MainActivity)context).helper.markAsListenedAsync(ep);
			        	return true;
				}
				return true;
			}
		});
		if (showIcons) forceShowIcons(actionMenu);
		return actionMenu;
	}
	
	/**
	 * A hacky method of forcing a PopupMenu to show icons.
	 * Uses reflection which might break on Android source changes.
	 * @param popup The PopupMenu that should show icons.
	 */
	public static void forceShowIcons(PopupMenu popup)
	{
		try 
		{
		    Field[] fields = popup.getClass().getDeclaredFields();
		    for (Field field : fields) 
		    {
		        if ("mPopup".equals(field.getName())) 
		        {
		            field.setAccessible(true);
		            Object menuPopupHelper = field.get(popup);
		            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
		            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
		            setForceIcons.invoke(menuPopupHelper, true);
		            break;
		        }
		    }
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
		}
	}

}
