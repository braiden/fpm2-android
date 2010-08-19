package org.braiden.fpm2.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.braiden.fpm2.crypto.PBKDF2KeyGenerator;

import junit.framework.TestCase;

public class PBKDF2KeyGeneratorTest extends TestCase {
	
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
    private static String encodeHexString(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = HEX_DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = HEX_DIGITS[0x0F & data[i]];
        }
        return new String(out);
    }
	
	public void testHmacSha256() throws Exception
	{
		String salt = "kohamnllcnepbdnk";
		PBKDF2KeyGenerator keyGenerator = new PBKDF2KeyGenerator();
		byte[] key = keyGenerator.generateKey("secret", salt.getBytes());
		assertEquals(
				"3dc7fa34fa2aa5cab9e1c00f75f1c7e39e20cbd12446c8d07cbd3de7568e7683",
				encodeHexString(key));
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
			assertEquals(encodeHexString(javaKey), encodeHexString(myKey));
		}
	}
	
	
	
}
