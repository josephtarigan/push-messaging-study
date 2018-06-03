package id.co.sm.dandelion.handler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.frame.JsonWebSocketFrame.MessageType;
import id.co.sm.dandelion.observer.WebSocketServerObserver;
import id.co.sm.dandelion.server.WebSocketServer;
import id.co.sm.dandelion.util.Constant;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

/**
 * 
 * @author josepht
 *
 */
@Sharable
public class WebSocketServerHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServerHandler.class);
	
	private ObjectMapper objectMapper = new ObjectMapper();
	private WebSocketServerObserver observer;
	
	private final WebSocketServer serverInstance;
	
	protected WebSocketServerHandshaker handShaker;

	/**
	 * PASS A CONCRETE OBSERVER INTO THIS
	 * @param observer
	 */
	public WebSocketServerHandler(WebSocketServerObserver observer, WebSocketServer serverInstance) {
		this.observer = observer;
		this.serverInstance = serverInstance;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("Channel is active {} {}", ctx.channel().id().asLongText(), DateTime.now()); // is a channel reflect a client?
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		serverInstance.getAllChannels().remove(ctx.channel());
		
		try {
			String key = getKeyByValue(serverInstance.getChannelDict(), ctx.channel());
			serverInstance.getChannelDict().remove(key);
		} catch (Exception e) {
			// nothing
		}
		
		LOGGER.info("Channel {} is inactive {}", ctx.channel().id().asLongText(), DateTime.now());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		ChannelId connectionId = ctx.channel().id();
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest (ctx, (FullHttpRequest) msg);
			LOGGER.debug("Full HTTP request received {} from channel ID : {}", DateTime.now(), connectionId.asLongText());
		} else if (msg instanceof WebSocketFrame) {
			if (msg instanceof TextWebSocketFrame) {
				final TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
				JsonWebSocketFrame message = null;
				
				// try to convert to object
				try {
					message = objectMapper.readValue(textFrame.content().toString(CharsetUtil.UTF_8), JsonWebSocketFrame.class);
				} catch (IllegalArgumentException e) {
					LOGGER.error("Incompatible message format has received {}", DateTime.now());
				}
				
				if (message != null) {
					// check whether the message is an introductory message
					if (message.getMessageType().equalsIgnoreCase(MessageType.INTRODUCE.getType())) {
						LOGGER.info("Client {} is trying to register itself", message.getMessageContent());
						// check whether the id is exist or it doesn't
						if (serverInstance.getChannelDict().containsKey(message.getMessageContent())) {
							// will replace the existing channel
							serverInstance.getChannelDict().replace(message.getMessageContent(), ctx.channel());
						} else {
							serverInstance.getChannelDict().put(message.getMessageContent(), ctx.channel());
						}
						LOGGER.info("Client {} is now registered", message.getMessageContent());
						// execute
						observer.onClientConnected(message);
					} else {
						// if success, call the observer
						observer.onMessageReceived(message);
					}
					
					LOGGER.debug("TextWebSocketFrame is received \n content : {} \n timestamp : {} \n from connection ID : {}", ((WebSocketFrame) msg).content().toString(CharsetUtil.UTF_8), DateTime.now(), connectionId.asLongText());
				} else {
					LOGGER.error("Encoded message is empty {}", DateTime.now());
				}
			} else if (msg instanceof CloseWebSocketFrame) {
				serverInstance.getAllChannels().remove(ctx.channel());
				LOGGER.debug("CloseWebSocketFrame is received, close request received \n timestamp : {} from connection ID : {}", DateTime.now(), connectionId.asLongText());
			} else {
				LOGGER.warn("Unsupported message has received {}", DateTime.now());
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		observer.onExceptionCaught(cause);
		LOGGER.error("Error happen", cause);
	}
	
	private void handleHttpRequest (ChannelHandlerContext ctx, FullHttpRequest req) {
		if (!req.decoderResult().isSuccess()) {
			LOGGER.error("{}", "Not success");
			return;
		}
		
		// handshake
		handleHandshake(ctx, req);
	}
	
	/**
	 * Handle websocket handshake
	 * @param ctx
	 * @param req
	 */
	protected void handleHandshake (ChannelHandlerContext ctx, HttpRequest req) {
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(this.getWebSocketUrl(req), null, Constant.ALLOW_EXTENSIONS, 1280000, false);
		handShaker = wsFactory.newHandshaker(req);
		
		if (handShaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			handShaker.handshake(ctx.channel(), req);
			
			// add the channel to the list
			serverInstance.getAllChannels().add(ctx.channel());
			LOGGER.debug("Connected channel : {}", serverInstance.getAllChannels().size());
		}
	}
	
	protected String getWebSocketUrl (HttpRequest req) {
		String url = "ws://" + req.headers().get("Host") + req.uri();
		LOGGER.debug("URL : {}", url);
		return url;
	}
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
}