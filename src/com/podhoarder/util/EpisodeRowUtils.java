package com.podhoarder.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.podhoarder.object.Episode;
import com.podhoarder.util.ViewHolders.EpisodeRowViewHolder;
import com.podhoarderproject.podhoarder.R;

public class EpisodeRowUtils
{
	private static float ALPHA_LISTENED = .65f;
	private static float ALPHA_NORMAL = 1f;

	public static void setRowListened(ViewGroup row, boolean listened)
	{
		recursiveLoopChildren(row,listened);
	}
	
	public static void setRowListened(EpisodeRowViewHolder row, boolean listened)
	{
		if (listened)
		{
			row.feedImage.setAlpha(ALPHA_LISTENED);
			row.title.setAlpha(ALPHA_LISTENED);
			row.age.setAlpha(ALPHA_LISTENED);
		}
		else
		{
			row.feedImage.setAlpha(ALPHA_NORMAL);
			row.title.setAlpha(ALPHA_NORMAL);
			row.age.setAlpha(ALPHA_NORMAL);
		}
	}
	
	/*public static void setRowListened(PlaylistRowViewHolder row, boolean listened)
	{
		if (listened)
		{
			row.feedImage.setAlpha(ALPHA_LISTENED);
			row.title.setAlpha(ALPHA_LISTENED);
			row.feedTitle.setAlpha(ALPHA_LISTENED);
			row.age.setAlpha(ALPHA_LISTENED);
			row.timeListened.setAlpha(ALPHA_LISTENED);
		}
		else
		{
			row.feedImage.setAlpha(ALPHA_NORMAL);
			row.title.setAlpha(ALPHA_NORMAL);
			row.feedTitle.setAlpha(ALPHA_NORMAL);
			row.age.setAlpha(ALPHA_NORMAL);
			row.timeListened.setAlpha(ALPHA_NORMAL);
		}
	}*/

	public static void setRowIndicator(Context ctx, EpisodeRowViewHolder row, Episode ep)
	{
		if (ep.isNew())
		{
			if (NetworkUtils.isOnline(ctx))
			{
				row.arrow.setImageResource(R.drawable.ic_new_releases_pink_24dp);
				row.arrow.setAlpha(1f);
				//row.episodeTitle.setTypeface(row.episodeTitle.getTypeface(), Typeface.BOLD);
//				row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_new));
//				if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_new));
			}
			else
			{
				row.arrow.setImageResource(R.drawable.ic_info_outline_black_24dp);
				row.arrow.setAlpha(.54f);
				//row.indicator.setStatus(Status.UNAVAILABLE);
//				row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
//				if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_unavailable));
			}
		} 
		else
		{
			if (ep.isDownloaded())
			{
				row.arrow.setImageResource(R.drawable.ic_info_outline_black_24dp);
				row.arrow.setAlpha(.54f);
//				row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_downloaded));
//				if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_downloaded));
			}
			else
			{
				if (NetworkUtils.isOnline(ctx))	//Phone has internet access, streaming is possible.
				{
					row.arrow.setImageResource(R.drawable.ic_info_outline_black_24dp);
					row.arrow.setAlpha(.54f);
//					row.indicator.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_stream));
//					if (row.indicatorExtension != null) row.indicatorExtension.setBackgroundColor(ctx.getResources().getColor(R.color.indicator_stream));
				}
				else	//Device does not have internet access, so all streaming episodes should be set to unavailable.
				{
					row.arrow.setImageResource(R.drawable.ic_info_outline_black_24dp);
					row.arrow.setAlpha(.54f);
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
