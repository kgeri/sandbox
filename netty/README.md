# HTTP/2 with Mutual TLS

**Context**: I was looking for how to have mTLS in Micronaut, for a peer-to-peer app, but found nothing. Then I dug into whether mTLS is possible on
Netty (the default server for Micronaut), and while at it, tried to combine it with a HTTP/2 config. That went fairly quickly, but next I also wanted
to have the client running with standard JDK 11+ HttpClient, similarly enforcing mTLS and HTTP/2... again not too much material on that.

So this repo contains sample code that I ended up with.

## Generating a self-signed cert

* Re-generate the self-signed certificate with [SelfSignedCertificateGenerator](src/main/java/org/ogreg/sandbox/SelfSignedCertificateGenerator.java)
  if you like

The certificate will be shared by the client and the server for Mutual TLS (will be part of the truststore as well as the keystore).
See [SharedTrust](src/main/java/org/ogreg/sandbox/SharedTrust.java).

## Netty server

* Run [Http2MTlsServer](src/main/java/org/ogreg/sandbox/Http2MTlsServer.java)
* Test with: `curl --key selfsigned.key --cert selfsigned.crt --cacert selfsigned.crt -X GET -v https://localhost:8443`

Note: the server is configured to do ALPN (which I don't yet fully understand), loosely based
on [SSLEngineTest](https://github.com/netty/netty/blob/4.1/handler/src/test/java/io/netty/handler/ssl/SSLEngineTest.java)
and mostly using code from [HTTP/2 in Netty](https://www.baeldung.com/netty-http2)

## Java 11 HttpClient

* Run [Http2MTlsServer](src/main/java/org/ogreg/sandbox/Http2MTlsServer.java) as per above
* Run [Http2MTlsClient](src/main/java/org/ogreg/sandbox/Http2MTlsClient.java)
