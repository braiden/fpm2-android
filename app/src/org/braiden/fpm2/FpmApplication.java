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

import android.app.Application;
import android.content.Intent;
import android.util.Log;

/**
 * FpmApplication provides all Actitities and Services with access 
 * to the FPM PasswordItems, and ability to unlock the datastore.
 * 
 * @author braiden
 *
 */

public class FpmApplication extends Application {

	// Action (Intent) published whenever the FPM db is unlocked
	public static final String ACTION_FPM_UNLOCK = "org.braiden.fpm2.FPM_UNLOCKED";
	// Action (Intent) published whenever the FPM db is locked
	public static final String ACTION_FPM_LOCK = "org.braiden.fpm2.FPM_LOCKED";
	
	// default time, in milliseconds, before the FPM database is locked
	public static final long FPM_AUTO_LOCK_MILLISECONDS = 60L * 1000L;
	// default location if the FPM databased (XML) file
	public static final String FPM_FILE = "/sdcard/fpm";
	
	protected static final String TAG = "FpmApplication";
	
	private Timer autoLockTimer = new Timer();
	private FpmCrypt fpmCrypt = new FpmCrypt();
	
	@Override
	public void onTerminate() {
		autoLockTimer.cancel();
		super.onTerminate();
	}

	public void broadcastState() {
		Intent broadcast = new Intent(isCryptOpen() ? FpmApplication.ACTION_FPM_UNLOCK : FpmApplication.ACTION_FPM_LOCK);
		sendBroadcast(broadcast);		
	}
	
	/**
	 * Try to unlock the store. This can take a while
	 * and must be called in seperate thread / store
	 * @param passphrase
	 * @throws Exception
	 */
	public void openCrypt(String passphrase) throws Exception {
		boolean success = true;
		try {
			fpmCrypt.open(new FileInputStream(FPM_FILE), passphrase);
		} catch (Exception e) {
			success = false;
			throw e;
		} finally {
			// setup the timer to automatically re-lock 
			// the password database. Can't trust android
			// to do it, don't know when the OS is going
			// to purge the task.
			autoLockTimer.purge();
			if (success) {
				autoLockTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						closeCrypt();
						broadcastState();
					}
				}, FPM_AUTO_LOCK_MILLISECONDS);
			}
		}
	}
	
	/**
	 * Close the FPM datastore. User will need to enter
	 * passphrase again, before accessing any data. 
	 */
	public void closeCrypt() {
		fpmCrypt.close();
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
	
}
