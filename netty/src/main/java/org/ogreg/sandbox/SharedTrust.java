package org.ogreg.sandbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class SharedTrust {
	static final File CertificateFile = new File("selfsigned.crt");
	static final X509Certificate Certificate;

	static final File PrivateKeyFile = new File("selfsigned.key");
	static final PrivateKey PrivateKey;

	static {
		try (InputStream is = new FileInputStream(CertificateFile)) {
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
			Certificate = (X509Certificate) fact.generateCertificate(is);
		} catch (IOException | CertificateException e) {
			throw new RuntimeException(e);
		}

		try {
			String privateKeyPem = Files.readString(PrivateKeyFile.toPath())
					.replace("-----BEGIN PRIVATE KEY-----", "")
					.replaceAll("[\r\n]", "")
					.replace("-----END PRIVATE KEY-----", "");
			byte[] encoded = Base64.getDecoder().decode(privateKeyPem);

			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			PrivateKey = keyFactory.generatePrivate(keySpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}
}
