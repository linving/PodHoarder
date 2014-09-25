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

package com.podhoarder.view;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.podhoarder.adapter.DragNDropAdapter;
import com.podhoarder.listener.DragListener;
import com.podhoarder.util.AnimUtils;

public class DragNDropListView extends ListView {

	boolean mDragMode;
	
	GestureDetector mGestureDetector;
	
	DragListener mDragListener;
	
	public DragNDropListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setDragListener(DragListener l) {
		mDragListener = l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();	
		
		if (action == MotionEvent.ACTION_DOWN && ((DragNDropAdapter)this.getAdapter()).isReorderingEnabled() && x > (this.getWidth()-75)) {
			mDragMode = true;
		}

		if (!mDragMode) 
			return super.onTouchEvent(ev);
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (pointToPosition(0,y) != INVALID_POSITION) 
				{
					mDragListener.onStartDrag();
					Log.i(VIEW_LOG_TAG, "Grabbed index: " + pointToPosition(0,y));
					mDragListener.onDrag(0, y, this);
				}	
				break;
			case MotionEvent.ACTION_MOVE:
				mDragListener.onDrag(0, y, this);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			default:
				Log.i(VIEW_LOG_TAG, "dropped at: " + pointToPosition(0,y));
				mDragListener.onStopDrag();
				mDragMode = false;
				break;
		}
		return true;
	}
	
	public void animateMove(int from, int to)
	{
		View fromView = this.getChildAt(from);
		View toView = this.getChildAt(to);

		if (from < to)
		{
			AnimUtils.verticalTranslateAnimation(fromView, fromView.getHeight(), 0);
			AnimUtils.verticalTranslateAnimation(toView, -toView.getHeight(), 0);
		}
		else
		{
			AnimUtils.verticalTranslateAnimation(fromView, -fromView.getHeight(), 0);
			AnimUtils.verticalTranslateAnimation(toView, toView.getHeight(), 0);
		}
		
	}
	
	
}
