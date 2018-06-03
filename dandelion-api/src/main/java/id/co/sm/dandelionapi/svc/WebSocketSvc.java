package id.co.sm.dandelionapi.svc;

/**
 * 
 * @author Joseph Tarigan
 *
 */
public interface WebSocketSvc {
		
	/**
	 * Start the server
	 */
	public void start ();
	
	public void sendMessage (String destination, String message) throws Exception;
	
	public void broadcastMessage (String message);
}