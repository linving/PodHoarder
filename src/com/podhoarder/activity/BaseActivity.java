package com.podhoarder.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.podhoarder.adapter.NavDrawerListAdapter;
import com.podhoarder.datamanager.DataManager;
import com.podhoarder.fragment.CollectionFragment;
import com.podhoarder.object.Episode;
import com.podhoarder.object.NavDrawerItem;
import com.podhoarder.util.Constants;
import com.podhoarder.view.CheckableImageButton;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-10-21.
 */
public abstract class BaseActivity extends ActionBarActivity {
    public static final int NAVIGATION_LIBRARY = 0, NAVIGATION_PLAYER = 1, NAVIGATION_SETTINGS = 2, NAVIGATION_ABOUT = 3;
    //Top layout
    private DrawerLayout mDrawerLayout;
    protected LinearLayout mLeftDrawer;
    protected LinearLayout mRightDrawer;
    //Drawer toggle
    protected ActionBarDrawerToggle mDrawerToggle;
    private boolean mDrawerToggleEnabled;
    //Drawer ListView Banner
    protected RelativeLayout mNavDrawerBanner;

    //Drawer ListView
    private ListView mNavDrawerListView;
    private ListView mQuickDrawerListView;
    private CheckableImageButton mQuicklistFilterFavorites, mQuicklistFilterDownloads;
    private TextView mRightDrawerHeader;

    //Main Toolbar View
    public Toolbar mToolbar;
    public int mToolbarSize;
    public FrameLayout mToolbarContainer;
    public View mToolbarBackground;
    public List<MenuItemImpl> mPreviousMenuItems;

    public int mStatusBarHeight;

    //Colors
    private int mCurrentPrimaryColor;
    private int mCurrentPrimaryColorDark;

    //Main View
    public FrameLayout mContentRoot;
    //Listener interface
    protected QuicklistItemClickListener mQuicklistItemClickListener;

    //Data Manager
    public DataManager mDataManager;

    //Quick list filter
    private QuicklistFilter mCurrentQuicklistFilter;

    //Grid Item Size
    protected int mGridItemSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mPreviousMenuItems = new ArrayList<MenuItemImpl>();

        mStatusBarHeight = getStatusBarHeight();
        mToolbarContainer = (FrameLayout) findViewById(R.id.toolbar_container);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        FrameLayout.LayoutParams mToolbarLayoutParams = (FrameLayout.LayoutParams) mToolbar.getLayoutParams();
        mToolbarLayoutParams.topMargin = mStatusBarHeight;
        mToolbar.setLayoutParams(mToolbarLayoutParams);
        mToolbarSize = mToolbar.getMinimumHeight();

        mToolbarBackground = findViewById(R.id.toolbar_background);
        mToolbarBackground.setMinimumHeight(mToolbarSize + mStatusBarHeight);

        ViewGroup.LayoutParams params = mToolbarContainer.getLayoutParams();
        params.height = mToolbarSize + mStatusBarHeight;
        mToolbarContainer.setMinimumHeight(mToolbarSize + mStatusBarHeight);
        mToolbarContainer.setLayoutParams(params);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mCurrentPrimaryColor = getResources().getColor(R.color.colorPrimary);
        mCurrentPrimaryColorDark = getResources().getColor(R.color.colorPrimaryDark);

        mGridItemSize = setupScreenVars();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggleEnabled = true;

