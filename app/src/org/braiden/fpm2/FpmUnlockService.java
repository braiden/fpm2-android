package org.braiden.fpm2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
public class FpmUnlockService extends Service {

	private static final String TAG = "FpmUnlockService";
		
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
					app.unlock(passphrase);
					app.dismissBusyDialog();
					// dispatch event notifying activities that data is ready
					sendBroadcast(new Intent(FpmApplication.ACTION_FPM_OPEN));
				} catch (Exception e) {
					app.dismissBusyDialog();
					Log.w(TAG, "Failed to unlock FpmCrypt.", e);
					// dispatch event saying data is gone.
					sendBroadcast(new Intent(FpmApplication.ACTION_FPM_CLOSE));
				}
				stopSelf();
			}
		}.start();
		
	}
	
	
	
}
