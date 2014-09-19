package com.podhoarder.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.podhoarder.activity.MainActivity;
import com.podhoarder.adapter.FeedDetailsListAdapter;
import com.podhoarder.object.Episode;
import com.podhoarder.object.EpisodeMultiChoiceModeListener;
import com.podhoarder.util.Constants;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.OnItemDoubleClickListener;
import com.podhoarder.util.PodcastHelper;
import com.podhoarder.util.ToastMessages;
import com.podhoarderproject.podhoarder.R;
 
/**
 * 
 * @author Emil Almrot
 * 2014-07-15
 */
public class FeedDetailsFragment extends Fragment
{

	@SuppressWarnings("unused")
	private static final 	String 				LOG_TAG = "com.podhoarderproject.podhoarder.FeedDetailsFragment";
	
	public ListView mEpisodesListView;
	
	private View mView;
	private PodcastHelper mHelper;
	
	private EpisodeMultiChoiceModeListener mListSelectionListener;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.mView = inflater.inflate(R.layout.fragment_feed_details, container, false);
    	setupHelper();
		if (mHelper.feedDetailsListAdapter.mFeed != null)
		{
			setupFeedDetails();
	    	setupListView();
		}
		return mView;
    }
    
    public FeedDetailsFragment() {}
    
	@Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    }

    
    private void setupFeedDetails()
    {
    	if (this.mHelper.feedDetailsListAdapter.mFeed != null)
    	{
    		TextView title = (TextView) mView.findViewById(R.id.feed_details_title);
    		TextView author = (TextView) mView.findViewById(R.id.feed_details_author);
    		
    		title.setText(this.mHelper.feedDetailsListAdapter.mFeed.getTitle());
    		author.setText(getActivity().getString(R.string.notification_by) + " " + this.mHelper.feedDetailsListAdapter.mFeed.getAuthor());
    	}
    }
    
    private void setupListView()
    {
    	if (this.mHelper.feedDetailsListAdapter.mFeed != null)
    	{
    		this.mEpisodesListView = (ListView) mView.findViewById(R.id.episodesListView);
    		if (this.mHelper.feedDetailsListAdapter == null) this.mHelper.feedDetailsListAdapter = new FeedDetailsListAdapter(getActivity());
        	if (this.mHelper.feedDetailsListAdapter != null)
        	{
        		this.mEpisodesListView.setAdapter(this.mHelper.feedDetailsListAdapter);
        		//Normal clicks should just expand the description container.
        		this.mEpisodesListView.setOnItemClickListener(new OnItemDoubleClickListener()
    			{
    				@Override
    				public void onSingleClick(AdapterView<?> parent, View v, int pos, long id)
    				{
    					Episode currentEp = (Episode)mEpisodesListView.getItemAtPosition(pos);
    					if (currentEp.isDownloaded() || NetworkUtils.isOnline(getActivity()))
    					{
    						((MainActivity)getActivity()).podService.playEpisode((Episode)mEpisodesListView.getItemAtPosition(pos));
    						((MainActivity)getActivity()).setTab(Constants.PLAYER_TAB_POSITION);
    					}
    					else
    						ToastMessages.PlaybackFailed(getActivity());
    				}

    				@Override
    				public void onDoubleClick(AdapterView<?> parent, View v, int pos, long id)
    				{
    					((FeedDetailsListAdapter)mEpisodesListView.getAdapter()).toggleRowExpanded(v);
    				}
    			});
        		
        		this.mEpisodesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        		this.mListSelectionListener = new EpisodeMultiChoiceModeListener(getActivity(), this.mEpisodesListView);
        		this.mEpisodesListView.setMultiChoiceModeListener(this.mListSelectionListener);
        		this.mEpisodesListView.smoothScrollToPosition(0);
        	}
        	else
        	{
        		//TODO: Show some kind of "list is empty" text instead of the episodesListView here.
        	}	
    	}
    }
   
    private void setupHelper()
    {
    	this.mHelper = ((com.podhoarder.activity.MainActivity)this.getActivity()).helper;
    }

	public EpisodeMultiChoiceModeListener getListSelectionListener()
	{
		return mListSelectionListener;
	}
}
