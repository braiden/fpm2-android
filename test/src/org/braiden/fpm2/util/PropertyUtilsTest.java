package org.braiden.fpm2.util;

import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.braiden.fpm2.model.FpmFile;
import org.braiden.fpm2.model.KeyInfo;
import org.braiden.fpm2.model.PasswordItem;
import org.braiden.fpm2.util.PropertyUtils;

public class PropertyUtilsTest extends TestCase {

	public void testIsReadable() throws Exception {
		assertTrue(PropertyUtils.isReadable(new FpmFile(), "displayVersion"));
		assertFalse(PropertyUtils.isReadable(new FpmFile(), "foo"));
		assertTrue(PropertyUtils.isReadable(new PasswordItem(), "default"));
	}
	
	public void testIsWriteable() throws Exception {
		assertTrue(PropertyUtils.isWriteable(new FpmFile(), "displayVersion"));
		assertFalse(PropertyUtils.isWriteable(new FpmFile(), "foo"));
		assertTrue(PropertyUtils.isWriteable(new PasswordItem(), "default"));
	}
	
	public void testSetProperty() throws Exception {
		PasswordItem bean = new PasswordItem();
		bean.setDefault(false);
		PropertyUtils.setProperty(bean, "default", true);
		assertTrue(bean.isDefault());
		PropertyUtils.setProperty(bean, "password", "secret");
		PropertyUtils.setProperty(bean, "default", false);
		assertEquals("secret", bean.getPassword());
		assertFalse(bean.isDefault());
	}

	public void testGetProperty() throws Exception {
		PasswordItem bean = new PasswordItem();
		bean.setDefault(true);
		bean.setPassword("password");
		assertEquals(true, PropertyUtils.getProperty(bean, "default"));
		assertEquals("password", PropertyUtils.getProperty(bean, "password"));
	}
	
	public void testDescribe() throws Exception {
		PasswordItem bean = new PasswordItem();
		bean.setCategory("category");
		bean.setLauncher("launcher");
		bean.setNotes("notes");
		bean.setPassword("password");
		bean.setTitle("title");
		bean.setUrl("url");
		bean.setUser("user");
		bean.setDefault(true);
		Map<String, Object> beanMap = PropertyUtils.describe(bean);
		assertEquals(8, beanMap.size());
		for (Entry<String, Object> e : beanMap.entrySet()) {
			if (String.class.isAssignableFrom(e.getValue().getClass()))
				assertEquals(e.getKey(), e.getValue());
			else if (Boolean.class.isAssignableFrom(e.getValue().getClass()))
				assertTrue((Boolean) e.getValue());
		}
	}
	
}
