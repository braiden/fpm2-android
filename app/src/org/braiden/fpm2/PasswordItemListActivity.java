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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Main entry point for application, lists all items in db.
 * 
 * @author braiden
 *
 */
public class PasswordItemListActivity extends FpmListActivity {
	
	public final static String EXTRA_ID = "id";
	
	protected final static String TAG = "PasswordListActivity";
	
	private Spinner categoryPicker;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);    	
    	setContentView(R.layout.password_item_list);
    	
    	// populate the category picker with some default values
    	categoryPicker = (Spinner) findViewById(R.id.category_picker);
    	final ArrayAdapter<String> categoryData = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, 
    			new String[] { getString(R.string.all_category), getString(R.string.default_category) } );
    	categoryData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	categoryPicker.setAdapter(categoryData);
    	
    	categoryPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItem, int position, long id) {				

			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				
			}
		});
    	
    	// register the adapter for building view for each element of our list
    	BaseAdapter adapter = new FpmCryptListAdapter(this);
    	setListAdapter(adapter);
    	getListView().setTextFilterEnabled(true);
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		// open the View details activity when an item is selected.
    	Intent intent = new Intent(this, ViewPasswordItemActivity.class);
    	intent.putExtra(EXTRA_ID, id);
    	startActivityForResult(intent, 0);
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
    	
    	public FpmCryptListAdapter(Activity activity) {
    		layoutInflater = LayoutInflater.from(activity);
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
				data = app.getPasswordItems();
			}
			super.notifyDataSetChanged();
		}

		@Override
		public Filter getFilter() {
			return filter != null ? filter : new FpmPasswordItemFilter(app, this);
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
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			viewHolder.title.setText(item.getTitle());
			viewHolder.url.setText(item.getUrl());

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
		}	
		
	}
	
	public static class FpmPasswordItemFilter extends Filter {
		
		private FpmApplication fpmApp;
		private FpmCryptListAdapter fpmListAdapter;
		
		public FpmPasswordItemFilter(FpmApplication fpmApp, FpmCryptListAdapter fpmListAdapter) {
			this.fpmApp = fpmApp;
			this.fpmListAdapter = fpmListAdapter;
		}
		
		@Override
		protected FilterResults performFiltering(CharSequence constraintSeq) {
			String constraint = constraintSeq == null ? StringUtils.EMPTY : constraintSeq.toString().trim().toUpperCase();
			List<PasswordItem> allItems = getPasswordItems();
			FilterResults result = new FilterResults();
			
			if (constraint == null || StringUtils.isBlank(constraint)) {
				result.values = allItems;
			} else {
				List<PasswordItem> filteredItems = new ArrayList<PasswordItem>(allItems.size());
				for (PasswordItem item : allItems) {
					if (item.getTitle().toUpperCase().contains(constraint)) {
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
		
	}
	    
}