package rsa.exception;

public class ROSGiException extends Exception {

	public ROSGiException(String message){
		super(message);
	}
	
	public ROSGiException(String message, Throwable t){
		super(message, t);
	}
}
