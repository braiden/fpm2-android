package org.braiden.fpm2;

import java.io.IOException;
import java.io.InputStream;

import org.braiden.fpm2.FpmApplication.FpmFileLocator;

import android.content.Context;
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
	
	protected void open(final String passphrase) throws Throwable {
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				getFpmApplication().openCrypt(passphrase);
			}
		});
		getInstrumentation().waitForIdleSync();
	}
	
	protected void close() throws Throwable {
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				getFpmApplication().closeCrypt();
			}
		});
		getInstrumentation().waitForIdleSync();
	}
	
	public void testAutoLock() throws Exception {
		
	}
	
	public void testStates() throws Throwable {	
		// override where the fpm file is openned from for this unit test
		getFpmApplication().setFpmFileLocator(new TestAssetsFpmFileLocator("plain.xml", getInstrumentation().getContext()));
		
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
		open("THIS_IS_THE_WRONG_PASSPHRASE");
		
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
		close();
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_LOCKED, getFpmApplication().getCryptState());
		synchronized (this) {
			assertEquals(0, eventFpmUnlock);
			assertEquals(0, eventFpmLock);
			assertEquals(1, eventFpmError);
		}
		
		// try to open the crypt
		open("password");
		
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
		

		// close, and change state
		close();
		assertFalse(getFpmApplication().isCryptOpen());
		assertEquals(FpmApplication.STATE_LOCKED, getFpmApplication().getCryptState());
		synchronized (this) {
			assertEquals(1, eventFpmUnlock);
			assertEquals(1, eventFpmLock);
			assertEquals(1, eventFpmError);
		}
		
		// close, again, and make sure event is not reboardcast
		close();
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
