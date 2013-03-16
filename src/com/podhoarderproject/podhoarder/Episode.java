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
	private String 	pubDate;
	private String 	description;
	private String 	fileURL;
	private int		feedId;
	
	
	public Episode (int episodeId, String title, String link, String pubDate, String description, String fileURL, int feedId)
	{
		this.episodeId = episodeId;
		this.title = title;
		this.link = link;
		this.pubDate = pubDate;
		this.description = description;
		this.fileURL = fileURL;
		this.feedId = feedId;
	}

	public Episode()
	{
		this.episodeId = 0;
		this.title = "";
		this.link = "";
		this.pubDate = "";
		this.description = "";
		this.fileURL = "";
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

	public String getFileURL() 
	{
		return fileURL;
	}
	public void setFileURL(String fileURL) 
	{
		this.fileURL = fileURL;
	}

	public int getFeedId()
	{
		return this.feedId;
	}
}
