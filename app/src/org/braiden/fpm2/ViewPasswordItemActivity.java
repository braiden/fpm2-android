package org.braiden.fpm2;

import org.braiden.fpm2.model.PasswordItem;

import android.app.Activity;
import android.os.Bundle;

public class ViewPasswordItemActivity extends Activity {

	private PasswordItem item;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.view_password_item);
		item = (PasswordItem) FpmCrypt.getInstance().getFpmFile().getPasswordItems().get((int)getIntent().getLongExtra("id", -1L));
		String title = getResources().getString(R.string.app_name) + " - " + item.getTitle();
		this.setTitle(title);
	}
	
}
