package org.braiden.fpm2;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class FpmApplicationTest extends ActivityInstrumentationTestCase2<AboutActivity> implements FpmBroadcastReceiver.Listener {

	protected static final String TAG = "FpmApplicationTest";
	
	private int eventFpmError = 0;
	private int eventFpmLock = 0;
	private int eventFpmUnlock = 0;
	
	public FpmApplicationTest() {
		// test for App needs Instrumentation and UI thread
		// so it extends AcvityTestCase, use AboutActity
		// as the host activity - i have to use something that
		// is defined in manifest.
		super("org.braiden.fpm2", AboutActivity.class);
	}

	protected FpmApplication getFpmApplication() {
		return (FpmApplication) getActivity().getApplication();
	}
	
	public void testStates() throws Throwable {
		// disable database autolock for this test
		SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		defaultPrefs.edit().putString(FpmApplication.PREF_AUTOLOCK, "-1").commit();
		
		// register junit to listen for events
		resetEventCounts();
		FpmBroadcastReceiver receiver = new FpmBroadcastReceiver(getFpmApplication(), FpmApplicationTest.this);				
		
		// check all outputs will crypt locked
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_LOCKED, getFpmApplication().getCryptState());
		assertTrue(getFpmApplication().getCategories().isEmpty());
		assertTrue(getFpmApplication().getPasswordItems().isEmpty());
		assertNull(getFpmApplication().getPasswordItemById(0));
		
		// try to open the crypt
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				getFpmApplication().openCrypt("WRONG_PASSWORD");
			}
		});
		getInstrumentation().waitForIdleSync();
		
		// once UI has services thread, should imediately show as busy
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_BUSY, getFpmApplication().getCryptState());
		
		// give the db some time to try to open
		Thread.sleep(2000L);
		
		// check for valid data, and event broadcasts
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_FAILED, getFpmApplication().getCryptState());
		synchronized (this) {
			assertEquals(0, eventFpmUnlock);
			assertEquals(0, eventFpmLock);
			assertEquals(1, eventFpmError);
		}

		// try to close
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				getFpmApplication().closeCrypt();
			}
		});
		getInstrumentation().waitForIdleSync();
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_LOCKED, getFpmApplication().getCryptState());
		synchronized (this) {
			assertEquals(0, eventFpmUnlock);
			assertEquals(0, eventFpmLock);
			assertEquals(1, eventFpmError);
		}
		
		// try to open the crypt
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				getFpmApplication().openCrypt("password");
			}
		});
		getInstrumentation().waitForIdleSync();
		
		// give the db some time to try to open
		Thread.sleep(2000L);
		
		// check for valid data, and event broadcasts
		assertTrue(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_UNLOCKED, getFpmApplication().getCryptState());
		synchronized (this) {
			assertEquals(1, eventFpmUnlock);
			assertEquals(0, eventFpmLock);
			assertEquals(1, eventFpmError);
		}
		assertEquals(getFpmApplication().getCategories().size(), 3);
		assertEquals(getFpmApplication().getPasswordItems().size(), 3);
		assertEquals(getFpmApplication().getPasswordItemById(0).getTitle(), "Amazon");
		
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				getFpmApplication().closeCrypt();
			}
		});
		getInstrumentation().waitForIdleSync();

		// check for valid data, and event broadcasts
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_LOCKED, getFpmApplication().getCryptState());
		synchronized (this) {
			assertEquals(1, eventFpmUnlock);
			assertEquals(1, eventFpmLock);
			assertEquals(1, eventFpmError);
		}
		
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				getFpmApplication().closeCrypt();
			}
		});
		getInstrumentation().waitForIdleSync();
		synchronized (this) {
			assertEquals(1, eventFpmUnlock);
			assertEquals(1, eventFpmLock);
			assertEquals(1, eventFpmError);
		}
		
		receiver.unregister();
	}
	
	private void resetEventCounts() {
		synchronized (this) {
			eventFpmError = 0;
			eventFpmLock = 0;
			eventFpmUnlock = 0;
		}
	}
	
	@Override
	public void onFpmError(int err) {
		Log.d(TAG, "onFpmError(" + err + ")");
		synchronized (this) {
			eventFpmError++;
			notifyAll();
		}
	}

	@Override
	public void onFpmLock() {
		Log.d(TAG, "onFpmLock()");
		synchronized (this) {
			eventFpmLock++;
			notifyAll();
		}
	}

	@Override
	public void onFpmUnlock() {
		Log.d(TAG, "onFpmUnlock()");
		synchronized (this) {
			eventFpmUnlock++;
			notifyAll();
		}
	}
	
}
