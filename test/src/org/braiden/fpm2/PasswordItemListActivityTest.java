package org.braiden.fpm2;

import org.braiden.fpm2.model.PasswordItem;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class PasswordItemListActivityTest extends ActivityInstrumentationTestCase2<PasswordItemListActivity> {
	
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
		super("org.braiden.fpm2", PasswordItemListActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		instrumentation = getInstrumentation();
		activity = getActivity();
		
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				application = (FpmApplication) activity.getApplication();
				listView = activity.getListView();
				listData = (BaseAdapter) listView.getAdapter();
				categoryPicker = (Spinner) activity.findViewById(R.id.category_picker);
				clipboardManager = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
				
				// disable locking of FPM database, and unlock
				application.setFpmFileLocator(new FpmApplicationTest.TestAssetsFpmFileLocator("plain.xml", getInstrumentation().getContext()));
				SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
				defaultPrefs.edit().putString(FpmApplication.PREF_AUTOLOCK, "-1").commit();
				application.openCrypt("password");
			}
		});
		instrumentation.waitForIdleSync();
		while (application.getCryptState() == FpmApplication.STATE_BUSY) {
			Thread.sleep(100L);
		}
		instrumentation.waitForIdleSync();
	}

	@Override
	protected void tearDown() throws Exception {
		activity.finish();
		super.tearDown();
	}

	protected void setBooleanPref(final String pref, final boolean value) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
				prefs.edit().putBoolean(pref, value).commit();
			}
		});
	}
	
	protected void fireOnClick(final int idx) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.onListItemClick(null, null, idx, idx);
			}
		});
	}
	
	private void copyToClipboard(final long itemId, final String property) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				PasswordItemListActivity.copyItemProperty(activity, itemId, property);
			}
		});
		instrumentation.waitForIdleSync();
	}
	
	public void testPreconditions() {
		assertTrue(application.isCryptOpen());
		assertNotNull(listView);
		assertNotNull(listData);
		assertNotNull(categoryPicker);
	}
	
	public void testCopyToClipboard() {	
		copyToClipboard(0, FpmCrypt.PROPERTY_USER);
		assertEquals("sample@gmail.com", clipboardManager.getText());
		copyToClipboard(2, FpmCrypt.PROPERTY_PASSWORD);
		assertEquals("chewy", clipboardManager.getText());
	}
	
	public void testListViewItemClicked() throws Exception {
		// add a monitor for blocking / watching request to launch browser
		IntentFilter webFilter = new IntentFilter(Intent.ACTION_VIEW);
		webFilter.addDataScheme("http");
		webFilter.addDataScheme("https");
		ActivityMonitor webMonitor = new ActivityMonitor(webFilter, null, true);
		instrumentation.addMonitor(webMonitor);
		// add monitor for view item
		ActivityMonitor viewItemMonitor = new ActivityMonitor("org.braiden.fpm2.ViewPasswordItemActivity", null, true);
		instrumentation.addMonitor(viewItemMonitor);
		
		// default is view item
		setBooleanPref(FpmApplication.PREF_LAUNCH_DEFAULT, false);
		fireOnClick(1);
		viewItemMonitor.waitForActivityWithTimeout(1000);
		assertEquals(1, viewItemMonitor.getHits());
		
		// default is web browser
		setBooleanPref(FpmApplication.PREF_LAUNCH_DEFAULT, true);
		setBooleanPref(FpmApplication.PREF_COPY_PASSWORD, false);
		clipboardManager.setText(null);
		fireOnClick(1);
		webMonitor.waitForActivityWithTimeout(1000);
		assertEquals(1, webMonitor.getHits());
		assertFalse(clipboardManager.hasText());
		
		// default is web browser
		setBooleanPref(FpmApplication.PREF_LAUNCH_DEFAULT, true);
		setBooleanPref(FpmApplication.PREF_COPY_PASSWORD, true);
		clipboardManager.setText(null);
		fireOnClick(1);
		webMonitor.waitForActivityWithTimeout(1000);
		assertEquals(2, webMonitor.getHits());
		assertEquals(application.decrypt(application.getPasswordItemById(1).getPassword()), clipboardManager.getText());
	}
	
	public void testCategoryFilter() throws Exception {
		listData.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				String category = (String) categoryPicker.getSelectedItem();
				CharSequence textFilter = listView.getTextFilter();
				Log.d(TAG, "DataSetObserver#onChanged() category = \"" + category + "\" textFilter=\"" + textFilter + "\".");
				for (int idx = 0; idx < listData.getCount(); idx++) {
					PasswordItem item = (PasswordItem) listData.getItem(idx);
					if (activity.getString(R.string.default_category).equals(category)) {
						assertTrue(item.isDefault());
					} else if (activity.getString(R.string.all_category).equals(category)) {
						assertTrue(!TextUtils.isEmpty(textFilter) || listData.getCount() == 3);
					} else {
						assertTrue(listData.getCount() > 0);
						assertEquals(category, item.getCategory());
					}
					if (textFilter != null) {
						assertTrue(item.getTitle().toUpperCase().contains(textFilter.toString().toUpperCase()));
					}
				}
			}
		});
		
		for (String textFilter : new String[] {null, "a", "amaZON", "SDFSDFSDF", null}) {
			
			final String textFilterFinal = textFilter;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (textFilterFinal == null) {
						listView.clearTextFilter();
					} else {
						listView.setFilterText(textFilterFinal);
					}
				}
			});
			Thread.sleep(FILTER_WAIT_MILLISECONDS);
			
			for (int idx = 0; idx < categoryPicker.getAdapter().getCount(); idx++) {
				final int finalIdx = idx;
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						categoryPicker.setSelection(finalIdx);
					}
				});
				Thread.sleep(FILTER_WAIT_MILLISECONDS);
			}
		}
	}
	
}
