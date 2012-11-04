package rsaother.exception;

public class RESTException extends Exception {

	public RESTException(String message){
		super(message);
	}
	
	public RESTException(String message, Throwable t){
		super(message, t);
	}
}
