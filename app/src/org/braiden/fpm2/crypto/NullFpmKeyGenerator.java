package org.braiden.fpm2.crypto;

import java.security.GeneralSecurityException;

public class NullFpmKeyGenerator implements FpmKeyGenerator {

	@Override
	public byte[] generateKey(String secret, String salt)
			throws GeneralSecurityException {
		return secret.getBytes();
	}

}
