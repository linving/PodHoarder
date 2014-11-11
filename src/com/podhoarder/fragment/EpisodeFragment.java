package com.podhoarder.fragment;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.podhoarder.activity.BaseActivity;
import com.podhoarder.activity.LibraryActivity;
import com.podhoarder.datamanager.LibraryActivityManager;
import com.podhoarder.object.Episode;
import com.podhoarder.util.DataParser;
import com.podhoarder.util.EpisodeRowUtils;
import com.podhoarder.util.NetworkUtils;
import com.podhoarder.util.ToastMessages;
import com.podhoarder.view.CheckableImageButton;
import com.podhoarderproject.podhoarder.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-10-29.
 */
public class EpisodeFragment extends BaseFragment {

    //Episode and Podcast
    private Episode mCurrentEpisode;

    //UI Views
    private CheckableImageButton mFAB;
    private TextView mEpisodeDescription;
    private LinearLayout mTextContainer, mHeadlineContainer;
    private float mOriginalToolbarElevation = 2f;

    private boolean mExitAnimationsFinished = false;

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
        super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.activity_episode, container, false);

        mOriginalToolbarElevation = mToolbar.getElevation();
        mDataManager = ((LibraryActivity) getActivity()).getDataManager();

        int episodeId = getArguments().getInt("episodeId", 0);

        mCurrentEpisode = mDataManager.getEpisode(episodeId);

        setupUI();

        ((LibraryActivity)getActivity()).setCurrentFragment(this);

        return mContentView;
    }

    @Override
    public void onDestroy() {
        mToolbar.setElevation(mOriginalToolbarElevation);
        mOriginalToolbarElevation = 2f;
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contextual_menu_episode, menu);
        EpisodeRowUtils.configureMenu(getActivity(),menu,mCurrentEpisode);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Context context = getActivity();
        switch (item.getItemId()) {
            case R.id.menu_episode_markAsListened:
                List<Episode> eps = new ArrayList<Episode>();
                eps.add(mCurrentEpisode);
                ((LibraryActivityManager) ((LibraryActivity) context).mDataManager).markAsListened(eps);
                break;
            case R.id.menu_episode_add_playlist:
                if (((LibraryActivity) context).mDataManager.findEpisodeInPlaylist(mCurrentEpisode) == -1)
                    ((LibraryActivity) context).mDataManager.addToPlaylist(mCurrentEpisode);    //We only add items that aren't already in the playlist.
                break;
            case R.id.menu_episode_available_offline:
                ((LibraryActivity) context).mDataManager.DownloadManager().downloadEpisode(mCurrentEpisode);
                break;
            case R.id.menu_episode_playnow:
                if (mCurrentEpisode.isDownloaded() || NetworkUtils.isOnline(context))
                    ((LibraryActivity) context).getPlaybackService().playEpisode(mCurrentEpisode);
                else
                    ToastMessages.PlaybackFailed(context).show();
                break;
            case R.id.menu_episode_delete_file:
                if (mCurrentEpisode.isDownloaded())
                    ((LibraryActivity) context).mDataManager.deleteEpisodeFile(mCurrentEpisode);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        if (!mExitAnimationsFinished) {
            endFragmentAnimation();
            return true;
        }
        return false;
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onFragmentResumed() {

    }

    private void setupUI() {

        //mToolbar.setElevation(0f);
        TextView episodeTitle = (TextView) mContentView.findViewById(R.id.episode_title);
        episodeTitle.setText(mCurrentEpisode.getTitle());
        //episodeTitle.setScaleX(0f);
        episodeTitle.setAlpha(0f);
        episodeTitle.animate().alpha(1f).setDuration(200).setInterpolator(new AccelerateInterpolator()).start();

        TextView episodeTimestamp = (TextView) mContentView.findViewById(R.id.episode_timeStamp);
        try {
            episodeTimestamp.setText(DateUtils.getRelativeTimeSpanString(
                    DataParser.correctFormat.parse(
                            mCurrentEpisode.getPubDate()).getTime()));	//Set a time stamp since Episode publication.
        } catch (ParseException e) {
            e.printStackTrace();
        }
        episodeTimestamp.setAlpha(0f);
        episodeTimestamp.animate().alpha(1f).setDuration(200).setInterpolator(new AccelerateInterpolator()).start();

        mEpisodeDescription = (TextView) mContentView.findViewById(R.id.episode_description);
        mEpisodeDescription.setText(mCurrentEpisode.getDescription());
        mEpisodeDescription.setAlpha(0f);
        mEpisodeDescription.animate().alpha(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300).start();

        mTextContainer = (LinearLayout) mContentView.findViewById(R.id.episode_text_container);
        mHeadlineContainer = (LinearLayout) mContentView.findViewById(R.id.episode_headline_text_container);
        int currentColor = ((BaseActivity)getActivity()).getCurrentPrimaryColorDark();
        mHeadlineContainer.setBackgroundColor(currentColor);
        setToolbarTransparent(false);
        mHeadlineContainer.setMinimumHeight(mToolbar.getMinimumHeight() * 2);

        mFAB = (CheckableImageButton) mContentView.findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentEpisode.setFavorite(!mCurrentEpisode.isFavorite()); //Set the object property
                mDataManager.updateEpisode(mCurrentEpisode);    //Commit update to the DB
                ((CheckableImageButton) v).setChecked(mCurrentEpisode.isFavorite());    //Toggle the button
            }
        });
        mFAB.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTextContainer.invalidate();
                return false;
            }
        });
        mFAB.setChecked(mCurrentEpisode.isFavorite());

        mFAB.setScaleY(0f);
        mFAB.animate().scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).start();
    }

    private void endFragmentAnimation() {
        mFAB.animate().scaleY(0f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).start();
        mEpisodeDescription.animate().translationY(-(mEpisodeDescription.getMeasuredHeight()+10)).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimationFinished();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void onAnimationFinished() {
        mFAB.setAlpha(0f);
        mEpisodeDescription.setAlpha(0f);
        mHeadlineContainer.animate().translationY(-mHeadlineContainer.getMeasuredHeight()).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(100).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mExitAnimationsFinished = true;
                //onBackPressed();
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

}
