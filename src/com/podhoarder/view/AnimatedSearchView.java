package com.podhoarder.view;

import android.content.Context;
import android.support.v7.internal.widget.TintImageView;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
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
        //Log.i(LOG_TAG, "ActionView expanded!");
        mSearchFrame.setVisibility(VISIBLE);
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, MeasureSpec.EXACTLY);
        mSearchFrame.measure(widthMeasureSpec, heightMeasureSpec);
        //viewWidthAnimation(mSearchPlate,1000,0,(mSearchFrame.getMeasuredWidth() - mSearchMagIcon.getMeasuredWidth())).start();
        ScaleAnimation anim = new ScaleAnimation(0f,1f,1f,1f, Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(200);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        mSearchField.startAnimation(anim);
        //super.onActionViewExpanded();
    }

    @Override
    public void onActionViewCollapsed() {
        //Log.i(LOG_TAG, "ActionView collapsed!");
        super.onActionViewCollapsed();
    }
}
