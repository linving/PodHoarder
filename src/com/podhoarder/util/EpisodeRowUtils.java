package com.podhoarder.util;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.object.Episode;
import com.podhoarder.util.ViewHolders.EpisodeRowViewHolder;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

public class EpisodeRowUtils
{
	private static float ALPHA_LISTENED = .65f;
	private static float ALPHA_NORMAL = 1f;

    public static Menu getMultiSelectionMenu(Context context, ActionMode mode, Menu menu) {
        // Inflate the menu for the CAB
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu_episode, menu);
        menu.findItem(R.id.menu_episode_playnow).setVisible(false);

        if (!NetworkUtils.isOnline(context))
            menu.findItem(R.id.menu_episode_available_offline).setVisible(false);

        return menu;
    }

    public static PopupMenu getContextMenu(final Context context, View v, final Episode ep) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.getMenuInflater().inflate(R.menu.contextual_menu_episode, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_episode_markAsListened:
                        List<Episode> eps = new ArrayList<Episode>();
                        eps.add(ep);
                        ((LibraryActivityManager) ((LibraryActivity) context).mDataManager).markAsListened(eps);
                        break;
                    case R.id.menu_episode_add_playlist:
                        if (((LibraryActivity) context).mDataManager.findEpisodeInPlaylist(ep) == -1)
                            ((LibraryActivity) context).mDataManager.addToPlaylist(ep);    //We only add items that aren't already in the playlist.
                        break;
                    case R.id.menu_episode_available_offline:
                        ((LibraryActivity) context).mDataManager.DownloadManager().downloadEpisode(ep);
                        break;
                    case R.id.menu_episode_playnow:
                        if (ep.isDownloaded() || NetworkUtils.isOnline(context))
                            ((LibraryActivity) context).getPlaybackService().playEpisode(ep);
                        else
                            ToastMessages.PlaybackFailed(context).show();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        return popupMenu;
    }

	public static void setRowListened(ViewGroup row, boolean listened)
	{
		recursiveLoopChildren(row,listened);
	}
	
	public static void setRowListened(EpisodeRowViewHolder row, boolean listened)
	{
/*		if (listened)
		{
			row.title.setAlpha(ALPHA_LISTENED);
			row.age.setAlpha(ALPHA_LISTENED);
		}
		else
		{
			row.title.setAlpha(ALPHA_NORMAL);
			row.age.setAlpha(ALPHA_NORMAL);
		}*/
	}


	public static void setRowIndicator(Context ctx, EpisodeRowViewHolder row, Episode ep)
	{
		if (ep.isNew())
		{
            row.icon.setAlpha(1f);
            row.icon.setBackgroundResource(R.drawable.list_icon_outline_new);
			if (NetworkUtils.isOnline(ctx))
			{
				row.icon.setImageResource(R.drawable.ic_cloud_white_24dp);
				//row.episodeTitle.setTypeface(row.episodeTitle.getTypeface(), Typeface.BOLD);
//				row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_new));
//				if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_new));
			}
			else
			{
				row.icon.setImageResource(R.drawable.ic_cloud_off_white_24dp);
				//row.indicator.setStatus(Status.UNAVAILABLE);
//				row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
//				if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
			}
		} 
		else
		{
            row.icon.setAlpha(.26f);
            row.icon.setBackgroundResource(R.drawable.list_icon_outline);
			if (ep.isDownloaded())
			{
				row.icon.setImageResource(R.drawable.ic_folder_white_24dp);
//				row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_downloaded));
//				if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_downloaded));
			}
			else
			{
				if (NetworkUtils.isOnline(ctx))	//Phone has internet access, streaming is possible.
				{
					row.icon.setImageResource(R.drawable.ic_cloud_white_24dp);
//					row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_stream));
//					if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_stream));
				}
				else	//Device does not have internet access, so all streaming episodes should be set to unavailable.
				{
					row.icon.setImageResource(R.drawable.ic_cloud_off_white_24dp);
//					row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
//					if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
				}
			}
		}
		
	}

	private static void recursiveLoopChildren(ViewGroup parent, boolean listened) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            final View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                recursiveLoopChildren((ViewGroup) child, listened);
                // DO SOMETHING WITH VIEWGROUP, AFTER CHILDREN HAS BEEN LOOPED
            } else {
                if (child != null) {
                    // DO SOMETHING WITH VIEW
                	if (listened) child.setAlpha(ALPHA_LISTENED);
                	else	child.setAlpha(ALPHA_NORMAL);
                }
            }
        }
    }
}
