package id.co.sm.dandelionapi.svc.impl;

import org.springframework.stereotype.Service;

import id.co.sm.dandelion.server.WebSocketServer;
import id.co.sm.dandelionapi.observer.ServerObserver;
import id.co.sm.dandelionapi.svc.WebSocketSvc;

/**
 * 
 * @author Joseph Tarigan
 *
 */
@Service
public class WebSocketSvcImpl implements WebSocketSvc {
	
	private WebSocketServer wsServer;

	@Override
	public void start() {
		try {
			wsServer = new WebSocketServer(8800, new ServerObserver());
			wsServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WebSocketServer getWsServer() {
		return wsServer;
	}

	@Override
	public void sendMessage(String destination, String message) throws Exception {
		try {
			if (wsServer != null) {
				wsServer.sendMessage(destination, message);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void broadcastMessage(String message) {
		wsServer.sendBlastMessage(message);
	}
}