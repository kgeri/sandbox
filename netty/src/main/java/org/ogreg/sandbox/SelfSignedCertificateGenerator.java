package org.ogreg.sandbox;

import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.*;
import java.security.cert.CertificateException;

public class SelfSignedCertificateGenerator {
	public static void main(String[] args) throws CertificateException {
		SelfSignedCertificate cert = new SelfSignedCertificate();
		copy(cert.certificate(), new File("selfsigned.crt"));
		copy(cert.privateKey(), new File("selfsigned.key"));
	}

	private static void copy(File in, File out) {
		try (InputStream is = new FileInputStream(in);
		     OutputStream os = new FileOutputStream(out)) {
			byte[] buf = new byte[4096];
			int len;
			while ((len = is.read(buf)) != -1) {
				os.write(buf, 0, len);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
