package com.podhoarder.util;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.object.SearchResultRow;
import com.podhoarderproject.podhoarder.R;

/**
 * Provides static convenience methods for showing various dialog windows from one place.
 * @author Emil Almrot
 *
 */
public class DialogUtils
{
	public static void addFeedsDialog(final Context ctx, final List<SearchResultRow> selectedResults)
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
	    	    	((MainActivity)ctx).helper.addSearchResults(selectedResults);
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
			final SearchResultRow currentItem = selectedResults.get(0);
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
	    	builder.setTitle(ctx.getString(R.string.popup_window_title_single));
	    	builder.setMessage(ctx.getString(R.string.popup_areyousure) + " " + currentItem.getTitle() + "?");

	    	// Set up the buttons
	    	builder.setPositiveButton(R.string.popup_window_ok_button, new DialogInterface.OnClickListener() { 
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) 
	    	    {
	    	    	((MainActivity)ctx).helper.addSearchResults(selectedResults);
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
