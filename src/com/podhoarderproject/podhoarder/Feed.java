package com.podhoarderproject.podhoarder;

import java.util.List;

import android.content.Context;

public class Feed 
{
	private int				feedId;
	private String 			title;
	private String 			author;
	private String 			description;
	private String[] 		keywords;
	private String 			link;
	private String 			category;
	private FeedImage 		feedImage;
	private List<Episode>	episodes;
	private Context			ctx;
}
