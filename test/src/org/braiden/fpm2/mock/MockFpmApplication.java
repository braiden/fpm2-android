package org.braiden.fpm2.mock;

import org.braiden.fpm2.FpmApplication;
import org.braiden.fpm2.FpmCrypt;

public class MockFpmApplication extends FpmApplication {

	volatile private boolean isOpen = true;
	
	@Override
	public void closeCrypt() {
		isOpen = false;
	}
	
	@Override
	public int getCryptState() {
		return isOpen ? FpmApplication.STATE_UNLOCKED : FpmApplication.STATE_LOCKED;
	}

	@Override
	public boolean isCryptOpen() {
		return isOpen;
	}

	@Override
	public void onCreate() {
		fpmCrypt = new FpmCrypt();
		try {
			fpmCrypt.open(this.getAssets().open("plain.xml"), "password");
		} catch (Exception e) {
			throw new RuntimeException("Failed to open FPM.", e);
		}
	}

	@Override
	public void onTerminate() {

	}

	@Override
	public void openCrypt(String passphrase) {
		isOpen = true;
	}
		
}
