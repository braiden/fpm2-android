package org.braiden.fpm2.crypto;

import test.Hex;

import junit.framework.TestCase;

public class JCEFpmCipherTest extends TestCase {

	public void testDecrypt() throws Exception {
		FpmCipher d = new JCEFpmCipher();
		
		byte[] key = Hex.decodeHex("e9275c4bd60c2dbabb98b7d822e6f0d123e99ad1c7d3b22e37c9fd49843afa15");
		String encryptedData = "clkpkceijlnlicakdnkjfmnempafacapcdcfpgjcmfnkifkicnlhmgnjgcbabmdgneodljhllcdkngfmleipbboncnjdbbaijbhnbibojcnpeogbfelegoffchjjegpcebbfodhlepnliklhgdgmbfllfdldadlbjkklkmhhhhdpndcpgaljfabkgcnaafblpbdnbdofaakadffbolcfghohjpknfoimgehoehllijcahdjdacbhodnomonhkedognmimnpmmncaodelhnadmejcialembkoimgnglkhffkpgelcimajbmkibhiloeibnaphjjhndmganiocendcibcmcnlolpgfpdfihdbocfmbicjgggicgceliglmpignllmfjdfcoknegjfgjepbfbofngfpplmifdfnaidjlhdifandhabhlbnbkijcffmlpdofjkdbhkikflaafbeadjhidpeokjnaipaidnigkgafojjnppjpbhbgponiebhc";
		String result = d.decrypt(key, encryptedData);
		assertEquals("password", result);
	}
	
}
