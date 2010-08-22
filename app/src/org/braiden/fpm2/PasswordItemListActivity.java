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
import org.braiden.fpm2.util.Hex;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RemoteViews.ActionException;

public class PasswordItemListActivity extends ListActivity {
	
	protected final static String TAG = "PasswordListActivity";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	initFpmCrypt();
    	if (!FpmCrypt.getInstance().isOpen()) {
    		new AlertDialog.Builder(this)
    			.setMessage("Failed to open FpmCrypt.")
    			.create()
    			.show();
    	}
    	setListAdapter(new FpmFileListAdapter(this));
        super.onCreate(savedInstanceState);
    }
    
    private void initFpmCrypt() {
    	try {
			FpmCrypt.getInstance().open(
					getAssets().open("fpm.xml"),
					Hex.decodeHex("e9275c4bd60c2dbabb98b7d822e6f0d123e99ad1c7d3b22e37c9fd49843afa15"));
		} catch (Exception e) {
			Log.w(TAG, "Failed to init FpmCrypt.", e);
		}
    }

    @Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
    	Intent intent = new Intent(this, ViewPasswordItemActivity.class);
    	intent.putExtra("id", id);
    	startActivity(intent);
	}

	public static class FpmFileListAdapter extends BaseAdapter {
    	
    	LayoutInflater layoutInflater;
    	FpmCrypt fpmCrypt;
    	
    	public FpmFileListAdapter(Context context) {
    		layoutInflater = LayoutInflater.from(context);
    		fpmCrypt = FpmCrypt.getInstance();
    	}
    	
		@Override
		public int getCount() {
			return fpmCrypt.getFpmFile().getPasswordItems().size();
		}

		@Override
		public Object getItem(int position) {
			return fpmCrypt.getFpmFile().getPasswordItems().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			PasswordItem item = fpmCrypt.getFpmFile().getPasswordItems().get(position);
			
			if (convertView == null) {
				viewHolder = new ViewHolder(); 
				convertView = layoutInflater.inflate(R.layout.password_item_row, null);
				viewHolder.title = (TextView) convertView.findViewById(R.id.passwordItemTitle);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			viewHolder.title.setText(item.getTitle());

			return convertView;
		}

		private static class ViewHolder {
			public TextView title;
		}
		
    }
    
}