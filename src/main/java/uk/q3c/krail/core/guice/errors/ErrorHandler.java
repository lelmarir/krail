package uk.q3c.krail.core.guice.errors;

public interface ErrorHandler {
	
	public static class ErrorEvent {
		
		private Throwable throwable;
		
		public ErrorEvent(Throwable throwable) {
			this.throwable = throwable;
		}
		
		public Throwable getThrowable() {
			return throwable;
		}
	}
	
	boolean handle(ErrorEvent event);
}
