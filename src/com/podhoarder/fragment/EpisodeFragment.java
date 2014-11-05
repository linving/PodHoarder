package com.podhoarder.fragment;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.util.DataParser;
import com.podhoarder.view.FloatingToggleButton;
import com.podhoarderproject.podhoarder.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Emil on 2014-10-29.
 */
public class EpisodeFragment extends BaseFragment {

    //Episode and Podcast
    private Feed mCurrentFeed;
    private Episode mCurrentEpisode;

    //UI Views
    private FloatingToggleButton mFAB;
    private TextView episodeTitle, episodeTimestamp, episodeDescription;
    private LinearLayout textContainer, headlineContainer;
    private Toolbar mToolbar;

    private DateFormat mDisplayFormat;

    public static EpisodeFragment newInstance(int episodeId) {
        EpisodeFragment f = new EpisodeFragment();
        Bundle args = new Bundle();
        args.putInt("episodeId", episodeId);
        f.setArguments(args);
        return f;
    }

    //Overrides
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG_TAG = "com.podhoarder.fragment.EpisodeFragment";
        mContentView = inflater.inflate(R.layout.activity_episode, container, false);
        setHasOptionsMenu(true);

        mToolbar = ((BaseActivity)getActivity()).mToolbar;
        mToolbar.setAlpha(0f);
        //mToolbar.animate().translationY(0f).alpha(1.0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).start();

        mDataManager = ((LibraryActivity) getActivity()).getDataManager();

        mDisplayFormat = android.text.format.DateFormat.getDateFormat(getActivity());

        int episodeId = getArguments().getInt("episodeId", 0);

        mCurrentEpisode = mDataManager.getEpisode(episodeId);
        mCurrentFeed = mDataManager.getFeed(mCurrentEpisode.getFeedId());

        setupUI();

        ((LibraryActivity)getActivity()).setCurrentFragment(this);

        return mContentView;
    }

    @Override
    public void onDestroy() {
        //mToolbar.animate().scaleY().setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contextual_menu_episode, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onServiceConnected() {

    }

    private void setupUI() {

        episodeTitle = (TextView) mContentView.findViewById(R.id.episode_title);
        episodeTitle.setText(mCurrentEpisode.getTitle());

        episodeTimestamp = (TextView) mContentView.findViewById(R.id.episode_timeStamp);
        try {
            Date date = DataParser.correctFormat.parse(mCurrentEpisode.getPubDate());
            episodeTimestamp.setText(mDisplayFormat.format(date));
        }
        catch (ParseException e) {
            episodeTimestamp.setText(mCurrentEpisode.getPubDate());
        }

        episodeDescription = (TextView) mContentView.findViewById(R.id.episode_description);
        episodeDescription.setText(mCurrentEpisode.getDescription());

        textContainer = (LinearLayout) mContentView.findViewById(R.id.episode_text_container);
        headlineContainer = (LinearLayout) mContentView.findViewById(R.id.episode_headline_text_container);
        headlineContainer.setMinimumHeight(mToolbar.getMinimumHeight()*2);

        mFAB = (FloatingToggleButton) mContentView.findViewById(R.id.episode_favorite_toggle);
        mFAB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentEpisode.setFavorite(!mCurrentEpisode.isFavorite()); //Set the object property
                mDataManager.updateEpisode(mCurrentEpisode);    //Commit update to the DB
                ((FloatingToggleButton) v).setToggled(mCurrentEpisode.isFavorite());    //Toggle the button
            }
        });
        mFAB.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                textContainer.invalidate();
                return false;
            }
        });
        mFAB.setToggled(mCurrentEpisode.isFavorite());
    }

}
