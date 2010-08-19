package org.braiden.fpm2.crypto;

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
