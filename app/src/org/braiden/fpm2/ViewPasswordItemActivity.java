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
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ViewPasswordItemActivity extends ListActivity {

	protected final static String TAG = "ViewPasswordItemActivity";
	
	private PasswordItem item;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		FpmApplication app = (FpmApplication) this.getApplication();
		item = (PasswordItem) app.getPasswordItemById((int)getIntent().getLongExtra("id", -1L));
		String title = getResources().getString(R.string.app_name) + " - " + item.getTitle();
		this.setTitle(title);
		this.setListAdapter(new PasswordItemPropertyListAdapter(this, item));
	}

	public static class PasswordItemPropertyListAdapter extends BaseAdapter {
		
		private static final int POSITION_OF_PASSWORD = 3;
		
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
		private PasswordItem passwordItem;
		
		public PasswordItemPropertyListAdapter(Context context, PasswordItem passwordItem) {
			layoutInflater = LayoutInflater.from(context);
			this.passwordItem = passwordItem;
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
				if (position != POSITION_OF_PASSWORD) {
					viewHolder.value.setText("" + PropertyUtils.getProperty(passwordItem, PROPERTIES[position]));
				} else {
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
