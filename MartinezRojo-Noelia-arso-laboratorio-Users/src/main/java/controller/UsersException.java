package controller;

@SuppressWarnings("serial")
public class UsersException extends Exception {
	
	public UsersException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public UsersException(String msg) {
		super(msg);
	}
}
