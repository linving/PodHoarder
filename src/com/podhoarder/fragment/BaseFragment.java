package com.podhoarder.fragment;

import android.support.v4.app.Fragment;
import android.view.View;

import com.podhoarder.datamanager.LibraryActivityManager;

/**
 * Created by Emil on 2014-10-28.
 */
public abstract class BaseFragment extends Fragment {
    protected String LOG_TAG;

    protected LibraryActivityManager mDataManager;
    protected View mContentView;

    public abstract boolean onBackPressed();
    public abstract void onServiceConnected();
    public abstract void onFragmentResumed();
}
