package org.braiden.fpm2.crypto;

/**
 * Copyright (c) 2009 Braiden Kindt
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

import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.StringUtils;

public class PBKDF2FpmKeyGenerator implements FpmKeyGenerator {

	public static final int DEFAULT_KEY_LENGTH_BYTES = 32;
	public static final int DEFAULT_ITERATIONS = 8192;
	public static final String DEFAULT_HMAC_ALGORITH = "HMACSHA256";
	
	private PBKDF2KeyGenerator keyGenerator;
	
	public PBKDF2FpmKeyGenerator() throws NoSuchAlgorithmException {
		this(DEFAULT_KEY_LENGTH_BYTES, DEFAULT_ITERATIONS, DEFAULT_HMAC_ALGORITH);
	}
	
	public PBKDF2FpmKeyGenerator(int keyLength, int iterations, String hmac) throws NoSuchAlgorithmException {
		keyGenerator = new PBKDF2KeyGenerator(keyLength, iterations, hmac);
	}

	@Override
	public byte[] generateKey(String secret, String salt) throws Exception {
		byte[] saltBytes = StringUtils.substring(salt, 0, keyGenerator.getKeyLengthBytes() / 2).getBytes();
		return keyGenerator.generateKey(secret, saltBytes);
	}
	
}
