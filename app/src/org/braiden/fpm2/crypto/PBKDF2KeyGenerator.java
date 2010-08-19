package org.braiden.fpm2.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PBKDF2KeyGenerator implements KeyGenerator {

	private static final int DEFAULT_KEY_LENGTH_BYTES = 32;
	private static final int DEFAULT_ITERATIONS = 8192;
	private static final String DEFAULT_HMAC_ALGORITH = "HMACSHA256";
	
	private int keyLengthBytes;
	private int iterations;
	private Mac hmac;
	
	public PBKDF2KeyGenerator() throws NoSuchAlgorithmException {
		this(DEFAULT_KEY_LENGTH_BYTES, DEFAULT_ITERATIONS, DEFAULT_HMAC_ALGORITH);
	}
	
	public PBKDF2KeyGenerator(int keyLengthBytes, int iterations, String hmacAlgorith) throws NoSuchAlgorithmException	{
		this.keyLengthBytes = keyLengthBytes;
		this.iterations = iterations;
		this.hmac = Mac.getInstance(hmacAlgorith);
	}
	
	@Override
	public byte[] generateKey(String secret, byte[] salt) throws InvalidKeyException {
		SecretKey key = new SecretKeySpec(secret.getBytes(), hmac.getAlgorithm());
		byte[] result = new byte[keyLengthBytes];
		byte[] initialHashInput = new byte[salt.length + 4];

		System.arraycopy(salt, 0, initialHashInput, 0, salt.length);
		
		for (int count = 1, bytesRemaining = keyLengthBytes; bytesRemaining > 0; count++) {
			
			initialHashInput[salt.length + 0] = (byte)(count >> 24);
			initialHashInput[salt.length + 1] = (byte)(count >> 16);
			initialHashInput[salt.length + 2] = (byte)(count >> 8);
			initialHashInput[salt.length + 3] = (byte)(count);
			
			hmac.init(key);
			byte[] hash1 = hmac.doFinal(initialHashInput);
			byte[] intermediateResult = new byte[hash1.length];
			System.arraycopy(hash1, 0, intermediateResult, 0, hash1.length);
			
			for (int iter = 1; iter < this.iterations; iter++) {
				hmac.init(key);
				byte[] hash2 = hmac.doFinal(hash1);
				System.arraycopy(hash2, 0, hash1, 0, hash2.length);
				for (int n = 0; n < hash1.length; n++) {
					intermediateResult[n] ^= hash1[n];
				}
			}
			
			int len = intermediateResult.length < bytesRemaining ? intermediateResult.length : bytesRemaining;
			int offset = keyLengthBytes - bytesRemaining;
			System.arraycopy(intermediateResult, 0, result, offset, len);
			bytesRemaining -= intermediateResult.length;

		}
				
		return result;
	}
	
}
