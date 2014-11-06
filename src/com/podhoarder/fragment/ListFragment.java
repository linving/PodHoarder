package com.podhoarder.fragment;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.listener.EpisodeMultiChoiceModeListener;
import com.podhoarder.object.Episode;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.ToastMessages;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-03.
 */
public class ListFragment extends LibraryFragment implements PodHoarderService.StateChangedListener {

    //Episodes List
    private ListView mListView;

    private ImageView mPodcastBanner;

    private EpisodeMultiChoiceModeListener mListSelectionListener;

    public static ListFragment newInstance(ListFilter filter) {
        ListFragment f = new ListFragment();
        Bundle b = new Bundle();
        b.putInt("filter", filter.ordinal());
        b.putInt("feedId", filter.getFeedId());
        b.putString("search", filter.getSearchString());
        f.setArguments(b);
        return f;
    }

    public ListFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.ListFragment";
        super.onCreateView(inflater, container, savedInstanceState);

        mContentView = inflater.inflate(R.layout.activity_list, container, false);
        mPodcastBanner = (ImageView) mContentView.findViewById(R.id.podcast_banner);

        mToolbar = ((BaseActivity) getActivity()).mToolbar;
        mToolbarSize = mToolbar.getMinimumHeight();

        if (mCurrentFilter == null) {
            mCurrentFilter = ListFilter.values()[getArguments().getInt("filter")];
            if (mCurrentFilter == ListFilter.FEED) {
                mCurrentFilter.setFeedId(getArguments().getInt("feedId"));
                mPodcastBanner.setMinimumHeight(mDataManager.mFeedsGridAdapter.mGridItemSize);
                mPodcastBanner.setMinimumWidth(mDataManager.mFeedsGridAdapter.mGridItemSize);
                mPodcastBanner.setImageBitmap(mDataManager.getFeed(mCurrentFilter.getFeedId()).getFeedImage().largeImage());
//                mPodcastBanner.setTransitionName(getString(R.string.transition_banner) + mCurrentFilter.getFeedId());
            }
            mCurrentFilter.setSearchString(getArguments().getString("search"));
            if (!mCurrentFilter.getSearchString().isEmpty())
                mSearchEnabled = true;
        }
        if (mPlaybackService != null)
            onServiceConnected();
        mDataManager.switchLists(mCurrentFilter);

        populate();

        ((LibraryActivity) getActivity()).setCurrentFragment(this);

