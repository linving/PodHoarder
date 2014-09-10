package com.podhoarder.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.podhoarder.component.CheckableRelativeLayout;

/**
 * This is to improve ListView performance. See link for details.
 * 
 * @see http
 *      ://developer.android.com/training/improving-layouts/smooth-scrolling.
 *      html
 * @author Emil
 * 
 */
public class ViewHolders
{

	public static class FeedsAdapterViewHolder
	{
		public TextView feedTitle;
		public TextView feedNumberOfEpisodes;
		public ImageView feedImage;
		public CheckableRelativeLayout checkableLayout;
	}
	
	public static class EpisodeRowViewHolder
	{
		public TextView episodeTitle;
		public TextView episodeAge;
		public ImageView feedImage;
		public View indicator;
		public View indicatorExtension;
	}
	
	public static class LatestEpisodesRowViewHolder extends EpisodeRowViewHolder
	{
		public TextView feedTitle;
		public TextView episodeDescription;
	}
	
	public static class FeedDetailsRowViewHolder extends EpisodeRowViewHolder
	{
		public TextView episodeDescription;
	}

	public static class PlaylistRowViewHolder extends EpisodeRowViewHolder
	{
		public TextView feedTitle;
		public TextView timeListened;
		public ImageView handle;
	}

	public static class SearchResultsAdapterViewHolder
	{
		public TextView feedTitle;
		public TextView feedAuthor;
		public TextView lastUpdated;
		public ImageView explicitIndicator;
		public ImageView feedImage;
	}
}
