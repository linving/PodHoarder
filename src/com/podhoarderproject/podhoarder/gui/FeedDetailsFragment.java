package com.podhoarderproject.podhoarder.gui;

import com.podhoarderproject.podhoarder.R;
import com.podhoarderproject.podhoarder.activity.MainActivity;
import com.podhoarderproject.podhoarder.adapter.FeedDetailsListAdapter;
import com.podhoarderproject.podhoarder.adapter.FirstPageFragmentListener;
import com.podhoarderproject.podhoarder.util.Episode;
import com.podhoarderproject.podhoarder.util.ImageUtils;
import com.podhoarderproject.podhoarder.util.PodcastHelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
 
/**
 * 
 * @author Emil Almrot
 * 2014-07-15
 */
public class FeedDetailsFragment extends Fragment implements OnRefreshListener
{

	@SuppressWarnings("unused")
	private static final 	String 				LOG_TAG = "com.podhoarderproject.podhoarder.FeedDetailsFragment";
	
	public ListView episodesListView;
	
	private SwipeRefreshLayout swipeLayout;
	
	private View view;
	private PodcastHelper helper;
	
	private static FirstPageFragmentListener firstPageListener;
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	this.view = inflater.inflate(R.layout.fragment_feed_details, container, false);
    	setupHelper();
		if (helper.feedDetailsListAdapter.feed != null)
		{
			setupFeedDetails();
			setupRefreshControls();
	    	setupListView();
	    	setupGestureDetection(view);
		}
		return view;
    }
    
    public FeedDetailsFragment() {}
    
    public FeedDetailsFragment(FirstPageFragmentListener listener) {
        firstPageListener = listener;
    }
    
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

    public void backPressed() 
    {
        firstPageListener.onSwitchToNextFragment();
    }
    
    private void setupFeedDetails()
    {
    	if (this.helper.feedDetailsListAdapter.feed != null)
    	{
    		ImageView img = (ImageView) view.findViewById(R.id.feed_details_image);
    		TextView title = (TextView) view.findViewById(R.id.feed_details_title);
    		TextView author = (TextView) view.findViewById(R.id.feed_details_author);
    		TextView category = (TextView) view.findViewById(R.id.feed_details_category);
    		TextView description = (TextView) view.findViewById(R.id.feed_details_description);
    		img.setImageBitmap(ImageUtils.getCircularBitmap(this.helper.feedDetailsListAdapter.feed.getFeedImage().imageObject().getBitmap()));
    		//img.setBackground(new BitmapDrawable(getActivity().getResources(),ImageUtils.getCircularBitmap(this.helper.feedDetailsListAdapter.feed.getFeedImage().imageObject().getBitmap())));
    		title.setText(this.helper.feedDetailsListAdapter.feed.getTitle());
    		author.setText(getActivity().getString(R.string.notification_by) + " " + this.helper.feedDetailsListAdapter.feed.getAuthor());
    		category.setText(this.helper.feedDetailsListAdapter.feed.getCategory());
    		description.setText(this.helper.feedDetailsListAdapter.feed.getDescription());
    	}
    }
    
    private void setupListView()
    {
    	if (this.helper.feedDetailsListAdapter.feed != null)
    	{
    		this.episodesListView = (ListView) view.findViewById(R.id.episodesListView);
    		if (this.helper.feedDetailsListAdapter == null) this.helper.feedDetailsListAdapter = new FeedDetailsListAdapter(getActivity());
        	if (this.helper.feedDetailsListAdapter != null)
        	{
        		this.episodesListView.setAdapter(this.helper.feedDetailsListAdapter);
        		//Normal clicks should just expand the description container.
        		this.episodesListView.setOnItemClickListener(new OnItemClickListener()
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
        		this.episodesListView.setOnItemLongClickListener(new OnItemLongClickListener()
    			{

    				@Override
    				public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id)
    				{
    					final Episode currentEp = (Episode)episodesListView.getItemAtPosition(pos);
    					
    					PopupMenu actionMenu = new PopupMenu(getActivity(), v);
    					MenuInflater inflater = actionMenu.getMenuInflater();
    					if (!currentEp.getLocalLink().isEmpty()) inflater.inflate(R.menu.episode_menu_downloaded, actionMenu.getMenu());	//Different menus depending on if the file is downloaded or not.
    					else inflater.inflate(R.menu.episode_menu_not_downloaded, actionMenu.getMenu());
    		    	   
    					actionMenu.setOnMenuItemClickListener(new OnMenuItemClickListener()
    					{
    						@Override
    						public boolean onMenuItemClick(MenuItem item)
    						{
    							switch (item.getItemId()) 
    							{
    						        case R.id.menu_episode_download:
    						        	((MainActivity)getActivity()).downloadEpisode(currentEp.getFeedId(), currentEp.getEpisodeId());
    						            return true;
    						        case R.id.menu_episode_stream:
    						        	((MainActivity)getActivity()).podService.streamEpisode(currentEp);
    							    	((MainActivity)getActivity()).getActionBar().setSelectedNavigationItem(2);	//Navigate to the Player Fragment automatically.
    						            return true;
    						        case R.id.menu_episode_playFile:
    						        	((MainActivity)getActivity()).podService.startEpisode(currentEp);
    							    	((MainActivity)getActivity()).getActionBar().setSelectedNavigationItem(2);	//Navigate to the Player Fragment automatically.
    							    	return true;
    						        case R.id.menu_episode_deleteFile:
    						        	((MainActivity)getActivity()).podService.deletingEpisode(currentEp.getEpisodeId());
    							    	((MainActivity)getActivity()).helper.deleteEpisode(currentEp.getFeedId(), currentEp.getEpisodeId());
    							    	return true;
    							}
    							return true;
    						}
    					});
    		    	   
    		    	   actionMenu.show();
    		    	   return true;
    				}
    			});
        		
        	}
        	else
        	{
        		//TODO: Show some kind of "list is empty" text instead of the episodesListView here.
        	}	
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

    private void setupGestureDetection(View v)
    {
    	final View container = v.findViewById(R.id.feed_details_container);
    	final ListView listView = (ListView)v.findViewById(R.id.episodesListView);
    	
    	final GestureDetector upSwipeDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

    	            @Override
    	            public boolean onDown(MotionEvent e) {
    	                return true;
    	            }

    	            @Override
    	            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
    	            {
    	            	final int SWIPE_THRESHOLD = 100;
    	                final int SWIPE_VELOCITY_THRESHOLD = 100;
    	                try {
    	                    float diffY = e2.getY() - e1.getY();
    	                    float diffX = e2.getX() - e1.getX();
    	                    if (Math.abs(diffX) > Math.abs(diffY)) 
    	                    {
    	                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) 
    	                        {
    	                            if (diffX > 0) 
    	                            {
    	                            	Log.i(LOG_TAG,"Swipe right!");
    	                            } 
    	                            else 
    	                            {
    	                            	Log.i(LOG_TAG,"Swipe left!");
    	                            }
    	                        }
    	                    } 
    	                    else 
    	                    {
    	                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) 
    	                        {
    	                            if (diffY > 0) 
    	                            {
    	                            	Log.i(LOG_TAG,"Swipe down!");
    	                            } 
    	                            else 
    	                            {
    	                            	slideAnimation(container, R.anim.slide_out_top);
    	    	                        container.setVisibility(View.GONE);
    	    	                        swipeLayout.setEnabled(false);
    	    	                        listView.setVisibility(View.VISIBLE);
    	    	                        slideAnimation(listView, R.anim.slide_in_bottom);
    	                            }
    	                        }
    	                    }
    	                }
    	                
    	                catch (Exception e) 
    	                {
    	                    // nothing
    	                }
    	                return super.onFling(e1, e2, velocityX, velocityY);
    	            }
    	            
    	        });
    	
    	final GestureDetector downSwipeDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return listView.onTouchEvent(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
            {
            	final int SWIPE_THRESHOLD = 100;
                final int SWIPE_VELOCITY_THRESHOLD = 100;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) 
                        {
                            if (diffX > 0) 
                            {
                            	Log.i(LOG_TAG,"Swipe right!");
                            } 
                            else 
                            {
                            	Log.i(LOG_TAG,"Swipe left!");
                            }
                        }
                    } 
                    else 
                    {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) 
                        {
                            if (diffY > 0) 
                            {
                            	slideAnimation(listView, R.anim.slide_out_bottom);
                                listView.setVisibility(View.GONE);
                                swipeLayout.setEnabled(true);
                                container.setVisibility(View.VISIBLE);
                                slideAnimation(container, R.anim.slide_in_top);
                            } 
                            else 
                            {
                            	Log.i(LOG_TAG, "Swipe up!");
                            }
                        }
                    }
                }
                
                catch (Exception e) 
                {
                    // nothing
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    	

	    container.setOnTouchListener(new View.OnTouchListener() 
	    {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) 
	        {
	            return upSwipeDetector.onTouchEvent(event);
	        }
	    });
	    
	    listView.setOnScrollListener(new AbsListView.OnScrollListener() 
	    {  
			  @Override
			  public void onScrollStateChanged(AbsListView view, int scrollState) {

			  }

			  @Override
			  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) 
			  {
				  boolean enable = false;
				  if(episodesListView != null && episodesListView.getChildCount() > 0)
				  {
					  // check if the first item of the list is visible
					  boolean firstItemVisible = episodesListView.getFirstVisiblePosition() == 0;
					  // check if the top of the first item is visible (because of the margins we've set this is 8 instead of 0)
					  boolean topOfFirstItemVisible = episodesListView.getChildAt(0).getTop() == 8;
					  // enabling or disabling the swipe to hide logic
					  enable = firstItemVisible && topOfFirstItemVisible;
				  }
				  
				  if (enable)
				  {
					  	final OnTouchListener onTouchListener = new View.OnTouchListener() 
					    {
					        @Override
					        public boolean onTouch(View v, MotionEvent event) 
					        {
					        	if (downSwipeDetector.onTouchEvent(event)) return true;
					        	else return false;
					        }
					    };
					    listView.setOnTouchListener(onTouchListener);
				  }
				  else
				  {
					  listView.setOnTouchListener(null);
				  }
			  }
		});
    }
    
	@Override
	public void onRefresh()
	{
		this.helper.setRefreshLayout(swipeLayout);	//Set the layout that should be updated once the Refresh task is done executing.
		this.helper.refreshFeed(helper.feedDetailsListAdapter.feed.getFeedId());	//Start the refresh process.
	}
	
	private void slideAnimation(View viewToSlide, int anim)
    {
    	Animation animation = AnimationUtils.loadAnimation(getActivity(), anim);
    	animation.setDuration(100);
    	viewToSlide.startAnimation(animation);
    }
}
