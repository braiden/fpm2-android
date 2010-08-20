package org.braiden.fpm2.crypto;

import junit.framework.TestCase;

public class FpmCryptoUtilsTest extends TestCase {

	public void testDecodeString() throws Exception 
	{
		byte[] result = FpmCryptoUtils.decodeString("eigfgmgmgpcafhgphcgmgeco");
		assertEquals("Hello World.", new String(result));
	}
	
	public void testUnrotate() throws Exception 
	{
		byte[] tmp = "abcdefghijklmnopqrstuvwxyz012345".getBytes();
		tmp = FpmCryptoUtils.unrotate(tmp,16);
		assertEquals("aqbrcsdteufvgwhxiyjzk0l1m2n3o4p5", new String(tmp));		
	}
	
}
