package id.co.sm.dandelion.initializer;

import id.co.sm.dandelion.handler.factory.WebSocketServerHandlerFactory;
import id.co.sm.dandelion.observer.WebSocketServerObserver;
import id.co.sm.dandelion.server.WebSocketServer;
import id.co.sm.dandelion.util.Constant;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * 
 * @author josepht
 *
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel>{
	
	private WebSocketServerObserver observer;
	private final WebSocketServer serverInstance;
	
	public WebSocketServerInitializer(WebSocketServerObserver observer, WebSocketServer serverInstance) {
		// init
		this.observer = observer;
		this.serverInstance = serverInstance;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		
		pipeline.addLast(Constant.HTTP_CODEC, new HttpServerCodec());
		pipeline.addLast(Constant.AGGREGATOR, new HttpObjectAggregator(Constant.AGGREGATOR_SIZE));
		pipeline.addLast(Constant.WEBSOCKET_HANDLER, WebSocketServerHandlerFactory.getHandler(observer, serverInstance));
	}
}