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
		
		if (!NetworkUtils.isOnline(context)) 
		{
			actionMenu.getMenu().findItem(R.id.menu_episode_stream).setEnabled(false);	//If the phone isn't connected to the internet, you can't stream anything, so we disable the stream option.
			actionMenu.getMenu().findItem(R.id.menu_episode_download).setEnabled(false);//If the phone isn't connected to the internet, we can't download anything, so we disable the download option.
		}
		
		if (!ep.getLocalLink().isEmpty())	//ep contains a file link, which means that the file is downloaded.
		{
			actionMenu.getMenu().removeItem(R.id.menu_episode_download);	//The file is already downloaded, so we remove the Download File row.
		}
		else //File is not downloaded.
		{
			actionMenu.getMenu().removeItem(R.id.menu_episode_deleteFile);	//Can't delete a file that doesn't exist, so we remove the Delete File row.
			actionMenu.getMenu().removeItem(R.id.menu_episode_playFile);	//Can't play a file that doesn't exist, so we remove the Play File row.
		}
	   
		if (ep.isListened()) //File has been listened to already, so we can't mark it as listened.
			actionMenu.getMenu().removeItem(R.id.menu_episode_markAsListened); //No need to show "Mark As Listened" alternative.
	
		actionMenu.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				switch (item.getItemId()) 
				{
			        case R.id.menu_episode_download:
			        	actionMenu.dismiss();
			        	((MainActivity)context).downloadEpisode(ep.getFeedId(), ep.getEpisodeId());
			            return true;
			        case R.id.menu_episode_stream:
			        	actionMenu.dismiss();
			        	((MainActivity)context).podService.streamEpisode(ep);
				    	((MainActivity)context).getActionBar().setSelectedNavigationItem(Constants.PLAYER_TAB_POSITION);	//Navigate to the Player Fragment automatically.
			            return true;
			        case R.id.menu_episode_playFile:
			        	actionMenu.dismiss();
			        	((MainActivity)context).podService.startEpisode(ep);
				    	((MainActivity)context).getActionBar().setSelectedNavigationItem(Constants.PLAYER_TAB_POSITION);	//Navigate to the Player Fragment automatically.
				    	return true;
			        case R.id.menu_episode_deleteFile:
			        	actionMenu.dismiss();
			        	((MainActivity)context).podService.deletingEpisode(ep.getEpisodeId());
				    	((MainActivity)context).helper.deleteEpisode(ep.getFeedId(), ep.getEpisodeId());
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
