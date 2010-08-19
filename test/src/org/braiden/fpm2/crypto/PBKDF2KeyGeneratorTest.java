package org.braiden.fpm2.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.braiden.fpm2.crypto.PBKDF2KeyGenerator;

import junit.framework.TestCase;

public class PBKDF2KeyGeneratorTest extends TestCase {
	
	public void testHmacSha256() throws Exception
	{
		String salt = "kohamnllcnepbdnk";
		PBKDF2KeyGenerator keyGenerator = new PBKDF2KeyGenerator();
		byte[] key = keyGenerator.generateKey("secret", salt.getBytes());
		assertEquals(
				"3dc7fa34fa2aa5cab9e1c00f75f1c7e39e20cbd12446c8d07cbd3de7568e7683",
				Hex.encodeHexString(key));
	}
	
	public void testHmacSha1() throws Exception
	{
		SecretKeyFactory javaSha1KeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		
		PBEKeySpec[] tests = {
				new PBEKeySpec("secret".toCharArray(), "salt".getBytes(), 1024, 64),
				new PBEKeySpec("secret".toCharArray(), "wertyuiopdfgnfna64397gf".getBytes(), 8192, 1024),
		};
		
		for (PBEKeySpec keySpec : tests) {
			SecretKey javaSecretKey = javaSha1KeyFactory.generateSecret(keySpec);
			byte[] javaKey = javaSecretKey.getEncoded();
			PBKDF2KeyGenerator myKeyGenerator = new PBKDF2KeyGenerator(keySpec.getKeyLength() / 8, keySpec.getIterationCount(), "HmacSHA1");
			byte[] myKey = myKeyGenerator.generateKey(new String(keySpec.getPassword()), keySpec.getSalt());
			assertEquals(Hex.encodeHexString(javaKey), Hex.encodeHexString(myKey));
		}
	}
	
}
