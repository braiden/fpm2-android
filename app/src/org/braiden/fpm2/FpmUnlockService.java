package org.braiden.fpm2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
					app.unlock(passphrase);
					app.dismissBusyDialog();
					sendBroadcast(new Intent(FpmApplication.ACTION_FPM_OPEN));
				} catch (Exception e) {
					app.dismissBusyDialog();
					Log.w(TAG, "Failed to unlock FpmCrypt.", e);
					sendBroadcast(new Intent(FpmApplication.ACTION_FPM_CLOSE));
				}
				stopSelf();
			}
		}.start();
		
	}
	
	
	
}
