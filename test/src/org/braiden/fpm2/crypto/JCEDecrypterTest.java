package org.braiden.fpm2.crypto;

import junit.framework.TestCase;

public class JCEDecrypterTest extends TestCase {

	public void testDecrypt() throws Exception {
		Decrypter d = new JCEDecrypter();
		KeyGenerator k = new PBKDF2KeyGenerator();
		
		byte[] key = k.generateKey("secret", "lmfloihibngmffko".getBytes());
		String encryptedData = "clkpkceijlnlicakdnkjfmnempafacapcdcfpgjcmfnkifkicnlhmgnjgcbabmdgneodljhllcdkngfmleipbboncnjdbbaijbhnbibojcnpeogbfelegoffchjjegpcebbfodhlepnliklhgdgmbfllfdldadlbjkklkmhhhhdpndcpgaljfabkgcnaafblpbdnbdofaakadffbolcfghohjpknfoimgehoehllijcahdjdacbhodnomonhkedognmimnpmmncaodelhnadmejcialembkoimgnglkhffkpgelcimajbmkibhiloeibnaphjjhndmganiocendcibcmcnlolpgfpdfihdbocfmbicjgggicgceliglmpignllmfjdfcoknegjfgjepbfbofngfpplmifdfnaidjlhdifandhabhlbnbkijcffmlpdofjkdbhkikflaafbeadjhidpeokjnaipaidnigkgafojjnppjpbhbgponiebhc";
		String result = d.decrypt(key, encryptedData);
		assertEquals("password", result);
	}
	
}
