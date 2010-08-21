package org.braiden.fpm2.crypto;

import org.braiden.fpm2.util.Hex;

import junit.framework.TestCase;

public class PBKDF2FpmKeyGeneratorTest extends TestCase {

	public void testGenerateKey() throws Exception {
		PBKDF2FpmKeyGenerator kg = new PBKDF2FpmKeyGenerator(32, 128, "HMACSHA256");
		byte[] key = kg.generateKey("password", "lmfloihibngmffkopbaobogkaamdddao");
		assertEquals(
				"11823a61dcb189981a600ca1c6450bde93fd4141ff67d2881160388c56388553",
				Hex.encodeHexString(key)
		);
		
	}
	
}
