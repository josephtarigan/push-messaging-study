package id.co.sm.dandelion.client;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.frame.JsonWebSocketFrame.MessageType;
import id.co.sm.dandelion.handler.WebSocketClientHandler;
import id.co.sm.dandelion.initializer.WebSocketClientInitializer;
import id.co.sm.dandelion.observer.WebSocketClientObserver;
import id.co.sm.dandelion.util.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

/**
 * 
 * @author josepht
 *
 */
public class WebSocketClient {

	private Channel channel;
	
	public static final int RETRY_INTERVAL = 5;
	
	public static boolean startFlag = false;
	public static Channel serverChannel;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClient.class);
	private static final EventLoopGroup GROUP = new NioEventLoopGroup();
	private final URI uri;
	private final String id;
	private final WebSocketClientHandshaker handShaker;
	private final WebSocketClientHandler webSocketClientHandler;
	
	private final WebSocketClientObserver observer;
	
	/**
	 * Websocket Client Constructor
	 * ID as client ID. Must be unique.
	 * URI is the server address.
	 * Observer is the message received listener.
	 * @param id
	 * @param uri
	 * @param observer
	 */
	public WebSocketClient(final String id, final String uri, WebSocketClientObserver observer) {
		this.uri = URI.create(uri);
		this.id = id;
		this.observer = observer;
		
		// instantiate handshaker
		this.handShaker = WebSocketClientHandshakerFactory.newHandshaker(this.uri, WebSocketVersion.V13, null, Constant.ALLOW_EXTENSIONS, EmptyHttpHeaders.INSTANCE, 1280000, true, false);
		
		// instantiate handler
		this.webSocketClientHandler = new WebSocketClientHandler(handShaker, this.id,  this.observer);
	}
	
	public void start () {		
		try {
			// put the flag
			startFlag = true;
			
			Bootstrap b = new Bootstrap();
			b.group(GROUP);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Constant.TIMEOUT_LIMIT);
			b.channel(NioSocketChannel.class);
			
			if (!"ws".equalsIgnoreCase(uri.getScheme())) {
				throw new IllegalArgumentException("Unsupported protocol");
			}
			
			b.handler(new WebSocketClientInitializer(webSocketClientHandler));
			
			b.connect(uri.getHost(), uri.getPort()).addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isCancelled()) {
						observer.onExceptionCaught(new Exception("Connect cancelled"));
					} else if (!future.isSuccess() && startFlag) {
						// fire observer method
						observer.onConnectingFail();
						LOGGER.warn("Connection fail, retrying in " + RETRY_INTERVAL + " second(s)");
						
						// this will try to reconnect to the server with 5 seconds time delay
						future.channel().eventLoop().schedule(new Runnable() {
							
							@Override
							public void run() {
								start();
								// fire observer method
								observer.onClientReconnecting();
								LOGGER.warn("Retrying to connect...");
							}
						}, RETRY_INTERVAL, TimeUnit.SECONDS);
					} else if (future.isSuccess()) {
						// fire observer method
						observer.onConnectionSuccess();
						
						channel = future.syncUninterruptibly().channel();
						webSocketClientHandler.getHandShakeFuture().sync();
					}
				}
			});
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			observer.onExceptionCaught(e);
		}
	}
	
	/**
	 * shutdown the client
	 * @throws InterruptedException
	 */
	public void shutdown () throws InterruptedException {
		if (serverChannel != null) {
			LOGGER.debug("Sending {} request to the server", "CloseWebSocketFrame");
			channel.writeAndFlush(new CloseWebSocketFrame()); // send close the socket
			channel.close();
			channel.closeFuture().sync();
			observer.onClientDisconnected();
			startFlag = false;
			LOGGER.debug("{}", "Client shutting down");
		}
	}
	
	/**
	 * Send message
	 * @param text
	 */
	public void sendMessage (final String text) {
		if (serverChannel != null && serverChannel.pipeline().channel().isWritable()) {
			LOGGER.debug("Sending {} to server {}", text, DateTime.now());
			serverChannel.pipeline().channel().writeAndFlush(new TextWebSocketFrame(new JsonWebSocketFrame(MessageType.MESSAGE,text).toString()));
		}
	}
	
	/**
	 * Send message
	 * @param type
	 * @param text
	 */
	public void sendMessage (final MessageType type, final String text) {
		if (serverChannel != null && serverChannel.pipeline().channel().isWritable()) {
			LOGGER.debug("Sending {} to server {}", text, DateTime.now());
			serverChannel.pipeline().channel().writeAndFlush(new TextWebSocketFrame(new JsonWebSocketFrame(type,text).toString()));
		}
	}
	
	/**
	 * get connection status
	 * @return
	 */
	public boolean isConnected () {
		return serverChannel == null ? false : serverChannel.isActive() && serverChannel.isRegistered() && serverChannel.isOpen();
	}
}