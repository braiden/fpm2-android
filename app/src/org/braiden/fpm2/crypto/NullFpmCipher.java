package org.braiden.fpm2.crypto;

import java.security.GeneralSecurityException;

public class NullFpmCipher implements FpmCipher {

	@Override
	public String decrypt(byte[] key, String encryptedData)
			throws GeneralSecurityException {
		return encryptedData;
	}

	@Override
	public byte[] decryptRaw(byte[] key, String encryptedData)
			throws GeneralSecurityException {
		return encryptedData.getBytes();
	}

	@Override
	public String encrypt(byte[] key, String plainText)
			throws GeneralSecurityException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String encryptRaw(byte[] key, byte[] clear)
			throws GeneralSecurityException {
		throw new UnsupportedOperationException();
	}

}
