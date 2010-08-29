package org.braiden.fpm2;

/*
 * Copyright (c) 2010 Braiden Kindt
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.braiden.fpm2.model.PasswordItem;
import org.braiden.fpm2.util.PropertyUtils;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Main entry point for application, lists all items in db.
 * 
 * @author braiden
 *
 */
public class PasswordItemListActivity extends ListActivity implements FpmBroadcastReceiver.Listener {
	
	public final static String EXTRA_ID = "id";
	
	protected final static String TAG = "PasswordListActivity";
	protected final static String CATEGORY_DEFAULT = "org.braiden.fpm2.__DEFAULT__";
	
	private Spinner categoryPicker;
	private boolean isCategoryPickerInitialized;
	private FpmBroadcastReceiver receiver;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);    	
    	setContentView(R.layout.password_item_list);
    	
    	// register the adapter for building view for each element of our list
    	BaseAdapter adapter = new FpmCryptListAdapter(this, getListView());
    	setListAdapter(adapter);
    	getListView().setTextFilterEnabled(true);
    	
    	// get all the categories
    	List<String> allCategories = new ArrayList<String>(20);
    	allCategories.add(getString(R.string.all_category));
    	allCategories.add(getString(R.string.default_category));
    	if (getFpmApplication().isCryptOpen()) {
    		isCategoryPickerInitialized = true;
    		allCategories.addAll(getFpmApplication().getCategories());
    	}
    	
    	// populate the category picker with default values, and any other categories (if availible)
    	categoryPicker = (Spinner) findViewById(R.id.category_picker);
    	final ArrayAdapter<String> categoryData = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, allCategories );
    	categoryData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	categoryPicker.setAdapter(categoryData);
    	// on change of picker, we force ListView's filter to be re-applied
    	categoryPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItem, int position, long id) {
				CharSequence textFilter = getListView().getTextFilter();
				FpmPasswordItemFilter filter = (FpmPasswordItemFilter) ((Filterable) getListAdapter()).getFilter();
				String category = null;
				if (position == 1) {
					category = CATEGORY_DEFAULT;
				} else if (position != 0) {
					category = (String) categoryPicker.getItemAtPosition(position);
				}
				filter.setCategory(category);
				if (getFpmApplication().isCryptOpen()) {
					filter.filter(textFilter);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				CharSequence textFilter = getListView().getTextFilter();
				FpmPasswordItemFilter filter = (FpmPasswordItemFilter) ((Filterable) getListAdapter()).getFilter();
				filter.setCategory(null);
				if (getFpmApplication().isCryptOpen()) {
					filter.filter(textFilter);
				}
			}
		});
    	    	
    	receiver = new FpmBroadcastReceiver(this);
    	
    	getListView().setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
				PasswordItem item = getFpmApplication().getPasswordItemById(info.id);
				getMenuInflater().inflate(R.menu.context_menu, menu);
				if (item != null) {
					menu.setHeaderTitle(item.getTitle());
				}
			}
		});
    	
    	
		UnlockCryptActivity.unlockIfRequired(this);
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		receiver.unregister();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			// whenever this view obtains focus, it tries
			// to unlock the fpm store, if the store
			// is not availible, a new acitivity is started
			// to unlock it.
			UnlockCryptActivity.unlockIfRequired(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		android.util.Log.d(TAG, "onActivityResult(requestCode=" + requestCode 
					+ ", resultCode=" + resultCode
					+ ", itent.action=" + (data == null ? null : data.getAction()) + ")");
		
		// if the password unlock activity was canceled,
		// this activity can't continue, finish it.
		if (requestCode == UnlockCryptActivity.UNLOCK_CRYPT_REQUEST_CODE
				&& resultCode == RESULT_CANCELED) {
			finish();
		}
	}

	@Override
	public boolean onSearchRequested() {
		// don't pop-up the search widget if the crypt is locked
		if (getFpmApplication().isCryptOpen()) {
			return super.onSearchRequested();
		} else {
			return false;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// if this acitity receives input from search widget, apply the search string a listView text filter
		if (intent.getAction().equals(Intent.ACTION_SEARCH) && getFpmApplication().isCryptOpen()) {
			String searchString = intent.getStringExtra(SearchManager.QUERY);
			getListView().setFilterText(searchString);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onFpmUnlock() {
		android.util.Log.d(TAG, "onFpmUnlock()");
		// if the categories have never been initialized,
		// initialize them now.
		if (!isCategoryPickerInitialized) {
			isCategoryPickerInitialized = true;
			for (String cat : getFpmApplication().getCategories()) {
				((ArrayAdapter<String>) categoryPicker.getAdapter()).add(cat);
			}
		}
		((BaseAdapter) getListAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onFpmError(int err) {
		android.util.Log.d(TAG, "onFpmError(" + err + ")");
	}

	@Override
	public void onFpmLock() {
		android.util.Log.d(TAG, "onFpmLock()");
		((BaseAdapter) getListAdapter()).notifyDataSetChanged();
		UnlockCryptActivity.unlockIfRequired(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.main_menu_prefs:
				startActivity(new Intent(this, FpmPreferencesActivity.class));
				return true;
			case R.id.main_menu_lock:
				getFpmApplication().closeCrypt();
				return true;
			case R.id.main_menu_about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		viewItem(id);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.context_menu_view:
				viewItem(info.id);
				return true;
			case R.id.context_menu_launch:
				launchItem(info.id);
				return true;
			case R.id.context_menu_copy_password:
				copyItemProperty(info.id, FpmCrypt.PROPERTY_PASSWORD);
				return true;
			case R.id.context_menu_copy_user:
				copyItemProperty(info.id, FpmCrypt.PROPERTY_USER);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	protected void copyItemProperty(long id, String property) {
		PasswordItem item = getFpmApplication().getPasswordItemById(id);
		if (item != null) {
			try {
				String value = (String) PropertyUtils.getProperty(item, property);
				if (FpmCrypt.PROPERTY_PASSWORD.equals(property)) {
					value = getFpmApplication().decrypt(value);
				}
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(value);
			} catch (Exception e) {
				Log.w(TAG, "Failed to access property \"" + property + "\" of item id " + id + ".", e);
			}
		}
	}

	protected void launchItem(long id) {		
		PasswordItem item = getFpmApplication().getPasswordItemById(id);
		if (item != null) {
			try {
				boolean copyPassword = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
						FpmApplication.PREF_COPY_PASSWORD, false);
				if (copyPassword) {
					copyItemProperty(id, FpmCrypt.PROPERTY_PASSWORD);
				}
				Uri uri = Uri.parse(item.getUrl());
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			} catch (Exception e) {
				Log.i(TAG, "Cannot launch item (id=" + id + ").", e);
			}
		}
	}

	protected void viewItem(long id) {
    	Intent intent = new Intent(this, ViewPasswordItemActivity.class);
    	intent.putExtra(EXTRA_ID, id);
    	startActivityForResult(intent, 0);
	}

	protected FpmApplication getFpmApplication() {
		return (FpmApplication) getApplication();
	}
	
	/**
	 * Map our Fpm PasswordItem's to List elements.
	 * 
	 * @author braiden
	 *
	 */
	public static class FpmCryptListAdapter extends BaseAdapter implements Filterable {
    	
    	private LayoutInflater layoutInflater;
    	private FpmApplication app;
    	private List<PasswordItem> data;
    	private Filter filter = null;
    	private ListView listView;
    	
    	public FpmCryptListAdapter(Activity activity, ListView listView) {
    		layoutInflater = LayoutInflater.from(activity);
    		this.listView = listView;
    		app = (FpmApplication) activity.getApplication();
    		if (app.isCryptOpen()) {
    			data = app.getPasswordItems();
    		}
    	}
    	
		@Override
		public void notifyDataSetChanged() {
			// if no data has ever been associated with the
			// view, get all data from the crypt.
			// (if data already associated, don't change it.)
			if (data == null && app.isCryptOpen()) {
				getFilter().filter(listView.getTextFilter());
			} else {
				super.notifyDataSetChanged();
			}
		}

		@Override
		public Filter getFilter() {
			return filter != null ? filter : (filter = new FpmPasswordItemFilter(app, this));
		}

		@Override
		public int getCount() {
			return app.isCryptOpen() && data != null ? data.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return ((PasswordItem) getItem(position)).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			PasswordItem item = (PasswordItem) this.getItem(position);
			
			if (convertView == null) {
				viewHolder = new ViewHolder(); 
				convertView = layoutInflater.inflate(R.layout.password_item_row, null);
				viewHolder.title = (TextView) convertView.findViewById(R.id.passwordItemRowTitle);
				viewHolder.url = (TextView) convertView.findViewById(R.id.passwordItemRowUrl);
				viewHolder.user = (TextView) convertView.findViewById(R.id.passwordItemRowUser);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			viewHolder.title.setText(item.getTitle());
			viewHolder.url.setText(item.getUrl());
			viewHolder.user.setText(item.getUser());

			return convertView;
		}

		public void setData(List<PasswordItem> data) {			
			if (app.isCryptOpen()) {
				this.data = data;
			} else {
				this.data = Collections.emptyList();
			}
			
			notifyDataSetChanged();
		}

		private static class ViewHolder {
			public TextView title;
			public TextView url;
			public TextView user;
		}	
		
	}
	
	public static class FpmPasswordItemFilter extends Filter {
				
		private FpmApplication fpmApp;
		private FpmCryptListAdapter fpmListAdapter;
		volatile private String category = null;
		
		public FpmPasswordItemFilter(FpmApplication fpmApp, FpmCryptListAdapter fpmListAdapter) {
			this.fpmApp = fpmApp;
			this.fpmListAdapter = fpmListAdapter;
		}
		
		@Override
		protected FilterResults performFiltering(CharSequence constraintSeq) {
			String constraint = constraintSeq == null ? StringUtils.EMPTY : constraintSeq.toString().trim().toUpperCase();
			List<PasswordItem> allItems = getPasswordItems();
			FilterResults result = new FilterResults();
			
			android.util.Log.d(TAG, "performFiltering(string=" + constraintSeq + ", category=" + category + ")");
			
			if ((constraint == null || StringUtils.isBlank(constraint)) && category == null) {
				result.values = allItems;
			} else {
				List<PasswordItem> filteredItems = new ArrayList<PasswordItem>(allItems.size());
				for (PasswordItem item : allItems) {
					if (item.getTitle().toUpperCase().contains(constraint)
							&& acceptCategory(item)) {
						filteredItems.add(item);
					}
				}
				result.values = filteredItems;
			}
			
			return result;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			fpmListAdapter.setData((List<PasswordItem>) results.values);
		}
		
		/**
		 * Get a list of passwords from the Applications FpmCrypt.
		 * The method may performs unsafe / unsynchronized access
		 * to FpmApplication. publishResults() must re-assert 
		 * the state of FpmCrypt before showing the passwords.
		 * 
		 * @return
		 */
		private List<PasswordItem> getPasswordItems() {
			List<PasswordItem> result = Collections.emptyList();
			try {
				result = fpmApp.getPasswordItems();
			} catch (NullPointerException e) {
				 // If closeCrypt() and getPasswordItems() are running at
				 // the same time null pointer is possible, i try to catch
				 // it here. I don't know why I don't just syhcronize.
				if (fpmApp.isCryptOpen()) {
					throw e;
				}
			}
			return result;
		}

		private boolean acceptCategory(PasswordItem item) {
			return category == null 
					|| (CATEGORY_DEFAULT.equals(category) && item.isDefault())
					|| (category.equals(item.getCategory()));
		}
		
		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}
		
	}
	    
}