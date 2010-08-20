package org.braiden.fpm2.crypto;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.ArrayUtils;

public class JCEDecrypter implements Decrypter {

	private final static String DEFAULT_CIPHER = "AES/ECB/NoPadding";

	private Cipher cipher;
	
	public JCEDecrypter() throws NoSuchAlgorithmException, NoSuchPaddingException {
		this(DEFAULT_CIPHER);
	}
	
	public JCEDecrypter(String cipherName) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.cipher = Cipher.getInstance(cipherName);
	}

	@Override
	public String decrypt(byte[] key, byte[] encryptedData) throws Exception {
		SecretKey cipherKey = new SecretKeySpec(key, cipher.getAlgorithm());

		cipher.init(Cipher.DECRYPT_MODE, cipherKey);
		byte[] result = cipher.doFinal(encryptedData);
		result = FpmCryptoUtils.unrotate(result, cipher.getBlockSize());
		int idxOfNil = ArrayUtils.indexOf(result, (byte)0);
		
		return new String(result, 0, idxOfNil);
	}
	
}
