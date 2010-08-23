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
import org.braiden.fpm2.util.PropertyUtils;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Display details for a single password entry.
 * 
 * @author braiden
 *
 */

public class ViewPasswordItemActivity extends ListActivity {

	protected final static String TAG = "ViewPasswordItemActivity";
	
	private long id;
	private BroadcastReceiver fpmCryptReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// get the element id being displayed
		FpmApplication app = (FpmApplication) this.getApplication();
		this.id = getIntent().getLongExtra("id", -1L);
		
		// update the title
		String title = getIntent().getStringExtra("title");
		BaseAdapter adapter = new PasswordItemPropertyListAdapter(this, app, id);
		this.setTitle(getResources().getString(R.string.app_name) + " - " + title);
		this.setListAdapter(adapter);
		
		// create a reciever to listen for data lock/unlock
		IntentFilter filter = new IntentFilter();
		filter.addAction(FpmApplication.ACTION_FPM_LOCKED);
		filter.addAction(FpmApplication.ACTION_FPM_UNLOCKED);
		fpmCryptReceiver = new PasswordItemListActivity.FpmCryptBroadcastReceiver(this, adapter);
		registerReceiver(fpmCryptReceiver, filter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// whenever user switches back to our app generate password
		// prompt if data is locked.
		FpmApplication app = (FpmApplication) this.getApplication();
		app.openCrypt(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(fpmCryptReceiver);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// clicking on the password item, causes the "****" to be replaced with real password.
		if (position == PasswordItemPropertyListAdapter.POSITION_OF_PASSWORD) {
			FpmApplication app = ((FpmApplication) getApplication());
			PasswordItem item = app.getPasswordItemById(this.id);
			if (item != null) {
				String password = app.decrypt(item.getPassword());
				if (password != null) {
					TextView text = (TextView) v.findViewById(R.id.passwordItemPropertyRowValue);
					text.setText(password);
				}
			}
		}
	}
	
	public static class PasswordItemPropertyListAdapter extends BaseAdapter {
		
		public static final int POSITION_OF_PASSWORD = 3;
		
		private static final  int[] TITLES = {
			R.string.password_item_title,
			R.string.password_item_url,
			R.string.password_item_username,
			R.string.password_item_password,
			R.string.password_item_category,
			R.string.password_item_default,
			R.string.password_item_notes,
			R.string.password_item_launcher,
		};
		
		private static final String[] PROPERTIES = {
			"title",
			"url",
			"user",
			"password",
			"category",
			"default",
			"notes",
			"launcher",
		};
		
		private LayoutInflater layoutInflater;
		private long passwordItemId;
		private FpmApplication app;
		
		public PasswordItemPropertyListAdapter(Context context, FpmApplication app, long passwordItemId) {
			this.layoutInflater = LayoutInflater.from(context);
			this.passwordItemId = passwordItemId;
			this.app = app;
		}
		
		@Override
		public int getCount() {
			return PROPERTIES.length;
		}
		
		@Override
		public Object getItem(int position) {
			return PROPERTIES[position];
		}
		
		@Override
		public long getItemId(int position) {
			return TITLES[position];
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PasswordItem passwordItem = app.getPasswordItemById(this.passwordItemId);
			
			ViewHolder viewHolder = null;
			
			if (convertView == null) {
				viewHolder = new ViewHolder(); 
				convertView = layoutInflater.inflate(R.layout.password_item_property_row, null);
				viewHolder.key = (TextView) convertView.findViewById(R.id.passwordItemPropertyRowKey);
				viewHolder.value = (TextView) convertView.findViewById(R.id.passwordItemPropertyRowValue);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			viewHolder.key.setText(TITLES[position]);
			try {
				if (passwordItem == null) {
					// the db is locked, display a place holder.
					viewHolder.value.setText("????????");
				} if (position != POSITION_OF_PASSWORD) {
					// display the valie
					viewHolder.value.setText("" + PropertyUtils.getProperty(passwordItem, PROPERTIES[position]));
				} else {
					// .getPassword returns the FPM encrypted string. display **** instead.
					viewHolder.value.setText("********");
				}
			} catch (Exception e) {
				Log.w(TAG, "Failed to access property \"" + PROPERTIES[position] + "\".", e);
			}
			
			return convertView;
		}
		
		private static class ViewHolder {
			public TextView key;
			public TextView value;
		}
		
	}
	
}
