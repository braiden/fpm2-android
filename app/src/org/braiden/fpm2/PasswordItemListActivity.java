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
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
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
public class PasswordItemListActivity extends ListActivity {
	
	protected final static String TAG = "PasswordListActivity";
	
	private BroadcastReceiver fpmCryptReceiver;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	// register the adapter for building view for each element of our list
    	BaseAdapter adapter = new FpmCryptListAdapter(this);
    	setListAdapter(adapter);
    	
    	// the crypt reciever listens for broadcasts regarding state
    	// of FPM db, and notifies the list view to update accordingly
    	fpmCryptReceiver = new FpmCryptBroadcastReceiver(this, adapter);    	
    	IntentFilter filter = new IntentFilter();
    	filter.addAction(FpmApplication.ACTION_FPM_CLOSE);
    	filter.addAction(FpmApplication.ACTION_FPM_OPEN);
    	registerReceiver(fpmCryptReceiver, filter);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// must deregister on exit
		unregisterReceiver(fpmCryptReceiver);
	}

	@Override
    protected void onResume() {
		super.onResume();
		// when ever the app regains view on device, make sure db is unlocked.
		// if it is locked, user will be prompted for password.
    	FpmApplication app = (FpmApplication) this.getApplication();
    	app.openCrypt(this);
    }
    
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		// open the View details activity when an item is selected.
    	Intent intent = new Intent(this, ViewPasswordItemActivity.class);
    	intent.putExtra("id", id);
    	intent.putExtra("title", ((PasswordItem) listView.getItemAtPosition(position)).getTitle());
    	startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if a child activity retuned -1, users canceled password prompt
		// exit this activity too, instead of prompting for password again.
		if (resultCode < 0) {
			finish();
		}
	}

	/**
	 * Listens for FPM open / close events, and notifies list view of datamodel changes.
	 * Also, if an FPM close event occurs while user if viewing this activity,
	 * generates a prompt for user to unlock the db again. 
	 * 
	 * @author braiden
	 *
	 */
	public static class FpmCryptBroadcastReceiver extends BroadcastReceiver {

		private Activity activity;
		private BaseAdapter listAdapter;
		
		public FpmCryptBroadcastReceiver(Activity activity, BaseAdapter listAdapter) {
			this.listAdapter = listAdapter;
			this.activity = activity;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// update view
			listAdapter.notifyDataSetChanged();
			
			// fpm as been locked, if user is still in this screen
			// popup dialog to unlock it again.
			if (activity.hasWindowFocus()) {
				((FpmApplication) activity.getApplication()).openCrypt(activity);
			}
		}
		
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