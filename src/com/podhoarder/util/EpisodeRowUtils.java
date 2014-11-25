package com.podhoarder.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.preference.PreferenceManager;
import android.support.v7.graphics.Palette;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.adapter.EpisodesListAdapter;
import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.fragment.ListFragment;
import com.podhoarder.object.Episode;
import com.podhoarder.view.PieProgressDrawable;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

public class EpisodeRowUtils
{
	private static float ALPHA_LISTENED = .65f;
	private static float ALPHA_NORMAL = 1f;

    public static Menu getMultiSelectionMenu(Context context, ActionMode mode, Menu menu) {
        // Inflate the secondaryAction for the CAB
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu_episodes, menu);

        if (!NetworkUtils.isOnline(context))
            menu.findItem(R.id.menu_episode_available_offline).setVisible(false);

        return menu;
    }

    public static PopupMenu getContextMenu(final Context context, final View v, final Episode ep) {
        PopupMenu popupMenu = new PopupMenu(context, v);


        popupMenu.getMenuInflater().inflate(R.menu.contextual_menu_episode, popupMenu.getMenu());

        configureMenu(context,popupMenu.getMenu(),ep);

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
                        ((LibraryActivity) context).mDataManager.addToPlaylist(ep);
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
                    case R.id.menu_episode_delete_file:
                        if (ep.isDownloaded())
                            ((LibraryActivity) context).mDataManager.deleteEpisodeFile(ep);
                        break;
                    case R.id.menu_episode_show_info:
                        ((ListFragment)((LibraryActivity) context).mCurrentFragment).goToEpisodeFragment(ep);
                    default:
                        break;
                }
                return true;
            }
        });
        return popupMenu;
    }

    public static void configureMenu(Context context, Menu menu, final Episode ep) {

        if (ep.isDownloaded() || !NetworkUtils.isOnline(context)) {
            menu.findItem(R.id.menu_episode_available_offline).setVisible(false);
        }
        else {
            menu.findItem(R.id.menu_episode_delete_file).setVisible(false);
        }
        int pref = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_defaultEpisodeAction", "1"));
        if (pref == 1) {
            menu.findItem(R.id.menu_episode_show_info).setVisible(false);
        }
        else {
            menu.findItem(R.id.menu_episode_playnow).setVisible(false);
        }

    }

	public static void setRowIndicator(Context ctx, final EpisodesListAdapter.EpisodeRowViewHolder row, Episode ep)
	{
        Palette p = ((LibraryActivity)ctx).mDataManager.getFeed(ep.getFeedId()).getFeedImage().palette();
        row.progressDrawable = new PieProgressDrawable(ctx.getResources().getColor(R.color.indicator_default),p.getVibrantColor(Color.BLACK));
        row.icon.setBackground(row.progressDrawable);

        if (((BaseActivity)ctx).mDataManager.DownloadManager().getDownloadProgress(ep.getEpisodeId()) != -1) {  //This means that the Episode is currently downloading. We should show an animated download arrow as indicator.

            row.progressDrawable.setLevel(0);
            row.icon.setImageResource(android.R.drawable.stat_sys_download);
            row.icon.post(new Runnable() {
                @Override
                public void run() {
                    AnimationDrawable frameAnimation =
                            (AnimationDrawable) row.icon.getDrawable();
                    frameAnimation.start();
                }
            });
        }

        else {  //Episode isn't downloading. We should show current file status.
            if (ep.isNew())
            {
                row.progressDrawable.setBackgroundColor(ctx.getResources().getColor(R.color.notificationBackground));
                row.progressDrawable.setLevel(0);
                if (NetworkUtils.isOnline(ctx))
                {
                    row.icon.setImageResource(R.drawable.ic_cloud_white_24dp);
                }
                else
                {
                    row.icon.setImageResource(R.drawable.ic_cloud_off_white_24dp);
                }
            }
            else
            {
                row.progressDrawable.setLevel(ep.getProgress());
                if (ep.isDownloaded())
                {
                    row.icon.setImageResource(R.drawable.ic_folder_white_24dp);
                }
                else
                {
                    if (NetworkUtils.isOnline(ctx))	//Phone has internet access, streaming is possible.
                    {
                        row.icon.setImageResource(R.drawable.ic_cloud_white_24dp);
                    }
                    else	//Device does not have internet access, so all streaming episodes should be set to unavailable.
                    {
                        row.icon.setImageResource(R.drawable.ic_cloud_off_white_24dp);
                    }
                }
            }
        }
        row.icon.invalidate();
	}
}
