package com.podhoarder.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.adapter.GridAdapter;
import com.podhoarder.listener.GridActionModeCallback;
import com.podhoarder.util.Constants;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Emil on 2014-10-28.
 */
public class GridFragment extends LibraryFragment implements SwipeRefreshLayout.OnRefreshListener, GridAdapter.GridItemClickListener, LibraryActivity.onFirstFeedAddedListener {

    //Feeds Grid
    private GridView mGridView;
    private int mGridItemSize;
    private GridActionModeCallback mActionModeCallback;
    private ActionMode mActionMode;

    //SwipeRefreshLayout
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static GridFragment newInstance(ListFilter filter) {
        GridFragment f = new GridFragment();
        f.setFilter(ListFilter.ALL);
        return f;
    }

    //OVERRIDES
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.GridFragment";
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.activity_grid, container, false);

        mCurrentFilter = ListFilter.ALL;

        if (mDataManager.hasPodcasts())
            mDataManager.mFeedsGridAdapter.setGridItemClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.windowBackground, R.color.colorAccent, R.color.windowBackground);

        populate();

        ((LibraryActivity)getActivity()).setOnFirstFeedAddedListener(this);

        mToolbar = ((BaseActivity)getActivity()).mToolbar;
        mToolbarSize = mToolbar.getMinimumHeight();

        ((LibraryActivity)getActivity()).setCurrentFragment(this);

        return mContentView;
    }

    @Override
    public void onResume() {
        if (mPlaybackService != null) {
            onServiceConnected();
        }
        super.onResume();
    }

    @Override
    public void onRefresh() {
        mDataManager.refreshFeeds(mSwipeRefreshLayout);
    }

    @Override
    public void onGridItemClicked(final int pos, final int feedId) {
        mToolbar.setAlpha(1.0f);
        mToolbar.animate().translationY(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).start();
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_fade_out);
        animation.setFillAfter(true);
        int fixedPos = getGridChildPositionWithIndex(pos);

        for (int i = mGridView.getFirstVisiblePosition(); i < mGridView.getChildCount(); i++) {
            if (i != fixedPos) {
                mGridView.getChildAt(i).startAnimation(animation);
            }
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                onGridFaded(pos, feedId);

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onFirstFeedAdded() {

        mDataManager.reloadListData(false);
        populate();
        if (mDataManager.hasPodcasts()) mFAB.setVisibility(View.VISIBLE);
    }

    //VIEW SETUPS
    private void setupGridView() {

        mGridView = (GridView) mContentView.findViewById(R.id.feedsGridView);
        if (this.mDataManager.hasPodcasts()) {
            //TODO: Make a nicer solution
            if (mGridItemSize == 0)
                mGridItemSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(Constants.SETTINGS_KEY_GRIDITEMSIZE,"-1"));

            mGridView.setColumnWidth(mGridItemSize);

            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_fade_in);
            GridLayoutAnimationController animationController = new GridLayoutAnimationController(animation, 0.15f, 0.45f);

            mDataManager.mFeedsGridAdapter.setLoadingViews(setupLoadingViews());
            mGridView.setAdapter(mDataManager.mFeedsGridAdapter);

            //mGridView.setLayoutAnimation(animationController);

            mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mActionModeCallback = new GridActionModeCallback(getActivity(), mGridView);
            mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
                    if (mActionMode == null || !mActionModeCallback.isActive()) {
                        mActionMode = getActivity().startActionMode(mActionModeCallback);
                        mDataManager.mFeedsGridAdapter.setActionModeVars(mActionMode, mActionModeCallback);
                    }
                    mActionModeCallback.onItemCheckedStateChanged(pos, !((CheckBox) v.findViewById(R.id.feeds_grid_item_checkmark)).isChecked());
                    return true;
                }
            });

            mScrollListener = new AbsListView.OnScrollListener() {
                private int scrollDelta;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    boolean enable = false;
                    if (mGridView != null && mGridView.getChildCount() > 0) {
                        // check if the first item of the list is visible
                        boolean firstItemVisible = mGridView.getFirstVisiblePosition() == 0;
                        // check if the top of the first item is visible
                        boolean topOfFirstItemVisible = mGridView.getChildAt(0).getTop() > 0;
                        // enabling or disabling the refresh layout
                        enable = firstItemVisible && topOfFirstItemVisible;
                        if (mToolbar != null) {
                            int maxTranslationY = -(mToolbarSize+4);    //The toolbar should only be allowed to move until it is fully off screen.

                            int toolbarTop = (mGridView.getChildAt(0).getTop() - (mToolbarSize+4));   //Calculate the distance to move by subtracting the toolbar height + gridview padding from the top line.

                            if (toolbarTop < 0 && toolbarTop > maxTranslationY) {//If we are within the bounds where the app bar needs to move we should apply the moved distance.
                                scrollDelta = toolbarTop;
                                enable = false;
                            }
                            else if (toolbarTop <= maxTranslationY) { //If the toolbar top is above the max translation line it should just align at the same height and stay there.
                                scrollDelta = maxTranslationY;
                                enable = false;
                            }
                            else
                                scrollDelta = 0;    //Default position.

                            mToolbar.setTranslationY(scrollDelta);  //Move the toolbar vertically.
                            mToolbar.setAlpha((Math.abs(maxTranslationY)-Math.abs((float)scrollDelta))/Math.abs(maxTranslationY));    //Fade out the toolbar. When it is fully off screen that alpha is .0f, and when it is fully visible it's 1f.
                        }
                    }

                    mSwipeRefreshLayout.setEnabled(enable);
                }

            };

            mGridView.setOnScrollListener(mScrollListener);

        }

    }

    private List<View> setupLoadingViews() {
        //TODO: Replace these with awesome status indicators in normal grid items.
        List<View> views = new ArrayList<View>();    //This is an ugly solution but in order to use the GridViews LayoutParams the loading views must be inflated here.
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);    //get the inflater service.
        for (int i = 0; i < Constants.NEW_SEARCH_RESULT_LIMIT; i++)    //Inflate a collection of Loading views, same size as the maximum amount Search Results.
        {
            views.add(inflater.inflate(R.layout.feeds_grid_loading_item, mGridView, false));    //Inflate the "loading" grid item to show while data is downloaded
        }
        return views;
    }

    private void setupEmptyText() {
        TextView mEmptyText = (TextView) mContentView.findViewById(R.id.emptyLibraryString);
        if (mDataManager.hasPodcasts()) {
            mEmptyText.setVisibility(View.GONE);
        }
        else {
            if (mEmptyText.getVisibility() != View.VISIBLE) {
                mEmptyText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void populate() {
        setupEmptyText();
        if (mDataManager.hasPodcasts()) {
            setupGridView();
            mGridView.setVisibility(View.VISIBLE);
        }

    }

    public void onGridFaded(final int pos, final int feedId) {
        final int actualPos = getGridChildPositionWithIndex(pos);
        View v = mGridView.getChildAt(actualPos);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        float f = (float)displaymetrics.widthPixels / v.getMeasuredWidth(); //Calculate the percent value of the grid image as opposed to the entire screen width (which will be the final width & height)


        ScaleAnimation scaleAnim = new
                ScaleAnimation(1.0f, f, 1.0f, f, 1f, 1f);
        scaleAnim.setFillAfter(true);
        scaleAnim.setDuration(250);
        TranslateAnimation moveAnim =  new TranslateAnimation(0, 0,
                TranslateAnimation.ABSOLUTE, (0 - v.getLeft())/f, 0, 0,
                TranslateAnimation.ABSOLUTE, (mToolbarSize - v.getTop())/f);
        moveAnim.setDuration(250);
        moveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        moveAnim.setFillAfter(true);
        moveAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onImageRepositioned(actualPos, feedId);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(moveAnim);
        set.addAnimation(scaleAnim);
        set.setFillAfter(true);
        v.startAnimation(set);
    }

    public void onImageRepositioned(int pos, int feedId) {
        ListFilter filter = ListFilter.FEED;
        filter.setFeedId(feedId);
        ImageView v = (ImageView) mGridView.getChildAt(pos).findViewById(R.id.feeds_grid_item_image);
        v.setTransitionName(getString(R.string.transition_banner));
        setFilterAnimateTransition(filter, v);
    }

    public int getGridChildPositionWithIndex(int index) {
        final int numVisibleChildren = mGridView.getChildCount();
        final int firstVisiblePosition = mGridView.getFirstVisiblePosition();

        for ( int i = 0; i < numVisibleChildren; i++ ) {
            int positionOfView = firstVisiblePosition + i;

            if (positionOfView == index) {
                return i;
            }
        }
        return -1;
    }






}