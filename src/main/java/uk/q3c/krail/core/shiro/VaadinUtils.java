package uk.q3c.krail.core.shiro;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import com.vaadin.server.VaadinSession;

public abstract class VaadinUtils {

	private static class ConvertingFuture<T1, T2> implements Future<T2> {

		private final Future<T1> future;
		private final Function<T1, T2> conversion;

		public ConvertingFuture(Future<T1> future, Function<T1, T2> conversion) {
			super();
			this.future = future;
			this.conversion = conversion;
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			return future.cancel(mayInterruptIfRunning);
		}

		public boolean isCancelled() {
			return future.isCancelled();
		}

		public boolean isDone() {
			return future.isDone();
		}

		public T2 get() throws InterruptedException, ExecutionException {
			return conversion.apply(future.get());
		}

		public T2 get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return conversion.apply(future.get(timeout, unit));
		}

	};

	public static <T> Future<T> runInSession(VaadinSession session, Function<VaadinSession, T> function)
			throws InterruptedException, ExecutionException {
		final AtomicReference<T> result = new AtomicReference<T>(null);
		if(!session.hasLock()) {
			Future<Void> future = session.access(() -> {
				result.set(function.apply(session));
			});
			return new ConvertingFuture<>(future, v -> result.get());
		}else {
			return CompletableFuture.completedFuture(function.apply(session));
		}
	}
	
	public static <T> Future<T> runInSessionOrThrow(VaadinSession session, Function<VaadinSession, T> function) {
		try {
			return runInSession(session, function);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
