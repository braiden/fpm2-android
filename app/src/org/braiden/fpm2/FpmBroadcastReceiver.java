package org.braiden.fpm2;

/*
 * Copyright (c) 2010 Braiden Kindt
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * A broadcast receiver which calls our onLock, onUnlock methods
 * when fpm broadcasts are received. 
 * 
 * @author braiden
 *
 */
public class FpmBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "FpmBroadcastReceiver";
	private Context context;
	private Listener listener;
	
	public FpmBroadcastReceiver(Context activity) {
		this(activity, (Listener) activity);
	}
	
	public FpmBroadcastReceiver(Context activity, Listener listener) {
		this.listener = listener;
		this.context = activity;
		this.context.registerReceiver(this, createIntentFilter());
	}
	
	
	public void unregister() {
		this.context.unregisterReceiver(this);
	}
	
	public static IntentFilter createIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(FpmApplication.ACTION_FPM_LOCK);
		filter.addAction(FpmApplication.ACTION_FPM_UNLOCK);
		filter.addAction(FpmApplication.ACTION_FPM_FAIL);
		return filter;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive(this=" + this + ", action=" + intent.getAction() + ")");
		if (FpmApplication.ACTION_FPM_UNLOCK.equals(intent.getAction())) {
			listener.onFpmUnlock();
		} else if (FpmApplication.ACTION_FPM_LOCK.equals(intent.getAction())) {
			listener.onFpmLock();
		} else if (FpmApplication.ACTION_FPM_FAIL.equals(intent.getAction())) {
			listener.onFpmError(intent.getIntExtra(FpmApplication.EXTRA_MSG, 0));
		}
	}
	
	public static interface Listener {
		void onFpmUnlock();
		void onFpmLock();
		void onFpmError(int err);
	}
	
}
