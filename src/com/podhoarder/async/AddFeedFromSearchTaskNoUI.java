package com.podhoarder.async;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.object.FeedImage;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.util.DataParser;
import com.podhoarder.util.ToastMessages;

import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-10-24.
 */
public class AddFeedFromSearchTaskNoUI  extends AsyncTask<SearchResultRow, Integer, Feed> implements FeedImage.ImageDownloadListener {
    private static          String      LOG_TAG = "com.podhoarder.async.AddFeedFromSearchTask";

    private Feed newFeed;
    private int progressPercent;

    private Context mContext;
    private FeedDBHelper fDbH;

    public AddFeedFromSearchTaskNoUI(Context mContext, FeedDBHelper fDbH)
    {
        this.mContext = mContext;
        this.fDbH = fDbH;
    }

    protected Feed doInBackground(SearchResultRow... result)
    {
        SearchResultRow data = result[0];
        newFeed = new Feed();
        double percentIncrement;
        List<Episode> eps = new ArrayList<Episode>();
        try
        {

            // This is the root node of each section you want to parse
            NodeList itemLst = data.getXml().getElementsByTagName("item");

            // Loop through the XML passing the data to the arrays
            percentIncrement = (itemLst.getLength() / 100);
            for (int i = 0; i < itemLst.getLength(); i++)
            {

                Episode ep = DataParser.parseNewEpisode(itemLst.item(i));
                publishProgress((int) percentIncrement);
                if (ep != null)
                    eps.add(ep);
            }

        }
        catch (SQLiteConstraintException e)
        {
            cancel(true);
        }
        this.newFeed = new Feed(data.getTitle(), data.getAuthor(), data.getDescription(),
                data.getLink(), data.getCategory(), data.getImageUrl(), false, eps, mContext);

        //Insert all the data into the db.
        try
        {
            this.newFeed = fDbH.insertFeed(this.newFeed);
            if (this.newFeed != null)
            {
                this.newFeed.getFeedImage().setImageDownloadListener(this);
                return newFeed;
            }
            else
            {
                return null;
            }
        }
        catch (SQLiteConstraintException e)
        {
            Log.e(LOG_TAG,
                    "NOT A UNIQUE LINK. FEED ALREADY EXISTS IN THE DATABASE?");
            throw e;
        }

    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Feed result)
    {
        if (result != null)
            Log.i(LOG_TAG, "Added Feed: " + result.getTitle());
        else
        {
            ToastMessages.AddFeedFailed(mContext);
            Log.e(LOG_TAG, "Couldn't Add Feed!");
        }
    }

    @SuppressWarnings("unused")
    public int getProgressPercent()
    {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent)
    {
        this.progressPercent = progressPercent;
    }

    @Override
    public void downloadFinished(int feedId) {
        ((MainActivity)mContext).firstFeedAdded();
    }
}
