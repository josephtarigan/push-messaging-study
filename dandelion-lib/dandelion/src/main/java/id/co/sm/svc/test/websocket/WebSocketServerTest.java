package id.co.sm.svc.test.websocket;

import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.observer.WebSocketServerObserver;
import id.co.sm.dandelion.server.WebSocketServer;

/**
 * 
 * @author josepht
 *
 */
public class WebSocketServerTest implements WebSocketServerObserver {

	public static void main (String[] args) {
		WebSocketServer server = new WebSocketServer(8800, new WebSocketServerTest());
		try {
			server.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReceived(JsonWebSocketFrame object) {
		System.out.println("Message received : " + object.getMessageContent());
	}

	@Override
	public void onClientConnected(JsonWebSocketFrame object) {
	}

	@Override
	public void onExceptionCaught(Throwable throwable) {
		// TODO Auto-generated method stub
		
	}
}