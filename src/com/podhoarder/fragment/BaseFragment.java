package com.podhoarder.fragment;

import android.support.v4.app.Fragment;

/**
 * Created by Emil on 2014-10-28.
 */
public abstract class BaseFragment extends Fragment {
    public abstract boolean onBackPressed();
    public abstract void onServiceConnected();
}
