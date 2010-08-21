package org.braiden.fpm2.crypto;

import junit.framework.TestCase;

public class JCEFpmCipherTest extends TestCase {

	public void testDecrypt() throws Exception {
		FpmCipher d = new JCEFpmCipher();
		FpmKeyGenerator k = new PBKDF2FpmKeyGenerator();
		
		byte[] key = k.generateKey("secret", "lmfloihibngmffkopbaobogkaamdddao");
		String encryptedData = "clkpkceijlnlicakdnkjfmnempafacapcdcfpgjcmfnkifkicnlhmgnjgcbabmdgneodljhllcdkngfmleipbboncnjdbbaijbhnbibojcnpeogbfelegoffchjjegpcebbfodhlepnliklhgdgmbfllfdldadlbjkklkmhhhhdpndcpgaljfabkgcnaafblpbdnbdofaakadffbolcfghohjpknfoimgehoehllijcahdjdacbhodnomonhkedognmimnpmmncaodelhnadmejcialembkoimgnglkhffkpgelcimajbmkibhiloeibnaphjjhndmganiocendcibcmcnlolpgfpdfihdbocfmbicjgggicgceliglmpignllmfjdfcoknegjfgjepbfbofngfpplmifdfnaidjlhdifandhabhlbnbkijcffmlpdofjkdbhkikflaafbeadjhidpeokjnaipaidnigkgafojjnppjpbhbgponiebhc";
		String result = d.decrypt(key, encryptedData);
		assertEquals("password", result);
	}
	
}
