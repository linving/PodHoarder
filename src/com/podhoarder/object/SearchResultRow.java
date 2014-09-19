package com.podhoarder.object;

import org.w3c.dom.Document;

public class SearchResultRow
{
	private String title;
	private String author;
	private String description;
	private String link;
	private String category;
	private String imageUrl;
	private Document xml;
	private String lastUpdated;
	
	public SearchResultRow() {	}
	
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
	public String getImageUrl()
	{
		return imageUrl;
	}
	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
	}

	public Document getXml()
	{
		return xml;
	}

	public void setXml(Document xml)
	{
		this.xml = xml;
	}

	public String getLastUpdated()
	{
		return lastUpdated;
	}
	public void setLastUpdated(String lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}
	public void startImageDownload()
	{
		
	}
}
