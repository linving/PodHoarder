package com.podhoarder.fragment;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.listener.EpisodeMultiChoiceModeListener;
import com.podhoarder.object.Episode;
import com.podhoarder.service.PodHoarderService;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-03.
 */
public class ListFragment extends CollectionFragment implements PodHoarderService.StateChangedListener {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.ListFragment";
        super.onCreateView(inflater, container, savedInstanceState);

        mContentView = inflater.inflate(R.layout.activity_list, container, false);
        mPodcastBanner = (ImageView) mContentView.findViewById(R.id.podcast_banner);


        if (mCurrentFilter == null) {
            mCurrentFilter = ListFilter.values()[getArguments().getInt("filter")];
            if (mCurrentFilter == ListFilter.FEED) {
                mCurrentFilter.setFeedId(getArguments().getInt("feedId"));
                mPodcastBanner.setMinimumHeight(mDataManager.mFeedsGridAdapter.mGridItemSize);
                mPodcastBanner.setMinimumWidth(mDataManager.mFeedsGridAdapter.mGridItemSize);
                mPodcastBanner.setImageBitmap(mDataManager.getFeed(mCurrentFilter.getFeedId()).getFeedImage().largeImage());
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
        super.onResume();
        if (!mSearchEnabled && mCurrentFilter.getSearchString().isEmpty())
            mPodcastBanner.setImageBitmap(mDataManager.getFeed(mCurrentFilter.getFeedId()).getFeedImage().largeImage());

        setToolbarTransparent(true);
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
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_top);
            //LayoutAnimationController animationController = new LayoutAnimationController(animation, 0.2f);

            this.mListView.setAdapter(mDataManager.mEpisodesListAdapter);
            this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterview, View v, int pos, long id) {
                    Episode currentEp = (Episode) mListView.getItemAtPosition(pos);
                    onListItemClicked(mListView,v,pos, currentEp);
                    //((LibraryActivity)getActivity()).startEpisodeActivity(currentEp);
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

                            int maxBannerTranslationY = -(mPodcastBanner.getMeasuredHeight());    //The banner should only be allowed to move until it is fully off screen.
                            int maxToolbarTranslationY = -(mToolbarSize);                           //The toolbar should only be allowed to move until it is fully off screen.

                            if (firstItemVisible) {

                                int bannerTop = (mListView.getChildAt(0).getTop() - mPodcastBanner.getMeasuredHeight());   //Calculate the distance to move by subtracting the toolbar height + gridview padding from the top line.
                                int toolbarTop = (mListView.getChildAt(0).getTop() - (mToolbarSize));

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

                                mToolbarContainer.setTranslationY(toolbarScrollDelta);  //Move the toolbar vertically.
                            } else {
                                mPodcastBanner.setTranslationY(maxBannerTranslationY);  //Move the banner vertically.

                                mToolbarContainer.setTranslationY(maxToolbarTranslationY);
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
                this.mListView.setPadding(0, displaymetrics.widthPixels, 0, 0);
            else
                this.mListView.setPadding(0, 0, 0, 0);

            //this.mListView.setLayoutAnimation(animationController);

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

    private void onListItemClicked(AbsListView listView, final View clickedView, int clickedViewPos, final Episode ep) {
        final int ANIMATION_DURATION = 250;

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        mToolbarContainer.animate().translationY(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(ANIMATION_DURATION).start();
        mPodcastBanner.startAnimation(bannerMoveAnimation(mPodcastBanner, ANIMATION_DURATION));

        fadeOtherRows(clickedViewPos, ANIMATION_DURATION);

        clickedView.startAnimation(rowMoveAnimation(clickedView, ANIMATION_DURATION, ep));
        rowBackgroundColorAnimation(clickedView,ANIMATION_DURATION).start();
        rowTextColorAnimation(clickedView, ANIMATION_DURATION).start();
        rowTextSizeAnimation(clickedView, displaymetrics, ANIMATION_DURATION).start();
        rowTextAlphaAnimation(clickedView, ANIMATION_DURATION).start();
        rowScaleHeightAnimation(clickedView, ANIMATION_DURATION).start();
        rowScaleWidthAnimation(clickedView,displaymetrics, ANIMATION_DURATION).start();
        rowTopPaddingAnimation(clickedView,ANIMATION_DURATION).start();
    }

    private int getViewPosition(int pos)
    {
        final int numVisibleChildren = this.mListView.getChildCount();
        final int firstVisiblePosition = this.mListView.getFirstVisiblePosition();

        for ( int i = 0; i < numVisibleChildren; i++ ) {
            int positionOfView = firstVisiblePosition + i;

            if (positionOfView == pos) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void onServiceConnected() {
        mPlaybackService = ((LibraryActivity) getActivity()).getPlaybackService();
        mPlaybackService.setStateChangedListener(ListFragment.this);
    }

    public Animation bannerMoveAnimation(ImageView banner, int duration) {
        TranslateAnimation moveAnim =  new TranslateAnimation(
                0, 0,
                0, 0,
                0, 0,
                TranslateAnimation.ABSOLUTE, -(banner.getMeasuredHeight()+20f));
        moveAnim.setDuration(duration);
        moveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        return moveAnim;
    }

    public void fadeOtherRows(int clickedViewPos, int duration) {
        for (int i=mListView.getFirstVisiblePosition(); i<=mListView.getLastVisiblePosition(); i++) {
            int relativePos = getViewPosition(i);
            if (relativePos != getViewPosition(clickedViewPos))
                mListView.getChildAt(relativePos).animate().alpha(0f).setDuration(duration/2).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        }
    }
    public Animation rowMoveAnimation(View clickedView, int duration, final Episode ep) {
        TranslateAnimation moveAnim =  new TranslateAnimation(
                0, 0,
                TranslateAnimation.ABSOLUTE, (0 - clickedView.getLeft()),
                0, 0,
                TranslateAnimation.ABSOLUTE, (0 - clickedView.getTop()));
        moveAnim.setDuration(duration);
        moveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        moveAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ((LibraryActivity) getActivity()).startEpisodeActivity(ep);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return moveAnim;
    }

    public Animator rowTextAlphaAnimation(View clickedView, int duration) {
        final TextView title = (TextView) clickedView.findViewById(R.id.list_episode_row_episodeName);
        final TextView timestamp = (TextView) clickedView.findViewById(R.id.list_episode_row_episodeAge);

        ValueAnimator textScaleAnimator = ValueAnimator.ofFloat(1f, 0f);
        textScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                title.setAlpha((Float) animator.getAnimatedValue());
                timestamp.setAlpha((Float) animator.getAnimatedValue());
            }

        });
        textScaleAnimator.setDuration(duration / 2);
        textScaleAnimator.setStartDelay(duration/2);
        return textScaleAnimator;
    }
    public Animator rowTextSizeAnimation(View clickedView, DisplayMetrics displayMetrics, int duration) {
        float currentTextSize = ((TextView)clickedView.findViewById(R.id.list_episode_row_episodeName)).getTextSize()/displayMetrics.density;
        ObjectAnimator textSizeAnimator = ObjectAnimator.ofFloat(clickedView.findViewById(R.id.list_episode_row_episodeName),"textSize",currentTextSize,currentTextSize*1.5f);
        textSizeAnimator.setDuration(duration);
        return textSizeAnimator;
   }
    public Animator rowScaleWidthAnimation(final View clickedView, DisplayMetrics displayMetrics, int duration) {
        ValueAnimator anim = ValueAnimator.ofInt(clickedView.getMeasuredWidth(), displayMetrics.widthPixels);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = clickedView.getLayoutParams();
                layoutParams.width = val;
                clickedView.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(duration);
        return anim;
    }
    public Animator rowScaleHeightAnimation(final View clickedView, int duration) {

        ValueAnimator anim = ValueAnimator.ofInt(clickedView.getMeasuredHeight(), mToolbarSize * 3);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = clickedView.getLayoutParams();
                layoutParams.height = val;
                clickedView.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(duration);
        return anim;
    }
    public Animator rowTopPaddingAnimation(final View clickedView, int duration) {
        ValueAnimator anim = ValueAnimator.ofInt(0, mToolbarSize+10);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                clickedView.setPadding((val/3),val,0,0);
            }
        });
        anim.setDuration(duration);
        return anim;
    }
    public Animator rowBackgroundColorAnimation(final View clickedView, int duration) {

        Integer backgroundColorFrom = getResources().getColor(R.color.windowBackground);
        Integer backgroundColorTo = ((BaseActivity)getActivity()).getCurrentPrimaryColorDark();
        ValueAnimator backgroundColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), backgroundColorFrom, backgroundColorTo);
        backgroundColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                clickedView.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        backgroundColorAnimation.setDuration(duration);

        clickedView.findViewById(R.id.list_episode_row_feed_image).animate().alpha(0f).setDuration(duration/2).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        clickedView.findViewById(R.id.list_episode_row_checkbox).animate().alpha(0f).setDuration(duration/2).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        clickedView.findViewById(R.id.list_episode_row_info).animate().alpha(0f).setDuration(duration/2).setInterpolator(new AccelerateDecelerateInterpolator()).start();

        return backgroundColorAnimation;
    }
    public Animator rowTextColorAnimation(final View clickedView, int duration) {
        final TextView title = (TextView) clickedView.findViewById(R.id.list_episode_row_episodeName);
        final TextView timestamp = (TextView) clickedView.findViewById(R.id.list_episode_row_episodeAge);

        Integer textColorFrom = title.getCurrentTextColor();
        Integer textColorTo = getResources().getColor(android.R.color.primary_text_dark);

        ValueAnimator textColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), textColorFrom, textColorTo);
        textColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                title.setTextColor((Integer) animator.getAnimatedValue());
                timestamp.setTextColor((Integer)animator.getAnimatedValue());
            }

        });
        textColorAnimation.setDuration(duration);
        return textColorAnimation;
    }
}
