package com.podhoarder.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.podhoarder.adapter.SearchResultsAdapter;
import com.podhoarder.listener.SearchResultMultiChoiceModeListener;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.util.SearchManager;
import com.podhoarder.view.ButteryProgressBar;
import com.podhoarderproject.podhoarder.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-11-01.
 */
public class AddFragment extends BaseFragment implements SearchView.OnQueryTextListener, SearchResultMultiChoiceModeListener.onDialogResultListener  {

    private ListView mListView;
    private SearchResultsAdapter mListAdapter;

    private SearchManager mSearchManager;

    private ButteryProgressBar mProgressBar;

    private SearchResultMultiChoiceModeListener mListSelectionListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.AddFragment";
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.activity_add, container, false);
        setHasOptionsMenu(true);
        if (isDrawerIconEnabled())
            setDrawerIconEnabled(false,300);
        setupListView();
        mProgressBar = (ButteryProgressBar) mContentView.findViewById(R.id.search_progressBar);

        mSearchManager = new SearchManager(getActivity(), mListAdapter, mProgressBar);

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
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (null != searchView) {
            //searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setIconifiedByDefault(false);
        }
        searchView.setOnQueryTextListener(this);
        searchView.requestFocus();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onFragmentResumed() {

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

        this.mListView = (ListView) mContentView.findViewById(R.id.mainListView);
        this.mListView.setAdapter(this.mListAdapter);
        this.mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        this.mListSelectionListener = new SearchResultMultiChoiceModeListener(
                getActivity(), this.mListView);
        this.mListSelectionListener.setmOnDialogResultListener(this);
        this.mListView.setMultiChoiceModeListener(this.mListSelectionListener);
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v,
                                    int position, long id) {
                List<SearchResultRow> selectedItem = new ArrayList<SearchResultRow>();
                selectedItem.add((SearchResultRow) mListView.getItemAtPosition(position));
                mListSelectionListener.addFeedsDialog(getActivity(), selectedItem);
            }
        });
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onDialogResultReceived(int result, List<SearchResultRow> selectedResults) {
        if (result == 1) {
            finish(selectedResults);
        }
    }
}
