package id.co.sm.dandelionapi.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.frame.JsonWebSocketFrame.MessageType;
import id.co.sm.dandelion.observer.WebSocketServerObserver;
import id.co.sm.dandelion.server.WebSocketServer;
import id.co.sm.imgscaler.ImageScaller;

/**
 * 
 * @author josepht
 *
 */
public class WebSocketServerTest implements WebSocketServerObserver {

	static WebSocketServer server;
	
	public static void main (String[] args) {
		server = new WebSocketServer(8800, new WebSocketServerTest());
		try {
			server.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReceived(JsonWebSocketFrame object) {
		if (object.getMessageType().equals(MessageType.MESSAGE.getType())) {
			System.out.println("Message received : " + object.getMessageContent());
			
			// dummy image response
			// will response an image after each message from the client
			// POC use only
			// send to the client
			File inputFile = new File("C:\\Users\\Joseph Tarigan\\Downloads\\15794331735_6c9e03b2f1_o.jpg");
			FileInputStream fis;
			try {
				fis = new FileInputStream(inputFile);
			
				ByteArrayOutputStream baos = ImageScaller.scaleImage(fis, 800);
				String base64String = Base64.getEncoder().encodeToString(baos.toByteArray());
				
				server.sendBlastMessage(base64String, MessageType.BASE64);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else if (object.getMessageType().equals(MessageType.BASE64.getType())) {
			try {
				FileOutputStream fos = new FileOutputStream(new File("C:\\Users\\Joseph Tarigan\\Downloads\\" + UUID.randomUUID() + ".jpg"));
				
				// convert base64 to byte array
				byte[] bytes = Base64.getDecoder().decode(object.getMessageContent());
				
				// byte[] to outputStream
				fos.write(bytes);
				
				// close
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClientConnected(JsonWebSocketFrame object) {
	}

	@Override
	public void onExceptionCaught(Throwable throwable) {
		// TODO Auto-generated method stub
		
	}
}