package id.co.sm.dandelion.observer;

import id.co.sm.dandelion.frame.JsonWebSocketFrame;

/**
 * 
 * @author josepht
 *
 */
public interface WebSocketClientObserver {

	/**
	 * on message received prototype method
	 * @param sentObject
	 */
	public void onMessageReceived (JsonWebSocketFrame object);

	/**
	 * on client connected
	 */
	public void onClientConnected ();
	
	/**
	 * on client diconnected
	 */
	public void onClientDisconnected ();
	
	/**
	 * on connecting fail
	 */
	public void onConnectingFail ();
	
	/**
	 * on client reconnecting
	 */
	public void onClientReconnecting ();
	
	/**
	 * on connection success
	 */
	public void onConnectionSuccess ();
	
	/**
	 * on exception caught
	 */
	public void onExceptionCaught (Throwable cause);
}