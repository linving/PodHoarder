package com.podhoarder.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;

import com.podhoarderproject.podhoarder.R;

/**
 * Created by Emil on 2014-11-19.
 */
public class CheckableImageView extends ImageView implements Checkable {

    private OnCheckedChangeListener onCheckedChangeListener;
    private int mAccentColor, mDefaultColor;

    public CheckableImageView(Context context) {
        super(context);
    }

    public CheckableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttrs(attrs);
    }

    public CheckableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        loadAttrs(attrs);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    private void loadAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CheckableImageView);
        mDefaultColor = a.getColor(R.styleable.CheckableImageView_defaultColor, Color.BLACK);
        mAccentColor = a.getColor(R.styleable.CheckableImageView_accentColor, Color.BLACK);
        a.recycle();
        setChecked(isSelected());
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked)
            getDrawable().setColorFilter(checkedColorFilter());
        else
            getDrawable().setColorFilter(defaultColorFilter());

        setSelected(checked);

        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(this, checked);
        }
    }

    @Override
    public boolean isChecked() {
        return isSelected();
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    private ColorFilter checkedColorFilter() {

        int red = (mAccentColor & 0xFF0000) / 0xFFFF;
        int green = (mAccentColor & 0xFF00) / 0xFF;
        int blue = mAccentColor & 0xFF;

        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };

        return new ColorMatrixColorFilter(matrix);
    }

    private ColorFilter defaultColorFilter() {
        int red = (mDefaultColor & 0xFF0000) / 0xFFFF;
        int green = (mDefaultColor & 0xFF00) / 0xFF;
        int blue = mDefaultColor & 0xFF;

        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };

        return new ColorMatrixColorFilter(matrix);
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

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public static interface OnCheckedChangeListener {
        public void onCheckedChanged(CheckableImageView imageView, boolean isChecked);
    }
}
