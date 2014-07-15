package com.podhoarderproject.podhoarder.activity;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.util.PodcastHelper;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ExpandableListView;

public class TestActivity extends Activity
{
	private PodcastHelper helper;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		this.helper = new PodcastHelper(this);
		this.helper.addFeed("http://smodcast.com/channels/plus-one/feed/");
		//this.helper.updateEpisodeListened(1, 1, 45);
		//this.helper.deleteFeed(2);
		//this.helper.downloadEpisode(1, 24);
		this.helper.refreshFeeds();
		ExpandableListView list = (ExpandableListView)findViewById(R.id.mainListView);
		list.setAdapter(helper.listAdapter);
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