package id.co.sm.svc.test.websocket;

import java.util.UUID;

import org.joda.time.DateTime;

import id.co.sm.dandelion.client.WebSocketClient;
import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.observer.WebSocketClientObserver;

/**
 * 
 * @author josepht
 *
 */
public class WebSocketClientTest implements WebSocketClientObserver {

	public static void main (String[] args) throws Exception {
		final WebSocketClient client = new WebSocketClient(UUID.randomUUID().toString(), "ws://localhost:8800/", new WebSocketClientTest());
		client.start();
		client.sendMessage("Test send message " + DateTime.now().toString());
		client.sendMessage("Test send message 2 " + DateTime.now().toString());
		client.sendMessage("Test send message 3 " + DateTime.now().toString());
		client.shutdown();
	}

	@Override
	public void onMessageReceived(JsonWebSocketFrame object) {
		if (object != null) {
			System.out.println("Message received from server : " + object.getMessageContent());
		}
	}

	@Override
	public void onClientConnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClientDisconnected() {
		System.out.println("Disconnected");
	}

	@Override
	public void onExceptionCaught(Throwable cause) {
		System.err.println(cause.getMessage());
	}

	@Override
	public void onConnectingFail() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClientReconnecting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionSuccess() {
		// TODO Auto-generated method stub
		
	}
}