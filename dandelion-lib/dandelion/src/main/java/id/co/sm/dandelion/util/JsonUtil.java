package id.co.sm.dandelion.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.sm.dandelion.frame.JsonWebSocketFrame;
import id.co.sm.dandelion.frame.JsonWebSocketFrame.MessageType;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * 
 * @author josepht
 *
 */
public class JsonUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	/**
	 * EMPTY LOCKED CONSTRUCTOR
	 */
	private JsonUtil () {}
	
	public static TextWebSocketFrame toFrame (MessageType type, String text) throws JsonProcessingException {
		return new TextWebSocketFrame(OBJECT_MAPPER.writeValueAsString(new JsonWebSocketFrame(type, text)));
	}
}