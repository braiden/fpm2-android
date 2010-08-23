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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * FpmApplication provides all Actitities and Services with access 
 * to the FPM PasswordItems, and ability to unlock the datastore.
 * 
 * @author braiden
 *
 */

public class FpmApplication extends Application {

	// Action (Itent) published whenever the FPM db is unlocked
	public static final String ACTION_FPM_OPEN = "org.braiden.fpm2.FPM_OPEN";
	// Action (Itent) published whenever the FPM db is locked
	public static final String ACTION_FPM_CLOSE = "org.braiden.fpm2.FPM_CLOSE";
	
	// default time, in milliseconds, before the FPM database is locked
	public static final long FPM_AUTO_LOCK_MILLISECONDS = 60L * 1000L;
	// default location if the FPM databased (XML) file
	public static final String FPM_FILE = "/sdcard/fpm";
	
	protected static final String TAG = "FpmApplication";
	
	private FpmCrypt fpmCrypt;
	private BroadcastReceiver autoCloseReceiver;
	volatile private ProgressDialog dialog;

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

	/**
	 * If the FPM store is not already open and unlocked,
	 * try to open it. Creates a password dialog in the given
	 * activity.
	 * 
	 * @param activity
	 */
	public void openCrypt(final Activity activity) {
		// db is not already open, and not currently checking a password
		if (!isCryptOpen() && dialog == null) {
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
           				dialog.dismiss();

           				// create a busy dialog
           				ProgressDialog d = FpmApplication.this.dialog = new ProgressDialog(activity);
           				d.setMessage(activity.getResources().getString(R.string.checking_passphrase));
           				d.setCancelable(false);
           				d.setIndeterminate(true);
           				d.show();
           				
           				// start a service which will generate the crypto key
           				// on slow devices this can take a while. event on 
           				// fast devices can see ANR.
           				Intent intent = new Intent(activity, FpmUnlockService.class);
           				intent.putExtra("passphrase", editText.getText().toString());
           				activity.startService(intent);
            		}
            	})
            	.setNegativeButton(R.string.passphrase_dialog_cancel, new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialog, int which) {
            			// user canceled passphrase dialog, application will terminate
            			// stop this activity, and cascade -1 result.
            			activity.setResult(-1);
            			activity.finish();
            		}
            	})
            	.create()
            	.show();
		}		
	}
	
	/**
	 * Close the FPM datastore. User will need to enter
	 * passphrase again, before accessing any data. 
	 */
	public void closeCrypt() {
		fpmCrypt.close();
		System.gc();
	}
	
	/**
	 * True if the FPM store is open and unencrypted
	 * @return
	 */
	public boolean isCryptOpen() {
		return fpmCrypt.isOpen();
	}
	
	protected void dismissBusyDialog() {
		dialog.dismiss();
		dialog = null;
	}

	/**
	 * Try to open the FPM store with given passphrase.
	 * This method can be slow, and should be run in a seperate Service/Thread
	 * 
	 * @param passphrase
	 * @throws Exception
	 */
	protected void unlock(String passphrase) throws Exception {
		FpmApplication.this.fpmCrypt.open(
				new FileInputStream(FPM_FILE),
				passphrase);
	}

	/**
	 * Decrypt the given string using the key of open store.
	 * 
	 * @param s
	 * @return
	 */
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
	
	/**
	 * Get all password items in this store.
	 * 
	 * @return
	 */
	public List<PasswordItem> getPasswordItems() {
		if (isCryptOpen()) {
			return fpmCrypt.getFpmFile().getPasswordItems();
		} else {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Get a password item by ID.
	 * 
	 * @param id
	 * @return
	 */
	public PasswordItem getPasswordItemById(long id) {
		List<PasswordItem> items = getPasswordItems();
		if (id >= items.size()) {
			return null;
		} else {
			return items.get((int) id);
		}
	}
	
	/**
	 * The class listens for the FPM_OPEN (unlocked) event,
	 * and creates a thread to automatically close the store
	 * at sometime in the future.
	 * 
	 * @author braiden
	 *
	 */
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
					// a java Thread is fine, no user impact 
					// of OS killing our app with only this thread is alive.
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
