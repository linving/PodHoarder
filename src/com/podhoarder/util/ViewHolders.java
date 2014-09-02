package com.podhoarder.util;

import com.podhoarder.component.CheckableRelativeLayout;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This is to improve ListView performance. See link for details.
 * @see http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 * @author Emil
 *
 */
public class ViewHolders
{

	public static class FeedsAdapterViewHolder {	
		public TextView feedTitle;
		public TextView feedNumberOfEpisodes;
		public ImageView feedImage;
		public CheckableRelativeLayout checkableLayout;
	}
		
		public static class LatestEpisodesAdapterViewHolder {	
		    public TextView episodeTitle;
		    public TextView feedTitle;
		    public TextView episodeAge;
		    public TextView newNotification;
		    public TextView episodeDescription;
		    public ImageView feedImage;
		}
		
		public static class FeedDetailsAdapterViewHolder {	
			public TextView episodeTitle;
			public TextView episodeAge;
			public TextView newNotification;
			public TextView episodeDescription;
		    public ImageView feedImage;
		}
		
		public static class PlaylistAdapterViewHolder {	
			public TextView episodeTitle;
			public TextView podcastTitle;
			public ProgressBar elapsedTimeBar;
			public ImageView currentEpisodeIndicator;
	  	}
		
		public static class SearchResultsAdapterViewHolder {	
			public TextView 			feedTitle;
			public TextView 			feedAuthor;
			public TextView 			lastUpdated;
			public ImageView 			explicitIndicator;
			public ImageView 			feedImage;
		}
}
