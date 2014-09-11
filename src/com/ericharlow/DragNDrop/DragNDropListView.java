/*
 * Copyright (C) 2010 Eric Harlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ericharlow.DragNDrop;


import com.podhoarder.adapter.DragNDropAdapter;
import com.podhoarder.object.Episode;
import com.podhoarderproject.podhoarder.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

public class DragNDropListView extends ListView {

	boolean mDragMode;

	int mStartPosition;
	int mEndPosition;
	int mDragPointOffset;		//Used to adjust drag view location
	
	ImageView mDragView;
	GestureDetector mGestureDetector;
	
	DropListener mDropListener;
	DragListener mDragListener;
	
	private Episode tempEpisode;
	private int defaultBackgroundColor;
	
	public DragNDropListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setDropListener(DropListener l) {
		mDropListener = l;
	}
	
	public void setDragListener(DragListener l) {
		mDragListener = l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();	
		
		if (action == MotionEvent.ACTION_DOWN && ((DragNDropAdapter)this.getAdapter()).isReorderingEnabled() && x > ((this.getWidth()/4)*3.5)) {
			mDragMode = true;
		}

		if (!mDragMode) 
			return super.onTouchEvent(ev);
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mStartPosition = pointToPosition(x,y);
				if (mStartPosition != INVALID_POSITION) {
					int mItemPosition = mStartPosition - getFirstVisiblePosition();
                    mDragPointOffset = y - getChildAt(mItemPosition).getTop();
                    mDragPointOffset -= ((int)ev.getRawY()) - y;
					startDrag(mItemPosition,y);
					drag(0,y);// replace 0 with x if desired
				}	
				break;
			case MotionEvent.ACTION_MOVE:
				drag(0,y);// replace 0 with x if desired
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			default:
				mDragMode = false;
				mEndPosition = pointToPosition(x,y);
				stopDrag(mStartPosition - getFirstVisiblePosition());
				
				if (mDropListener != null && mStartPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION) 
	        		 mDropListener.onDrop(mStartPosition, mEndPosition);
				else if (mDropListener != null && mStartPosition != INVALID_POSITION && mEndPosition == INVALID_POSITION && getChildCount() >= 1)
				{
					if (y < getChildAt(0).getTop())
					{
						mDropListener.onDrop(mStartPosition, 0);
					}
					else
					{
						mDropListener.onDrop(mStartPosition, getChildCount());
					}
				}
				break;
		}
		return true;
	}	
	
	// move the drag view
	private void drag(int x, int y) {
		if (mDragView != null) {
			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mDragView.getLayoutParams();
			layoutParams.x = x;
			layoutParams.y = y - mDragPointOffset;
			WindowManager mWindowManager = (WindowManager) getContext()
					.getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.updateViewLayout(mDragView, layoutParams);

			if (mDragListener != null)
				mDragListener.onDrag(x, y, this);// change null to "this" when ready to use
		}
	}

	// enable the drag view for dragging
	private void startDrag(int itemIndex, int y) {
		//stopDrag(itemIndex);
		layoutChildren();	//This fixed a bug where the drawing cache would contain an outdated image (another list row in this case.)
		View item = getChildAt(itemIndex);
		if (item == null) return;
		this.defaultBackgroundColor =  item.getDrawingCacheBackgroundColor();	//Store the background color of the item
		
    	int color = getResources().getColor(R.color.app_detail);
    	int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        
		item.setBackgroundColor(Color.argb(125, red, green, blue));	//Set the background color.
		item.setDrawingCacheEnabled(true);
		
		if (mDragListener != null)
			mDragListener.onStartDrag(item);
		
        // Create a copy of the drawing cache so that it does not get recycled
        // by the framework when the list tries to clean up memory
        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
        //Log.i(VIEW_LOG_TAG, itemIndex + ". " + item.toString());
        //Log.i(VIEW_LOG_TAG, itemIndex + ". " + ((Episode)this.getItemAtPosition(itemIndex)).getTitle());
        
        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = y - mDragPointOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;
        
        Context context = getContext();
        ImageView v = new ImageView(context);
        v.setImageBitmap(bitmap);      

        WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
        
        DragNDropAdapter ad = (DragNDropAdapter) getAdapter();
        this.tempEpisode = ad.mPlayList.get(itemIndex);
        ad.mPlayList.remove(itemIndex);
        item.setBackgroundColor(defaultBackgroundColor);	//Reset background color.
        invalidateViews();
	}

	// destroy drag view
	private void stopDrag(int itemIndex) {
		if (mDragView != null) {
			if (this.tempEpisode != null)
			{
				DragNDropAdapter ad = (DragNDropAdapter) getAdapter();
				ad.mPlayList.add(itemIndex, this.tempEpisode);
				invalidateViews();
				this.tempEpisode = null;
			}
			if (mDragListener != null)
				mDragListener.onStopDrag(getChildAt(itemIndex));
            mDragView.setVisibility(GONE);
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
	}
	

}
