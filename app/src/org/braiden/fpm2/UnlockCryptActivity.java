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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * View for unlocking the fpm database.
 * Other acitivities can call unlockIfRequired()
 * to unlock db before attempting access.
 *  
 * @author braiden
 *
 */
public class UnlockCryptActivity extends Activity implements FpmBroadcastReceiver.Listener {
	
	public static final int UNLOCK_CRYPT_REQUEST_CODE = 10;
	
	private static boolean isUnlockActivityRunning = false;
	
	private LayoutInflater layoutInflater;
	private FpmBroadcastReceiver broadcastReceiver;
	private AlertDialog passphraseDialog = null;
	private ProgressDialog progressDialog = null;
	
	protected FpmApplication getFpmApplication() {
		return (FpmApplication) getApplication();
	}
	
	public static void unlockIfRequired(Activity caller) {
		if (!((FpmApplication) caller.getApplication()).isCryptOpen()
				&& !isUnlockActivityRunning) {
			isUnlockActivityRunning = true;
			Intent intent = new Intent(caller, UnlockCryptActivity.class);
			caller.startActivityForResult(intent, UNLOCK_CRYPT_REQUEST_CODE);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layoutInflater = LayoutInflater.from(this);
		broadcastReceiver = new FpmBroadcastReceiver(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		broadcastReceiver.unregister();
		isUnlockActivityRunning = false;
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && !getFpmApplication().isCryptOpen()) {
			// initiate attempt to lock db
			// window is focused whenever the dialogs are dismissed
			// so this causes password to continue prompting until
			// acitivity finishes
			onFpmLock();
		}
	}

	/**
	 * Call back when openning FPM store generates an error.
	 * If the error is anything other than bad password, 
	 * show an error dialog.
	 * 
	 * @param msg
	 */
	@Override
	public void onFpmError(int msg) {
		dismissDialogs();
		
		if (msg != 0 && msg != R.string.exception_fpm_passphrase) {
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
				.setTitle(msg)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setCancelable(false)
				.setPositiveButton(R.string.passphrase_dialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						onFpmLock();
					}
				});
			int extendedError = getExtendedErrorMessage(msg);
			if (extendedError != 0) {
				dialogBuilder.setMessage(extendedError);
			}
			dialogBuilder.create().show();
		} else {
			// no error prompt for bad passphrase,
			// just go back and prompt again.
			onFpmLock();
		}
	}
	
	/**
	 * Callback when the database is unlocked an availible.
	 * sub-classes can override this method but, should
	 * call super.onFpmUnlock()
	 */
	@Override
	public void onFpmUnlock() {
		// once datastore is unlocked
		// clear any passphrase related dialogs
		dismissDialogs();
		setResult(RESULT_OK);
		finish();
	}
	
	/**
	 * Callback when datadase is locked.
	 * sub-classes who override this method should
	 * take care to call super.onFpmLock()
	 * if they want user to be prompted for passphrase.
	 */
	@Override
	public void onFpmLock() {
		if (getFpmApplication().getCryptState() != FpmApplication.STATE_BUSY) {
			createPassphraseDialog().show();
		} else {
			createProgressDialog().show();
		}		
	}
	
	private void dismissDialogs() {
		// once the password crypt is unlocked dismiss
		// any dialogs the might exist.
		
		if (passphraseDialog != null) {
			passphraseDialog.dismiss();
		}
		
		if (progressDialog != null) {
			progressDialog.dismiss();
			// progress dialog can't be
			// reused, the busy spinner stops spinning.
			progressDialog = null;
		}
	}

	/**
	 * Callback once the user has entered a passphrase.
	 * @param passphrase
	 */
	protected void onFpmPassphraseOk(String passphrase) {
		createProgressDialog().show();
		getFpmApplication().openCrypt(passphrase);
	}
		
	/**
	 * Callback if user clicks cacnel in passphrase prompt.
	 */
	protected void onFpmPassphraseCancel() {
		android.util.Log.d(PasswordItemListActivity.TAG, "onFpmPassphraseCancel()");
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * Create progress dialog for "Checking Passphrase..."
	 * @return
	 */
	protected Dialog createProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(getResources().getString(R.string.checking_passphrase));
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
		}
		return progressDialog;
	}
	
	/**
	 * Create dialog for passphrase prompt.
	 * @return
	 */
	protected Dialog createPassphraseDialog() {
		if (passphraseDialog == null ) {		
			View textEntryView = layoutInflater.inflate(R.layout.passphrase_dialog, null);
			final EditText editText = (EditText) textEntryView.findViewById(R.id.password_edit);
			passphraseDialog =  new AlertDialog.Builder(this)
        		.setTitle(R.string.passphrase_dialog_title)
        		.setView(textEntryView)
        		.setCancelable(true)
        		.setPositiveButton(R.string.passphrase_dialog_ok, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				dialog.dismiss();
        				String passphrase = editText.getText().toString();
        				editText.setText("");
        				onFpmPassphraseOk(passphrase);
        			}
        		})
        		.setNegativeButton(R.string.passphrase_dialog_cancel, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				dialog.dismiss();
        				onFpmPassphraseCancel();
        			}
        		})
        		.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						onFpmPassphraseCancel();
					}
				})
				.create();
			editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					passphraseDialog.dismiss();
    				String passphrase = editText.getText().toString();
    				editText.setText("");
    				onFpmPassphraseOk(passphrase);
					return true;
				}
			});
		}
		return passphraseDialog;
	}
	
	protected int getExtendedErrorMessage(int shortmsg) {
		switch (shortmsg) {
			case R.string.exception_file_not_found:
			case R.string.exception_io:
			case R.string.exception_sax:
				return R.string.exception_file_not_found_extra;
			case R.string.exception_fpm_unsupported:
				return R.string.exception_fpm_unsupported_extra;
			case R.string.exception_jce:
				return R.string.exception_jce_extra;
			default:
				return 0;
		}
	
	}

}
