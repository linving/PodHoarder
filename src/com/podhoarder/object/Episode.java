package com.podhoarder.object;

/**
 * @author Emil Almrot
 * 2013-03-15
 */
public class Episode 
{
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.Episode";
	
	
	
	private int		episodeId;
	private String 	title;
	private String 	link;
	private String 	localLink;
	private String 	pubDate;
	private String 	description;
	private boolean favorite;
	private int 	elapsedTime;
	private int		totalTime;
	private int		feedId;
	
	
	public Episode (int episodeId, String title, String link, String localLink, String pubDate, String description, int elapsedTime, int length, int feedId)
	{
		this.episodeId = episodeId;
		this.title = title;
		this.link = link;
		this.localLink = localLink;
		this.pubDate = pubDate;
		
		this.description = description;
		this.elapsedTime = elapsedTime;
		this.totalTime = length;
		this.feedId = feedId;
		
		this.favorite = false;	//TODO: Delete this when support for favorites are added to db.
	}

	public Episode()
	{
		this.episodeId = 0;
		this.title = "";
		this.link = "";
		this.localLink = "";
		this.pubDate = "";
		this.description = "";
		this.elapsedTime = 0;
		this.totalTime = 0;
		this.feedId = 0;
	}
	
	public int getEpisodeId() 
	{
		return episodeId;
	}	
	
	public String getTitle() 
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getLink() 
	{
		return link;
	}
	public void setLink(String link) 
	{
		this.link = link;
	}
	
	public String getLocalLink()
	{
		return localLink;
	}
	public void setLocalLink(String localLink)
	{
		this.localLink = localLink;
	}

	public String getPubDate() 
	{
		return pubDate;
	}
	public void setPubDate(String pubDate) 
	{
		this.pubDate = pubDate;
	}

	public String getDescription() 
	{
		return description;
	}
	public void setDescription(String description) 
	{
		this.description = description;
	}

	public int getElapsedTime() 
	{
		return this.elapsedTime;
	}
	public void setElapsedTime(int elapsedTime) 
	{
		this.elapsedTime = elapsedTime;
	}

	public int getTotalTime()
	{
		return totalTime;
	}

	public void setTotalTime(int totalTime)
	{
		this.totalTime = totalTime;
	}

	public void setFavorite(boolean favorite)
	{
		//TODO: Add support for this in the db
		this.favorite = favorite;
	}
	
	public int getFeedId()
	{
		return this.feedId;
	}
	
	public boolean isNew()
	{
		if (this.elapsedTime == 0 && this.totalTime == 0 && this.localLink.equals("")) return true;
		else return false;
	}
	
	public boolean isListened()
	{
		if (this.elapsedTime >= this.totalTime && this.totalTime > 0) return true;
		else return false;
	}
	
	public boolean isDownloaded()
	{
		if (this.localLink.isEmpty()) return false;
		else return true;
	}

	public boolean isFavorite()
	{
		return this.favorite;
	}
}

//TODO: Implement Comparator for Episodes (compare dates)