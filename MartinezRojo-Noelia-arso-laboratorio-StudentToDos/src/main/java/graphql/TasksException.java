package graphql;

@SuppressWarnings("serial")
public class TasksException extends Exception {
	
	public TasksException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public TasksException(String msg) {
		super(msg);
	}
}
