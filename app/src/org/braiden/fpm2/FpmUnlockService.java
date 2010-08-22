package org.braiden.fpm2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
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
		Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		FpmApplication app = (FpmApplication) getApplication();
		String passphrase = intent.getStringExtra("passphrase");
		try {
			app.unlock(passphrase);
			sendBroadcast(new Intent(FpmApplication.ACTION_FPM_OPEN));
		} catch (Exception e) {
			vib.vibrate(250L);
			Log.w(TAG, "Failed to unlock FpmCrypt.", e);
			sendBroadcast(new Intent(FpmApplication.ACTION_FPM_CLOSE));
		}
		stopSelf();
	}
	
}
