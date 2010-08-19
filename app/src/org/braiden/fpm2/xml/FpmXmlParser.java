package org.braiden.fpm2.xml;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.braiden.fpm2.model.Fpm;
import org.braiden.fpm2.model.KeyInfo;
import org.braiden.fpm2.model.LauncherItem;
import org.braiden.fpm2.model.PasswordItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class FpmXmlParser {

	private static final String TAG = "FpmXmlParser";
	
	public static Fpm parse(InputStream is) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		SAXParser parser = factory.newSAXParser();
		FpmFileSaxHandler handler = new FpmFileSaxHandler();
		parser.parse(is, handler);
		return handler.getFpmFile();
	}
	
	public static class FpmFileSaxHandler extends DefaultHandler {
		
		public static final String TAG_FPM = "FPM";
		public static final String TAG_KEY_INFO = "KeyInfo";
		public static final String TAG_LAUNCHER_ITEM = "LauncherItem";
		public static final String TAG_PASSWORD_ITEM = "PasswordItem";
		public static final String TAG_DEFAULT = "default";
		
		private Fpm fpmFile = null;
		private Object currentNode = null;
		private StringBuffer currentText = null;

		public Fpm getFpmFile() {
			return fpmFile;
		}

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			fpmFile = new Fpm();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {			
			super.startElement(uri, localName, qName, attributes);
			
			Object thisNode = null;
			
			if (qName.equals(TAG_FPM)) {
				thisNode = fpmFile;
			} else if (qName.equals(TAG_KEY_INFO)) {
				thisNode = new KeyInfo();
				fpmFile.setKeyInfo((KeyInfo) thisNode);
			} else if (qName.equals(TAG_LAUNCHER_ITEM)) {
				thisNode = new LauncherItem(); 
				fpmFile.getLauncherItems().add((LauncherItem) thisNode);
			} else if (qName.equals(TAG_PASSWORD_ITEM)) {
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
			String propertyName = toCamelCase(qName);
			if (currentText != null && currentNode != null && PropertyUtils.isWriteable(currentNode, propertyName)) {
				try {
					if (TAG_DEFAULT.equals(qName)) {
						BeanUtils.setProperty(currentNode, propertyName, true);
					} else {
						BeanUtils.setProperty(currentNode, propertyName, currentText.toString().trim());
					}
				} catch (Exception e) {
					Log.w(TAG, "Exception applying bean property \"" + propertyName + "\" to \"" + currentNode + "\".", e);
				}
			}
		}

		private void applyAttributesToBean(Object bean, Attributes attributes) {
			for (int n = 0; n < attributes.getLength(); n++) {
				String propertyName = toCamelCase(attributes.getQName(n));
				String value = attributes.getValue(n);
				if (PropertyUtils.isWriteable(bean, propertyName)) {
					try {
						BeanUtils.setProperty(bean, propertyName, value);
					} catch (Exception e) {
						Log.w(TAG, "Exception applying bean property \"" + propertyName + "\" to \"" + bean + "\".", e);
					}
				}
			}
		}
		
		private String toCamelCase(String qName) {
			StringBuffer result = new StringBuffer();
			
			for (int n = 0; n < qName.length(); n++)
			{
				char c = qName.charAt(n);
				if (c == '_' && n > 0 && n + 1 < qName.length())
				{
					c = Character.toUpperCase(qName.charAt(++n));
				}
				result.append(c);
			}
			
			return result.toString();
		}
		
	}
	
}
