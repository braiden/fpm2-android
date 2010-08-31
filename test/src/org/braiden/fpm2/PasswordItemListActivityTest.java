package org.braiden.fpm2;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.braiden.fpm2.mock.MockFpmApplication;
import org.braiden.fpm2.model.PasswordItem;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.text.ClipboardManager;
import android.text.TextUtils;
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

	protected void setBooleanPref(final String pref, final boolean value) throws Throwable {		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		prefs.edit().putBoolean(pref, value).commit();
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
		Intent i = null;
		
		// default is view item
		setBooleanPref(FpmApplication.PREF_LAUNCH_DEFAULT, false);
		activity.onListItemClick(null, null, 1, 1);
		i = getStartedActivityIntent();
		assertEquals(ViewPasswordItemActivity.class.getName(), i.getComponent().getClassName());
		assertEquals(1, i.getLongExtra(PasswordItemListActivity.EXTRA_ID, -1));
		
		// default is web browser
		setBooleanPref(FpmApplication.PREF_LAUNCH_DEFAULT, true);
		setBooleanPref(FpmApplication.PREF_COPY_PASSWORD, false);
		clipboardManager.setText(null);
		activity.onListItemClick(null, null, 1, 1);
		i = getStartedActivityIntent();
		assertEquals(Intent.ACTION_VIEW, i.getAction());
		assertNotNull(i.getScheme());
		assertFalse(clipboardManager.hasText());
		
		// default is web browser
		setBooleanPref(FpmApplication.PREF_LAUNCH_DEFAULT, true);
		setBooleanPref(FpmApplication.PREF_COPY_PASSWORD, true);
		clipboardManager.setText(null);
		activity.onListItemClick(null, null, 1, 1);
		assertEquals(application.decrypt(application.getPasswordItemById(1).getPassword()), clipboardManager.getText());
	}
	
	public void testListViewFilter() throws Throwable {
		final CyclicBarrier barrier = new CyclicBarrier(2);
		
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				listData.registerDataSetObserver(new DataSetObserver() {
					@Override
					public void onChanged() {
						try {
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
						} finally {
							try {
								Log.d(TAG, Thread.currentThread() + " is waiting for barier.");
								barrier.await();
							} catch (Exception e) {
								Log.e(TAG, "Barrier synchronization failed.", e);
							}
						}
					}
				});
			}
		});
		instrumentation.waitForIdleSync();
		
		
		for (String textFilter : new String[] {"a", null, "amaZON", "SDFSDFSDF", null}) {
			
			final String textFilterFinal = textFilter;
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (textFilterFinal == null) {
						listView.clearTextFilter();
					} else {
						listView.setFilterText(textFilterFinal);
					}
				}
			});
			Log.d(TAG, Thread.currentThread() + " is waiting for barier.");
			barrier.await(2, TimeUnit.SECONDS);
			barrier.reset();
		}
	}
	
}
