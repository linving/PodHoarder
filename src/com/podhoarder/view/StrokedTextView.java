package com.podhoarder.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint.Style;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.widget.TextView;

import com.podhoarderproject.podhoarder.R;

public class StrokedTextView extends TextView {

    // fields
    private int mStrokeColor;
    private float mStrokeWidth;
    private Shader mFillShader, mStrokeShader;
    
    // constructors
    public StrokedTextView(Context context, AttributeSet attrs, int defStyle) {
        
    	super(context, attrs, defStyle);
    	
    	TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingToggleButton);
		
		mStrokeColor = a.getColor(R.styleable.StrokedTextView_strokeColor, getResources().getColor(android.R.color.black));
		mStrokeWidth = a.getFloat(R.styleable.StrokedTextView_strokeWidth, 1.0f);
		
		a.recycle();
    	
    	mFillShader = new LinearGradient(0, 0, 0, getHeight(), getCurrentTextColor(), getCurrentTextColor(), TileMode.CLAMP );
    	mStrokeShader = new LinearGradient(0, 0, 0, getHeight(), mStrokeColor, mStrokeColor, TileMode.CLAMP );
    }

    public StrokedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingToggleButton);
		
		mStrokeColor = a.getColor(R.styleable.StrokedTextView_strokeColor, getResources().getColor(android.R.color.black));
		mStrokeWidth = a.getFloat(R.styleable.StrokedTextView_strokeWidth, 1.0f);
		
		a.recycle();
		
    	mFillShader = new LinearGradient(0, 0, 0, getHeight(), getCurrentTextColor(), getCurrentTextColor(), TileMode.CLAMP );
    	mStrokeShader = new LinearGradient(0, 0, 0, getHeight(), mStrokeColor, mStrokeColor, TileMode.CLAMP );
    }

    public StrokedTextView(Context context) {
        super(context);
        
        mStrokeColor = getResources().getColor(android.R.color.black);
        mStrokeWidth = 1.0f;
    	
    	mFillShader = new LinearGradient(0, 0, 0, getHeight(), getCurrentTextColor(), getCurrentTextColor(), TileMode.CLAMP );
    	mStrokeShader = new LinearGradient(0, 0, 0, getHeight(), mStrokeColor, mStrokeColor, TileMode.CLAMP );
    }

    // getters + setters
    public void setStrokeColor(int color) {
        mStrokeColor = color;
    	
    	mStrokeShader = new LinearGradient(0, 0, 0, getHeight(), mStrokeColor, mStrokeColor, TileMode.CLAMP );
    }

    public void setStrokeWidth(float width) {
        mStrokeWidth = width;
    }

    // overridden methods
    @Override
    protected void onDraw(Canvas canvas) {
                
    	if (mFillShader == null || mStrokeShader == null)
    	{
    		mFillShader = new LinearGradient(0, 0, 0, getHeight(), getCurrentTextColor(), getCurrentTextColor(), TileMode.CLAMP );
        	mStrokeShader = new LinearGradient(0, 0, 0, getHeight(), mStrokeColor, mStrokeColor, TileMode.CLAMP );
    	}
    	
        getPaint().setStyle(Style.STROKE);
        
        getPaint().setStrokeWidth(mStrokeWidth);
        getPaint().setShader(mStrokeShader);
        getPaint().setAntiAlias(true);
        super.onDraw(canvas);
        
        // draw the gradient filled text
        getPaint().setStyle(Style.FILL);
        getPaint().setShader(mFillShader);
        super.onDraw(canvas);
    }

}
