package com.podhoarder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.object.Episode;
import com.podhoarder.object.Feed;
import com.podhoarder.view.FloatingToggleButton;
import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-10-29.
 */
public class EpisodeFragment extends BaseFragment {


    //Fragment Content View
    private View mContentView;
    //Data Manager
    private LibraryActivityManager mDataManager;
    //Episode and Podcast
    private Feed mCurrentFeed;
    private Episode mCurrentEpisode;

    //UI Views
    private ImageView banner;
    private FloatingToggleButton mFAB;
    private TextView episodeTitle, episodeTimestamp, episodeDescription;
    private LinearLayout textContainer, headlineContainer;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static EpisodeFragment newInstance(int episodeId) {
        EpisodeFragment f = new EpisodeFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("episodeId", episodeId);
        f.setArguments(args);
        return f;
    }

    //Overrides
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.activity_episode, container, false);

        mDataManager = ((LibraryActivity) getActivity()).getDataManager();

        int episodeId = getArguments().getInt("episodeId", 0);

        mCurrentEpisode = mDataManager.getEpisode(episodeId);
        mCurrentFeed = mDataManager.getFeed(mCurrentEpisode.getFeedId());

        setupUI();

        ((LibraryActivity)getActivity()).setCurrentFragment(this);

        return mContentView;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onServiceConnected() {

    }

    private void setupUI() {

        banner = (ImageView) mContentView.findViewById(R.id.episode_banner);
        banner.setImageBitmap(mCurrentFeed.getFeedImage().largeImage());

        episodeTitle = (TextView) mContentView.findViewById(R.id.episode_title);
        episodeTitle.setText(mCurrentEpisode.getTitle());

        episodeTimestamp = (TextView) mContentView.findViewById(R.id.episode_timeStamp);
        episodeTimestamp.setText(mCurrentEpisode.getPubDate());

        episodeDescription = (TextView) mContentView.findViewById(R.id.episode_description);
        episodeDescription.setText(mCurrentEpisode.getDescription());

        textContainer = (LinearLayout) mContentView.findViewById(R.id.episode_text_container);
        headlineContainer = (LinearLayout) mContentView.findViewById(R.id.episode_headline_text_container);

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
