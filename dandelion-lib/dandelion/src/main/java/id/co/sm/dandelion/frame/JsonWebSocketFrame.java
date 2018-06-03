package id.co.sm.dandelion.frame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author josepht
 *
 */
public class JsonWebSocketFrame {

	/**
	 * JSON frame message type.
	 * @author josepht
	 *
	 */
	@JsonIgnoreType
	public enum MessageType {
		INTRODUCE	("INTRODUCE"),
		INFO 		("INFO"),
		MESSAGE		("MESSAGE"),
		ERROR		("ERROR"),
		BASE64		("BASE64");
		
		private final String type;

		private MessageType(String type) {
			this.type = type;
		}
		public String getType() {
			return type;
		}
	}
	
	@JsonIgnore
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonWebSocketFrame.class);
	private String messageType;
	private String messageContent;
	
	public JsonWebSocketFrame() {}
	
	public JsonWebSocketFrame(MessageType messageType, String messageContent) {
		this.messageType = messageType.getType();
		this.messageContent = messageContent;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	@Override
	@JsonIgnore
	public String toString () {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(this);
		} catch (Exception e) {
			LOGGER.error("Error converting object to string", e);
			return "";
		}
	}
}