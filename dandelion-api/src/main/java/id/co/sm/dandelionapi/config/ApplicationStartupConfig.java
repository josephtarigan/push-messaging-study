package id.co.sm.dandelionapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import id.co.sm.dandelionapi.svc.WebSocketSvc;

/**
 * 
 * @author Joseph Tarigan
 *
 */
@Component
public class ApplicationStartupConfig implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	WebSocketSvc webSocketSvc;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent arg0) {
		
		webSocketSvc.start();
		return;		
	}
}