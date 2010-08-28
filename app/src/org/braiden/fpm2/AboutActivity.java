package org.braiden.fpm2;

import java.io.InputStream;

import org.braiden.fpm2.util.IOUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class AboutActivity extends Activity {

	protected static final String TAG = "AboutActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		WebView view = (WebView) findViewById(R.id.about);
		InputStream is = getResources().openRawResource(R.raw.about);
		try {
			view.loadData(IOUtils.read(is), "text/html", "utf-8");
		} catch (Exception e) {
			Log.w(TAG, "Failed to read resource.", e);
		}
	}

}
