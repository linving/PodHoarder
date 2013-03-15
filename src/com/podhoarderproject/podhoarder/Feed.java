package com.podhoarderproject.podhoarder;

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
	private String[] keywords;
	private String link;
	private String category;
	private FeedImage feedImage;
	private List<Episode> episodes;
	private Context ctx;

	public Feed(int feedId, String title, String author, String description,
			String[] keywords, String link, String category, String imageURL,
			List<Episode> episodes, Context ctx)
	{
		this.feedId = feedId;
		this.title = title;
		this.author = author;
		this.description = description;
		this.keywords = keywords;
		this.link = link;
		this.category = category;
		this.episodes = episodes;
		this.ctx = ctx;
		this.feedImage = new FeedImage(this.feedId, imageURL, this.ctx);
	}

	public Feed()
	{
		this.feedId = 0;
		this.title = "";
		this.author = "";
		this.description = "";
		this.keywords = null;
		this.link = "";
		this.category = "";
		this.episodes = null;
		this.ctx = null;
		this.feedImage = null;
	}

	public int getFeedId()
	{
		return feedId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String[] getKeywords()
	{
		return keywords;
	}

	public void setKeywords(String[] keywords)
	{
		this.keywords = keywords;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public FeedImage getFeedImage()
	{
		return feedImage;
	}

	public void setFeedImage(FeedImage feedImage)
	{
		this.feedImage = feedImage;
	}

	public List<Episode> getEpisodes()
	{
		return episodes;
	}

	public void setEpisodes(List<Episode> episodes)
	{
		this.episodes = episodes;
	}

	public Context getCtx()
	{
		return ctx;
	}

	public void setCtx(Context ctx)
	{
		this.ctx = ctx;
	}
}
