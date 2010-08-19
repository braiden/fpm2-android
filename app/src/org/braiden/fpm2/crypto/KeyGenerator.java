package org.braiden.fpm2.crypto;

public interface KeyGenerator {
	
	byte[] generateKey(String secret, byte[] salt) throws Exception;

}
