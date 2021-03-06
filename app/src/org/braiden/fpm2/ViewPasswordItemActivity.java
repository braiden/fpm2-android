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
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

	private static final String KEY_IS_PASSWORD_DISPLAYED = "isPasswordDisplayed";

	protected final static String TAG = "ViewPasswordItemActivity";
	
	private long id;
	private BroadcastReceiver receiver;
	private boolean isPasswordDisplayed = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			isPasswordDisplayed = savedInstanceState.getBoolean(KEY_IS_PASSWORD_DISPLAYED);
		}
		
		// get the element id being displayed
		FpmApplication app = (FpmApplication) this.getApplication();
		id = getIntent().getLongExtra(PasswordItemListActivity.EXTRA_ID, -1L);
		
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				ViewPasswordItemActivity.this.finish();
			}
		};
		registerReceiver(receiver, new IntentFilter(FpmApplication.ACTION_FPM_LOCK));
		
		PasswordItem item = app.getPasswordItemById(id);
		
		if (item != null) {
			// update the title
			setTitle(getResources().getString(R.string.app_name) + " - " + item.getTitle());
			// register the list adapter
			BaseAdapter adapter = new PasswordItemPropertyListAdapter(this, item);
			setListAdapter(adapter);
			// register a reciver to close this view if the FPM database is locked
		} else {
			// if the item is null, the fpm db was closed terminate this action
			// the parent will handle reprompt of passphrase.
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.view_item_menu, menu);
		return true;
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IS_PASSWORD_DISPLAYED, isPasswordDisplayed);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.view_item_menu_launch:
				PasswordItemListActivity.launchItem(this, id);
				return true;
			case R.id.view_item_menu_copy_user:
				PasswordItemListActivity.copyItemProperty(this, id, FpmCrypt.PROPERTY_USER);
				return true;
			case R.id.view_item_menu_copy_password:
				PasswordItemListActivity.copyItemProperty(this, id, FpmCrypt.PROPERTY_PASSWORD);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// clicking on the password item, causes the "****" to be replaced with real password.
		if (PasswordItemPropertyListAdapter.TITLES[position] == R.string.password_item_password) {
			FpmApplication app = ((FpmApplication) getApplication());
			PasswordItem item = app.getPasswordItemById(this.id);
			if (item != null) {
				String password = app.decrypt(item.getPassword());
				if (password != null) {
					TextView text = (TextView) v.findViewById(R.id.passwordItemPropertyRowValue);
					text.setText(password);
					isPasswordDisplayed = true;
				}
			}
		} else if (PasswordItemPropertyListAdapter.TITLES[position] == R.string.password_item_url) {
			PasswordItemListActivity.launchItem(this, this.id);
		}
	}

	public static class PasswordItemPropertyListAdapter extends BaseAdapter {
				
		public static final int[] TITLES = {
			R.string.password_item_title,
			R.string.password_item_url,
			R.string.password_item_username,
			R.string.password_item_password,
			R.string.password_item_category,
			R.string.password_item_default,
			R.string.password_item_notes,
			R.string.password_item_launcher,
		};
		
		public static final String[] PROPERTIES = {
			"title",
			"url",
			"user",
			"password",
			"category",
			"default",
			"notes",
			"launcher",
		};
		
		private ViewPasswordItemActivity activity;
		private LayoutInflater layoutInflater;
		private PasswordItem passwordItem;
		
		public PasswordItemPropertyListAdapter(ViewPasswordItemActivity activity, PasswordItem passwordItem) {
			this.activity = activity;
			this.layoutInflater = LayoutInflater.from(activity);
			this.passwordItem = passwordItem;
		}

		@Override
		public boolean isEnabled(int position) {
			return TITLES[position] == R.string.password_item_password
					|| TITLES[position] == R.string.password_item_url;
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
				if (TITLES[position] != R.string.password_item_password) {
					// display the valid
					viewHolder.value.setText("" + PropertyUtils.getProperty(passwordItem, PROPERTIES[position]));
					// notes are multiple lines
					if (TITLES[position] == R.string.password_item_notes) {
						viewHolder.value.setSingleLine(false);
					}
				} else if (activity.isPasswordDisplayed) {
					// the password is being displayed. decrypt and show
					FpmApplication app = ((FpmApplication) activity.getApplication());
					String cypherText = (String) PropertyUtils.getProperty(passwordItem, PROPERTIES[position]);
					viewHolder.value.setText(app.decrypt(cypherText));
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
