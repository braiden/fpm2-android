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

import org.braiden.fpm2.model.PasswordItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);    	
    	// register the adapter for building view for each element of our list
    	BaseAdapter adapter = new FpmCryptListAdapter(this);
    	setListAdapter(adapter);
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
	public static class FpmCryptListAdapter extends BaseAdapter {
    	
    	LayoutInflater layoutInflater;
    	FpmApplication app;
    	
    	public FpmCryptListAdapter(Activity activity) {
    		layoutInflater = LayoutInflater.from(activity);
    		app = (FpmApplication) activity.getApplication();
    	}
    	
		@Override
		public int getCount() {
			return app.getPasswordItems().size();
		}

		@Override
		public Object getItem(int position) {
			return app.getPasswordItemById(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
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
			
			// item can be null if db is locked at same time
			// as users is scrolling.
			if (item != null) {
				viewHolder.title.setText(item.getTitle());
				viewHolder.url.setText(item.getUrl());
			}

			return convertView;
		}

		private static class ViewHolder {
			public TextView title;
			public TextView url;
		}
		
    }
    
}