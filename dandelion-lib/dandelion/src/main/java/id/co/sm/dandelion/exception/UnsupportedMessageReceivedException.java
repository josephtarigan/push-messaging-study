package id.co.sm.dandelion.exception;

/**
 * 
 * @author josepht
 *
 */
public class UnsupportedMessageReceivedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnsupportedMessageReceivedException(String message) {
		super(message);
	}
	
	public UnsupportedMessageReceivedException(String message, Throwable cause) {
		super(message, cause);
	}
}