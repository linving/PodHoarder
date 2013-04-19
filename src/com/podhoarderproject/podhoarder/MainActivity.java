package com.podhoarderproject.podhoarder;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.podhoarderproject.podhoarder.gui.FeedFragment;
import com.podhoarderproject.podhoarder.gui.PagerAdapter;
import com.podhoarderproject.podhoarder.gui.PlayerFragment;
 
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

/**
 * 
 * @author Sebastian Andersson
 * 2013-04-17
 */
public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener
{
    private TabHost tabHost;
    private ViewPager viewPager;
    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, MainActivity.TabInfo>();
    private PagerAdapter pagerAdapter;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager_layout);
        this.initialiseTabHost(savedInstanceState);
        if (savedInstanceState != null)
        {
            tabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
        this.intialiseViewPager();
    }

    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putString("tab", tabHost.getCurrentTabTag());
        super.onSaveInstanceState(outState);
    }
 
    private void intialiseViewPager()
    {
        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, PlayerFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, FeedFragment.class.getName()));
        this.pagerAdapter  = new PagerAdapter(super.getSupportFragmentManager(), fragments);

        this.viewPager = (ViewPager)super.findViewById(R.id.viewpager);
        this.viewPager.setAdapter(this.pagerAdapter);
        this.viewPager.setOnPageChangeListener(this);
    }
 
    private void initialiseTabHost(Bundle args)
    {
        tabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabInfo tabInfo = null;
        this.addTab(this, this.tabHost, this.tabHost.newTabSpec(getResources().getString(R.string.player_tab)).setIndicator(getResources().getString(R.string.player_tab)), ( tabInfo = new TabInfo(getResources().getString(R.string.player_tab), PlayerFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        this.addTab(this, this.tabHost, this.tabHost.newTabSpec(getResources().getString(R.string.feed_tab)).setIndicator(getResources().getString(R.string.feed_tab)), ( tabInfo = new TabInfo(getResources().getString(R.string.feed_tab), FeedFragment.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        for(int i=0; i<tabHost.getTabWidget().getChildCount(); i++) 
        {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.tab_text));
        } 

        tabHost.setOnTabChangedListener(this);
    }
 
    private void addTab(MainActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo)
    {
        tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }
 
    public void onTabChanged(String tag)
    {
        int pos = this.tabHost.getCurrentTab();
        this.viewPager.setCurrentItem(pos);
    }
 
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
    }
 
    @Override
    public void onPageSelected(int position)
    {
        this.tabHost.setCurrentTab(position);
    }
 
    @Override
    public void onPageScrollStateChanged(int state)
    {
    }
    
    @SuppressWarnings("unused")
    private class TabInfo
    {
    	private String tag;
        private Class<?> c;
        private Bundle args;
        private Fragment fragment;
         
        TabInfo(String tag, Class<?> c, Bundle args)
        {
        	this.tag = tag;
            this.c = c;
            this.args = args;
        }
    }
    
    private class TabFactory implements TabContentFactory
    {
        private final Context context;

        public TabFactory(Context context)
        {
            this.context = context;
        }

        public View createTabContent(String tag)
        {
            View view = new View(this.context);
            view.setMinimumWidth(0);
            view.setMinimumHeight(0);
            return view;
        }
    }
}
