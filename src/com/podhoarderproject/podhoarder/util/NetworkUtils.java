package com.podhoarderproject.podhoarder.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public class NetworkUtils
{
	
	/**
	 * A basic check to determine if the phone has internet connectivity or not.
	 * @param context	App context (must be passed in order to access system services.
	 * @return True if a connection is available, otherwise false.
	 */
	public static boolean isOnline(Context context) 
	{
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) 
	    	return true;
	    else
	    	return false;
	}
	
	/**
	 *	Used to check the device version and DownloadManager information.
	 * 
	 * @param context Context object.
	 * @return true if the download manager is available
	 */
	public static boolean isDownloadManagerAvailable(Context context)
	{
		try
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
			{
				return false;
			}
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName("com.android.providers.downloads.ui",
					"com.android.providers.downloads.ui.DownloadList");
			List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
		} 
		catch (Exception e)
		{
			return false;
		}
	}
}