        mToolbarContainer.bringToFront();
    }

    @Override
    public void setContentView(int layoutResID) {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        mLeftDrawer = (LinearLayout) mDrawerLayout.findViewById(R.id.left_drawer);
        mRightDrawer = (LinearLayout) mDrawerLayout.findViewById(R.id.right_drawer);
        mContentRoot = (FrameLayout) mDrawerLayout.findViewById(R.id.root_container);
        // set the drawer layout as main_menu content view of Activity.
        setContentView(mDrawerLayout);
        // add layout of BaseActivities inside framelayout.i.e. frame_container
        //getLayoutInflater().inflate(layoutResID, mContentRoot, true);
        setupDataManager();
        setupNavigationDrawer();
        setupQuicklistDrawer();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //We only handle the menu drawer / home button here. Anything else is handled in the fragments.
        if (item.getItemId() == android.R.id.home) {
            //If the drawer toggle is enabled that means the user clicked the menu button and we should open the drawer.
            if (mDrawerToggleEnabled) {
                if (mDrawerToggle.onOptionsItemSelected(item))
                    mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
            //If the drawer toggle is not enabled that means the user pressed the back button and expects back button behavior.
            else {
                onBackPressed();
                return true;
            }
        }
        // Return false to let subclasses know that the event wasn't handled here.
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        else if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        else
            super.onBackPressed();
    }

    public void quicklistFilterClicked(View v) {
        if (!((CheckableImageButton) v).isChecked()) {
            switch (v.getId()) {
                case R.id.right_drawer_list_filter_favorites:
                    mDataManager.mQuicklistAdapter.replaceItems(mDataManager.Favorites());

                    mQuicklistFilterFavorites.setChecked(true);
                    mQuicklistFilterDownloads.setChecked(false);

                    mRightDrawerHeader.setText(getString(R.string.filter_favorites));

                    mCurrentQuicklistFilter = QuicklistFilter.FAVORITES;
                    break;
                case R.id.right_drawer_list_filter_downloads:
                    mDataManager.mQuicklistAdapter.replaceItems(mDataManager.Downloads());

                    mQuicklistFilterDownloads.setChecked(true);
                    mQuicklistFilterFavorites.setChecked(false);

                    mRightDrawerHeader.setText(getString(R.string.filter_downloaded));

                    mCurrentQuicklistFilter = QuicklistFilter.DOWNLOADS;
                    break;
            }
        }
    }

    //DATA MANAGER SETUP
    protected void setupDataManager() {
        mDataManager = new DataManager(this);
    }

    //SCREEN VARS SETUP
    private int setupScreenVars() {
        int storedGridItemSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.SETTINGS_KEY_GRIDITEMSIZE, "-1"));
        if (storedGridItemSize == -1) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            storedGridItemSize = (displayMetrics.widthPixels / 2) - 20; //The -20 is to account for the total amount of padding (3x4 horizontal item padding + 2x4 for grid layout margin)
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(Constants.SETTINGS_KEY_GRIDITEMSIZE, "" + storedGridItemSize).apply();
        }
        return storedGridItemSize;
    }

    //NAVIGATION
    public void startSettingsActivity() {
        //Intent intent = new Intent(this, SettingsActivity.class);
        //startActivity(intent);
    }

    public void startPlayerActivity() {
    }

    public void startGridActivity() {
    }

    public void startListActivity(CollectionFragment.ListFilter filter) {
    }

    public void startAddActivity() {

    }

    public void startEpisodeActivity(Episode currentEp) {


    }

    //UTILS
    private ArrayList<NavDrawerItem> generateNavigationMenu() {
        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();

        // load slide secondaryAction items
        String[] navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        // adding nav drawer items to array
        // Home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Find People
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Photos
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        // Communities, Will add a counter here
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));


        // Recycle the typed array
        navMenuIcons.recycle();

        return navDrawerItems;
    }

    protected void setSelectedNavigationItem(int pos) {
        if (mNavDrawerListView.getChildAt(pos) != null)
            mNavDrawerListView.getChildAt(pos).setSelected(true);

        ((NavDrawerListAdapter)mNavDrawerListView.getAdapter()).notifyDataSetChanged();
    }

    public int getCurrentPrimaryColor() {
        return mCurrentPrimaryColor;
    }

    public int getCurrentPrimaryColorDark() {
        return mCurrentPrimaryColorDark;
    }

    public void colorUI(int color) {
        ValueAnimator primaryColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mCurrentPrimaryColor, color);
        primaryColorAnimation.setDuration(200);
        primaryColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int value = (Integer) animator.getAnimatedValue();
                getWindow().setNavigationBarColor(value);
            }

        });
        primaryColorAnimation.start();
    }

    public void resetUI() {
        ValueAnimator primaryColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getWindow().getNavigationBarColor(), Color.BLACK);
        primaryColorAnimation.setDuration(200);
        primaryColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int value = (Integer) animator.getAnimatedValue();
                getWindow().setNavigationBarColor(value);
            }

        });
        primaryColorAnimation.start();
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //DRAWER SETUP
    protected void setupNavigationDrawer() {
        //Setup the navigation drawer.
        mNavDrawerBanner = (RelativeLayout) findViewById(R.id.left_drawer_list_banner);
        mNavDrawerListView = (ListView) findViewById(R.id.left_drawer_list);
        // setting the nav drawer list adapter
        final NavDrawerListAdapter adapter = new NavDrawerListAdapter(getApplicationContext(), generateNavigationMenu());
        mNavDrawerListView.setAdapter(adapter);
        mNavDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //mNavDrawerClickListener.onQuicklistItemClicked(view, position);
                //view.setSelected(true);
                //adapter.notifyDataSetChanged();
                mDrawerLayout.closeDrawer(Gravity.START);   //Close the drawer
                final int pos = position;
                new Handler().postDelayed(new Runnable() {  //Post a delayed runnable that will start the new activity once the drawer has been closed. This is to prevent animation lag.
                    @Override
                    public void run() {
                        switch (pos) {
                            case NAVIGATION_LIBRARY:
                                startGridActivity();
                                break;
                            case NAVIGATION_PLAYER:
                                startPlayerActivity();
                                break;
                            case NAVIGATION_SETTINGS:
                                startSettingsActivity();
                                break;
                            case NAVIGATION_ABOUT:
                                break;
                            default:
                                break;
                        }
                    }
                }, 150);
            }
        });
    }

    public void setDrawerIconEnabled(final boolean enabled, final int duration) {
        if ((enabled && !mDrawerToggleEnabled) || (!enabled && mDrawerToggleEnabled)) {   //Only play the animation if the drawer indicator isn't already in the desired state.
            mDrawerToggleEnabled = enabled;
            if (enabled) {
                mDrawerLayout.setDrawerListener(mDrawerToggle);
            } else {
                mDrawerLayout.setDrawerListener(null);
            }

            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float slideOffset = (Float) valueAnimator.getAnimatedValue();
                    if (enabled) {
                        slideOffset = 1f - slideOffset;
                    }
                    mDrawerToggle.onDrawerSlide(null, slideOffset);
                }
            });
            anim.setInterpolator(new DecelerateInterpolator());
            // You can change this duration to more closely match that of the default animation.
            anim.setDuration(duration);

            anim.start();
        }
    }

    public boolean isDrawerIconEnabled() {
        return mDrawerToggleEnabled;
    }

    private void setupQuicklistDrawer() {

        mQuickDrawerListView = (ListView) findViewById(R.id.right_drawer_list);
        mQuicklistFilterFavorites = (CheckableImageButton) findViewById(R.id.right_drawer_list_filter_favorites);
        mQuicklistFilterDownloads = (CheckableImageButton) findViewById(R.id.right_drawer_list_filter_downloads);
        mRightDrawerHeader = (TextView) findViewById(R.id.right_drawer_title);

        mQuickDrawerListView.setAdapter(mDataManager.mQuicklistAdapter);
        mQuickDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mQuicklistItemClickListener.onQuicklistItemClicked(view, position, mCurrentQuicklistFilter);
            }
        });
        mCurrentQuicklistFilter = QuicklistFilter.FAVORITES;
        mRightDrawerHeader.setText(getString(R.string.filter_favorites));
        mQuicklistFilterFavorites.setChecked(true);
        mQuicklistFilterFavorites.invalidate();
    }

    //DRAWER CLICK INTERFACE
    public interface QuicklistItemClickListener {
        public void onQuicklistItemClicked(View clickedView, int position, QuicklistFilter currentFilter);
    }

    //QUICK LIST FILTER ENUM
    public enum QuicklistFilter {
        FAVORITES, DOWNLOADS
    }


    public QuicklistFilter currentQuicklistFilter() {
        return mCurrentQuicklistFilter;
    }
}
