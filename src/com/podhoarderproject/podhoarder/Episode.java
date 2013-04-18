package com.podhoarderproject.podhoarder;
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
	private int 	minutesListened;
	private int		length;
	private int		feedId;
	
	
	public Episode (int episodeId, String title, String link, String localLink, String pubDate, String description, int minutesListened, int length, int feedId)
	{
		this.episodeId = episodeId;
		this.title = title;
		this.link = link;
		this.localLink = localLink;
		this.pubDate = pubDate;
		this.description = description;
		this.minutesListened = minutesListened;
		this.length = length;
		this.feedId = feedId;
	}

	public Episode()
	{
		this.episodeId = 0;
		this.title = "";
		this.link = "";
		this.localLink = "";
		this.pubDate = "";
		this.description = "";
		this.minutesListened = 0;
		this.length = 0;
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

	public int getMinutesListened() 
	{
		return this.minutesListened;
	}
	public void setMinutesListened(int minutesListened) 
	{
		this.minutesListened = minutesListened;
	}

	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}
	
	public int getFeedId()
	{
		return this.feedId;
	}
}

//TODO: Implement Comparator for Episodes (compare dates)