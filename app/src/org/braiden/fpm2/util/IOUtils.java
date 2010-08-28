package org.braiden.fpm2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class IOUtils {

	public final static String read(InputStream is) throws IOException {
		Reader reader = new InputStreamReader(is);
		StringBuffer result = new StringBuffer();
		char buffer[] = new char[2048];
		int count;
		while ((count = reader.read(buffer, 0, buffer.length)) > 0) {
			result.append(buffer, 0, count);
		}
		return result.toString();
	}
	
}
