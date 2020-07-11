package rest.exceptions;

@SuppressWarnings("serial")
public class SurveyException extends Exception {
	
	public SurveyException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public SurveyException(String msg) {
		super(msg);
	}
}
