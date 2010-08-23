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
import java.util.Timer;
import java.util.TimerTask;

import org.braiden.fpm2.model.PasswordItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
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
	public static final String ACTION_FPM_UNLOCKED = "org.braiden.fpm2.FPM_UNLOCKED";
	// Action (Itent) published whenever the FPM db is locked
	public static final String ACTION_FPM_LOCKED = "org.braiden.fpm2.FPM_LOCKED";
	
	// default time, in milliseconds, before the FPM database is locked
	public static final long FPM_AUTO_LOCK_MILLISECONDS = 60L * 1000L;
	// default location if the FPM databased (XML) file
	public static final String FPM_FILE = "/sdcard/fpm";
	
	protected static final String TAG = "FpmApplication";
	
	private Timer autoLockTimer = new Timer();
	private FpmCrypt fpmCrypt = new FpmCrypt();
	volatile private ProgressDialog dialog;
	
	@Override
	public void onTerminate() {
		autoLockTimer.cancel();
		super.onTerminate();
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
		sendBroadcast(new Intent(ACTION_FPM_LOCKED));
	}
	
	/**
	 * True if the FPM store is open and unencrypted
	 * @return
	 */
	public boolean isCryptOpen() {
		return fpmCrypt.isOpen();
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
	 * A background service for openning the encrypted FPM store.
	 * For AES-256, FPM uses PBKDF2-SHA2 with 8k iterations, this
	 * could take > 20 seconds on slower devices.
	 * 
	 * We need a seperate service / thread to prevent
	 * "Application Not Responding" dialog.
	 * 
	 * @author braiden
	 *
	 */
	public static class FpmUnlockService extends Service {
			
		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}

		@Override
		public void onStart(Intent intent, int startId) {
			super.onStart(intent, startId);
			final FpmApplication app = (FpmApplication) getApplication();
			final String passphrase = intent.getStringExtra("passphrase");
			
			new Thread() {
				@Override
				public void run() {
					try {
						// do the unlock
						app.fpmCrypt.open(new FileInputStream(FPM_FILE), passphrase);
						dismissBusyDialog(app);
						// schedule event to re-lock the db
						app.autoLockTimer.purge();
						app.autoLockTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								app.closeCrypt();
							}
						}, FPM_AUTO_LOCK_MILLISECONDS);
						// dispatch event notifying activities that data is ready
						sendBroadcast(new Intent(ACTION_FPM_UNLOCKED));
					} catch (Exception e) {
						app.autoLockTimer.purge();
						dismissBusyDialog(app);
						Log.w(TAG, "Failed to unlock FpmCrypt.", e);
						// dispatch event saying data is gone.
						sendBroadcast(new Intent(ACTION_FPM_LOCKED));
					}
					stopSelf();
				}
			}.start();
		}
		
		private static void dismissBusyDialog(FpmApplication app) {
			if (app.dialog != null) {
				app.dialog.dismiss();
				app.dialog = null;
			}
		}
		
	}
	
}
