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

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.NoSuchPaddingException;

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
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * FpmCrypt provides high level access to an FPM file.
 * It encapsulates the data and the ciphers.
 *  
 * @author braiden
 *
 */
public class FpmCrypt {
	
	public final static String FPM_CIPHER_AES_256 = "AES-256";
	
	protected final static String AES_VSTRING_HASH_FUNCTION = "SHA256";	
	protected final static String TAG = "FpmCrypt";
	protected final static String PROPERTY_PASSWORD = "password";
		
	private FpmCipher cipher;
	private FpmKeyGenerator keyGenerator;
	private FpmFile fpmFile;
	private boolean isOpen;
	private byte[] key;
	
	/**
	 * Open the given FPM file, pointed to by inputStream
	 * 
	 * @param inputStream open inputStream positioned at start of file.
	 * @param password passphrase used to generate key for reading file
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws FpmCipherUnsupportedException 
	 * @throws FpmPassphraseInvalidException 
	 * @throws Exception in case of bad password, unsupported file format, cipher, or IO error
	 */
	public void open(InputStream inputStream, String password) 	throws IOException, SAXException, 
			GeneralSecurityException, FpmCipherUnsupportedException, FpmPassphraseInvalidException {
		try {
			// build data objects form xml
			fpmFile = FpmFileXmlParser.parse(inputStream);
			
			// locate a cipher and key generator for the given inpurt file
			cipher = createCipher(fpmFile);
			keyGenerator = createKeyGenerator(fpmFile);
			
			// can't go further if cipher of key gen is null (this fpm file format is not supported).
			if (cipher == null || keyGenerator == null) {
				throw new FpmCipherUnsupportedException("FPM Cipher \"" + fpmFile.getKeyInfo().getCipher() + "\" is not supported.");
			}
			// build a key (byte[]) from the provided password.
			this.key = keyGenerator.generateKey(password, fpmFile.getKeyInfo().getSalt());
			
			// decrypt everything except passwords inplace in our model.
			decryptAll();
			
			// verify data decrypted ok, if not, key must be invalid.
			if (!verifyVstring()) {
				throw new FpmPassphraseInvalidException("Password invalid.");
			}
			
			isOpen = true;
		} catch (IOException e) {
			close();
			throw e;
		} catch (SAXException e) {
			close();
			throw e;
		} catch (GeneralSecurityException e) {
			close();
			throw e;
		} catch (FpmCipherUnsupportedException e) {
			close();
			throw e;
		} catch (FpmPassphraseInvalidException e) {
			close();
			throw e;
		}
	}
	
	/**
	 * True if this FpmCrypt is open and key is valid.
	 * @return
	 */
	public boolean isOpen() {
		return isOpen;
	}
	
	/**
	 * Close the crypt
	 */
	public void close() {
		if (isOpen()) {
			// futile(?) attempt to clean key from memory.
			Arrays.fill(key, (byte)0);
			isOpen = false;
			cipher = null;
			keyGenerator = null;
			fpmFile = null;
			key = null;
		}
	}
	
	/**
	 * Decrypt the provided string using FPM2's logic
	 * 
	 * @param encryptedData
	 * @return
	 * @throws Exception
	 */
	public String decrypt(String encryptedData) throws GeneralSecurityException {
		return cipher.decrypt(key, encryptedData);
	}
	
	/**
	 * Encrypt the provided string, tranform into FPM's base16 format.
	 * 
	 * @param clearTextData
	 * @return
	 * @throws Exception
	 */
	public String encrypt(String clearTextData) throws GeneralSecurityException {
		return cipher.encrypt(key, clearTextData);
	}

	/**
	 * Date the data object (passwords and launchers)
	 * 
	 * @return
	 */
	public FpmFile getFpmFile() {
		return fpmFile;
	}
	
	/**
	 * decrypt all PasswordItems and Launchers
	 */
	protected void decryptAll() {				
		for (PasswordItem passwordItem : fpmFile.getPasswordItems()) {
			decryptBean(passwordItem);
		}
		for (LauncherItem launcherItem : fpmFile.getLauncherItems()) {
			decryptBean(launcherItem);
		}
	}
	
	/**
	 * Assume the all string properties for the given
	 * bean are encrypted, and try to convert to clear text.
	 * Password property is skipped.
	 * 
	 * @param bean
	 */
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
	
	/**
	 * Verify the vstring hash is ok. This is used to confirm password/key is OK.
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	protected boolean verifyVstring() throws GeneralSecurityException {
		if (FPM_CIPHER_AES_256.equals(fpmFile.getKeyInfo().getCipher())) {
			return aesVerifyVstring();
		}
		return false;
	}
	
	/**
	 * AES version of vstring check.
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	protected boolean aesVerifyVstring() throws GeneralSecurityException {
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
	
	/**
	 * Get the FpmCipher which know how to decrypt data in this file.
	 * Currently only AES-256 is supported.
	 * 
	 * @param fpmFile
	 * @return
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws Exception
	 */
	protected static FpmCipher createCipher(FpmFile fpmFile) throws GeneralSecurityException {
		if (FPM_CIPHER_AES_256.equals(fpmFile.getKeyInfo().getCipher())) {
			return new JCEFpmCipher();
		}
		return null;
	}
	
	/**
	 * Get the FpmKeyGenerator which know how to convert password into
	 * our cipher key. Currently only AES-256/PBKDF2-SHA256 is supported.
	 * 
	 * @param fpmFile
	 * @return
	 * @throws Exception
	 */
	protected static FpmKeyGenerator createKeyGenerator(FpmFile fpmFile) throws GeneralSecurityException {
		if (FPM_CIPHER_AES_256.equals(fpmFile.getKeyInfo().getCipher())) {
			return new PBKDF2FpmKeyGenerator();
		}
		return null;
	}
	
	public static class FpmCipherUnsupportedException extends Exception {

		private static final long serialVersionUID = 5884333303992753439L;

		public FpmCipherUnsupportedException(String msg) {
			super(msg);
		}
	}
	
	public static class FpmPassphraseInvalidException extends Exception {
		
		private static final long serialVersionUID = -6602099021510293708L;

		public FpmPassphraseInvalidException(String msg) {
			super(msg);
		}
		
	}
	
}
