package id.co.sm.dandelion.observer;

import id.co.sm.dandelion.frame.JsonWebSocketFrame;

/**
 * will invoke method with event-based manner
 * @author josepht
 *
 */
public interface WebSocketServerObserver {

	/**
	 * on message received prototype method
	 * @param sentObject
	 */
	public void onMessageReceived (JsonWebSocketFrame object);
	
	/**
	 * on client connected prototype method
	 * use with server only
	 * @param object
	 */
	public void onClientConnected (JsonWebSocketFrame object);
	
	/**
	 * on exception caught prototype method
	 * @param throwable
	 */
	public void onExceptionCaught (Throwable throwable);
}