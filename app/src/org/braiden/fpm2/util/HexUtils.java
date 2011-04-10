package org.braiden.fpm2.util;

public class HexUtils {

	public static String toHex(byte[] data) {
		StringBuffer result = new StringBuffer();
		if (data != null) {
			for (int n = 0; n < data.length; n++)
			{
				String hex = Integer.toHexString(0xFF & data[n]);
				if (hex.length() == 1)
				{
					result.append("0");
				}
				result.append(hex);
			}
		}
		return result.toString();
	}
	
}
