package com.podhoarder.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.podhoarder.fragment.FeedDetailsFragment;
import com.podhoarder.fragment.FeedFragment;
import com.podhoarder.fragment.LatestEpisodesFragment;
import com.podhoarder.fragment.PlayerFragment;
import com.podhoarder.fragment.SearchFragment;
import com.podhoarder.util.Constants;

public class TabFragmentsAdapter extends FragmentStatePagerAdapter 
{
	private boolean detailsPageEnabled = false;
	private boolean searchPageEnabled = false;
	
    public TabFragmentsAdapter(FragmentManager fragmentManager) 
    {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) 
    {
        switch (position) 
        {
	        case Constants.FEEDS_TAB_POSITION: 	// Fragment # 0
	            return new FeedFragment();
	        case Constants.LATEST_TAB_POSITION: // Fragment # 1
	            return new LatestEpisodesFragment();
	        case Constants.PLAYER_TAB_POSITION:	// Fragment # 2
	            return new PlayerFragment();
	        case Constants.BONUS_TAB_POSITION:	// Fragment # 3
	        	if (detailsPageEnabled)	return new FeedDetailsFragment();
	        	else if (searchPageEnabled) return new SearchFragment();
        }
        return null;
    }
    
    @Override
    public int getItemPosition(Object object)
    {
    	if(object instanceof FeedDetailsFragment || object instanceof SearchFragment) 
    	{ //this includes deleting or adding pages
    		 return FragmentStatePagerAdapter.POSITION_NONE;
    	}
    	else return FragmentStatePagerAdapter.POSITION_UNCHANGED; //this ensures high performance in other operations such as editing list items.

    }

    @Override
    public int getCount() 
    {
    	if (this.detailsPageEnabled || this.searchPageEnabled)	return (Constants.TAB_COUNT);
    	else return (Constants.TAB_COUNT - 1);
    }
    
    public void setDetailsPageEnabled(boolean enabled)
    {
    	this.detailsPageEnabled = enabled;
    	//this.searchPageEnabled = !enabled;	//TODO: Might break!!
    	this.notifyDataSetChanged();
    }
    
    public void setSearchPageEnabled(boolean enabled)
    {
    	this.searchPageEnabled = enabled;
    	//this.detailsPageEnabled = !enabled;	//TODO: Might break!!
    	this.notifyDataSetChanged();
    }
}

