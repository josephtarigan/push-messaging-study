package id.co.sm.dandelion.handler.factory;

import id.co.sm.dandelion.handler.WebSocketServerHandler;
import id.co.sm.dandelion.observer.WebSocketServerObserver;
import id.co.sm.dandelion.server.WebSocketServer;

/**
 * 
 * @author josepht
 *
 */
public class WebSocketServerHandlerFactory {
	
	private WebSocketServerHandlerFactory() {}
	
	public static WebSocketServerHandler getHandler (WebSocketServerObserver observer, WebSocketServer serverInstance) {
		return new WebSocketServerHandler(observer, serverInstance);
	}
}