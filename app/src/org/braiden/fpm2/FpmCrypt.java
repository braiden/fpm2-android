package org.braiden.fpm2;

import java.io.InputStream;
import java.util.Arrays;

import org.braiden.fpm2.crypto.FpmCipher;
import org.braiden.fpm2.crypto.FpmKeyGenerator;
import org.braiden.fpm2.crypto.JCEFpmCipher;
import org.braiden.fpm2.crypto.PBKDF2FpmKeyGenerator;
import org.braiden.fpm2.model.FpmFile;
import org.braiden.fpm2.xml.FpmFileXmlParser;

public class FpmCrypt {

	public final static String FPM_CIPHER_AES_256 = "AES-256";
	
	private static FpmCrypt INSTANCE = null;
	
	private FpmCipher cipher;
	private FpmKeyGenerator keyGenerator;
	private FpmFile fpmFile;
	private byte[] key;
	
	public synchronized static FpmCrypt getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FpmCrypt();
		}
		return INSTANCE;
	}
	
	public void open(InputStream inputStream, String password) throws Exception {
		try {
			fpmFile = FpmFileXmlParser.parse(inputStream);
			cipher = createCipher(fpmFile);
			keyGenerator = createKeyGenerator(fpmFile);
			if (cipher == null || keyGenerator == null) {
				throw new Exception("FPM Cipher \"" + fpmFile.getKeyInfo().getCipher() + "\" is not supported.");
			}
			key = keyGenerator.generateKey(password, fpmFile.getKeyInfo().getSalt());
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
		Arrays.fill(key, (byte)0);
		cipher = null;
		keyGenerator = null;
		fpmFile = null;
		key = null;
	}
	
	public String decrypt(String encryptedData) throws Exception {
		return cipher.decrypt(key, encryptedData);
	}

	protected void decryptAll() {
	
	}
	
	protected boolean verifyVstring() {
		return false;
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
