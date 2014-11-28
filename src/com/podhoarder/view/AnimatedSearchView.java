package com.podhoarder.view;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.internal.widget.TintImageView;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-11.
 */
public class AnimatedSearchView extends SearchView {
    private final String LOG_TAG="com.podhoarder.view.AnimatedSearchView";

    private boolean mAnimating;
    private LinearLayout mSearchFrame, mSearchPlate;
    private TintImageView mSearchButton;
    private ImageView mSearchCloseIcon;
    private SearchAutoComplete mSearchField;
    private ProgressBar mProgressBar;
    private Context mContext;

    public AnimatedSearchView(Context context) {
        this(context, null);
    }

    public AnimatedSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setAnimationVars();
    }

    private void setAnimationVars() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSearchFrame = (LinearLayout) findViewById(R.id.search_edit_frame);
        mSearchField = (SearchAutoComplete) findViewById(R.id.search_src_text);
        mSearchPlate = (LinearLayout) findViewById(R.id.search_plate);
        mSearchCloseIcon = (ImageView) mSearchPlate.findViewById(R.id.search_close_btn);
        inflater.inflate(R.layout.searchview_progressbar, mSearchPlate, true);
        mProgressBar = (ProgressBar) mSearchFrame.findViewById(R.id.search_progressbar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);    //Set the progress drawable foreground color to white to match other controls in the toolbar.
        mAnimating = false;
    }

    @Override
    public void onActionViewExpanded() {
        Log.i(LOG_TAG, "ActionView expanded!");

        //mSearchFrame.setVisibility(VISIBLE);
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, MeasureSpec.EXACTLY);
        mSearchFrame.measure(widthMeasureSpec, heightMeasureSpec);
        //viewWidthAnimation(mSearchPlate,1000,0,(mSearchFrame.getMeasuredWidth() - mSearchMagIcon.getMeasuredWidth())).start();
        // get the center for the clipping circle
        int cx =  mSearchField.getRight();
        int cy = (mSearchField.getTop() + mSearchField.getBottom()) / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(mSearchField.getWidth(), mSearchField.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(mSearchField, cx, cy, 0, finalRadius);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        // make the view visible and start the animation
        mSearchField.setVisibility(View.VISIBLE);
        anim.start();
        //super.onActionViewExpanded();
    }

    @Override
    public void onActionViewCollapsed() {
        //Log.i(LOG_TAG, "ActionView collapsed!");
        super.onActionViewCollapsed();
    }

    public void setSearching(boolean isSearching) {
        mSearchCloseIcon.clearAnimation();
        mProgressBar.clearAnimation();
        if (isSearching) {
            mSearchCloseIcon.animate().scaleY(.5f).scaleX(.5f).alpha(0f).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mSearchCloseIcon.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setScaleY(.5f);
                    mProgressBar.setScaleX(.5f);
                    mProgressBar.setAlpha(0f);
                    mProgressBar.animate().scaleY(1f).scaleX(1f).alpha(1f).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(null).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mSearchCloseIcon.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
        else {
            mProgressBar.animate().scaleY(.5f).scaleX(.5f).alpha(0f).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressBar.setVisibility(View.GONE);
                    mSearchCloseIcon.setVisibility(View.VISIBLE);
                    mSearchCloseIcon.setScaleY(.5f);
                    mSearchCloseIcon.setScaleX(.5f);
                    mSearchCloseIcon.setAlpha(0f);
                    mSearchCloseIcon.animate().scaleY(1f).scaleX(1f).alpha(1f).setDuration(150).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(null).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mProgressBar.setVisibility(View.GONE);
                    mSearchCloseIcon.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
        invalidate();
    }
}
