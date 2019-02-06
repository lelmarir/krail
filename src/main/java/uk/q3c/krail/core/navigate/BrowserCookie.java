package uk.q3c.krail.core.navigate;

import com.vaadin.data.HasValue;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Objects;

import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;

import elemental.json.JsonArray;

/**
 * A helper that provides access to browser cookies.
 *
 * @author Matti Tahvonen
 */
public class BrowserCookie {

	public interface Callback {

		void onValueDetected(String value);
	}

	private static String encode(String value) throws UnsupportedEncodingException {
		if (value == null) {
			return "";
		} else {
			return URLEncoder.encode(value, "UTF-8");
		}
	}

	private static String decode(String string) throws UnsupportedEncodingException {
		return URLDecoder.decode(string, "UTF-8");
	}

	public static void setCookie(String key, String value) throws RuntimeException {
		setCookie(key, value, "/", LocalDateTime.now().plusYears(10l));
	}

	public static void setCookie(String key, String value, LocalDateTime expirationTime) {
		setCookie(key, value, null, expirationTime);
	}

	public static void setCookie(String key, String value, String path) throws RuntimeException {
		setCookie(key, value, path, null);
	}

	private static String toCookieGMTDate(LocalDateTime expirationTime) {
		ZonedDateTime zdt = ZonedDateTime.of(expirationTime, ZoneOffset.UTC);
		String expires = zdt.format(DateTimeFormatter.RFC_1123_DATE_TIME);
		return expires;
	}

	public static void setCookie(String key, String value, String path, LocalDateTime expirationTime) {
		Objects.requireNonNull(key);

		StringBuilder sb = new StringBuilder("document.cookie = \"");

		try {
			sb.append(encode(key) + "=" + encode(value));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		if (path != null) {
			sb.append(String.format("; path=%s", path));
		}

		if (expirationTime != null) {
			String expires = toCookieGMTDate(expirationTime);
			sb.append(String.format("; Expires=%s", expires));
		}

		sb.append("\"");

		JavaScript.getCurrent().execute(sb.toString());
	}
	
	public static void removeCookie(String key) {
		setCookie(key, null, LocalDateTime.of(1970, 1, 1, 0, 0));
	}

	public static void getCookieValueAsync(String key, final Callback callback) {
		final String callbackid = "viritincookiecb" + UUID.randomUUID().toString().substring(0, 8);
		JavaScript.getCurrent().addFunction(callbackid, new JavaScriptFunction() {
			private static final long serialVersionUID = -3426072590182105863L;

			@Override
			public void call(JsonArray arguments) {
				JavaScript.getCurrent().removeFunction(callbackid);
				if (arguments.length() == 0) {
					callback.onValueDetected(null);
				} else {
					try {
						callback.onValueDetected(decode(arguments.getString(0)));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});

		JavaScript.getCurrent().execute(String.format(
				"var nameEQ = \"%2$s=\";var ca = document.cookie.split(';');for(var i=0;i < ca.length;i++) {var c = ca[i];while (c.charAt(0)==' ') c = c.substring(1,c.length); if (c.indexOf(nameEQ) == 0) {%1$s( c.substring(nameEQ.length,c.length)); return;};} %1$s();",
				callbackid, key));
	}

	/**
	 *
	 * Binds a HasValue&lt;V&gt; to a cookie that lives for a month. The cookies
	 * value is updated via a ValueChangeListener.
	 *
	 * @param       <V> The value-type of the HasValue&lt;&gt;
	 * @param field The HasValue&lt;V&gt; that gets bound.
	 * @param name  The name of the cookie
	 * @param cb    A BrowserCookie.Callback that gets called with the actual value
	 *              of the cookie. The value is guaranteed to be not null.
	 *
	 * @throws IllegalArgumentException if field or name are null or if name is
	 *                                  empty.
	 */
	public static <V> void bindValueToCookie(HasValue<V> field, String name, Callback cb) {
		if (Objects.isNull(name) || name.isEmpty()) {
			throw new IllegalArgumentException("Name must not be null or empty");
		}
		if (Objects.isNull(field)) {
			throw new IllegalArgumentException("Field must not be null");
		}

		getCookieValueAsync(name, (v) -> {
			if (v != null) {
				cb.onValueDetected(v);
			}
		});

		field.addValueChangeListener((event) -> {
			setCookie(name, event.getValue().toString(), LocalDateTime.now().plusMonths(1l));
		});
	}

	/**
	 * Binds a HasValue&lt;String&gt; to a cookie that lives for a month. The
	 * cookies value is updated via a ValueChangeListener. Its crrent value is
	 * copied into the HasValue&lt;String&gt;.
	 *
	 * @param field The HasValue&lt;String&gt; that gets bound.
	 * @param name  The name of the cookie
	 *
	 * @throws IllegalArgumentException if field or name are null or if name is
	 *                                  empty.
	 */
	public static void bindValueToCookie(HasValue<String> field, String name) {
		if (Objects.isNull(name) || name.isEmpty()) {
			throw new IllegalArgumentException("Name must not be null or empty");
		}
		if (Objects.isNull(field)) {
			throw new IllegalArgumentException("Field must not be null");
		}

		getCookieValueAsync(name, (v) -> {
			if (v != null) {
				field.setValue(v);
			}
		});

		field.addValueChangeListener((event) -> {
			setCookie(name, event.getValue(), LocalDateTime.now().plusMonths(1l));
		});
	}
}