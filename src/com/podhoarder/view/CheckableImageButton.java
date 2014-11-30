package com.podhoarder.view;

/**
 * Created by Emil on 2014-11-10.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageButton;

import com.podhoarderproject.podhoarder.R;

public class CheckableImageButton extends ImageButton implements Checkable {
    private OnCheckedChangeListener onCheckedChangeListener;
    private int mAccentColor, mDefaultColor;

    public CheckableImageButton(Context context) {
        super(context);
    }

    public CheckableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChecked(attrs);
    }

    public CheckableImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setChecked(attrs);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    private void setChecked(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CheckableImageButton);
        mDefaultColor = a.getColor(R.styleable.CheckableImageButton_defaultColor, Color.BLACK);
        mAccentColor = a.getColor(R.styleable.CheckableImageButton_accentColor, Color.BLACK);
        a.recycle();
        setChecked(isSelected());
    }

    @Override
    public boolean isChecked() {
        return isSelected();
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked)
            getDrawable().setColorFilter(mAccentColor, PorterDuff.Mode.SRC_IN);
        else
            getDrawable().setColorFilter(mDefaultColor, PorterDuff.Mode.SRC_IN);

        setSelected(checked);

        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(this, checked);
        }
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    public void setAccentColor(int color) {
        mAccentColor = color;
    }

    public int getAccentColor() {
        return mAccentColor;
    }

    public void setDefaultColor(int color) {
        mDefaultColor = color;
    }

    public int getDefaultColor() {
        return mDefaultColor;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public static interface OnCheckedChangeListener {
        public void onCheckedChanged(CheckableImageButton buttonView, boolean isChecked);
    }


}

