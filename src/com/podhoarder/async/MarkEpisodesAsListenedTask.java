package com.podhoarder.async;

/**
 * Created by Emil on 2014-10-20.
 */

import android.os.AsyncTask;

import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.db.EpisodeDBHelper;
import com.podhoarder.object.Episode;

import java.io.File;
import java.util.List;

/**
 * AsyncTask for marking the chosen Episodes as listened.
 */
public class MarkEpisodesAsListenedTask extends AsyncTask<List<Episode>, Void, Void>
{
    private LibraryActivityManager mPodcastHelper;
    private EpisodeDBHelper mEPH;

    public MarkEpisodesAsListenedTask(LibraryActivityManager mPodcastHelper, EpisodeDBHelper eph)
    {
        this.mPodcastHelper = mPodcastHelper;
        this.mEPH = eph;
    }

    @Override
    protected void onPostExecute(Void v)
    {
        mPodcastHelper.reloadListData();
    }

    @Override
    protected Void doInBackground(List<Episode>... params)
    {
        List<Episode> eps = params[0];
        for (Episode ep : eps)
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
            mEPH.updateEpisode(ep);
        }
        return null;
    }
}