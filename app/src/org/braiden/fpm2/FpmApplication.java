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

import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;

import org.braiden.fpm2.model.PasswordItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class FpmApplication extends Application {

	public static final String ACTION_FPM_OPEN = "org.braiden.fpm2.FPM_OPEN";
	public static final String ACTION_FPM_CLOSE = "org.braiden.fpm2.FPM_CLOSE";
	
	public static final long FPM_AUTO_LOCK_MILLISECONDS = 60L * 1000L;
	
	protected static final String TAG = "FpmApplication";
	
	private FpmCrypt fpmCrypt;
	private BroadcastReceiver autoCloseReceiver;

	@Override
	public void onCreate() {
		super.onCreate();
		fpmCrypt = new FpmCrypt();
		autoCloseReceiver = new AutoCloseFpmBroadcastReceiver(this, fpmCrypt);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_FPM_CLOSE);
		filter.addAction(ACTION_FPM_OPEN);
		registerReceiver(autoCloseReceiver, filter);
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		unregisterReceiver(autoCloseReceiver);
	}

	public void openCrypt(final Activity activity) {
		if (!isCryptOpen()) {
			LayoutInflater factory = LayoutInflater.from(activity);
			final View textEntryView = factory.inflate(R.layout.passphrase_dialog, null);
			final EditText editText = (EditText) textEntryView.findViewById(R.id.password_edit);
			new AlertDialog.Builder(activity)
            	.setTitle(R.string.passphrase_dialog_title)
            	.setView(textEntryView)
            	.setCancelable(false)
            	.setPositiveButton(R.string.passphrase_dialog_ok, new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialog, int whichButton) {
           				Intent intent = new Intent(activity, FpmUnlockService.class);
           				intent.putExtra("passphrase", editText.getText().toString());
           				dialog.dismiss();
           				startService(intent);
            		}
            	})
            	.setNegativeButton(R.string.passphrase_dialog_cancel, new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialog, int which) {
            			activity.setResult(-1);
            			activity.finish();
            		}
            	})
            	.create()
            	.show();
		}		
	}
	
	public void closeCrypt() {
		fpmCrypt.close();
	}
	
	public boolean isCryptOpen() {
		return fpmCrypt.isOpen();
	}
	
	public void unlock(String passphrase) throws Exception {
		FpmApplication.this.fpmCrypt.open(
				new FileInputStream("/sdcard/fpm"),
				passphrase);
	}

	public String decrypt(String s) {
		if (isCryptOpen()) {
			try {
				return fpmCrypt.decrypt(s);
			} catch (Exception e) {
				Log.w(TAG, "Failed to decrypt String.", e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public List<PasswordItem> getPasswordItems() {
		if (isCryptOpen()) {
			return fpmCrypt.getFpmFile().getPasswordItems();
		} else {
			return Collections.emptyList();
		}
	}
	
	public PasswordItem getPasswordItemById(long id) {
		List<PasswordItem> items = getPasswordItems();
		if (id >= items.size()) {
			return null;
		} else {
			return items.get((int) id);
		}
	}
	
	public static class AutoCloseFpmBroadcastReceiver extends BroadcastReceiver {

		Thread autoCloseThread = null;
		FpmCrypt fpmCrypt;
		Context ctx;
		
		public AutoCloseFpmBroadcastReceiver(Context ctx, FpmCrypt crypt) {
			this.fpmCrypt = crypt;
			this.ctx = ctx;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ACTION_FPM_OPEN.equals(intent.getAction())) {
				if (autoCloseThread == null || !autoCloseThread.isAlive()) {
					autoCloseThread = new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(FPM_AUTO_LOCK_MILLISECONDS);
								fpmCrypt.close();
								Intent intent = new Intent(ACTION_FPM_CLOSE);
								ctx.sendBroadcast(intent);
							} catch (InterruptedException e) {
								
							}
						}
					};
					autoCloseThread.start();
				}
			} else if (ACTION_FPM_CLOSE.equals(intent.getAction())) {
				if (autoCloseThread != null && autoCloseThread.isAlive()) {
					autoCloseThread.interrupt();
					autoCloseThread = null;
				}
			}
		}
		
	}
	
}
