package org.braiden.fpm2.xml;

import java.io.InputStream;

import org.braiden.fpm2.model.FpmFile;

import junit.framework.TestCase;

public class FpmFileXmlParserTest extends TestCase {

	public void testParse() throws Exception
	{
		InputStream is = FpmFileXmlParserTest.class.getResourceAsStream("fpm.xml");
		FpmFile fpm = FpmFileXmlParser.parse(is);
		
		assertEquals(3, fpm.getLauncherItems().size());
		assertEquals(3, fpm.getPasswordItems().size());
				
		assertEquals(fpm.getFullVersion(), "00.75.00");
		assertEquals(fpm.getDisplayVersion(), "0.75");
		assertEquals(fpm.getMinVersion(), "00.75.00");
		
		assertEquals(fpm.getKeyInfo().getCipher(), "AES-256");
		assertEquals(fpm.getKeyInfo().getSalt(), "lmfloihibngmffkopbaobogkaamdddao");
		assertEquals(fpm.getKeyInfo().getVstring(), "hncojimajfpbobjnghkigpenmkboignpeebdnfojioledohnjpkndpehjkpkjlgk");
		
		assertEquals(fpm.getLauncherItems().get(0).getCmdline(), "nldfnjmkdmglnblfemanmpoognnbdflcihfbndfagffgghficeaccclnaggeebng");
		assertEquals(fpm.getLauncherItems().get(0).getTitle(), "eoembgigikcihfenancghoklkljjjbak");
		assertEquals(fpm.getLauncherItems().get(0).getCopyUser(), "kpidnekkpapkgfiilgcgbpbhcimeibka");
		assertEquals(fpm.getLauncherItems().get(0).getCopyPassword(), "eadmmelolakdekjenglijkdigjkhhlhk");
		
		assertEquals(fpm.getPasswordItems().get(0).getCategory(), "pmdlhgomjejccomnfobmgnchlaogblid");
		assertEquals(fpm.getPasswordItems().get(0).getLauncher(), "bohhekkndnppoagnklofnmmlommgmfhp");
		assertEquals(fpm.getPasswordItems().get(0).getNotes(), "ognfkllofaiapnpphffgnipflmfbnebngpggpkneakeikdfpipomaebnmggdipja");
		assertEquals(fpm.getPasswordItems().get(0).getPassword(), "clkpkceijlnlicakdnkjfmnempafacapcdcfpgjcmfnkifkicnlhmgnjgcbabmdgneodljhllcdkngfmleipbboncnjdbbaijbhnbibojcnpeogbfelegoffchjjegpcebbfodhlepnliklhgdgmbfllfdldadlbjkklkmhhhhdpndcpgaljfabkgcnaafblpbdnbdofaakadffbolcfghohjpknfoimgehoehllijcahdjdacbhodnomonhkedognmimnpmmncaodelhnadmejcialembkoimgnglkhffkpgelcimajbmkibhiloeibnaphjjhndmganiocendcibcmcnlolpgfpdfihdbocfmbicjgggicgceliglmpignllmfjdfcoknegjfgjepbfbofngfpplmifdfnaidjlhdifandhabhlbnbkijcffmlpdofjkdbhkikflaafbeadjhidpeokjnaipaidnigkgafojjnppjpbhbgponiebhc");
		assertEquals(fpm.getPasswordItems().get(0).getUrl(), "bnlihbbcpbdcbicgokcoehmkbanmojdgdlelehjglkhkdglhcmlfmdblpeejckgi");
		assertEquals(fpm.getPasswordItems().get(0).getUser(), "amjedfopdhibbenahobcbjehhgceahbd");
		assertEquals(fpm.getPasswordItems().get(0).getTitle(), "ndoaklplaoaonbpgjnikehlcdjheclgf");

		assertTrue(fpm.getPasswordItems().get(0).isDefault());
		assertFalse(fpm.getPasswordItems().get(1).isDefault());		
	}
	
}
