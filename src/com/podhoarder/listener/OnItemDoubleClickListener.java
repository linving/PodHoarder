package com.podhoarder.listener;

import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class OnItemDoubleClickListener implements OnItemClickListener
{

	private static final long DOUBLE_CLICK_TIME_DELTA = 300;// milliseconds

	private ClickRunnable mRunnable;
	protected boolean mPosted = false;
	private Handler mHandler = new Handler();

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
	{
		if (!mPosted)
		{
			this.mRunnable = new ClickRunnable(parent, v, pos, id);
			mPosted = mHandler.postDelayed(this.mRunnable, DOUBLE_CLICK_TIME_DELTA);
		}
		else
		{
			mHandler.removeCallbacks(this.mRunnable);
			mPosted = false;
			onDoubleClick(parent, v, pos, id);
		}
	}
	
	private class ClickRunnable implements Runnable
	{
		private final AdapterView<?> parent;
		private final View v;
		private final int pos;
		private final long id;
		
		public ClickRunnable(AdapterView<?> parent, View v, int pos, long id)
		{
			this.parent = parent;
			this.v = v;
			this.pos = pos;
			this.id = id;
		}

		@Override
		public void run()
		{
			mPosted = false;
			onSingleClick(parent, v, pos, id);
		}
		
	}

	public abstract void onSingleClick(AdapterView<?> parent, View v, int pos, long id);

	public abstract void onDoubleClick(AdapterView<?> parent, View v, int pos, long id);
}
