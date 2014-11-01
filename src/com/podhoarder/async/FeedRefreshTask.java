package com.podhoarder.async;

/**
 * Created by Emil on 2014-10-20.
 */

import android.content.Context;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.db.FeedDBHelper;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.DataParser;
import com.podhoarder.util.ToastMessages;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
 * Task used for refreshing Feeds.
 * @author Emil
 *
 */
public class FeedRefreshTask extends AsyncTask<List<Feed>, Integer, List<Feed>>
{
    private static          String      LOG_TAG = "com.podhoarder.async.FeedRefreshTask";

    private Context mContext;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FeedDBHelper fDbH;

    private int progressPercent;
    private String title, link, description, category, author, img;
    private List<Feed> feeds;
    private List<String> titles;
    private boolean shouldDelete = false;

    public FeedRefreshTask(Context mContext, FeedDBHelper fDbH, SwipeRefreshLayout swipeRefreshLayout)
    {
        this.mContext = mContext;
        this.fDbH = fDbH;
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    protected void onPreExecute() {
        this.feeds = new ArrayList<Feed>();
        this.titles = new ArrayList<String>();
    }

    protected List<Feed> doInBackground(List<Feed>... param)
    {

        for (Feed currentFeed:param[0])
        {
            double percentIncrement;
            List<Episode> eps = new ArrayList<Episode>();
            try
            {
                // Set the url (you will need to change this to your RSS URL
                URL url = new URL(currentFeed.getLink());

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

                    // This is the root node.
                    NodeList itemLst = doc.getElementsByTagName("item");
                    NodeList itemLst2 = doc.getElementsByTagName("channel");

                    this.title = DataParser.parsePodcastTitle(itemLst2);
                    this.link = currentFeed.getLink();
                    this.description = DataParser.parsePodcastDescription(itemLst2);
                    this.author = DataParser.parsePodcastAuthor(itemLst2);
                    this.category = DataParser.parsePodcastCategory(itemLst2);

                    percentIncrement = 10.0;
                    publishProgress((int) percentIncrement);

                    // Loop through the XML passing the data to the arrays
                    percentIncrement = ((100/param[0].size())/itemLst.getLength());
                    for (int i = 0; i < itemLst.getLength(); i++)							//Parse each Episode.
                    {
                        Episode ep = new Episode();
                        Node item = itemLst.item(i);
                        if (item.getNodeType() == Node.ELEMENT_NODE)
                        {

                            Element ielem = (Element) item;									//Initialise the base element for an Episode.

                            ep.setTitle(DataParser.parseEpisodeTitle(ielem));				//Parse Title

                            if (currentFeed != null)										//Nullcheck
                            {
                                titles.add(ep.getTitle());
                                if (episodeExists(ep.getTitle(), currentFeed.getEpisodes()))//If the current Episode is already in the local list, there's no need to keep processing it.
                                    continue;
                            }

                            ep.setLink(DataParser.parseEpisodeLink(ielem));					//Parse Link

                            ep.setPubDate(DataParser.parseEpisodePubDate(ielem));			//Parse Publish date

                            ep.setDescription(DataParser.parseEpisodeDescription(ielem));	//Parse Description
                        }
                        publishProgress((int) percentIncrement);							//Update AsyncTask progress
                        eps.add(ep);
                    }

                    if (itemLst.getLength() < (eps.size() + currentFeed.getEpisodes().size()))	//this means that there are fewer Episodes in the XML than in our db. We should remove those that aren't in the XML.
                    {
                        shouldDelete = true;
                    }
                    this.img = DataParser.parsePodcastImageLocation(itemLst2);				//We process the image last, because it can potentially take a lot of time and if we discover that we don't need to update anything, this shouldn't be done at all.
                }

            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (DOMException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                cancel(true);
                Log.e(LOG_TAG, e.getMessage());
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

            if (eps.size() > 0 || shouldDelete)	//If we haven't found any new Episodes, there's no need to add the entire Feed object and process it.
            {
                feeds.add(new Feed(this.title, this.author, this.description,
                        this.link, this.category, this.img, false, eps, mContext));
            }
        }

        return feeds;
    }

    @Override
    protected void onCancelled()
    {
        this.swipeRefreshLayout.setRefreshing(false);
        ToastMessages.RefreshFailed(mContext).show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        setProgressPercent(progress[0]);
    }

    protected void onPostExecute(List<Feed> feeds)
    {
        try
        {
            for (Feed newFeed : feeds)
            {
                //Get the Feed that's stored locally with the same Id.
                Feed oldFeed = fDbH.getFeedByURL(newFeed.getLink());
                //If the new Episode list has more Episodes, we need to add the new Episodes to the db.
                for (int i = 0; i<newFeed.getEpisodes().size(); i++)
                {
                    oldFeed.getEpisodes().add(0, newFeed.getEpisodes().get(i));
                }

                if (shouldDelete)	//This is true if we have too many episodes. titles contains all the episode titles from the XML, so we need to remove the Episodes we have that aren't in titles.
                {
                    if (titles != null)
                    {
                        for (int i = 0; i < oldFeed.getEpisodes().size(); i++)
                        {
                            if (!titles.contains(oldFeed.getEpisodes().get(i).getTitle()))
                                oldFeed.getEpisodes().remove(i);
                        }
                    }
                }
                //oldFeed.getFeedImage().imageObject().recycle();
                //Update the Feed with the new Episodes in the db.
                oldFeed = fDbH.updateFeed(oldFeed);
            }
            this.swipeRefreshLayout.setRefreshing(false);
            ((LibraryActivity)mContext).mDataManager.forceReloadListData(true);
            ToastMessages.RefreshSuccessful(mContext).show();
        }
        catch (CursorIndexOutOfBoundsException e)
        {
            Log.e(LOG_TAG,"CursorIndexOutOfBoundsException: Refresh failed.");
            this.swipeRefreshLayout.setRefreshing(false);
            ToastMessages.RefreshFailed(mContext).show();
            cancel(true);
        }
        catch (SQLiteConstraintException e)
        {
            Log.e(LOG_TAG,"SQLiteConstraintException: Refresh failed.");
            this.swipeRefreshLayout.setRefreshing(false);
            ToastMessages.RefreshFailed(mContext).show();
            cancel(true);
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

    /**
     * Does a check to see if an Episode with the specified title is contained within the specified List of Episodes.
     * @param episodeTitle  Title of the Episode to look for.
     * @param episodes List of Episode objects to search through.
     * @return True if the episode exists, False otherwise.
     */
    private static boolean episodeExists(String episodeTitle, List<Episode> episodes)
    {
        for (Episode episode : episodes) {
            if (episodeTitle.equals(episode.getTitle())) return true;
        }
        return false;
    }
}
