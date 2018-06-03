package id.co.sm.dandelionapi.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import id.co.sm.dandelion.frame.JsonWebSocketFrame.MessageType;
import id.co.sm.dandelion.util.Constant;
import id.co.sm.dandelionapi.svc.WebSocketSvc;
import id.co.sm.dandelionapi.svc.impl.WebSocketSvcImpl;

/**
 * 
 * @author Joseph Tarigan
 *
 */
@RestController
@RequestMapping("API")
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class MainRest {

	@Autowired
	WebSocketSvc webSocketSvc;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MainRest.class);
	
	@RequestMapping (value = "channelDict", method = RequestMethod.POST)
	public List<String> getChannelDict () {
		return new ArrayList<String>(((WebSocketSvcImpl) webSocketSvc).getWsServer().getChannelDict().keySet());
	}
	
	@RequestMapping (value = "sendMessage", method = RequestMethod.POST)
	public void sendMessage (@RequestBody Map<String, Object> request) {
		if ((request.keySet().contains(Constant.ID)) && (request.keySet().contains(MessageType.MESSAGE.getType()))) {
			try {
				webSocketSvc.sendMessage((String) request.get(Constant.ID), (String) request.get(MessageType.MESSAGE.getType()));
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		} else {
			LOGGER.warn("Bad request", request);
		}
	}
	
	@RequestMapping (value = "broadcastMessage", method = RequestMethod.POST)
	public void broadcastMessage (@RequestBody Map<String, Object> request) {
		if (request.keySet().contains(MessageType.MESSAGE.getType())) {
			webSocketSvc.broadcastMessage((String) request.get(MessageType.MESSAGE.getType()));
		} else {
			LOGGER.warn("Bad request", request);
		}
	}
}