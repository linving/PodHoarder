package com.podhoarder.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ExpandableListView;

import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends Activity
{
	private LibraryActivityManager helper;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		this.helper = new LibraryActivityManager(this);
		List<String> urls = new ArrayList<String>();
    	urls.add("http://smodcast.com/channels/plus-one/feed/");
		//this.helper.updateEpisodeListened(1, 1, 45);
		//this.helper.deleteFeed(2);
		//this.helper.downloadEpisode(1, 24);
		this.helper.refreshFeeds(null);
		ExpandableListView list = (ExpandableListView)findViewById(R.id.mainListView);
		list.setAdapter(helper.mFeedsGridAdapter);
		System.out.println("Done!");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}

}
