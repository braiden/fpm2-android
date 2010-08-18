package org.braiden.fpm2;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.braiden.fpm2.FpmCrypt;


import junit.framework.TestCase;

public class FpmCryptTest extends TestCase
{

	public void testDecodeString() throws Exception 
	{
		byte[] result = FpmCrypt.decodeString("eigfgmgmgpcafhgphcgmgeco");
		assertEquals("Hello World.", new String(result));
	}
	
	public void testUnrotate() throws Exception 
	{
		FpmCrypt crypt = new FpmCrypt();
		byte[] tmp = "abcdefghijklmnopqrstuvwxyz012345".getBytes();
		crypt.unrotate(tmp,16);
		assertEquals("aqbrcsdteufvgwhxiyjzk0l1m2n3o4p5", new String(tmp));		
	}
	
	public void testBuildSecretKey() throws Exception
	{
		byte[] salt = "kohamnllcnepbdnkobodfdjfegdeolog".getBytes();
		SecretKey key = FpmCrypt.buildSecretKey("secret", salt); 
		Key aesKey = new SecretKeySpec(key.getEncoded(), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, aesKey);		
		byte[] result = cipher.doFinal(
				FpmCrypt.decodeString("ilbakpmmnmllnmkdfipkoccfmdaacdpc"));
		System.err.println(cipher.getBlockSize());
		System.err.println(new String(result));
	}
	
}