        return mContentView;
    }

    @Override
    public void onResume() {
        if (!mSearchEnabled && mCurrentFilter.getSearchString().isEmpty())
            mPodcastBanner.setImageBitmap(mDataManager.getFeed(mCurrentFilter.getFeedId()).getFeedImage().largeImage());
        super.onResume();
    }

    @Override
    public void setFilter(ListFilter filterToSet) {
        mCurrentFilter = filterToSet;
        if (mCurrentFilter == ListFilter.FEED) {
            mPodcastBanner.setImageBitmap(mDataManager.getFeed(mCurrentFilter.getFeedId()).getFeedImage().largeImage());
        }
        mDataManager.switchLists(filterToSet);
    }

    public ListFilter getFilter() {
        return mCurrentFilter;
    }

    private void setupListView() {
        mListView = (ListView) mContentView.findViewById(R.id.episodesListView);
        if (this.mDataManager.hasPodcasts()) {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_fade_in);
            LayoutAnimationController animationController = new LayoutAnimationController(animation, 0.2f);

            this.mListView.setAdapter(mDataManager.mEpisodesListAdapter);
            this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterview, View v, int pos, long id) {
                    Episode currentEp = (Episode) mListView.getItemAtPosition(pos);
                    if (currentEp.isDownloaded() || NetworkUtils.isOnline(getActivity()))
                        mPlaybackService.playEpisode((Episode) mListView.getItemAtPosition(pos));
                    else
                        ToastMessages.PlaybackFailed(getActivity()).show();
                }

            });

            mScrollListener = new AbsListView.OnScrollListener() {
                private int bannerScrollDelta
                        ,
                        toolbarScrollDelta;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (mListView != null && mPodcastBanner != null && mToolbar != null) {
                        if (mListView.getChildCount() > 0) {
                            // check if the first item of the list is visible
                            boolean firstItemVisible = mListView.getFirstVisiblePosition() == 0;

                            int maxBannerTranslationY = -(mPodcastBanner.getMeasuredHeight() + mToolbarSize);    //The banner should only be allowed to move until it is fully off screen.
                            int maxToolbarTranslationY = -(mToolbarSize);                           //The toolbar should only be allowed to move until it is fully off screen.

                            if (firstItemVisible) {

                                int bannerTop = (mListView.getChildAt(0).getTop() - mPodcastBanner.getMeasuredHeight() - mToolbarSize);   //Calculate the distance to move by subtracting the toolbar height + gridview padding from the top line.
                                int toolbarTop = (mListView.getChildAt(0).getTop() - (mToolbarSize + 4));

                                if (bannerTop < mToolbarSize && bannerTop > maxBannerTranslationY) {//If we are within the bounds where the app bar needs to move we should apply the moved distance.
                                    bannerScrollDelta = bannerTop;
                                } else if (bannerTop <= maxBannerTranslationY) { //If the toolbar top is above the max translation line it should just align at the same height and stay there.
                                    bannerScrollDelta = maxBannerTranslationY * 2;
                                } else
                                    bannerScrollDelta = 0;    //Default position.

                                if (toolbarTop < 0 && toolbarTop > maxToolbarTranslationY) {//If we are within the bounds where the app bar needs to move we should apply the moved distance.
                                    toolbarScrollDelta = toolbarTop;
                                } else if (toolbarTop <= maxToolbarTranslationY) { //If the toolbar top is above the max translation line it should just align at the same height and stay there.
                                    toolbarScrollDelta = maxToolbarTranslationY;
                                } else
                                    toolbarScrollDelta = 0;    //Default position.

                                mPodcastBanner.setTranslationY(bannerScrollDelta / 2);  //Move the banner vertically.
                                mPodcastBanner.setAlpha((Math.abs(maxBannerTranslationY) - Math.abs((float) bannerScrollDelta)) / Math.abs(maxBannerTranslationY));    //Fade out the banner. When it is fully off screen that alpha is .0f, and when it is fully visible it's 1f.

                                mToolbar.setTranslationY(toolbarScrollDelta);  //Move the toolbar vertically.
                                mToolbar.setAlpha((Math.abs(maxToolbarTranslationY) - Math.abs((float) toolbarScrollDelta)) / Math.abs(maxToolbarTranslationY));    //Fade out the toolbar. When it is fully off screen that alpha is .0f, and when it is fully visible it's 1f.
                            } else {
                                mPodcastBanner.setTranslationY(maxBannerTranslationY);  //Move the banner vertically.
                                mPodcastBanner.setAlpha(1f);    //Fade out the banner. When it is fully off screen that alpha is .0f, and when it is fully visible it's 1f.

                                mToolbar.setTranslationY(maxToolbarTranslationY);
                            }
                            if (mListSelectionListener.isActive()) {
                                mListSelectionListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                            }

                        }
                    }

                }

            };
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            if (!mSearchEnabled)
                this.mListView.setPadding(0, displaymetrics.widthPixels + mToolbarSize, 0, 0);
            else
                this.mListView.setPadding(0, mToolbarSize, 0, 0);

            this.mListView.setLayoutAnimation(animationController);

            this.mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            this.mListSelectionListener = new EpisodeMultiChoiceModeListener(getActivity(), this.mListView);
            this.mListView.setMultiChoiceModeListener(this.mListSelectionListener);

            this.mListView.setOnScrollListener(mScrollListener);
        }
    }

    private void populate() {
        if (mDataManager.hasPodcasts()) {
            setupListView();
        }

    }

    @Override
    public void onServiceConnected() {
        mPlaybackService = ((LibraryActivity) getActivity()).getPlaybackService();
        mPlaybackService.setStateChangedListener(ListFragment.this);

        setupFAB();
    }

    @Override
    public void onStateChanged(PodHoarderService.PlayerState newPlayerState) {
        switch (newPlayerState) {
            case PLAYING:
                mFAB.setPlaying(true);
                break;
            case PAUSED:
                mFAB.setPlaying(false);
                break;
            case LOADING:
                break;
        }
    }
}
