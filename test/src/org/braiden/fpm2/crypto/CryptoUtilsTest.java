package org.braiden.fpm2.crypto;

import junit.framework.TestCase;

public class CryptoUtilsTest extends TestCase {

	public void testDecodeString() throws Exception 
	{
		byte[] result = CryptoUtils.decodeString("eigfgmgmgpcafhgphcgmgeco");
		assertEquals("Hello World.", new String(result));
	}
	
	public void testUnrotate() throws Exception 
	{
		byte[] tmp = "abcdefghijklmnopqrstuvwxyz012345".getBytes();
		tmp = CryptoUtils.unrotate(tmp,16);
		assertEquals("aqbrcsdteufvgwhxiyjzk0l1m2n3o4p5", new String(tmp));		
	}
	
}
