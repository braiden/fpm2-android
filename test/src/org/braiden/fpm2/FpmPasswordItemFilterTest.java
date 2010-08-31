package org.braiden.fpm2;

import org.braiden.fpm2.PasswordItemListActivity.FpmCryptListAdapter;
import org.braiden.fpm2.PasswordItemListActivity.FpmPasswordItemFilter;
import org.braiden.fpm2.mock.MockFpmApplication;

import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;
import android.util.Log;

public class FpmPasswordItemFilterTest extends InstrumentationTestCase {

	protected static final String TAG = "FpmPasswordItemFilterTest";
	
	private Instrumentation instrumentation = null;
	private FpmApplication app = null;
	private FpmPasswordItemFilter filter = null;
	private FpmCryptListAdapter adapter;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		instrumentation = getInstrumentation();
		app = (FpmApplication) Instrumentation.newApplication(MockFpmApplication.class, instrumentation.getContext());
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					adapter = new FpmCryptListAdapter(app, null);
					filter = new FpmPasswordItemFilter(app, adapter) {
						@Override
						protected void publishResults(CharSequence constraint, FilterResults results) {
							
						}
					};
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "setUp() failed.", e);
		}
	}
	
	@UiThreadTest
	public void testFilter() throws Throwable {
		
		filter.performFiltering("noResultsForThisString");
		Thread.sleep(100L);
		assertEquals(0, adapter.getCount());
	}
	
}
