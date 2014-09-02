package com.podhoarder.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.object.Episode;
import com.podhoarderproject.podhoarder.R;

public class PopupMenuUtils
{
	
	
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
			        	((MainActivity)context).downloadEpisode(ep);
			            return true;
			        case R.id.menu_playlist_delete:
			        	actionMenu.dismiss();
			        	((MainActivity)context).podService.deletingEpisode(ep.getEpisodeId());
			        	if (ep.isDownloaded())
			        		((MainActivity)context).helper.deleteEpisodeFile(ep);
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
