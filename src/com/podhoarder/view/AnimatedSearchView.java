package com.podhoarder.view;

import android.animation.Animator;
import android.content.Context;
import android.support.v7.internal.widget.TintImageView;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-11.
 */
public class AnimatedSearchView extends SearchView {
    private final String LOG_TAG="com.podhoarder.view.AnimatedSearchView";

    private boolean mAnimating;
    private LinearLayout mSearchFrame, mSearchPlate;
    private TintImageView mSearchButton;
    private ImageView mSearchMagIcon;
    private SearchAutoComplete mSearchField;

    public AnimatedSearchView(Context context) {
        super(context);
        setAnimationVars();
    }

    public AnimatedSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAnimationVars();
    }

    public AnimatedSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAnimationVars();
    }

    private void setAnimationVars() {
        mSearchFrame = (LinearLayout) findViewById(R.id.search_edit_frame);
        mSearchField = (SearchAutoComplete) findViewById(R.id.search_src_text);
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
}
