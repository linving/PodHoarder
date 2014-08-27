package com.podhoarder.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.object.EpisodeMultiChoiceModeListener;
import com.podhoarder.util.Constants;
import com.podhoarder.util.PodcastHelper;
import com.podhoarderproject.podhoarder.R;
 
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
	
	private EpisodeMultiChoiceModeListener mListSelectionListener;
	
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
//					LinearLayout episodeDescription = (LinearLayout)v.findViewById(R.id.list_episode_row_expandable_container);
//	   				 
//    		        // Creating the expand animation for the item
//    		        ExpandAnimation expandAni = new ExpandAnimation(episodeDescription, 100);
//    		 
//    		        // Start the animation on the toolbar
//    		        episodeDescription.startAnimation(expandAni);
					
					((MainActivity)getActivity()).podService.playEpisode((Episode)mainListView.getItemAtPosition(pos));
					((MainActivity)getActivity()).setTab(Constants.PLAYER_TAB_POSITION);
				}
			});
    		
    		this.mainListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    		this.mListSelectionListener = new EpisodeMultiChoiceModeListener(getActivity(), this.mainListView);
    		this.mainListView.setMultiChoiceModeListener(this.mListSelectionListener);
    	}
    	else
    	{
    		//TODO: Show some kind of "list is empty" text instead of the mainlistview here.
    	}
    }
    
    private void setupHelper()
    {
    	this.helper = ((com.podhoarder.activity.MainActivity)this.getActivity()).helper;
    }
    
    private void setupRefreshControls()
    {
    	swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(R.color.refresh_bar_blue, R.color.refresh_bar_green, R.color.refresh_bar_orange, R.color.refresh_bar_red);
    }

	@Override
	public void onRefresh()
	{
		this.helper.setRefreshLayout(swipeLayout);	//Set the layout that should be updated once the Refresh task is done executing.
		this.helper.refreshFeeds();	//Start the refresh process.
	}

	public EpisodeMultiChoiceModeListener getListSelectionListener()
	{
		return mListSelectionListener;
	}
}
