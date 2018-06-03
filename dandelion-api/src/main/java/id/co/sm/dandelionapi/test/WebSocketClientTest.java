package id.co.sm.dandelionapi.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.UUID;

import id.co.sm.dandelion.client.WebSocketClient;
import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.frame.JsonWebSocketFrame.MessageType;
import id.co.sm.dandelion.observer.WebSocketClientObserver;
import id.co.sm.imgscaler.ImageScaller;

/**
 * 
 * @author josepht
 *
 */
public class WebSocketClientTest implements WebSocketClientObserver {

	public static void main (String[] args) throws Exception {
		final WebSocketClient client = new WebSocketClient(UUID.randomUUID().toString(), "ws://localhost:8800/", new WebSocketClientTest());
		client.start();
		
		try {
			File inputFile = new File("C:\\Users\\Joseph Tarigan\\Downloads\\15794331735_6c9e03b2f1_o.jpg");
			FileInputStream fis = new FileInputStream(inputFile);
			
			ByteArrayOutputStream baos = ImageScaller.scaleImage(fis, 800);
			String base64String = Base64.getEncoder().encodeToString(baos.toByteArray());
			
			client.sendMessage(MessageType.BASE64, base64String);
			
			/*
			client.sendMessage("Test send message " + DateTime.now().toString());
			client.sendMessage("Test send message 2 " + DateTime.now().toString());
			client.sendMessage("Test send message 3 " + DateTime.now().toString());
			*/
			client.shutdown();
		} catch (Exception e) {
			
		}
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