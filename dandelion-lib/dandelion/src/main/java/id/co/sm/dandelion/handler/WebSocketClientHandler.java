package id.co.sm.dandelion.handler;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.sm.dandelion.client.WebSocketClient;
import id.co.sm.dandelion.exception.UnsupportedMessageReceivedException;
import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.frame.JsonWebSocketFrame.MessageType;
import id.co.sm.dandelion.observer.WebSocketClientObserver;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

/**
 * 
 * @author josepht
 *
 */
@Sharable
public class WebSocketClientHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientHandler.class);
	
	private final WebSocketClientHandshaker handShaker;
	private final String id;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	private ChannelPromise handShakeFuture;
	private WebSocketClientObserver observer;
	
	public WebSocketClientHandler(final WebSocketClientHandshaker handShaker, final String id, WebSocketClientObserver observer) {
		this.handShaker = handShaker;
		this.observer = observer;
		this.id = id;
	}	
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		final Channel channel = ctx.channel();
		if (!handShaker.isHandshakeComplete()) {
			handShaker.finishHandshake(channel, (FullHttpResponse) msg);
			handShakeFuture.setSuccess();
			
			// introduce yourself to the server
			introduceYourself(ctx.channel());
			
			// notify observer
			observer.onClientConnected();
			
			return;
		}
		
		if (msg instanceof FullHttpResponse) {
			final FullHttpResponse response = (FullHttpResponse) msg;
			LOGGER.error("Unexpected FullHttpResponse \n Status: {}\n Content: {}", response.status(), response.content().toString(CharsetUtil.UTF_8));
			throw new UnsupportedMessageReceivedException("Unexpected FullHttpResponse \n Status: " + response.status() + "\n Content: " + response.content().toString(CharsetUtil.UTF_8));
		}
		
		final WebSocketFrame webSocketFrame = (WebSocketFrame) msg;
		if (webSocketFrame instanceof TextWebSocketFrame) { // text frame
			final TextWebSocketFrame textFrame = (TextWebSocketFrame) msg;
			JsonWebSocketFrame message = null;
			
			// try to convert to object
			try {
				message = objectMapper.readValue(textFrame.content().toString(CharsetUtil.UTF_8), JsonWebSocketFrame.class);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Incompatible message format has received {}", DateTime.now());
			}
			
			if (message != null) {
				// if success, call the observer
				observer.onMessageReceived(message);
				
				LOGGER.debug("Received text frame : {}", textFrame.content().toString(CharsetUtil.UTF_8));
			} else {
				LOGGER.error("Encoded message is empty {}", DateTime.now());
			}
		} else if (webSocketFrame instanceof CloseWebSocketFrame) { // remote close channel request
			channel.close();
			LOGGER.debug("Remote close channel request received {}", DateTime.now());
		} else {
			LOGGER.warn("Unknown message type was received {}", DateTime.now());
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		if (!handShakeFuture.isDone()) {
			handShakeFuture.setFailure(cause);
		}
		
		// notify
		observer.onExceptionCaught(cause);
		
		LOGGER. error("Error happen", cause);
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		handShakeFuture = ctx.newPromise();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		handShaker.handshake(ctx.channel()); // do handshake
		
		if (ctx.channel() != null) {
			setServerChannel(ctx.channel());
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		observer.onClientDisconnected();
		LOGGER.warn("WebSocket client is inactive {}", DateTime.now());
	}
	
	private void introduceYourself (Channel channel) {
		channel.pipeline().channel().writeAndFlush(new TextWebSocketFrame(new JsonWebSocketFrame(MessageType.INTRODUCE,id).toString()));
	}
	
	private static void setServerChannel (Channel channel) {
		WebSocketClient.serverChannel = channel;
	}	

	public ChannelPromise getHandShakeFuture() {
		return handShakeFuture;
	}

	public WebSocketClientHandshaker getHandShaker() {
		return handShaker;
	}
}