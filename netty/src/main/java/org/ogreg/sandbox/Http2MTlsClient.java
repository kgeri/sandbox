package org.ogreg.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;

import static java.net.http.HttpClient.Version.HTTP_2;

public class Http2MTlsClient {
	private static final Logger log = LoggerFactory.getLogger(Http2MTlsClient.class);

	public static void main(String[] args) throws Exception {

		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);
		trustStore.setCertificateEntry("server", SharedTrust.Certificate);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		keyStore.setKeyEntry("client", SharedTrust.PrivateKey, new char[0], new Certificate[]{SharedTrust.Certificate});
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, new char[0]);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

		SSLParameters sslParam = new SSLParameters();
		sslParam.setProtocols(new String[]{"TLSv1.3"}); // Need to set this otherwise jdk.internal.net.http.HttpConnection.getConnection won't set ALPN
		sslParam.setNeedClientAuth(true);

		HttpClient client = HttpClient.newBuilder()
				.version(HTTP_2)
				.sslContext(sslContext)
				.sslParameters(sslParam)
				.build();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://localhost:8443"))
				.GET()
				.build();
		HttpResponse<String> rsp = client.send(request, HttpResponse.BodyHandlers.ofString());
		log.info(rsp.body());
	}
}
