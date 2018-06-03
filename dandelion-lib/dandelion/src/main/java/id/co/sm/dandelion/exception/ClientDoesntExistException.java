package id.co.sm.dandelion.exception;

/**
 * 
 * @author josepht
 *
 */
public class ClientDoesntExistException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClientDoesntExistException() {
		super("Client doesn't exist nor registered in the server");
	}
}