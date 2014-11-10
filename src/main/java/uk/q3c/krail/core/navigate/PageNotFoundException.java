package uk.q3c.krail.core.navigate;

public class PageNotFoundException extends RuntimeException {
	
	public PageNotFoundException(String message) {
		super(message);
	}
	
	public PageNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
