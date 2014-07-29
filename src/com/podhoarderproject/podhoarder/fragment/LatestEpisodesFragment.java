package com.podhoarderproject.podhoarder.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.activity.MainActivity;
import com.podhoarderproject.podhoarder.util.Constants;
import com.podhoarderproject.podhoarder.util.Episode;
import com.podhoarderproject.podhoarder.util.ExpandAnimation;
import com.podhoarderproject.podhoarder.util.PodcastHelper;
import com.podhoarderproject.podhoarder.util.PopupMenuUtils;
 
/**
 * 
 * @author Emil Almrot
 * 2014-05-21
 */
public class LatestEpisodesFragment extends Fragment implements OnRefreshListener
{
	
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.LatestEpisodesFragment";
	
	public ListView mainListView;
	
	private SwipeRefreshLayout swipeLayout;
	
	private View view;
	private PodcastHelper helper;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_latest, container, false);
		
		setupHelper();
		setupListView();
		setupRefreshControls();
		
		return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
        
    private void setupListView()
    {
    	this.mainListView = (ListView) view.findViewById(R.id.mainListView);
    	if (!this.helper.latestEpisodesListAdapter.isEmpty())
    	{
    		this.mainListView.setAdapter(this.helper.latestEpisodesListAdapter);
    		//Normal clicks should just expand the description container.
    		this.mainListView.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
				{
					LinearLayout episodeDescription = (LinearLayout)v.findViewById(R.id.list_episode_row_expandable_container);
	   				 
    		        // Creating the expand animation for the item
    		        ExpandAnimation expandAni = new ExpandAnimation(episodeDescription, 100);
    		 
    		        // Start the animation on the toolbar
    		        episodeDescription.startAnimation(expandAni);
				}
			});
    		//Longclicks should bring up the popupmenu.
    		this.mainListView.setOnItemLongClickListener(new OnItemLongClickListener()
			{

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id)
				{
					final Episode currentEp = (Episode)mainListView.getItemAtPosition(pos);
					
					final PopupMenu actionMenu = new PopupMenu(getActivity(), v);
					MenuInflater inflater = actionMenu.getMenuInflater();
					if (!currentEp.getLocalLink().isEmpty()) inflater.inflate(R.menu.episode_menu_downloaded, actionMenu.getMenu());	//Different menus depending on if the file is downloaded or not.
					else inflater.inflate(R.menu.episode_menu_not_downloaded, actionMenu.getMenu());
					
					if (currentEp.isListened()) actionMenu.getMenu().removeItem(R.id.menu_episode_markAsListened); //If the Episode is already fully listened to, no need to show "Mark As Listened" alternative.
					
					actionMenu.setOnMenuItemClickListener(new OnMenuItemClickListener()
					{
						@Override
						public boolean onMenuItemClick(MenuItem item)
						{
							switch (item.getItemId()) 
							{
						        case R.id.menu_episode_download:
						        	actionMenu.dismiss();
						        	((MainActivity)getActivity()).downloadEpisode(currentEp.getFeedId(), currentEp.getEpisodeId());
						            return true;
						        case R.id.menu_episode_stream:
						        	actionMenu.dismiss();
						        	((MainActivity)getActivity()).podService.streamEpisode(currentEp);
							    	((MainActivity)getActivity()).getActionBar().setSelectedNavigationItem(Constants.PLAYER_TAB_POSITION);	//Navigate to the Player Fragment automatically.
						            return true;
						        case R.id.menu_episode_playFile:
						        	actionMenu.dismiss();
						        	((MainActivity)getActivity()).podService.startEpisode(currentEp);
							    	((MainActivity)getActivity()).getActionBar().setSelectedNavigationItem(Constants.PLAYER_TAB_POSITION);	//Navigate to the Player Fragment automatically.
							    	return true;
						        case R.id.menu_episode_deleteFile:
						        	actionMenu.dismiss();
						        	((MainActivity)getActivity()).podService.deletingEpisode(currentEp.getEpisodeId());
							    	((MainActivity)getActivity()).helper.deleteEpisode(currentEp.getFeedId(), currentEp.getEpisodeId());
							    	return true;
						        case R.id.menu_episode_markAsListened:
						        	actionMenu.dismiss();
						        	((MainActivity)getActivity()).helper.markAsListened(currentEp);
						        	return true;
							}
							return true;
						}
					});
					PopupMenuUtils.forceShowIcons(actionMenu);
					actionMenu.show();
					return true;
				}
			});
    	}
    	else
    	{
    		//TODO: Show some kind of "list is empty" text instead of the mainlistview here.
    	}
    }
    
    private void setupHelper()
    {
    	this.helper = ((com.podhoarderproject.podhoarder.activity.MainActivity)this.getActivity()).helper;
    }
    
    private void setupRefreshControls()
    {
    	swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(R.color.refresh_bar_blue, 
        		R.color.refresh_bar_green, 
        		R.color.refresh_bar_orange, 
                R.color.refresh_bar_red);
    }

	@Override
	public void onRefresh()
	{
		this.helper.setRefreshLayout(swipeLayout);	//Set the layout that should be updated once the Refresh task is done executing.
		this.helper.refreshFeeds();	//Start the refresh process.
	}
}
