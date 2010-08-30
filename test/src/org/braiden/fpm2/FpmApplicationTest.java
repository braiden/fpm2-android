package org.braiden.fpm2;

import java.io.IOException;
import java.io.InputStream;

import org.braiden.fpm2.FpmApplication.FpmFileLocator;

import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

public class FpmApplicationTest extends InstrumentationTestCase implements FpmBroadcastReceiver.Listener {

	protected static final String TAG = "FpmApplicationTest";
	
	private FpmApplication application;
	
	private int eventFpmError = 0;
	private int eventFpmLock = 0;
	private int eventFpmUnlock = 0;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// override where the fpm file is openned from for this unit test
		getFpmApplication().setFpmFileLocator(new TestAssetsFpmFileLocator("plain.xml", getInstrumentation().getContext()));
		setFpmDatabaseAutolock(-1);
	}

	@Override
	protected void tearDown() throws Exception {
		application.onTerminate();
		application = null;
		super.tearDown();
	}

	protected FpmApplication getFpmApplication() {
		if (application == null) {
			try {
				application = (FpmApplication) Instrumentation.newApplication(
						FpmApplication.class,
						getInstrumentation().getTargetContext());
			} catch (Exception e) {
				Log.e(TAG, "Failed to create Application.", e);
				fail(e.toString());
			}
			getInstrumentation().callApplicationOnCreate(application);
		}
		return application;
	}
	
	protected void unlockFpmDatabase(final String passphrase) {
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					getFpmApplication().openCrypt(passphrase);
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "Failed to open FPM.", e);
			fail(e.toString());
		}
		getInstrumentation().waitForIdleSync();
	}
	
	protected void unlockFpmDatabaseSync(final String passphrase) throws Exception {
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					getFpmApplication().openCrypt(passphrase);
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "Failed to open FPM.", e);
			fail(e.toString());
		}
		getInstrumentation().waitForIdleSync();
		while (getFpmApplication().getCryptState() == FpmApplication.STATE_BUSY) {
			Thread.sleep(100L);
		}
		getInstrumentation().waitForIdleSync();
	}
	
	protected void lockFpmDatabaseSync() {
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					getFpmApplication().closeCrypt();
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "Failed to close FPM.", e);
			fail(e.toString());
		}
		getInstrumentation().waitForIdleSync();
	}
	
	protected void setFpmDatabaseAutolock(final int seconds) {
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(getFpmApplication());
					defaultPrefs.edit().putString(FpmApplication.PREF_AUTOLOCK, "" + seconds).commit();
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "Failed to set lock timeout.", e);
			fail(e.toString());
		}
		getInstrumentation().waitForIdleSync();
	}

	public void testStates() throws Throwable {	
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
		unlockFpmDatabase("THIS_IS_THE_WRONG_PASSPHRASE");
		
		// once UI has services thread, should imediately show as busy
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_BUSY, getFpmApplication().getCryptState());
		
		// give the db some time to try to open
		Thread.sleep(1000L);
		
		// check for valid data, and event broadcasts
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_FAILED, getFpmApplication().getCryptState());
		synchronized (this) {
			assertEquals(0, eventFpmUnlock);
			assertEquals(0, eventFpmLock);
			assertEquals(1, eventFpmError);
		}

		// try to close
		lockFpmDatabaseSync();
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_LOCKED, getFpmApplication().getCryptState());
		synchronized (this) {
			assertEquals(0, eventFpmUnlock);
			assertEquals(0, eventFpmLock);
			assertEquals(1, eventFpmError);
		}
		
		// try to open the crypt
		unlockFpmDatabase("password");
		
		// give the db some time to try to open
		Thread.sleep(1000L);
		
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
		

		// close, and change state
		lockFpmDatabaseSync();
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_LOCKED, getFpmApplication().getCryptState());
		synchronized (this) {
			assertEquals(1, eventFpmUnlock);
			assertEquals(1, eventFpmLock);
			assertEquals(1, eventFpmError);
		}
		
		// close, again, and make sure event is not reboardcast
		lockFpmDatabaseSync();
		getInstrumentation().waitForIdleSync();
		synchronized (this) {
			assertEquals(1, eventFpmUnlock);
			assertEquals(1, eventFpmLock);
			assertEquals(1, eventFpmError);
		}
		
		receiver.unregister();
	}
	
	public void testAutoLock() throws Throwable {
		unlockFpmDatabaseSync("password");
		assertTrue(getFpmApplication().isCryptOpen());
		setFpmDatabaseAutolock(1);
		Thread.sleep(800);
		assertTrue(getFpmApplication().isCryptOpen());
		Thread.sleep(300);
		assertFalse(getFpmApplication().isCryptOpen());
		unlockFpmDatabaseSync("password");
		assertTrue(getFpmApplication().isCryptOpen());
		Thread.sleep(1100);
		assertFalse(getFpmApplication().isCryptOpen());
		unlockFpmDatabaseSync("password");
		assertTrue(getFpmApplication().isCryptOpen());
		setFpmDatabaseAutolock(-1);
		Thread.sleep(1100);
		assertTrue(getFpmApplication().isCryptOpen());
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
	
	public static class TestAssetsFpmFileLocator implements FpmFileLocator {
		
		private String assetPath;
		private Context context;

		public TestAssetsFpmFileLocator(String assetPath, Context context) {
			this.assetPath = assetPath;
			this.context = context;
		}

		@Override
		public InputStream open(String file) throws IOException {
			return context.getAssets().open(assetPath);
		}
		
	}
	
}
