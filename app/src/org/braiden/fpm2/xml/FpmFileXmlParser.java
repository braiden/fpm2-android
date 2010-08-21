package org.braiden.fpm2.xml;

/**
 * Copyright (c) 2009 Braiden Kindt
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

import java.io.IOException;
import java.io.InputStream;

import org.braiden.fpm2.model.FpmFile;
import org.braiden.fpm2.model.DataObject;
import org.braiden.fpm2.model.KeyInfo;
import org.braiden.fpm2.model.LauncherItem;
import org.braiden.fpm2.model.PasswordItem;
import org.braiden.fpm2.util.PropertyUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import android.util.Xml;

public class FpmFileXmlParser {

	private static final String TAG = "FpmXmlParser";
	
	public static FpmFile parse(InputStream is) throws IOException, SAXException {
		FpmFileSaxHandler handler = new FpmFileSaxHandler();
		Xml.parse(is, Xml.Encoding.UTF_8, handler);
		return handler.getFpmFile();
	}
	
	public static class FpmFileSaxHandler extends DefaultHandler {
		
		public static final String TAG_FPM = "FPM";
		public static final String TAG_KEY_INFO = "KeyInfo";
		public static final String TAG_LAUNCHER_ITEM = "LauncherItem";
		public static final String TAG_PASSWORD_ITEM = "PasswordItem";
		public static final String TAG_DEFAULT = "default";
		
		private FpmFile fpmFile = null;
		private DataObject currentNode = null;
		private StringBuffer currentText = null;

		public FpmFile getFpmFile() {
			return fpmFile;
		}

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			fpmFile = new FpmFile();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {			
			super.startElement(uri, localName, qName, attributes);
			
			DataObject thisNode = null;
			
			if (localName.equals(TAG_FPM)) {
				thisNode = fpmFile;
			} else if (localName.equals(TAG_KEY_INFO)) {
				thisNode = new KeyInfo();
				fpmFile.setKeyInfo((KeyInfo) thisNode);
			} else if (localName.equals(TAG_LAUNCHER_ITEM)) {
				thisNode = new LauncherItem(); 
				fpmFile.getLauncherItems().add((LauncherItem) thisNode);
			} else if (localName.equals(TAG_PASSWORD_ITEM)) {
				thisNode = new PasswordItem();
				fpmFile.getPasswordItems().add((PasswordItem) thisNode);
			}

			if (thisNode != null)
			{
				currentNode = thisNode;
				applyAttributesToBean(thisNode, attributes);
			}
			
			currentText = new StringBuffer();
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			super.characters(ch, start, length);
			currentText.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			super.endElement(uri, localName, qName);
			String propertyName = toCamelCase(localName);
			if (currentText != null && currentNode != null && PropertyUtils.isWriteable(currentNode, propertyName)) {
				try {
					if (TAG_DEFAULT.equals(localName)) {
						PropertyUtils.setProperty(currentNode, propertyName, true);
					} else {
						PropertyUtils.setProperty(currentNode, propertyName, currentText.toString().trim());
					}
				} catch (Exception e) {
					Log.w(TAG, "Exception applying bean property \"" + propertyName + "\" to \"" + currentNode + "\".", e);
				}
			}
		}

		private void applyAttributesToBean(DataObject bean, Attributes attributes) {
			for (int n = 0; n < attributes.getLength(); n++) {
				String propertyName = toCamelCase(attributes.getLocalName(n));
				String value = attributes.getValue(n);
				if (PropertyUtils.isWriteable(bean, propertyName)) {
					try {
						PropertyUtils.setProperty(bean, propertyName, value);
					} catch (Exception e) {
						Log.w(TAG, "Exception applying bean property \"" + propertyName + "\" to \"" + bean + "\".", e);
					}
				}
			}
		}
		
		private String toCamelCase(String localName) {
			StringBuffer result = new StringBuffer();
			
			for (int n = 0; n < localName.length(); n++)
			{
				char c = localName.charAt(n);
				if (c == '_' && n > 0 && n + 1 < localName.length())
				{
					c = Character.toUpperCase(localName.charAt(++n));
				}
				result.append(c);
			}
			
			return result.toString();
		}
		
	}
	
}
