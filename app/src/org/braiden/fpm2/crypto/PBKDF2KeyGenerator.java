package org.braiden.fpm2.crypto;

/*
 * Copyright (c) 2010 Braiden Kindt
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 */

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public class PBKDF2KeyGenerator {
	
	private int keyLengthBytes;
	private int iterations;
	private Mac hmac;
	
	public PBKDF2KeyGenerator(int keyLengthBytes, int iterations, String hmacAlgorith) throws NoSuchAlgorithmException	{
		this.keyLengthBytes = keyLengthBytes;
		this.iterations = iterations;
		this.hmac = Mac.getInstance(hmacAlgorith);
	}
	
	public byte[] generateKey(String secret, byte[] salt) throws InvalidKeyException, ShortBufferException, IllegalStateException {
		SecretKey key = new SecretKeySpec(secret.getBytes(), hmac.getAlgorithm());
		byte[] result = new byte[keyLengthBytes];
		byte[] initialHashInput = new byte[salt.length + 4];
		byte[] hash1 = new byte[hmac.getMacLength()];
		byte[] hash2 = new byte[hmac.getMacLength()];
		byte[] intermediateResult = new byte[hmac.getMacLength()];

		System.arraycopy(salt, 0, initialHashInput, 0, salt.length);
		
		for (int count = 1, bytesRemaining = keyLengthBytes; bytesRemaining > 0; count++) {
			
			initialHashInput[salt.length + 0] = (byte)(count >>> 24);
			initialHashInput[salt.length + 1] = (byte)(count >>> 16);
			initialHashInput[salt.length + 2] = (byte)(count >>> 8);
			initialHashInput[salt.length + 3] = (byte)(count);
			
			hmac.init(key);
			hmac.update(initialHashInput);
			hmac.doFinal(hash1, 0);
			System.arraycopy(hash1, 0, intermediateResult, 0, hash1.length);
			
			for (int iter = 1; iter < this.iterations; iter++) {
				hmac.init(key);
				hmac.update(hash1);
				hmac.doFinal(hash2, 0);
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

		Arrays.fill(hash1, (byte)0);
		Arrays.fill(hash2, (byte)0);
		Arrays.fill(intermediateResult, (byte)0);
		Arrays.fill(initialHashInput, (byte)0);
		
		return result;
	}

	public int getKeyLengthBytes() {
		return keyLengthBytes;
	}

	public int getIterations() {
		return iterations;
	}

	public Mac getHmac() {
		return hmac;
	}
	
}
