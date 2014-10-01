package com.podhoarder.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.EpisodesListAdapter;
import com.podhoarder.listener.EpisodeMultiChoiceModeListener;
import com.podhoarder.listener.OnItemDoubleClickListener;
import com.podhoarder.object.Episode;
import com.podhoarder.util.Constants;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ToastMessages;
import com.podhoarderproject.podhoarder.R;
 
/**
 * 
 * @author Emil Almrot
 * 2014-05-21
 */
public class LatestEpisodesFragment extends Fragment
{
	
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "com.podhoarderproject.podhoarder.LatestEpisodesFragment";
	
	public ListView mainListView;
	
	private View view;
	private PodcastHelper helper;
	
	private EpisodeMultiChoiceModeListener mListSelectionListener;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_latest, container, false);
		
		setupHelper();
		setupListView();
		
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
    		this.mainListView.setOnItemClickListener(new OnItemDoubleClickListener()
			{
				@Override
				public void onSingleClick(AdapterView<?> parent, View v, int pos, long id)
				{
					Episode currentEp = (Episode)mainListView.getItemAtPosition(pos);
					if (currentEp.isDownloaded() || NetworkUtils.isOnline(getActivity()))
					{
						((MainActivity)getActivity()).podService.playEpisode((Episode)mainListView.getItemAtPosition(pos));
						((MainActivity)getActivity()).setTab(Constants.PLAYER_TAB_POSITION);
					}
					else
						ToastMessages.PlaybackFailed(getActivity());
				}

				@Override
				public void onDoubleClick(AdapterView<?> parent, View v, int pos, long id)
				{
					((EpisodesListAdapter)mainListView.getAdapter()).toggleRowExpanded(v);
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

	public EpisodeMultiChoiceModeListener getListSelectionListener()
	{
		return mListSelectionListener;
	}
}
