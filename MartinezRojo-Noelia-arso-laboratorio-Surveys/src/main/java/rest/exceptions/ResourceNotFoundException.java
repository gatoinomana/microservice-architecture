package rest.exceptions;

@SuppressWarnings("serial")
public class ResourceNotFoundException extends Exception {
	
	public ResourceNotFoundException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ResourceNotFoundException(String msg) {
		super(msg);
	}
}
