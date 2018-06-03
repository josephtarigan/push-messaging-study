package id.co.sm.dandelion.exception;

/**
 * 
 * @author josepht
 *
 */
public class SendMessageFailException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SendMessageFailException(String errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}