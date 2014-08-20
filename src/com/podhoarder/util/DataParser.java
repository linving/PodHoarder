package com.podhoarder.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.text.Html;
import android.util.Log;

import com.podhoarder.object.Episode;

public class DataParser
{
	private	static final	String						LOG_TAG = "com.podhoarderproject.podhoarder.DataParser";
	
	//XML Tags
	private static final	String[]					podcastTitleTagNames = {"title"};
	private static final	String[]					podcastDescriptionTagNames = {"description"};
	private static final	String[]					podcastAuthorTagNames = {"itunes:author"};
	private static final	String[]					podcastCategoryTagNames = {"itunes:category"};
	private static final	String[]					podcastImageTagNames = {"itunes:image"};
	
	private static final	String[]					episodeTitleTagNames = {"title"};
	private static final	String[]					episodeDescriptionTagNames = {"description","content:encoded","itunes:summary","itunes:subtitle"};
	private static final	String[]					episodeLinkTagNames = {"enclosure"};
	private static final	String[]					episodePubdateTagNames = {"pubDate"};

	
	public 	static final 	SimpleDateFormat 			xmlFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);	//Used when formatting timestamps in .xml's
	public 	static final 	SimpleDateFormat 			correctFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss", Locale.US);	//Used when formatting timestamps in .xml's

	//Podcast parsing
	
	public static String parsePodcastTitle(NodeList itemLst)
	{
		String title = "";
		Element element = (Element) itemLst.item(0);
		for (String titleTag : podcastTitleTagNames)									//Loop through all the alternatives
		{
			NodeList titleElement = element.getElementsByTagName(titleTag);
			if (titleElement.equals(null) || titleElement.getLength() < 1)				//If the titleElement has no children or is null there are no elements with the current tag.
			{
				continue;																//We didn't find anything by using the current titleTag, so we try the next one.
			}
			title = titleElement.item(0).getChildNodes().item(0).getNodeValue();
			if (!title.isEmpty()) break;												//If we have found a value for title, we can break here. No need to keep looping.
		}
		return title;
	}
	
	public static String parsePodcastDescription(NodeList itemLst)
	{
		String description = "";
		Element element = (Element) itemLst.item(0);
		for (String descriptionTag : podcastDescriptionTagNames)						//Loop through all the alternatives
		{
			NodeList descriptionElement = element.getElementsByTagName(descriptionTag);
			if (descriptionElement.equals(null) || descriptionElement.getLength() < 1)	//If the descriptionElement has no children or is null there are no elements with the current tag.
			{
				continue;																//We didn't find anything by using the current descriptionTag, so we try the next one.
			}
			description = descriptionElement.item(0).getChildNodes().item(0).getNodeValue();
			if (!description.isEmpty()) break;											//If we have found a value for title, we can break here. No need to keep looping.
		}
		return description;
	}
	
	public static String parsePodcastAuthor(NodeList itemLst)
	{
		String author = "";
		Element element = (Element) itemLst.item(0);
		for (String authorTag : podcastAuthorTagNames)									//Loop through all the alternatives
		{
			NodeList authorElement = element.getElementsByTagName(authorTag);
			if (authorElement.equals(null) || authorElement.getLength() < 1)			//If the authorElement has no children or is null there are no elements with the current tag.
			{
				continue;																//We didn't find anything by using the current authorTag, so we try the next one.
			}
			author = authorElement.item(0).getChildNodes().item(0).getNodeValue();
			if (!author.isEmpty()) break;												//If we have found a value for title, we can break here. No need to keep looping.
		}
		return author;
	}
	
	public static String parsePodcastCategory(NodeList itemLst)
	{
		String category = "";
		Element element = (Element) itemLst.item(0);
		for (String categoryTag : podcastCategoryTagNames)								//Loop through all the alternatives
		{
			try
			{
				NodeList categoryElement = element.getElementsByTagName(categoryTag);
				if (categoryElement.equals(null) || categoryElement.getLength() < 1)	//If the categoryElement has no children or is null there are no elements with the current tag.
				{
					continue;															//We didn't find anything by using the current categoryTag, so we try the next one.
				}
				if (categoryTag.equals(podcastCategoryTagNames[0]))	
					category = categoryElement.item(0).getAttributes().item(0).getNodeValue();
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
				break;
			}
			if (!category.isEmpty()) break;												//If we have found a value for title, we can break here. No need to keep looping.
		}
		return category;
		
	}
	
	public static String parsePodcastImageLocation(NodeList itemLst)
	{
		String imageURL = "";
		Element element = (Element) itemLst.item(0);
		for (String imageTag : podcastImageTagNames)									//Loop through all the alternatives
		{
			NodeList imageElement = element.getElementsByTagName(imageTag);
			if (imageElement.equals(null) || imageElement.getLength() < 1)				//If the imageElement has no children or is null there are no elements with the current tag.
			{
				continue;																//We didn't find anything by using the current imageTag, so we try the next one.
			}
			if (imageTag.equals(podcastImageTagNames[0]))
				imageURL = imageElement.item(0).getAttributes().item(0).getNodeValue();
			if (!imageURL.isEmpty()) break;												//If we have found a value for title, we can break here. No need to keep looping.
		}
		return imageURL;
	}

	//Episode parsing
	
	public static Episode parseNewEpisode(Node item)
	{
		Element ielem = (Element) item;
		Episode ep = new Episode();
		if (item.getNodeType() == Node.ELEMENT_NODE)
		{			
			// Extract relevant data from the NodeList objects.
			
			ep.setTitle(parseEpisodeTitle(ielem));				//EPISODE TITLE
			
			ep.setLink(parseEpisodeLink(ielem));				//URL LINK
			
			ep.setPubDate(parseEpisodePubDate(ielem));			//PUBLISH DATE
			
			ep.setDescription(parseEpisodeDescription(ielem));	//DESCRIPTION
		}
		
		ep.setTotalTime(100);									//We set this to 100 in order to discern new Episodes from old ones. And also 100 makes it easier when dealing with percentages.
		ep.setElapsedTime(0);									//Since we haven't listened to the Episode yet this should be set to 0.
																//A totalTime of 100 and ElapsedTime of 0 means that the Episode should be marked with NEW!
		return ep;
	}
	
	public static String parseEpisodeTitle(Element element)
	{
		String title = "";
		try
		{
			for (String titleTag : episodeTitleTagNames)							//Loop through all the alternatives
			{
				NodeList titleNode = element.getElementsByTagName(titleTag);
				if (titleNode.equals(null) || titleNode.getLength() < 1)			//If the titleNode has no children or is null there are no elements with the current tag.
				{
					continue;																	//We didn't find anything by using the current titleTag, so we try the next one.
				}
				title = titleNode.item(0).getChildNodes().item(0).getNodeValue();
				if (!title.isEmpty()) break;										//If we have found a value for title, we can break here. No need to keep looping.
			}
			return title;
		} 
		catch (NullPointerException e)
		{
			Log.e(LOG_TAG, "NullPointerException when parsing Episode Title.");
			e.printStackTrace();
			return title;
		}
	}
	
	public static String parseEpisodeDescription(Element element)
	{
		String description = "";
		try
		{
			for (String descriptionTag : episodeDescriptionTagNames)							//Loop through all the alternatives
			{
				NodeList descriptionNode = element.getElementsByTagName(descriptionTag);
				if (descriptionNode.equals(null) || descriptionNode.getLength() < 1)			//If the descriptionNode has no children or is null there are no elements with the current tag.
				{
					continue;																	//We didn't find anything by using the current descriptionTag, so we try the next one.
				}
				
				if (descriptionTag.equals(episodeDescriptionTagNames[0]))						//<description> often has html-tags, so we try to strip those here.
				{
					description = Html.fromHtml(descriptionNode.item(0).getChildNodes().item(0).getNodeValue()).toString();
				}
				else
				{
					description = descriptionNode.item(0).getChildNodes().item(0).getNodeValue();
				}
				if (!description.isEmpty()) break;												//If we have found a value for description, we can break here. No need to keep looping.
			}
			return description;
		} 
		catch (NullPointerException e)
		{
			Log.e(LOG_TAG, "NullPointerException when parsing Episode Description.");
			e.printStackTrace();
			return description;
		}
	}
	
	public static String parseEpisodeLink(Element element)
	{
		String link = "";
		try
		{
			for (String linkTag : episodeLinkTagNames)											//Loop through all the alternatives
			{
				NodeList linkNode = element.getElementsByTagName(linkTag);						//Extract the attributes from the NodeList
				if (linkNode.equals(null) || linkNode.getLength() < 1)							//If the linkNode has no children or is null there are no elements with the current tag.
				{
					continue;																	//We didn't find anything by using the current linkTag, so we try the next one.
				}
				
				if (linkTag.equals("enclosure"))	
					link = linkNode.item(0).getAttributes().getNamedItem("url").getNodeValue();	//Then extract value of the attribute named "url".
				
				if (!link.isEmpty()) break;
			}
			return link;	
		} 
		catch (NullPointerException e)
		{
			Log.e(LOG_TAG, "NullPointerException when parsing Episode Link.");
			e.printStackTrace();
			return link;
		}
	}
	
	public static String parseEpisodePubDate(Element element)
	{
		String pubDate = "";
		try
		{
			for (String pubdateTag : episodePubdateTagNames)							//Loop through all the alternatives
			{
				NodeList pubdateNode = element.getElementsByTagName(pubdateTag);
				if (pubdateNode.equals(null) || pubdateNode.getLength() < 1)			//If the pubdateNode has no children or is null there are no elements with the current tag.
				{
					continue;																	//We didn't find anything by using the current pubdateTag, so we try the next one.
				}
				pubDate = pubdateNode.item(0).getChildNodes().item(0).getNodeValue();
				if (!pubDate.isEmpty()) break;											//If we have found a value for pubdate, we can break here. No need to keep looping.
			}
			
			try
			{
				Date date = xmlFormat.parse(pubDate);									//Try to parse the date tag into the format the app handles.
				return correctFormat.format(date);										//Return the correctly formatted string.
			}
			
			catch (ParseException e)
			{
				Log.e(LOG_TAG, "ParseException when parsing Episode Publish date.");
				e.printStackTrace();
				return "";																//If we encounter something that we can't parse, we just return an empty string.
			}
		}
		catch (NullPointerException e)
		{
			Log.e(LOG_TAG, "NullPointerException when parsing Episode Pubdate.");
			e.printStackTrace();
			return pubDate;
		}
	}
	
	
}
