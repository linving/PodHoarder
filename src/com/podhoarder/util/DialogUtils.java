package com.podhoarder.util;

import java.util.ArrayList;
import java.util.List;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.json.SearchResultItem;
import com.podhoarderproject.podhoarder.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtils
{
	public static void addFeedsDialog(final Context ctx, final List<SearchResultItem> selectedResults)
	{
		if (selectedResults.size() > 1)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
	    	builder.setTitle(ctx.getString(R.string.popup_window_title_multiple));
	    	builder.setMessage(ctx.getString(R.string.popup_areyousure) + " " + selectedResults.size() + " " + ctx.getString(R.string.popup_podcasts) + "?");

	    	// Set up the buttons
	    	builder.setPositiveButton(R.string.popup_window_ok_button, new DialogInterface.OnClickListener() { 
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) 
	    	    {
	    	    	List<String> urls = new ArrayList<String>();
	    	    	for (SearchResultItem currentItem : selectedResults)
	    	    	{
	    	    		urls.add(currentItem.getFeedUrl());
	    	    	}
	    	    	((MainActivity)ctx).helper.addFeed(urls);
					((MainActivity)ctx).setTab(Constants.FEEDS_TAB_POSITION);
					((MainActivity)ctx).helper.feedsListAdapter.notifyDataSetChanged();
	    	    }
	    	});
	    	builder.setNegativeButton(R.string.popup_window_cancel_button, new DialogInterface.OnClickListener() {
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) {
	    	        dialog.cancel();
	    	    }
	    	});
			builder.show();
		}
		else if (selectedResults.size() == 1)
		{
			final SearchResultItem currentItem = selectedResults.get(0);
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
	    	builder.setTitle(ctx.getString(R.string.popup_window_title_single));
	    	builder.setMessage(ctx.getString(R.string.popup_areyousure) + " " + currentItem.getCollectionName() + "?");

	    	// Set up the buttons
	    	builder.setPositiveButton(R.string.popup_window_ok_button, new DialogInterface.OnClickListener() { 
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) 
	    	    {
	    	    	List<String> urls = new ArrayList<String>();
	    	    	urls.add(currentItem.getFeedUrl());
	    	    	((MainActivity)ctx).helper.addFeed(urls);
					((MainActivity)ctx).setTab(Constants.FEEDS_TAB_POSITION);
					((MainActivity)ctx).helper.feedsListAdapter.notifyDataSetChanged();
	    	    }
	    	});
	    	builder.setNegativeButton(R.string.popup_window_cancel_button, new DialogInterface.OnClickListener() {
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) {
	    	        dialog.cancel();
	    	    }
	    	});
			builder.show();
		}
	}
}
