package id.co.sm.dandelion.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import id.co.sm.dandelion.exception.ClientDoesntExistException;
import id.co.sm.dandelion.exception.SendMessageFailException;
import id.co.sm.dandelion.frame.JsonWebSocketFrame.MessageType;
import id.co.sm.dandelion.initializer.WebSocketServerInitializer;
import id.co.sm.dandelion.observer.WebSocketServerObserver;
import id.co.sm.dandelion.util.JsonUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 
 * @author josepht
 *
 */
public class WebSocketServer {
	/*
	 * KEEPS CONNECTED CLIENT IN A MAP
	 * KEY 		-> THE CLIENT ID
	 * VALUE 	-> ChannelHandlerContext
	 */
	private ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private ConcurrentHashMap<String, Channel> channelDict = new ConcurrentHashMap<>();	
	private ChannelFuture channelFuture;
	private EventLoopGroup parentGroup;
	private EventLoopGroup workerGroup;
	
	private final WebSocketServerObserver observer;
	
	private final int port;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);
	
	public WebSocketServer (int port, WebSocketServerObserver observer) {
		this.port = port;
		this.observer = observer;
		
		LOGGER.debug("Server initialised to listen port : {}", port);
	}
	
	/**
	 * Start the server
	 * @throws InterruptedException 
	 */
	public void start () throws InterruptedException {
		parentGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(parentGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.localAddress(new InetSocketAddress(port));
			b.handler(new LoggingHandler(LogLevel.DEBUG));
			b.childHandler(new WebSocketServerInitializer(observer, this));
			
			channelFuture = b.bind().sync();
		} finally {
			Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                	shutdown();
                }
			});
		}
	}
	
	/**
	 * Send message to specified client ID
	 * 
	 * @param id
	 * @param message
	 * @return
	 * @throws JsonProcessingException
	 */
	public String sendMessage (String id, String message) throws JsonProcessingException {
		return sendMessage(id, message, MessageType.MESSAGE);
	}
	
	/**
	 * Send message to specified client ID and specified Message Type
	 * 
	 * @param id
	 * @param message
	 * @return
	 * @throws JsonProcessingException
	 */
	public String sendMessage (String id, String message, MessageType messageType) throws JsonProcessingException {
		if (channelDict.containsKey(id)) {
			try {
				channelDict.get(id).writeAndFlush(JsonUtil.toFrame(messageType, message));
				return "SUCCESS " + DateTime.now();
			} catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage(), e);
				throw e;
			}
		} else {
			throw new ClientDoesntExistException();
		}
	}
	
	/**
	 * Send given text to all connected channel
	 * @param text
	 */
	public void sendBlastMessage (String text) {
		sendBlastMessage(text, MessageType.MESSAGE);
	}
	
	/**
	 * Send given content to all connected channel with specified message type
	 * @param content
	 * @param messageType
	 */
	public void sendBlastMessage (String content, MessageType messageType) {
		if (allChannels != null) {
        	try {
				allChannels.writeAndFlush(JsonUtil.toFrame(messageType, content));
			} catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage(), e);
				throw new SendMessageFailException("Fail to send message", e);
			}
        }
	}
	
	/**
	 * Shutdown the server
	 * @throws InterruptedException 
	 */
	public void shutdown () {
		LOGGER.debug("Server is shutting down {}", DateTime.now());
		if (allChannels != null) {
			sendBlastMessage("Server is shutting down");
			allChannels.close().awaitUninterruptibly();
        }
		if (channelFuture != null) {
            channelFuture.channel().close().syncUninterruptibly();
        }
        if (parentGroup != null) {
        	parentGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
        	workerGroup.shutdownGracefully();
        }
	}

	/**
	 * Get allChannel channel container
	 * @return
	 */
	public ChannelGroup getAllChannels() {
		return allChannels;
	}

	/**
	 * Get connected channel dictionary
	 * @return
	 */
	public ConcurrentMap<String, Channel> getChannelDict() {
		return channelDict;
	}
}