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

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.braiden.fpm2.crypto.FpmCipher;
import org.braiden.fpm2.crypto.FpmKeyGenerator;
import org.braiden.fpm2.crypto.JCEFpmCipher;
import org.braiden.fpm2.crypto.PBKDF2FpmKeyGenerator;
import org.braiden.fpm2.model.DataObject;
import org.braiden.fpm2.model.FpmFile;
import org.braiden.fpm2.model.LauncherItem;
import org.braiden.fpm2.model.PasswordItem;
import org.braiden.fpm2.util.PropertyUtils;
import org.braiden.fpm2.xml.FpmFileXmlParser;

import android.util.Log;

public class FpmCrypt {
	
	public final static String FPM_CIPHER_AES_256 = "AES-256";
	
	protected final static String AES_VSTRING_HASH_FUNCTION = "SHA256";	
	protected final static String TAG = "FpmCrypt";
	protected final static String PROPERTY_PASSWORD = "password";
		
	private FpmCipher cipher;
	private FpmKeyGenerator keyGenerator;
	private FpmFile fpmFile;
	volatile private byte[] key;
	
	public void open(InputStream inputStream, String password) throws Exception {
		try {
			fpmFile = FpmFileXmlParser.parse(inputStream);
			cipher = createCipher(fpmFile);
			keyGenerator = createKeyGenerator(fpmFile);
			if (cipher == null || keyGenerator == null) {
				throw new Exception("FPM Cipher \"" + fpmFile.getKeyInfo().getCipher() + "\" is not supported.");
			}
			this.key = keyGenerator.generateKey(password, fpmFile.getKeyInfo().getSalt());
			decryptAll();
			if (!verifyVstring()) {
				throw new Exception("Password invalid.");
			}
		} catch (Exception e) {
			close();
			throw e;
		}
	}
	
	public boolean isOpen() {
		return key != null;
	}
	
	public void close() {
		if (isOpen()) {
			Arrays.fill(key, (byte)0);
			cipher = null;
			keyGenerator = null;
			fpmFile = null;
			key = null;
		}
	}
	
	public String decrypt(String encryptedData) throws Exception {
		return cipher.decrypt(key, encryptedData);
	}
	
	public String encrypt(String clearTextData) throws Exception {
		return cipher.encrypt(key, clearTextData);
	}

	public FpmFile getFpmFile() {
		return fpmFile;
	}
	
	protected void decryptAll() {				
		for (PasswordItem passwordItem : fpmFile.getPasswordItems()) {
			decryptBean(passwordItem);
		}
		for (LauncherItem launcherItem : fpmFile.getLauncherItems()) {
			decryptBean(launcherItem);
		}
	}
	
	protected void decryptBean(DataObject bean) {
		Map<String, Object> beanProps;
		try {
			beanProps = PropertyUtils.describe(bean);
		} catch (Exception e) {
			Log.w(TAG, "Failed to decrypt bean.", e);
			return;
		}
		for (Entry<String, Object> entry : beanProps.entrySet()) {
			if (!PROPERTY_PASSWORD.equals(entry.getKey())
					&& entry.getValue() != null
					&& String.class.isAssignableFrom(entry.getValue().getClass())) {
				try {
					PropertyUtils.setProperty(bean, entry.getKey(), decrypt((String) entry.getValue()));
				} catch (Exception e) {
					Log.w(TAG, "Failed while decrypting property \"" + entry.getKey() + "\".", e);
				}
			}
		}
	}
	
	protected boolean verifyVstring() throws NoSuchAlgorithmException {
		if (FPM_CIPHER_AES_256.equals(fpmFile.getKeyInfo().getCipher())) {
			return aesVerifyVstring();
		}
		return false;
	}
	
	protected boolean aesVerifyVstring() throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(AES_VSTRING_HASH_FUNCTION);
		for (PasswordItem item : fpmFile.getPasswordItems()) {
			StringBuffer input = new StringBuffer();
			input.append(item.getTitle())
				.append(item.getUrl())
				.append(item.getUser())
				.append(item.getNotes())
				.append(item.getCategory())
				.append(item.getLauncher());
			digest.update(input.toString().getBytes());
		}
		
		byte[] myVstring = digest.digest();
		byte[] fileVstring;
		
		try {
			fileVstring = cipher.decryptRaw(key, fpmFile.getKeyInfo().getVstring());
		} catch (Exception e) {
			fileVstring = null;
			Log.w(TAG, "Failed to decrypt vstring.", e);
		}
		
		return MessageDigest.isEqual(myVstring, fileVstring);
	}
	
	protected static FpmCipher createCipher(FpmFile fpmFile) throws Exception {
		if (FPM_CIPHER_AES_256.equals(fpmFile.getKeyInfo().getCipher())) {
			return new JCEFpmCipher();
		}
		return null;
	}
	
	protected static FpmKeyGenerator createKeyGenerator(FpmFile fpmFile) throws Exception {
		if (FPM_CIPHER_AES_256.equals(fpmFile.getKeyInfo().getCipher())) {
			return new PBKDF2FpmKeyGenerator();
		}
		return null;
	}

}
