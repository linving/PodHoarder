package com.podhoarder.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.podhoarder.object.Episode;
import com.podhoarder.util.ViewHolders.EpisodeRowViewHolder;
import com.podhoarder.util.ViewHolders.FeedDetailsRowViewHolder;
import com.podhoarder.util.ViewHolders.LatestEpisodesRowViewHolder;
import com.podhoarder.util.ViewHolders.PlaylistRowViewHolder;
import com.podhoarderproject.podhoarder.R;

public class EpisodeRowUtils
{
	private static float ALPHA_LISTENED = .65f;
	private static float ALPHA_NORMAL = 1f;

	public static void setRowListened(ViewGroup row, boolean listened)
	{
		recursiveLoopChildren(row,listened);
	}
	
	public static void setRowListened(FeedDetailsRowViewHolder row, boolean listened)
	{
		if (listened)
		{
			row.feedImage.setAlpha(ALPHA_LISTENED);
			row.episodeTitle.setAlpha(ALPHA_LISTENED);
			row.episodeAge.setAlpha(ALPHA_LISTENED);
		}
		else
		{
			row.feedImage.setAlpha(ALPHA_NORMAL);
			row.episodeTitle.setAlpha(ALPHA_NORMAL);
			row.episodeAge.setAlpha(ALPHA_NORMAL);
		}
	}
	
	public static void setRowListened(PlaylistRowViewHolder row, boolean listened)
	{
		if (listened)
		{
			row.feedImage.setAlpha(ALPHA_LISTENED);
			row.episodeTitle.setAlpha(ALPHA_LISTENED);
			row.feedTitle.setAlpha(ALPHA_LISTENED);
			row.episodeAge.setAlpha(ALPHA_LISTENED);
			row.timeListened.setAlpha(ALPHA_LISTENED);
		}
		else
		{
			row.feedImage.setAlpha(ALPHA_NORMAL);
			row.episodeTitle.setAlpha(ALPHA_NORMAL);
			row.feedTitle.setAlpha(ALPHA_NORMAL);
			row.episodeAge.setAlpha(ALPHA_NORMAL);
			row.timeListened.setAlpha(ALPHA_NORMAL);
		}
	}
	
	public static void setRowListened(LatestEpisodesRowViewHolder row, boolean listened)
	{
		if (listened)
		{
			row.feedImage.setAlpha(ALPHA_LISTENED);
			row.episodeTitle.setAlpha(ALPHA_LISTENED);
			row.feedTitle.setAlpha(ALPHA_LISTENED);
			row.episodeAge.setAlpha(ALPHA_LISTENED);
		}
		else
		{
			row.feedImage.setAlpha(ALPHA_NORMAL);
			row.episodeTitle.setAlpha(ALPHA_NORMAL);
			row.feedTitle.setAlpha(ALPHA_NORMAL);
			row.episodeAge.setAlpha(ALPHA_NORMAL);
		}
		
	}

	public static void setRowIndicator(Context ctx, EpisodeRowViewHolder row, Episode ep)
	{
		if (ep.isNew())
		{
			if (NetworkUtils.isOnline(ctx))
			{
				row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_new));
				if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_new));
			}
			else
			{
				row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
				if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
			}
		} 
		else
		{
			if (ep.isDownloaded())
			{
				row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_downloaded));
				if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_downloaded));
			}
			else
			{
				if (NetworkUtils.isOnline(ctx))	//Phone has internet access, streaming is possible.
				{
					row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_stream));
					if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_stream));
				}
				else	//Device does not have internet access, so all streaming episodes should be set to unavailable.
				{
					row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
					if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
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
                	if (listened && child.getId() != R.id.row_indicator && child.getId() != R.id.row_indicator_extension) child.setAlpha(ALPHA_LISTENED);
                	else	child.setAlpha(ALPHA_NORMAL);
                }
            }
        }
    }
}
