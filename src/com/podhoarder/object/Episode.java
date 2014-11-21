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
	
	
	public Episode (int episodeId, String title, String link, String localLink, String pubDate, String description, int elapsedTime, int length, boolean isFavorite, int feedId)
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
		
		this.favorite = isFavorite;
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
        this.favorite = false;
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

    /**
     * @return A percentage of the amount listened.
     */
    public int getProgress() {
        if (isListened())
            return 100;
        else if (isNew() || elapsedTime == 0)
            return 0;
        else {
            int p = (int) Math.floor(((double)elapsedTime/totalTime) * 100);    //Calculate percentage and round down (That means that if there's even a second less in elapsed time it'll show 99%, isntead of rounding to 100%
            if (p == 0) //If it has been rounded down to 0 we say it's 1 instead. If an episode is compeltely unlistened the functioned as returned 0 already. So we show that the Episode has been listened to, even if only slightly.
                return 1;
            else
                return p;
        }
    }

	public boolean isFavorite()
	{
		return this.favorite;
	}
}

//TODO: Implement Comparator for Episodes (compare dates)