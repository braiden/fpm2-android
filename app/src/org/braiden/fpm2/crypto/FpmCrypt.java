package org.braiden.fpm2.crypto;

import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;

public class FpmCrypt
{
	
	protected static SecretKey buildSecretKey(String passphrase, byte[] salt) throws InvalidKeySpecException {
//
//		PBKDF2SecretKeyFactory keyFactory = new PBKDF2SecretKeyFactory.HMacSHA256();
//		
//		KeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt, 8192, 32);
//		SecretKey key = keyFactory.engineGenerateSecret(keySpec);
		return null;
	}
	
	protected static byte[] decodeString(String s)
	{
		byte[] result = new byte[s.length()/2];

		for (int n = 0; n < s.length() / 2; n++) {
			byte high = (byte) (s.charAt(n * 2) - 'a');
			byte low = (byte) (s.charAt(n * 2 + 1) - 'a');
			result[n] = low;
			result[n] |= high << 4;
		}
		
		return result;		
	}

	protected static byte[] unrotate(byte[] data, int blockSizeBytes)
	{
		byte result[] = new byte[data.length];
		
		int numBlocks = data.length / blockSizeBytes;
		
		for (int block = 0; block < numBlocks; block++) {
			for (int el = 0; el < blockSizeBytes; el++)	{
				result[el * numBlocks + block] = data[block * blockSizeBytes + el];
			}
		}
		
		return result;
	}

}
