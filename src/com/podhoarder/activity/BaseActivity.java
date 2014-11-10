package com.podhoarder.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

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
    //Top layout
    private DrawerLayout mDrawerLayout;
    //Drawer toggle
    private ActionBarDrawerToggle mDrawerToggle;
    //Drawer ListView Banner
    protected ImageView mNavDrawerBanner;
    //Drawer ListView
    private ListView mNavDrawerListView;
    private ListView mQuickDrawerListView, mQuickDrawerPlaylistView;
    private CheckableImageButton mQuicklistFilterFavorites, mQuicklistFilterPlaylist, mQuicklistFilterNew;
    //Main Toolbar View
    public Toolbar mToolbar;
    public int mToolbarSize;
    public FrameLayout mToolbarContainer;
    public View mToolbarBackground;

    //Colors
    private int mCurrentPrimaryColor;
    private int mCurrentPrimaryColorDark;

    //Main View
    public FrameLayout mContentRoot;
    //Listener interface
    protected NavDrawerItemClickListener mNavDrawerClickListener;
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

        mToolbarContainer = (FrameLayout) findViewById(R.id.toolbar_container);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarBackground = findViewById(R.id.toolbar_background);
        mToolbarSize = mToolbar.getMinimumHeight();
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
    }

    @Override
    public void setContentView(int layoutResID) {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
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
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        // Return false to let subclasses know that the event wasn't handled here.
        else
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
                    mQuickDrawerPlaylistView.setVisibility(View.GONE);
                    mQuickDrawerListView.setVisibility(View.VISIBLE);
                    mDataManager.mQuicklistAdapter.replaceItems(mDataManager.Favorites());

                    mQuicklistFilterFavorites.setChecked(true);
                    mQuicklistFilterPlaylist.setChecked(false);
                    mQuicklistFilterNew.setChecked(false);

                    mCurrentQuicklistFilter = QuicklistFilter.FAVORITES;
                    break;
                case R.id.right_drawer_list_filter_playlist:
                    mQuickDrawerListView.setVisibility(View.GONE);
                    mQuickDrawerPlaylistView.setVisibility(View.VISIBLE);

                    mQuicklistFilterPlaylist.setChecked(true);
                    mQuicklistFilterNew.setChecked(false);
                    mQuicklistFilterFavorites.setChecked(false);

                    mCurrentQuicklistFilter = QuicklistFilter.PLAYLIST;
                    break;
                case R.id.right_drawer_list_filter_new:
                    mQuickDrawerPlaylistView.setVisibility(View.GONE);
                    mQuickDrawerListView.setVisibility(View.VISIBLE);
                    mDataManager.mQuicklistAdapter.replaceItems(mDataManager.New());

                    mQuicklistFilterNew.setChecked(true);
                    mQuicklistFilterPlaylist.setChecked(false);
                    mQuicklistFilterFavorites.setChecked(false);

                    mCurrentQuicklistFilter = QuicklistFilter.NEW;
                    break;
            }
        }
    }

    //DATA MANAGER SETUP
    protected void setupDataManager() {
        mDataManager = new DataManager(this);
    }

    //SCREEN VARS SETUP
    private int setupScreenVars()   {
        int storedGridItemSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Constants.SETTINGS_KEY_GRIDITEMSIZE,"-1"));
        if (storedGridItemSize == -1) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            storedGridItemSize = (displayMetrics.widthPixels / 2) - 20; //The -20 is to account for the total amount of padding (3x4 horizontal item padding + 2x4 for grid layout margin)
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(Constants.SETTINGS_KEY_GRIDITEMSIZE, ""+storedGridItemSize).apply();
        }
        return storedGridItemSize;
    }

    //NAVIGATION
    public void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
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

        // load slide menu items
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

    public void colorUI(Palette p, final boolean isToolbarTransparent) {
        if (android.os.Build.VERSION.SDK_INT >=  Build.VERSION_CODES.LOLLIPOP) {
            AnimatorSet set = new AnimatorSet();

            int defaultPrimaryDark = getResources().getColor(R.color.colorPrimaryDark);
            int defaultPrimary = getResources().getColor(R.color.colorPrimary);

            ValueAnimator primaryDarkColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mCurrentPrimaryColorDark, p.getDarkVibrantColor(defaultPrimaryDark));
            primaryDarkColorAnimation.setDuration(300);
            ValueAnimator primaryColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mCurrentPrimaryColor, p.getVibrantColor(defaultPrimary));
            primaryColorAnimation.setDuration(300);
            primaryColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    mCurrentPrimaryColor = (Integer)animator.getAnimatedValue();
                    if (!isToolbarTransparent)
                        mToolbarBackground.setBackgroundColor(mCurrentPrimaryColor);
                }

            });
            primaryDarkColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    mCurrentPrimaryColorDark = (Integer) animator.getAnimatedValue();
                    getWindow().setStatusBarColor(mCurrentPrimaryColorDark);
                    getWindow().setNavigationBarColor(mCurrentPrimaryColorDark);
                }

            });

            List<Animator> animators = new ArrayList<Animator>();
            animators.add(primaryColorAnimation);
            animators.add(primaryDarkColorAnimation);

            primaryColorAnimation.start();
            primaryDarkColorAnimation.start();
            set.playTogether(animators);
        }

    }
    public int getCurrentPrimaryColor() {
        return mCurrentPrimaryColor;
    }
    public int getCurrentPrimaryColorDark() {
        return mCurrentPrimaryColorDark;
    }

    //DRAWER SETUP
    private void setupNavigationDrawer() {
        //Setup the navigation drawer.
        mNavDrawerBanner = (ImageView) findViewById(R.id.left_drawer_list_banner);
        mNavDrawerListView = (ListView) findViewById(R.id.left_drawer_list);
        // setting the nav drawer list adapter
        NavDrawerListAdapter adapter = new NavDrawerListAdapter(getApplicationContext(), generateNavigationMenu());
        mNavDrawerListView.setAdapter(adapter);
        mNavDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //mNavDrawerClickListener.onQuicklistItemClicked(view, position);
                final NavDrawerItemClickListener.NavDrawerItemPosition pos = NavDrawerItemClickListener.NavDrawerItemPosition.values()[position];
                mDrawerLayout.closeDrawer(Gravity.START);   //Close the drawer
                new Handler().postDelayed(new Runnable() {  //Post a delayed runnable that will start the new activity once the drawer has been closed. This is to prevent animation lag.
                    @Override
                    public void run() {
                        switch (pos) {
                            case LIBRARY:
                                startGridActivity();
                                break;
                            case PLAYER:
                                startPlayerActivity();
                                break;
                            case SETTINGS:
                                startSettingsActivity();
                                break;
                            case ABOUT:
                                break;
                            default:
                                break;
                        }
                    }
                }, 150);
            }
        });
        mNavDrawerListView.setSelection(0);
    }

    private void setupQuicklistDrawer() {

        mQuickDrawerListView = (ListView) findViewById(R.id.right_drawer_list);
        mQuickDrawerPlaylistView = (ListView) findViewById(R.id.right_drawer_playlist);
        mQuicklistFilterFavorites = (CheckableImageButton) findViewById(R.id.right_drawer_list_filter_favorites);
        mQuicklistFilterPlaylist = (CheckableImageButton) findViewById(R.id.right_drawer_list_filter_playlist);
        mQuicklistFilterNew = (CheckableImageButton) findViewById(R.id.right_drawer_list_filter_new);

        mQuickDrawerListView.setAdapter(mDataManager.mQuicklistAdapter);
        mQuickDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mQuicklistItemClickListener.onQuicklistItemClicked(view, position, mCurrentQuicklistFilter);
            }
        });
        mCurrentQuicklistFilter = QuicklistFilter.FAVORITES;
        mQuicklistFilterFavorites.setChecked(true);
        mQuickDrawerPlaylistView.setAdapter(mDataManager.mPlaylistAdapter);
        mQuickDrawerPlaylistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mQuicklistItemClickListener.onQuicklistItemClicked(view, position, mCurrentQuicklistFilter);
            }
        });
    }

    //DRAWER CLICK INTERFACE
    public interface NavDrawerItemClickListener {
        public void onItemClicked(View clickedView, int position);

        public static enum NavDrawerItemPosition {LIBRARY, PLAYER, SETTINGS, ABOUT};
    }

    public interface QuicklistItemClickListener {
        public void onQuicklistItemClicked(View clickedView, int position, QuicklistFilter currentFilter);
    }

    //QUICK LIST FILTER ENUM
    public enum QuicklistFilter {
        FAVORITES, PLAYLIST, NEW
    }
    public QuicklistFilter currentQuicklistFilter() {
        return mCurrentQuicklistFilter;
    }
}
