package com.podhoarder.util;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is to improve ListView performance. See link for details.
 * 
 * @see http://developer.android.com/training/improving-layouts/smooth-scrolling.html
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
	


    public static class QuickListRowViewHolder
    {
        public TextView title;
        public ImageView icon;
    }




}
