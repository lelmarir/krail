package uk.co.q3c.v7.base.navigate;

public class PageNotFoundException extends RuntimeException {
	
	public PageNotFoundException(String message) {
		super(message);
	}
	
	public PageNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
