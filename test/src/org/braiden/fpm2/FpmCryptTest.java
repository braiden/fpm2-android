package org.braiden.fpm2;

import org.braiden.fpm2.model.PasswordItem;

import android.test.InstrumentationTestCase;

public class FpmCryptTest extends InstrumentationTestCase {

	public void testOpen() throws Exception {
		FpmCrypt fpmCrypt = new FpmCrypt();
		fpmCrypt.open(getInstrumentation().getContext().getAssets().open("fpm.xml"), "secret");
		assertEquals(3, fpmCrypt.getFpmFile().getLauncherItems().size());
		assertEquals(3, fpmCrypt.getFpmFile().getPasswordItems().size());
		
		PasswordItem item = fpmCrypt.getFpmFile().getPasswordItems().get(0);
		assertEquals("Entry1", item.getTitle());
		assertEquals("http://braiden.org", item.getUrl());
		assertEquals("braiden", item.getUser());
		assertEquals("password", fpmCrypt.decrypt(item.getPassword()));
		assertEquals("Blog", item.getCategory());
		assertEquals("This is a sample note.", item.getNotes());
		assertEquals("Web", item.getLauncher());
	}

}
