package org.braiden.fpm2;

import org.braiden.fpm2.mock.MockFpmApplication;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.text.ClipboardManager;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class PasswordItemListActivityTest extends ActivityUnitTestCase<PasswordItemListActivity> {
	
	protected static final String TAG = "PasswordItemListActivityTest";
	// how long to wait for async filter thread to finish before
	// moving to next test, test case could fail if wait is too short.
	protected static final long FILTER_WAIT_MILLISECONDS = 150L;
	
	private PasswordItemListActivity activity;
	private FpmApplication application;
	private ListView listView;
	private BaseAdapter listData;
	private Spinner categoryPicker;
	private Instrumentation instrumentation;
	private ClipboardManager clipboardManager;
	
	public PasswordItemListActivityTest() {
		super(PasswordItemListActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		instrumentation = getInstrumentation();
		application = (FpmApplication) Instrumentation.newApplication(MockFpmApplication.class, instrumentation.getContext());		
		setApplication(application);
		
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					instrumentation.callApplicationOnCreate(application);
					activity = startActivity(new Intent(), null, null);
					listView = activity.getListView();
					listData = (BaseAdapter) listView.getAdapter();
					categoryPicker = (Spinner) activity.findViewById(R.id.category_picker);
					clipboardManager = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "Failed while running in UI thread.");
			fail(e.toString());
		}
		instrumentation.waitForIdleSync();
	}

	@Override
	protected void tearDown() throws Exception {
		activity.finish();
		super.tearDown();
	}

	public void testPreconditions() {
		assertTrue(application.isCryptOpen());
		assertNotNull(listView);
		assertNotNull(listData);
		assertNotNull(categoryPicker);
	}
	
	@UiThreadTest
	public void testCopyToClipboard() throws Throwable{	
		PasswordItemListActivity.copyItemProperty(activity, 0, FpmCrypt.PROPERTY_USER);
		assertEquals("sample@gmail.com", clipboardManager.getText());
		PasswordItemListActivity.copyItemProperty(activity, 2, FpmCrypt.PROPERTY_PASSWORD);
		assertEquals("chewy", clipboardManager.getText());
	}
	
	@UiThreadTest
	public void testListViewItemClicked() throws Throwable {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		Intent i = null;
		
		// default is view item
		prefs.edit().putBoolean(FpmApplication.PREF_LAUNCH_DEFAULT, false).commit();
		activity.onListItemClick(null, null, 1, 1);
		i = getStartedActivityIntent();
		assertEquals(ViewPasswordItemActivity.class.getName(), i.getComponent().getClassName());
		assertEquals(1, i.getLongExtra(PasswordItemListActivity.EXTRA_ID, -1));
		
		activity.onListItemClick(null, null, 1, 23);
		i = getStartedActivityIntent();
		assertEquals(ViewPasswordItemActivity.class.getName(), i.getComponent().getClassName());
		assertEquals(23, i.getLongExtra(PasswordItemListActivity.EXTRA_ID, -1));
		
		// default is web browser
		prefs.edit().putBoolean(FpmApplication.PREF_LAUNCH_DEFAULT, true).commit();
		prefs.edit().putBoolean(FpmApplication.PREF_COPY_PASSWORD, false).commit();
		clipboardManager.setText(null);
		activity.onListItemClick(null, null, 1, 1);
		i = getStartedActivityIntent();
		assertEquals(Intent.ACTION_VIEW, i.getAction());
		assertNotNull(i.getScheme());
		assertFalse(clipboardManager.hasText());
		
		// default is web browser
		prefs.edit().putBoolean(FpmApplication.PREF_LAUNCH_DEFAULT, true).commit();
		prefs.edit().putBoolean(FpmApplication.PREF_COPY_PASSWORD, true).commit();
		clipboardManager.setText(null);
		activity.onListItemClick(null, null, 1, 1);
		assertEquals(application.decrypt(application.getPasswordItemById(1).getPassword()), clipboardManager.getText());
	}
	
}
