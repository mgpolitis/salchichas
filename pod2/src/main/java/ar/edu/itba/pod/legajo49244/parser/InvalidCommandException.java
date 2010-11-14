package ar.edu.itba.pod.legajo49244.parser;

public class InvalidCommandException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidCommandException() {
		super();
	}
	
	public InvalidCommandException(final String message) {
		super(message);
	}
	
	public InvalidCommandException(final String message, Throwable e) {
		super(message,e);
	}
}
