package uk.q3c.krail.core.ui;

public class MethodReconfigured extends RuntimeException {

	public MethodReconfigured() {
		super();
	}

	public MethodReconfigured(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MethodReconfigured(String message, Throwable cause) {
		super(message, cause);
	}

	public MethodReconfigured(String message) {
		super(message);
	}

	public MethodReconfigured(Throwable cause) {
		super(cause);
	}

}
