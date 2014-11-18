package com.podhoarder.fragment;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.listener.EpisodeMultiChoiceModeListener;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.object.FeedImage;
import com.podhoarder.object.ReverseInterpolator;
import com.podhoarder.service.PodHoarderService;
import com.podhoarder.util.ImageUtils;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.ToastMessages;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-03.
 */
public class ListFragment extends CollectionFragment implements PodHoarderService.StateChangedListener {

    //Episodes List
    private ListView mListView;

    private ImageView mPodcastBanner;
    private Palette.Swatch mColorSwatch;

    private EpisodeMultiChoiceModeListener mListSelectionListener;

    int mSelectedListItemTop = Integer.MAX_VALUE, mSelectedListItemIndex = -1;

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
        ViewTreeObserver vto = mContentView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mSelectedListItemTop != Integer.MAX_VALUE && mSelectedListItemIndex != -1) {   //A Check to see if the selected item variables were set.
                    onFragmentRedrawn();
                }
            }
        });
        mPodcastBanner = (ImageView) mContentView.findViewById(R.id.podcast_banner);

        if (mCurrentFilter == null) {
            mCurrentFilter = ListFilter.values()[getArguments().getInt("filter")];
            if (mCurrentFilter == ListFilter.FEED) {
                mCurrentFilter.setFeedId(getArguments().getInt("feedId"));
                Feed f = mDataManager.getFeed(mCurrentFilter.getFeedId());
                mPodcastBanner.setMinimumHeight(mDataManager.mFeedsGridAdapter.mGridItemSize);
                mPodcastBanner.setMinimumWidth(mDataManager.mFeedsGridAdapter.mGridItemSize);
                mPodcastBanner.setImageBitmap(f.getFeedImage().largeImage());
                mColorSwatch = f.getFeedImage().palette().getDarkVibrantSwatch();
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
        if (!mSearchEnabled && mCurrentFilter.getSearchString().isEmpty()) {
            FeedImage img = mDataManager.getFeed(mCurrentFilter.getFeedId()).getFeedImage();
            mColorSwatch = img.palette().getDarkVibrantSwatch();
            mPodcastBanner.setImageBitmap(img.largeImage());
        }

        setToolbarTransparent(true);
        trySetScrimPadding();
    }

    @Override
    public void onFragmentRedrawn() {
        reverseListItemSelectionAnimation(mSelectedListItemIndex, mSelectedListItemTop);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mCurrentFilter == ListFilter.FEED)
            mSearchView.setQueryHint(getString(R.string.search) + " " + mDataManager.getFeed(mCurrentFilter.getFeedId()).getTitle());
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

            this.mListView.setAdapter(mDataManager.mEpisodesListAdapter);
            this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterview, View v, int pos, long id) {
                    Episode currentEp = (Episode) mListView.getItemAtPosition(pos);
                    int pref = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_defaultEpisodeAction", "1"));
                    if (pref == 1) {  //If the preference is set to open episode info on click, we play the animations.
                        onListItemClicked(mListView, v, pos, currentEp);
                    }
                    else {
                        if (currentEp.isDownloaded() || NetworkUtils.isOnline(getActivity()))
                            ((LibraryActivity) getActivity()).getPlaybackService().playEpisode(currentEp);
                        else
                            ToastMessages.PlaybackFailed(getActivity()).show();
                    }
                }
            });

            mScrollListener = new AbsListView.OnScrollListener() {
                private int bannerScrollDelta,
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
                            int maxToolbarTranslationY = -(mToolbarSize + mStatusBarHeight);                           //The toolbar should only be allowed to move until it is fully off screen.

                            if (firstItemVisible) {

                                int bannerTop = (mListView.getChildAt(0).getTop() - mPodcastBanner.getMeasuredHeight());   //Calculate the distance to move by subtracting the toolbar height + gridview padding from the top line.
                                int toolbarTop = (mListView.getChildAt(0).getTop() - (mToolbarSize+mStatusBarHeight));

                                if (bannerTop < (mToolbarSize + mStatusBarHeight) && bannerTop > maxBannerTranslationY) {//If we are within the bounds where the app bar needs to move we should apply the moved distance.
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

            //this.mListView.setTranslationY(displaymetrics.widthPixels);
            //this.mListView.animate().translationY(0f).setDuration(150).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    public void goToEpisodeFragment(Episode ep) {
        int pos = getViewPosition(ep);
        onListItemClicked(mListView, mListView.getChildAt(pos), pos, ep);
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

        mSelectedListItemIndex = clickedViewPos;
        mSelectedListItemTop = clickedView.getTop();

        //bannerMoveAnimation(mPodcastBanner, ANIMATION_DURATION).start();
        fadeOtherRows(clickedViewPos, ANIMATION_DURATION, true);
        fadeComponents(clickedView,ANIMATION_DURATION);
        clickedView.startAnimation(rowMoveAnimation(clickedView, displaymetrics, ANIMATION_DURATION, ep));
        rowBackgroundColorAnimation(clickedView,ANIMATION_DURATION).start();
        rowTextColorAnimation(clickedView, ANIMATION_DURATION).start();
        rowTextSizeAnimation(clickedView, displaymetrics, ANIMATION_DURATION).start();
        rowTextAlphaAnimation(clickedView, ANIMATION_DURATION-100).start();
        rowScaleHeightAnimation(clickedView, ANIMATION_DURATION).start();
        rowScaleWidthAnimation(clickedView.findViewById(R.id.list_episode_row_title),displaymetrics, ANIMATION_DURATION).start();

        rowLeftPaddingAnimation(clickedView,ANIMATION_DURATION).start();
        rowRightPaddingAnimation(clickedView,ANIMATION_DURATION).start();

        mToolbarContainer.animate().translationY(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(ANIMATION_DURATION).start();
        mPodcastBanner.animate().translationY(0f).setDuration(ANIMATION_DURATION).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        setDrawerIconEnabled(false,250);
    }

    private void reverseListItemSelectionAnimation(int clickedViewPos, int clickedViewTop) {
        final int ANIMATION_DURATION = 350;
        if (!isDrawerIconEnabled()) {
            setDrawerIconEnabled(true,ANIMATION_DURATION-100);
        }
        View clickedView = mListView.getChildAt(clickedViewPos);
        if (clickedView != null) {
            ViewGroup.LayoutParams layoutParams = clickedView.getLayoutParams();
            layoutParams.height = mToolbarSize*2;
            clickedView.setLayoutParams(layoutParams);

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            fadeOtherRows(clickedViewPos, ANIMATION_DURATION, false);

            reverseFadeComponents(clickedView, ANIMATION_DURATION);

            clickedView.startAnimation(reverseRowMoveAnimation(clickedView, displaymetrics, ANIMATION_DURATION));

            Animator rowScaleHeightAnimator = rowScaleHeightAnimation(clickedView, ANIMATION_DURATION);
            rowScaleHeightAnimator.setInterpolator(new ReverseInterpolator());
            rowScaleHeightAnimator.start();

            Animator backgroundColorAnimator = rowBackgroundColorAnimation(clickedView,ANIMATION_DURATION);
            backgroundColorAnimator.setInterpolator(new ReverseInterpolator());
            backgroundColorAnimator.start();

            reverseRowTextColorAnimation(clickedView,ANIMATION_DURATION).start();

            Animator rowTextSizeAnimator = rowTextSizeAnimation(clickedView, displaymetrics, ANIMATION_DURATION);
            rowTextSizeAnimator.setInterpolator(new ReverseInterpolator());
            rowTextSizeAnimator.start();

            clickedView.findViewById(R.id.list_episode_row_title).setAlpha(0f);
            clickedView.findViewById(R.id.list_episode_row_subtitle).setAlpha(0f);
            Animator rowAlphaAnimator = rowTextAlphaAnimation(clickedView, ANIMATION_DURATION-100);
            rowAlphaAnimator.setInterpolator(new ReverseInterpolator());
            rowAlphaAnimator.start();
        }

    }

    private int getViewPosition(int pos) {
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

    private int getViewPosition(Episode ep) {

        for (int i=mListView.getFirstVisiblePosition(); i<=mListView.getLastVisiblePosition(); i++) {
            if (mListView.getAdapter().getItem(i).equals(ep)) {
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

    private Animator bannerMoveAnimation(ImageView banner, int duration) {
        ValueAnimator bannerTranslationAnimator = ValueAnimator.ofFloat(banner.getTranslationY(), 0f);
        bannerTranslationAnimator.setDuration(duration);
        bannerTranslationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        return bannerTranslationAnimator;
    }

    private void fadeOtherRows(int clickedViewPos, int duration, boolean fadeOut) {
        for (int i=mListView.getFirstVisiblePosition(); i<=mListView.getLastVisiblePosition(); i++) {
            int relativePos = getViewPosition(i);
            if (relativePos != getViewPosition(clickedViewPos)) {
                if (fadeOut)
                    mListView.getChildAt(relativePos).animate().alpha(0f).setDuration(duration / 2).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                else {
                    mListView.getChildAt(relativePos).setAlpha(0f);
                    mListView.getChildAt(relativePos).animate().alpha(1f).setDuration(duration / 2).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
        }
    }

    private Animation rowMoveAnimation(View clickedView, DisplayMetrics displaymetrics, int duration, final Episode ep) {
        TranslateAnimation moveAnim =  new TranslateAnimation(
                0, 0,
                TranslateAnimation.ABSOLUTE, (0 - clickedView.getLeft()),
                0, 0,
                TranslateAnimation.ABSOLUTE, ((displaymetrics.widthPixels - ImageUtils.pixelsToDip(getActivity(),72)) - clickedView.getTop()));
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
    private Animation reverseRowMoveAnimation(View clickedView, DisplayMetrics metrics, int duration) {

        TranslateAnimation moveAnim =  new TranslateAnimation(
                0, 0,
                0, 0,
                TranslateAnimation.ABSOLUTE, ((metrics.widthPixels - ImageUtils.pixelsToDip(getActivity(),72)) - clickedView.getTop()),
                0,0);
        moveAnim.setDuration(duration);
        return moveAnim;
    }
    private Animator rowTextAlphaAnimation(View clickedView, int duration) {
        final TextView title = (TextView) clickedView.findViewById(R.id.list_episode_row_title);
        final TextView timestamp = (TextView) clickedView.findViewById(R.id.list_episode_row_subtitle);

        ValueAnimator textAlphaAnimator = ValueAnimator.ofFloat(1f, 0f);
        textAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                title.setAlpha((Float) animator.getAnimatedValue());
                timestamp.setAlpha((Float) animator.getAnimatedValue());
            }

        });
        textAlphaAnimator.setDuration(duration / 3);
        textAlphaAnimator.setStartDelay((duration/3)*2);
        return textAlphaAnimator;
    }
    private Animator rowTextSizeAnimation(View clickedView, DisplayMetrics displayMetrics, int duration) {
        TextView text = (TextView)clickedView.findViewById(R.id.list_episode_row_title);
        float currentTextSize = text.getTextSize()/displayMetrics.density;
        ObjectAnimator textSizeAnimator = ObjectAnimator.ofFloat(text,"textSize",currentTextSize,currentTextSize*1.5f);
        textSizeAnimator.setDuration(duration);
        return textSizeAnimator;
   }
    private Animator rowScaleWidthAnimation(final View clickedView, DisplayMetrics displayMetrics, int duration) {
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
    private Animator rowScaleHeightAnimation(final View clickedView, int duration) {

        ValueAnimator anim = ValueAnimator.ofInt(clickedView.getMeasuredHeight(), mToolbarSize * 2);
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
    private Animator rowLeftPaddingAnimation(final View clickedView, int duration) {
        ValueAnimator anim = ValueAnimator.ofInt(0, 88);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                clickedView.setPadding(val,0,0,0);
            }
        });


        anim.setDuration(duration);
        return anim;
    }
    private Animator rowRightPaddingAnimation(final View clickedView, int duration) {
        ValueAnimator anim = ValueAnimator.ofInt(55, 16);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                clickedView.setPadding(0,0,val,0);
            }
        });


        anim.setDuration(duration);
        return anim;
    }
    private Animator rowBackgroundColorAnimation(final View clickedView, int duration) {

        Integer backgroundColorFrom = getResources().getColor(R.color.windowBackground);
        Integer backgroundColorTo = mColorSwatch.getRgb();
        ValueAnimator backgroundColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), backgroundColorFrom, backgroundColorTo);
        backgroundColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                clickedView.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        backgroundColorAnimation.setDuration(duration);
        return backgroundColorAnimation;
    }
    private void fadeComponents(View clickedView, int duration) {
        clickedView.findViewById(R.id.list_episode_row_icon).animate().alpha(0f).setDuration(duration/2).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        clickedView.findViewById(R.id.list_episode_row_checkbox).animate().alpha(0f).setDuration(duration/2).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        clickedView.findViewById(R.id.list_episode_row_secondary_action).animate().alpha(0f).setDuration(duration/2).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }
    private void reverseFadeComponents(View clickedView, int duration) {
        View icon = clickedView.findViewById(R.id.list_episode_row_icon);
        View checkbox = clickedView.findViewById(R.id.list_episode_row_checkbox);
        View secondaryAction = clickedView.findViewById(R.id.list_episode_row_secondary_action);

        icon.setAlpha(0f);
        icon.animate().alpha(1f).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator()).start();

        checkbox.setAlpha(0f);
        checkbox.animate().alpha(1f).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator()).start();

        secondaryAction.setAlpha(0f);
        secondaryAction.animate().alpha(.56f).setDuration(duration).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }
    private Animator rowTextColorAnimation(final View clickedView, int duration) {
        final TextView title = (TextView) clickedView.findViewById(R.id.list_episode_row_title);
        final TextView timestamp = (TextView) clickedView.findViewById(R.id.list_episode_row_subtitle);

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
    private Animator reverseRowTextColorAnimation(final View clickedView, int duration) {
        final TextView title = (TextView) clickedView.findViewById(R.id.list_episode_row_title);
        final TextView timestamp = (TextView) clickedView.findViewById(R.id.list_episode_row_subtitle);

        Integer textColorFrom = mColorSwatch.getBodyTextColor();
        Integer textColorTo = title.getCurrentTextColor();

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
