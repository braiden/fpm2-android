package org.braiden.fpm2.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	public static void write(OutputStream os, InputStream is) throws IOException {
		byte[] buffer = new byte[2048];
		int count = 0;
		while ((count = is.read(buffer)) >= 0) {
			os.write(buffer, 0, count);
		}
	}
	
}
