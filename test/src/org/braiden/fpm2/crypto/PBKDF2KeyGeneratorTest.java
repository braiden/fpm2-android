package org.braiden.fpm2.crypto;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.braiden.fpm2.crypto.PBKDF2KeyGenerator;

import test.Hex;

import android.util.Log;

import junit.framework.TestCase;

public class PBKDF2KeyGeneratorTest extends TestCase {
	
	public void testHmacSha256() throws Exception
	{
		String salt = "kohamnllcnepbdnk";
		PBKDF2KeyGenerator keyGenerator = new PBKDF2KeyGenerator(32, 256, "HMACSHA256");
		byte[] key = keyGenerator.generateKey("secret", salt.getBytes());
		assertEquals(
				"cee31b0069c1720f1af039b602231e3885b082598829d38991dfa7b871394a17",
				Hex.encodeHexString(key));
	}
	
	public void testHmacSha1() throws Exception
	{
		SecretKeyFactory javaSha1KeyFactory = null;
		try {
			javaSha1KeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");			
		} catch (NoSuchAlgorithmException e) {
			// this test doesn't run on android device, algo is not availible
			Log.w("PBKDF2KeyGeneratorTest", "Skipping Test. PBKDF2WithHmacSHA1 is not availible.");
			return;
		}
		
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
