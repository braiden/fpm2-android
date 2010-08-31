package org.braiden.fpm2;

import java.util.List;

import org.braiden.fpm2.PasswordItemListActivity.PasswordItemFilter;
import org.braiden.fpm2.mock.MockFpmApplication;
import org.braiden.fpm2.model.PasswordItem;

import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;
import android.util.Log;

public class PasswordItemFilterTest extends InstrumentationTestCase {

	protected static final String TAG = "FpmPasswordItemFilterTest";
	
	private Instrumentation instrumentation = null;
	private FpmApplication app = null;
	private PasswordItemFilter filter = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		instrumentation = getInstrumentation();
		app = (FpmApplication) Instrumentation.newApplication(MockFpmApplication.class, instrumentation.getContext());
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run() {
					instrumentation.callApplicationOnCreate(app);
					filter = new PasswordItemFilter(app, null);
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "setUp() failed.", e);
		}
	}
	
	@UiThreadTest
	public void testFilter() throws Throwable {
		List<PasswordItem> result = filter.filterPasswordItems(null, "noResultsForThisString");
		assertEquals(0, result.size());
		
		result = filter.filterPasswordItems(PasswordItemListActivity.CATEGORY_DEFAULT, null);
		assertEquals(2, result.size());
		
		result = filter.filterPasswordItems("Banking", "Money");
		assertEquals(1, result.size());
		
		result = filter.filterPasswordItems(null, null);
		assertEquals(3, result.size());
		
		result = filter.filterPasswordItems(PasswordItemListActivity.CATEGORY_DEFAULT, "a");
		assertEquals(2, result.size());
		
		app.closeCrypt();
		
		result = filter.filterPasswordItems(null, null);
		assertEquals(0, result.size());
	}
	
}
