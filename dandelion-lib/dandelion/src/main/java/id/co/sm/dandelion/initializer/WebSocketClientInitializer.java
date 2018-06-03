package id.co.sm.dandelion.initializer;

import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import id.co.sm.dandelion.util.Constant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * 
 * @author josepht
 *
 */
public class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientInitializer.class);
	private final ChannelInboundHandler handler;
	protected WebSocketServerHandshaker handShaker;
	
	public WebSocketClientInitializer (final ChannelInboundHandler handler) {
		this.handler = handler;
	}
	
	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		ChannelPipeline pipeline = arg0.pipeline();
		pipeline.addLast(Constant.HTTP_CODEC, new HttpClientCodec());
		pipeline.addLast(Constant.AGGREGATOR, new HttpObjectAggregator(Constant.AGGREGATOR_SIZE));
		pipeline.addLast(Constant.WEBSOCKET_HANDLER, handler);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.pipeline().channel().writeAndFlush(new TextWebSocketFrame("Hi! I'm " + UUID.randomUUID()));
		LOGGER.debug("Client is connected to the server {}", DateTime.now());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		LOGGER.debug("Client is disconnected to the server {}", DateTime.now());
	}
}