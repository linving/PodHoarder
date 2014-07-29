package com.podhoarderproject.podhoarder.util;

/**
 * @author Emil Almrot
 * 2013-03-15
 */
import java.util.List;

import android.content.Context;

public class Feed
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.Feed";

	private int feedId;
	private String title;
	private String author;
	private String description;
	private String link;
	private String category;
	private FeedImage feedImage;
	private List<Episode> episodes;
	private Context ctx;

	public Feed(int feedId, String title, String author, String description,
			String link, String category, String imageURL, boolean shouldCreateImage, List<Episode> episodes, 
			Context ctx)
	{
		this.feedId = feedId;
		this.title = title;
		this.author = author;
		this.description = description;
		this.link = link;
		this.category = category;
		this.episodes = episodes;
		this.ctx = ctx;
		this.feedImage = new FeedImage(this.feedId, imageURL, shouldCreateImage, this.ctx);
	}
	
	public Feed(String title, String author, String description,
			String link, String category, String imageURL, boolean shouldCreateImage, List<Episode> episodes, 
			Context ctx)
	{
		this.title = title;
		this.author = author;
		this.description = description;
		this.link = link;
		this.category = category;
		this.episodes = episodes;
		this.ctx = ctx;
		this.feedImage = new FeedImage(this.feedId, imageURL, shouldCreateImage, this.ctx);
	}

	public Feed()
	{
		this.feedId = 0;
		this.title = "";
		this.author = "";
		this.description = "";
		this.link = "";
		this.category = "";
		this.episodes = null;
		this.ctx = null;
		this.feedImage = null;
	}

	/**
	 * Returns feedId property.
	 * @return feedId
	 */
	public int getFeedId()
	{
		return feedId;
	}

	/**
	 * Returns title property.
	 * @return Title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * Sets the title property.
	 * @param title Title to be set.
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Returns author property.
	 * @return Author
	 */
	public String getAuthor()
	{
		return author;
	}

	/**
	 * Sets the author property.
	 * @param author A string object containing the authors name.
	 */
	public void setAuthor(String author)
	{
		this.author = author;
	}

	/**
	 * Returns description property.
	 * @return Description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Sets the description parameter.
	 * @param description A string object containing a short description of the Feed.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * Returns the URL of the Feed.
	 * @return URL supplied by the feed creator.
	 */
	public String getLink()
	{
		return link;
	}

	/**
	 * Sets the link property.
	 * @param link A string pointing to an URL associated with the Feed.
	 */
	public void setLink(String link)
	{
		this.link = link;
	}

	/**
	 * Returns category property.
	 * @return Category
	 */
	public String getCategory()
	{
		return category;
	}

	/**
	 * Sets the category property.
	 * @param category A string explaining what category the feed belongs to.
	 */
	public void setCategory(String category)
	{
		this.category = category;
	}

	/**
	 * Returns FeedImage property.
	 * @return FeedImage object containing the retrieved image.
	 */
	public FeedImage getFeedImage()
	{
		return feedImage;
	}

	/**
	 * Sets the FeedImage property.
	 * @param feedImage A FeedImage object.
	 */
	public void setFeedImage(FeedImage feedImage)
	{
		this.feedImage = feedImage;
	}

	/**
	 * Returns a list of all the Episodes.
	 * @return A List<Episode>
	 */
	public List<Episode> getEpisodes()
	{
		return episodes;
	}

	/**
	 * Sets the episodes property.
	 * @param episodes A List<zEpisode>.
	 */
	public void setEpisodes(List<Episode> episodes)
	{
		this.episodes = episodes;
	}
	
	/**
	 * Returns the number of new (unlistened) Episodes.
	 * @return The number of new (unlistened) Episodes.
	 */
	public int getNewEpisodesCount()
	{
		int retVal = 0;
		for (Episode ep : this.episodes)
		{
			if (ep.isNew()) retVal++;
		}
		return retVal;
	}

	/**
	 * Returns context property.
	 * @return Context
	 */
	public Context getCtx()
	{
		return ctx;
	}

	/**
	 * Sets the context property.
	 * @param ctx Context object.
	 */
	public void setCtx(Context ctx)
	{
		this.ctx = ctx;
	}
}

//TODO: Implement Comparator for Feeds (order by name)