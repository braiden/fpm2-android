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

public class FpmCryptUtils
{
	
	public static byte[] decodeString(String s)
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

	public static byte[] unrotate(byte[] data, int blockSizeBytes)
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
