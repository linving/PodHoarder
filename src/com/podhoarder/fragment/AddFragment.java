package com.podhoarder.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import com.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.util.SearchManager;
import com.podhoarder.util.ToastMessages;
import com.podhoarder.view.AnimatedSearchView;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-11-01.
 */
public class AddFragment extends BaseFragment implements SearchView.OnQueryTextListener, SearchResultsAdapter.OnSubscribeListener {

    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private SearchResultsAdapter mListAdapter;

    private SearchManager mSearchManager;

    private AnimatedSearchView mSearchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.AddFragment";
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.fragment_add, container, false);
        setHasOptionsMenu(true);
        if (isDrawerIconEnabled())
            setDrawerIconEnabled(false,300);
        setupListView();

        mToolbarContainer.animate().translationY(0f).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        mContentView.setPadding(0,(mToolbarSize + mStatusBarHeight),0,0);
        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu,  MenuInflater inflater) {

        inflater.inflate(R.menu.search_menu, menu);
        //android.app.SearchManager searchManager = (android.app.SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (AnimatedSearchView) menu.findItem(R.id.action_search).getActionView();
        if (null != mSearchView) {
            //searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            mSearchView.setIconifiedByDefault(false);
            mSearchView.setQueryHint(getString(R.string.add_search_hint));
        }
        mSearchView.setOnQueryTextListener(this);
        mSearchView.requestFocus();
        mSearchManager = new SearchManager(getActivity(), mListAdapter, mSearchView);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onBackPressed() {
        hideKeyboard();
        return false;
    }

    @Override
    public void onFragmentResumed() {

    }

    @Override
    public void onSubscribeConfirmed(View v, int i, SearchResultRow resultData) {
        this.mListAdapter.remove(i);
        List<SearchResultRow> result = new ArrayList<SearchResultRow>();
        result.add(resultData);
        mDataManager.addSearchResults(result);
        ToastMessages.Subscribed(getActivity()).show();
    }

    @Override
    public boolean onQueryTextChange(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String text) {
        mSearchManager.doSearch(text);
        hideKeyboard();
        return false;
    }

    public void finish(List<SearchResultRow> selectedResults) {
//        for (SearchResultRow row : selectedResults)
//            row.cacheXml();  //Cache the file. Most XML documents are too large to be passed as Intent Extras so we need to do this.

        mDataManager.addSearchResults(selectedResults);

        getActivity().getSupportFragmentManager().popBackStack();
    }

    private void setupListView() {
        this.mListAdapter = new SearchResultsAdapter(getActivity());
        this.mListAdapter.setOnSubscribeListener(this);

        this.mListView = (RecyclerView) mContentView.findViewById(R.id.mainListView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mListView.setLayoutManager(mLayoutManager);
        this.mListView.setAdapter(this.mListAdapter);
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
