package com.podhoarder.async;

/**
 * Created by Emil on 2014-10-20.
 */

import android.os.AsyncTask;

import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.PodcastHelper;

import java.io.File;

/**
 * AsyncTask for marking all the Episodes of the selected Feed(s) as listened.
 */
public class MarkFeedsAsListenedTask extends AsyncTask<Feed, Void, Void>
{
    private PodcastHelper mPodcastHelper;
    private EpisodeDBHelper mEPH;

    public MarkFeedsAsListenedTask(PodcastHelper mPodcastHelper, EpisodeDBHelper eph)
    {
        this.mPodcastHelper = mPodcastHelper;
        this.mEPH = eph;
    }

    @Override
    protected void onPostExecute(Void v)
    {
        mPodcastHelper.refreshContent();
    }

    @Override
    protected Void doInBackground(Feed... params)
    {
        for (Feed feed : params)
        {
            for (Episode ep : feed.getEpisodes())
            {
                if (ep.getTotalTime() > 100) ep.setElapsedTime(ep.getTotalTime());	//If totalTime is more than 100 (ms) that means there's a "real" time stored already. So we set elapsedTime to totalTime.
                else																//Otherwise we make up the value 100 (easy when dealing with percent)
                {
                    ep.setTotalTime(100);
                    ep.setElapsedTime(100);
                }
                if (ep.isDownloaded())
                {
                    File file = new File(ep.getLocalLink());
                    if (file.delete())
                    {
                        ep.setLocalLink("");
                    }
                }
            }
            mEPH.bulkUpdateEpisodes(feed.getEpisodes());
        }

        return null;
    }
}