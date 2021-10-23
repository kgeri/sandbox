package org.ogreg.sandbox;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import io.netty.util.CharsetUtil;

import javax.net.ssl.SSLException;

import static io.netty.handler.ssl.ApplicationProtocolConfig.Protocol.ALPN;
import static io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
import static io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior.FATAL_ALERT;
import static io.netty.handler.ssl.ApplicationProtocolNames.HTTP_2;

public class Http2MTlsServer {

	public static void main(String[] args) throws SSLException, InterruptedException {

		SslContext sslCtx = SslContextBuilder.forServer(SharedTrust.CertificateFile, SharedTrust.PrivateKeyFile)
				.trustManager(SharedTrust.Certificate) // <-- specifies the accepted client certs
				.clientAuth(ClientAuth.REQUIRE) // <-- enforces client auth
				.sslProvider(SslProvider.JDK)
				.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
				.applicationProtocolConfig(new ApplicationProtocolConfig(ALPN, FATAL_ALERT, ACCEPT, HTTP_2))
				.build();

		EventLoopGroup group = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.option(ChannelOption.SO_BACKLOG, 1024);
			b.group(group)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) {
							ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()), new Http2APNHandler());
						}
					});
			Channel ch = b.bind(8443).sync().channel();
			ch.closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}

	private static class Http2APNHandler extends ApplicationProtocolNegotiationHandler {

		Http2APNHandler() {
			super(HTTP_2);
		}

		@Override
		protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
			if (HTTP_2.equals(protocol)) {
				ctx.pipeline().addLast(Http2FrameCodecBuilder.forServer().build(), new Http2ServerResponseHandler());
				return;
			}
			throw new IllegalStateException("Protocol: " + protocol + " not supported");
		}
	}

	private static class Http2ServerResponseHandler extends ChannelDuplexHandler {
		static final ByteBuf RESPONSE_BYTES = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8));

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			if (msg instanceof Http2HeadersFrame header) {
				if (header.isEndStream()) {
					ByteBuf content = ctx.alloc().buffer();
					content.writeBytes(RESPONSE_BYTES.duplicate());

					Http2Headers headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText());
					ctx.write(new DefaultHttp2HeadersFrame(headers).stream(header.stream()));
					ctx.write(new DefaultHttp2DataFrame(content, true).stream(header.stream()));
				}
			} else {
				super.channelRead(ctx, msg);
			}
		}
	}
}
