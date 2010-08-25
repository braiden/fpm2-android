package org.braiden.fpm2.crypto;

import java.security.GeneralSecurityException;

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

public interface FpmCipher {
	
	/**
	 * Given a key, and string of encrypted data encoded in FPM2's format
	 * convert to a pritable plaintext java string.
	 * 
	 * @param key
	 * @param encryptedData
	 * @return
	 * @throws Exception
	 */
	String decrypt(byte[] key, String encryptedData) throws GeneralSecurityException;
	
	/**
	 * Given a key, and FPM encoded ecrypted String, return a byte[]
	 * containing the raw (but FPM rotated) output of the cipher.
	 * 
	 * @param key
	 * @param encryptedData
	 * @return
	 * @throws Exception
	 */
	byte[] decryptRaw(byte[] key, String encryptedData) throws GeneralSecurityException;
	
	/**
	 * Given key, and plain text string encrypt to FPM2's rules.
	 * Ensuring '\00' terminated C string and randomg data padding
	 * to next blocksize.
	 * 
	 * @param key
	 * @param plainText
	 * @return
	 * @throws Exception
	 */
	String encrypt(byte[] key, String plainText) throws GeneralSecurityException;
	
	/**
	 * Rotate and encode the given byte[] using FPM's cipher.
	 * 
	 * @param key
	 * @param clear
	 * @return
	 * @throws Exception
	 */
	String encryptRaw(byte[] key, byte clear[]) throws GeneralSecurityException;
	
}
