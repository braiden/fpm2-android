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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.braiden.fpm2.FpmCrypt.FpmCipherUnsupportedException;
import org.braiden.fpm2.FpmCrypt.FpmPassphraseInvalidException;
import org.braiden.fpm2.model.PasswordItem;
import org.xml.sax.SAXException;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * FpmApplication provides all Actitities and Services with access 
 * to the FPM PasswordItems, and ability to unlock the datastore.
 * 
 * @author braiden
 *
 */

public class FpmApplication extends Application implements OnSharedPreferenceChangeListener {

	// Action (Intent) published whenever the FPM db is unlocked
	public static final String ACTION_FPM_UNLOCK = "org.braiden.fpm2.FPM_UNLOCKED";
	// Action (Intent) published whenever the FPM db is locked
	public static final String ACTION_FPM_LOCK = "org.braiden.fpm2.FPM_LOCKED";
	// Action (Intent) published when the FPM database has failed to open
	public static final String ACTION_FPM_FAIL = "org.braiden.fpm2.FPM_FAILED";
	
	// the crypt is locked, and must be openned with passphrase before access
	public static final int STATE_LOCKED = 0;
	// the crypt is unlocked, data can be read, and passwords can be decrypted
	public static final int STATE_UNLOCKED = 1;
	// the most recent attempt to access the crypt failed
	public static final int STATE_FAILED = 2;
	// the crypt is currently locked with an unlock operation in progress
	public static final int STATE_BUSY = 3;
	
	public static final String PREF_AUTOLOCK = "fpm_autolock";
	
	public static final String EXTRA_MSG = "org.braiden.fpm2.EXTRA_MESSAGE";
	
	// default location if the FPM databased (XML) file
	public static final String FPM_FILE = "/sdcard/fpm";
	
	protected static final String TAG = "FpmApplication";
	
	private FpmCrypt fpmCrypt = new FpmCrypt();
	private Timer autoLockTimer = null;
	private SharedPreferences prefs;
	private int failureMsg = 0;
	private int state = STATE_LOCKED;
	
	@Override
	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onTerminate() {
		if (autoLockTimer != null) {
			autoLockTimer.cancel();
		}
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onTerminate();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (PREF_AUTOLOCK.equals(key)) {
			scheduleAutoLock();
		}
	}
	
	/**
	 * 
	 * Get the state of the FPM database
	 * @return STATE_LOCKED, STATE_UNLOCKED, STATE_ERROR, STATE_BUSY
	 * 
	 */
	synchronized
	public int getCryptState() {
		return state;
	}
	
	synchronized
	public boolean isCryptOpen() {
		return state == STATE_UNLOCKED;
	}
	
	/**
	 * Try to unlock the store. This can take a while
	 * and must be called in seperate thread / store
	 * @param passphrase
	 */
	public void openCrypt(String passphrase) {
		int failureMsg = 0;
		try {
			synchronized (this) {
				state = STATE_BUSY;
			}
			fpmCrypt.open(new FileInputStream(FPM_FILE), passphrase);
			scheduleAutoLock();
		} catch (FileNotFoundException e) {
			failureMsg = R.string.exception_file_not_found;
			Log.w(TAG, "Failed to open FPM database.", e);
		} catch (IOException e) {
			failureMsg = R.string.exception_io;
			Log.w(TAG, "Failed to open FPM database.", e);
		} catch (SAXException e) {
			failureMsg = R.string.exception_sax;
			Log.w(TAG, "Failed to open FPM database.", e);
		} catch (GeneralSecurityException e) {
			failureMsg = R.string.exception_jce;
			Log.w(TAG, "Failed to open FPM database.", e);
		} catch (FpmCipherUnsupportedException e) {
			failureMsg = R.string.exception_fpm_unsupported;
			Log.w(TAG, "Failed to open FPM database.", e);
		} catch (FpmPassphraseInvalidException e) {
			failureMsg = R.string.exception_fpm_passphrase;
			Log.w(TAG, "Failed to open FPM database.", e);
		}
		
		synchronized (this) {
			this.failureMsg = failureMsg;
			state = failureMsg == 0 ? STATE_UNLOCKED : STATE_FAILED;
			broadcastState();
		}
		
	}
	
	/**
	 * Close the FPM datastore. User will need to enter
	 * passphrase again, before accessing any data. 
	 */
	synchronized
	public void closeCrypt() {
		fpmCrypt.close();
		state = STATE_LOCKED;
		broadcastState();
	}

	/**
	 * Decrypt the given string using the key of open store.
	 * 
	 * @param s
	 * @return
	 */
	synchronized
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
	synchronized
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
	synchronized
	public PasswordItem getPasswordItemById(long id) {
		List<PasswordItem> items = getPasswordItems();
		if (id >= items.size()) {
			return null;
		} else {
			return items.get((int) id);
		}
	}
	
	synchronized
	private void scheduleAutoLock() {
		if (isCryptOpen()) {
			long autoLockMilliseconds = getAutoLockMilliseconds();
			
			if (autoLockTimer != null) {
				autoLockTimer.cancel();
				autoLockTimer = null;
			}
			
			if (autoLockMilliseconds > 0) {
				autoLockTimer = new Timer();
				autoLockTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						closeCrypt();
					}
				}, autoLockMilliseconds);
			}
		}
	}
	
	private void broadcastState() {
		String action = null;
		
		switch (state) {
			case STATE_UNLOCKED:
				action = ACTION_FPM_UNLOCK;
				break;
			case STATE_FAILED:
				action = ACTION_FPM_FAIL;
				break;
			case STATE_LOCKED:
				action = ACTION_FPM_LOCK;
				break;
		}
		
		Intent broadcast = new Intent(action);
		broadcast.putExtra(EXTRA_MSG, failureMsg);
		sendBroadcast(broadcast);		
	}
	
	private long getAutoLockMilliseconds() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String stringResult = prefs.getString(PREF_AUTOLOCK, null);
		long result = 0;
		try {
			result = stringResult == null ? (60L * 1000L) : (Long.parseLong(stringResult) * 1000L);
		} catch (NumberFormatException e) {
			Log.w(TAG, "\"" + PREF_AUTOLOCK + "\" has invalid value \"" + stringResult + "\". Will default to 1 minute.");
		}
		return result;
	}
	
}
