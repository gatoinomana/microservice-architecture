package rest.exceptions;

@SuppressWarnings("serial")
public class ForbiddenException extends Exception {
	
	public ForbiddenException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ForbiddenException(String msg) {
		super(msg);
	}
}
