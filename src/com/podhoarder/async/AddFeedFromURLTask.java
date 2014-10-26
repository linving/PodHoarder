package com.podhoarder.async;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import com.podhoarder.adapter.GridAdapter;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.DataParser;
import com.podhoarder.util.ToastMessages;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Task used for parsing an XML file that contains podcast data.
 * @author Emil
 *
 */
public class AddFeedFromURLTask extends AsyncTask<String, Integer, Feed>
{
    private static          String      LOG_TAG = "com.podhoarder.async.AddFeedFromURLTask";

    private Feed newFeed;
    private int progressPercent;
    private String title, link, description, category, author, img;

    private Context mContext;
    private FeedDBHelper fDbH;
    private GridAdapter mFeedsGridAdapter;

    public AddFeedFromURLTask(Context mContext, FeedDBHelper fDbH, GridAdapter mFeedsGridAdapter)
    {
        this.mContext = mContext;
        this.fDbH = fDbH;
        this.mFeedsGridAdapter = mFeedsGridAdapter;
    }

    protected Feed doInBackground(String... urls)
    {
        newFeed = new Feed();
        double percentIncrement;
        List<Episode> eps = new ArrayList<Episode>();
        try
        {

            // Set the url
            URL url = new URL(urls[0]);

            // Setup the connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Connect
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                // Retreive the XML from the URL
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc;
                doc = db.parse(url.openStream());
                doc.getDocumentElement().normalize();

                // This is the root node of each section you want to parse
                NodeList itemLst = doc.getElementsByTagName("item");
                NodeList itemLst2 = doc.getElementsByTagName("channel");

                this.title = 		DataParser.parsePodcastTitle(itemLst2);
                this.link = 		urls[0];
                this.description = 	DataParser.parsePodcastDescription(itemLst2);
                this.author = 		DataParser.parsePodcastAuthor(itemLst2);
                this.category = 	DataParser.parsePodcastCategory(itemLst2);
                this.img = 			DataParser.parsePodcastImageLocation(itemLst2);

                percentIncrement = 10.0;
                publishProgress((int) percentIncrement);

                // Loop through the XML passing the data to the arrays
                percentIncrement = (itemLst.getLength() / 90);
                for (int i = 0; i < itemLst.getLength(); i++)
                {

                    Episode ep = DataParser.parseNewEpisode(itemLst.item(i));
                    publishProgress((int) percentIncrement);
                    eps.add(ep);
                }
            }

        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (DOMException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        } catch (SAXException e)
        {
            e.printStackTrace();
        } catch (SQLiteConstraintException e)
        {
            cancel(true);
        }
        this.newFeed = new Feed(this.title, this.author, this.description,
                this.link, this.category, this.img, false, eps, mContext);

        //Insert all the data into the db.
        try
        {
            this.newFeed = fDbH.insertFeed(this.newFeed);
            if (this.newFeed != null)
            {
                this.newFeed.getFeedImage().setImageDownloadListener(mFeedsGridAdapter);
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
            mFeedsGridAdapter.resetLoading();
            ToastMessages.AddFeedFailed(mContext).show();
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
}
