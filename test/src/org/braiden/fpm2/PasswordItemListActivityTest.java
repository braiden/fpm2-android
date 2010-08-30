package org.braiden.fpm2;

import org.braiden.fpm2.model.PasswordItem;

import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.text.TextUtils;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class PasswordItemListActivityTest extends ActivityInstrumentationTestCase2<PasswordItemListActivity> {
	
	protected static final String TAG = "PasswordItemListActivityTest";
	// how long to wait for async filter thread to finish before
	// moving to next test, test case could fail if wait is too short.
	protected static final long FILTER_WAIT_MILLISECONDS = 100L;
	
	private PasswordItemListActivity activity;
	private FpmApplication application;
	private ListView listView;
	private BaseAdapter listData;
	private Spinner categoryPicker;
	private Instrumentation instrumentation;
	
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

	public void testPreconditions() {
		assertTrue(application.isCryptOpen());
		assertNotNull(listView);
		assertNotNull(listData);
		assertNotNull(categoryPicker);
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
