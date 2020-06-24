package controller;

@SuppressWarnings("serial")
public class UserException extends Exception {
	
	public UserException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public UserException(String msg) {
		super(msg);
	}
}
