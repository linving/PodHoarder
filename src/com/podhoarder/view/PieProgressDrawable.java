package com.podhoarder.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
public class PieProgressDrawable extends Drawable {

    private Paint mPaint;
    private int mBackgroundColor, mProgressColor;
    private RectF mBoundsF;
    private final float START_ANGLE = 0.f;
    private float mDrawTo;

    public PieProgressDrawable() {
        super();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public PieProgressDrawable(int backgroundColor, int progressColor) {
        super();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundColor = backgroundColor;
        mProgressColor = progressColor;
    }

    /**
     * Set the border width.
     * @param color you want the background of the pie to be drawn in
     */
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    /**
     * @param color you want the pie to be drawn in
     */
    public void setProgressColor(int color) {
        mProgressColor = color;
    }

    @Override
    public void draw(Canvas canvas) {
        // Rotate the canvas around the center of the pie by 90 degrees
        // counter clockwise so the pie stars at 12 o'clock.
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(-90f, getBounds().centerX(), getBounds().centerY());
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBackgroundColor);
        canvas.drawOval(mBoundsF, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mProgressColor);
        canvas.drawArc(mBoundsF, START_ANGLE, mDrawTo, true, mPaint);
        canvas.restore();
        // Draw inner oval and text on top of the pie (or add any other
        // decorations such as a stroke) here..
        // Don't forget to rotate the canvas back if you plan to add text!
        // ...
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mBoundsF = new RectF(bounds);
    }

    @Override
    protected boolean onLevelChange(int level) {
        final float drawTo = START_ANGLE + ((float)360*level)/100f;
        boolean update = drawTo != mDrawTo;
        mDrawTo = drawTo;
        return update;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return mPaint.getAlpha();
    }
}
