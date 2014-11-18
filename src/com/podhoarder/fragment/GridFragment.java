package com.podhoarder.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.adapter.GridAdapter;
import com.podhoarder.listener.GridActionModeCallback;
import com.podhoarder.util.Constants;
import com.podhoarder.util.ImageUtils;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Emil on 2014-10-28.
 */
public class GridFragment extends CollectionFragment implements SwipeRefreshLayout.OnRefreshListener, GridAdapter.GridItemClickListener, LibraryActivity.onFirstFeedAddedListener {

    protected boolean FABVisible;
    //Feeds Grid
    private GridView mGridView;
    private int mGridItemSize;
    private GridActionModeCallback mActionModeCallback;
    private ActionMode mActionMode;

    //Feeds Grid Persistent Values
    int mSelectedGridItemLeft = Integer.MAX_VALUE, mSelectedGridItemTop = Integer.MAX_VALUE, mSelectedGridItemIndex = -1; //The mSelectedGridItem variables are set to the max value of an Integer per default, so we can know if they have been set.

    //SwipeRefreshLayout
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static GridFragment newInstance() {
        GridFragment f = new GridFragment();
        return f;
    }

    //OVERRIDES
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.GridFragment";
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.activity_grid, container, false);
        ViewTreeObserver vto = mContentView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mSelectedGridItemTop != Integer.MAX_VALUE && mSelectedGridItemLeft != Integer.MAX_VALUE && mSelectedGridItemIndex != -1) {   //A Check to see if the selected item variables were set.
                    onFragmentRedrawn();
                }
            }
        });

        mCurrentFilter = ListFilter.ALL;

        setToolbarTransparent(false);

        if (mDataManager.hasPodcasts())
            mDataManager.mFeedsGridAdapter.setGridItemClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) mContentView.findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressViewOffset(true,(mToolbarSize),(mToolbarSize) + 200);
        mSwipeRefreshLayout.setDistanceToTriggerSync(200);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.windowBackground, R.color.colorAccent, R.color.windowBackground);

        populate();
        ((LibraryActivity)getActivity()).setOnFirstFeedAddedListener(this);
        setupFAB();
        ((LibraryActivity)getActivity()).setCurrentFragment(this);

        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlaybackService != null) {
            onServiceConnected();
        }
        mToolbarContainer.setTranslationY(0f);
    }

    @Override
    public void onFragmentResumed() {

    }

    @Override
    public void onFragmentRedrawn() {
        reverseGridItemSelectionAnimation(mSelectedGridItemIndex, mSelectedGridItemTop, mSelectedGridItemLeft);
    }

    @Override
    public void onRefresh() {
        mDataManager.refreshFeeds(mSwipeRefreshLayout);
    }

    @Override
    public void onGridItemClicked(final int pos, final int feedId) {
        mToolbarBackground.animate().alpha(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).start();
        mToolbarContainer.animate().translationY(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).start();
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_fade_out);
        animation.setFillAfter(true);
        int fixedPos = getGridChildPositionWithIndex(pos);

        for (int i = mGridView.getFirstVisiblePosition(); i < mGridView.getChildCount(); i++) {
            if (i != fixedPos) {
                mGridView.getChildAt(i).startAnimation(animation);
            }
        }
        if (animation.hasStarted()) {
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
        else {
            onGridFaded(pos, feedId);
        }

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
            mGridView.setPadding((int)ImageUtils.pixelsToDip(getActivity(),4),(mToolbarSize + mStatusBarHeight + (int)ImageUtils.pixelsToDip(getActivity(),4)),(int)ImageUtils.pixelsToDip(getActivity(),4),0);


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
                    if (mGridView != null && mToolbar != null) {
                        if (mGridView.getChildCount() > 0) {
                            // check if the first item of the list is visible
                            boolean firstItemVisible = mGridView.getFirstVisiblePosition() == 0;
                            // check if the top of the first item is visible
                            boolean topOfFirstItemVisible = mGridView.getChildAt(0).getTop() > 0;
                            // enabling or disabling the refresh layout
                            enable = firstItemVisible && topOfFirstItemVisible;

                            int maxTranslationY = -(mToolbarSize + mStatusBarHeight);    //The toolbar should only be allowed to move until it is fully off screen.
                            int toolbarTop = (mGridView.getChildAt(0).getTop() - (mToolbarSize+mStatusBarHeight+4));   //Calculate the distance to move by subtracting the toolbar height + gridview padding from the top line.

                            if (firstItemVisible) {
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

                                mToolbarContainer.setTranslationY(scrollDelta);  //Move the toolbar vertically.

                                if (!topOfFirstItemVisible && FABVisible) {
                                    mFAB.animate().translationY(mFAB.getMeasuredHeight() * 2).setDuration(100).setInterpolator(new AccelerateInterpolator());
                                    FABVisible = false;
                                }
                                else if (topOfFirstItemVisible && !FABVisible) {
                                    mFAB.animate().translationY(0f).setDuration(100).setInterpolator(new DecelerateInterpolator());
                                    FABVisible = true;
                                }
                            }
                            else {
                                enable = false;
                                mToolbarContainer.setTranslationY(maxTranslationY);
                                //mToolbar.setAlpha(.0f);
                            }
                        }
                        else {
                            mToolbarContainer.setTranslationY(0f);
                            //mToolbar.setAlpha(1.0f);
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

    protected void setupFAB() {
        if (android.os.Build.VERSION.SDK_INT >=  Build.VERSION_CODES.LOLLIPOP) {
            mFAB = (ImageButton) mContentView.findViewById(R.id.fab);
            mFAB.setVisibility(View.VISIBLE);
            mFAB.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((LibraryActivity)getActivity()).startAddActivity();
                }
            });
            FABVisible = true;
        }
        FABVisible = false;
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
        mSelectedGridItemLeft = v.getLeft();
        mSelectedGridItemTop = v.getTop();
        mSelectedGridItemIndex = actualPos;

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        float f = (float)displaymetrics.widthPixels / v.getMeasuredWidth(); //Calculate the percent value of the grid image as opposed to the entire screen width (which will be the final width & height)

        ScaleAnimation scaleAnim = new
                ScaleAnimation(1.0f, f, 1.0f, f, 1f, 1f);
        //scaleAnim.setFillAfter(true);
        scaleAnim.setDuration(250);
        TranslateAnimation moveAnim =  new TranslateAnimation(0, 0,
                TranslateAnimation.ABSOLUTE, (0 - v.getLeft())/f, 0, 0,
                TranslateAnimation.ABSOLUTE, (0 - v.getTop())/f);
        moveAnim.setDuration(250);
        moveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        //moveAnim.setFillAfter(true);
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
        //set.setFillAfter(true);
        v.startAnimation(set);
    }

    public void reverseGridItemSelectionAnimation(int index, int originalTop, int originalLeft) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grid_fade_in);
        animation.setFillAfter(true);

        for (int i = mGridView.getFirstVisiblePosition(); i < mGridView.getChildCount(); i++) {
            if (i != index) {
                mGridView.getChildAt(i).startAnimation(animation);
            }
        }

        final View v = mGridView.getChildAt(index);

        if (v != null) {
            if (android.os.Build.VERSION.SDK_INT >=  Build.VERSION_CODES.LOLLIPOP)
                v.setZ(5f);

            float gridItemScaleFactor = displaymetrics.widthPixels / (float)mGridItemSize; //Calculate the percent value of the grid image as opposed to the entire screen width (which will be the final width & height)

            ScaleAnimation scaleAnim = new ScaleAnimation(gridItemScaleFactor, 1.0f, gridItemScaleFactor, 1.0f, 1f, 1f);
            scaleAnim.setFillAfter(true);
            scaleAnim.setDuration(250);
            TranslateAnimation moveAnim =  new TranslateAnimation(TranslateAnimation.ABSOLUTE, (0 - originalLeft)/gridItemScaleFactor,0, 0,
                    TranslateAnimation.ABSOLUTE, (0 - originalTop)/gridItemScaleFactor, 0, 0);
            moveAnim.setDuration(250);
            moveAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            moveAnim.setFillAfter(true);
            AnimationSet set = new AnimationSet(true);
            set.addAnimation(moveAnim);
            set.addAnimation(scaleAnim);
            set.setFillAfter(true);
            if (android.os.Build.VERSION.SDK_INT >=  Build.VERSION_CODES.LOLLIPOP) {
                set.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        v.setZ(1f);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
            v.startAnimation(set);
        }

        mSelectedGridItemIndex = -1;
        mSelectedGridItemTop = Integer.MAX_VALUE;
        mSelectedGridItemLeft = Integer.MAX_VALUE;
    }

    public void onImageRepositioned(int pos, int feedId) {
        mCurrentFilter = ListFilter.FEED;
        mCurrentFilter.setFeedId(feedId);
        ImageView v = (ImageView) mGridView.getChildAt(pos).findViewById(R.id.feeds_grid_item_image);
        setFilter(mCurrentFilter);
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
