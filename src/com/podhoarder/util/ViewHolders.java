package com.podhoarder.util;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is to improve ListView performance. See link for details.
 * 
 * @see http
 *      developer.android.com/training/improving-layouts/smooth-scrolling.
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
		public CheckBox	checkmark;
		public boolean checked;
	}
	
	public static class EpisodeRowViewHolder
	{
		public TextView title;
		public TextView age;
		public ImageView arrow;
		public ImageView feedImage;
		public CheckBox checkbox;
		public boolean checked;
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
		public TextView feedDescription;
		public ImageView feedImage;
	}
}
