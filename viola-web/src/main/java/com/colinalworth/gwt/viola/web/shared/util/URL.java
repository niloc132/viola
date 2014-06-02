package com.colinalworth.gwt.viola.web.shared.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Utility class for the encoding and decoding URLs in their entirety or by
 * their individual components.
 *
 * <h3>Required Module</h3>
 * Modules that use this class should inherit
 * <code>com.google.gwt.http.HTTP</code>.
 *
 * {@gwt.include com/google/gwt/examples/http/InheritsExample.gwt.xml}
 */
@SuppressWarnings("NonJREEmulationClassesInClientCode")
public final class URL {

	/**
	 * Returns a string where all URL escape sequences have been converted back to
	 * their original character representations.
	 *
	 * @param encodedURL string containing encoded URL encoded sequences
	 * @return string with no encoded URL encoded sequences
	 *
	 * @throws NullPointerException if encodedURL is <code>null</code>
	 */
	public static String decode(String encodedURL) {
		StringValidator.throwIfNull("encodedURL", encodedURL);
		try {
			return URLDecoder.decode(encodedURL.replaceAll("\\+", "%2B"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//not possible
			return null;
		}
	}

	/**
	 * Returns a string where all URL component escape sequences have been
	 * converted back to their original character representations.
	 * <p>
	 * Note: this method will convert the space character escape short form, '+',
	 * into a space. It should therefore only be used for query-string parts.
	 *
	 * @param encodedURLComponent string containing encoded URL component
	 *        sequences
	 * @return string with no encoded URL component encoded sequences
	 *
	 * @throws NullPointerException if encodedURLComponent is <code>null</code>
	 *
	 * @deprecated Use {@link #decodeQueryString(String)}
	 */
	@Deprecated
	public static String decodeComponent(String encodedURLComponent) {
		return decodeQueryString(encodedURLComponent);
	}

	/**
	 * Returns a string where all URL component escape sequences have been
	 * converted back to their original character representations.
	 *
	 * @param encodedURLComponent string containing encoded URL component
	 *        sequences
	 * @param fromQueryString if <code>true</code>, +'s will be turned into
	 *        spaces, otherwise they'll be kept as-is.
	 * @return string with no encoded URL component encoded sequences
	 *
	 * @throws NullPointerException if encodedURLComponent is <code>null</code>
	 *
	 * @deprecated Use {@link #decodeQueryString(String)},
	 *             {@link #decodePathSegment(String)}
	 */
	@Deprecated
	public static String decodeComponent(String encodedURLComponent,
										 boolean fromQueryString) {
		StringValidator.throwIfNull("encodedURLComponent", encodedURLComponent);
		return fromQueryString ? decodeQueryString(encodedURLComponent)
				: decodePathSegment(encodedURLComponent);
	}

	/**
	 * Returns a string where all URL component escape sequences have been
	 * converted back to their original character representations.
	 *
	 * @param encodedURLComponent string containing encoded URL component
	 *          sequences
	 * @return string with no encoded URL component encoded sequences
	 *
	 * @throws NullPointerException if encodedURLComponent is <code>null</code>
	 */
	public static String decodePathSegment(String encodedURLComponent) {
		StringValidator.throwIfNull("encodedURLComponent", encodedURLComponent);
		try {
			return URLDecoder.decode(encodedURLComponent.replaceAll("\\+", "%2B"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//not possible
			return null;
		}
	}

	/**
	 * Returns a string where all URL component escape sequences have been
	 * converted back to their original character representations.
	 * <p>
	 * Note: this method will convert the space character escape short form, '+',
	 * into a space. It should therefore only be used for query-string parts.
	 *
	 * @param encodedURLComponent string containing encoded URL component
	 *          sequences
	 * @return string with no encoded URL component encoded sequences
	 *
	 * @throws NullPointerException if encodedURLComponent is <code>null</code>
	 */
	public static String decodeQueryString(String encodedURLComponent) {
		StringValidator.throwIfNull("encodedURLComponent", encodedURLComponent);
		try {
			return URLDecoder.decode(encodedURLComponent, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//not possible
			return null;
		}
	}

	/**
	 * Returns a string where all characters that are not valid for a complete URL
	 * have been escaped. The escaping of a character is done by converting it
	 * into its UTF-8 encoding and then encoding each of the resulting bytes as a
	 * %xx hexadecimal escape sequence.
	 *
	 * <p>
	 * The following character sets are <em>not</em> escaped by this method:
	 * <ul>
	 * <li>ASCII digits or letters</li>
	 * <li>ASCII punctuation characters:
	 *
	 * <pre>
	 * - _ . ! ~ * ' ( )
	 * </pre>
	 *
	 * </li>
	 * <li>URL component delimiter characters:
	 *
	 * <pre>
	 * ; / ? : &amp; = + $ , #
	 * </pre>
	 *
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @param decodedURL a string containing URL characters that may require
	 *        encoding
	 * @return a string with all invalid URL characters escaped
	 *
	 * @throws NullPointerException if decodedURL is <code>null</code>
	 */
	public static String encode(String decodedURL) {
		StringValidator.throwIfNull("decodedURL", decodedURL);
		String[] parts = decodedURL.split("\\?", 2);
		String path = encodePathSegment(parts[0])
				.replaceAll("%3A", ":")
				.replaceAll("%2F", "/")
				.replaceAll("%2B", "+")
				.replaceAll("%3B", ";");
		if (parts.length == 1) {
			return path;
		}
		return path + "?" + encodeQueryString(parts[1])
				.replaceAll("%40", "@")
				.replaceAll("%26", "&")
				.replaceAll("%3D", "=")
				.replaceAll("%2B", "+")
				.replaceAll("%24", "\\$")
				.replaceAll("%2C", ",")
				.replaceAll("%23", "#");
	}

	/**
	 * Returns a string where all characters that are not valid for a URL
	 * component have been escaped. The escaping of a character is done by
	 * converting it into its UTF-8 encoding and then encoding each of the
	 * resulting bytes as a %xx hexadecimal escape sequence.
	 * <p>
	 * Note: this method will convert any the space character into its escape
	 * short form, '+' rather than %20. It should therefore only be used for
	 * query-string parts.
	 *
	 * <p>
	 * The following character sets are <em>not</em> escaped by this method:
	 * <ul>
	 * <li>ASCII digits or letters</li>
	 * <li>ASCII punctuation characters:
	 *
	 * <pre>- _ . ! ~ * ' ( )</pre>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Notice that this method <em>does</em> encode the URL component delimiter
	 * characters:<blockquote>
	 *
	 * <pre>
	 * ; / ? : &amp; = + $ , #
	 * </pre>
	 *
	 * </blockquote>
	 * </p>
	 *
	 * @param decodedURLComponent a string containing invalid URL characters
	 * @return a string with all invalid URL characters escaped
	 *
	 * @throws NullPointerException if decodedURLComponent is <code>null</code>
	 *
	 * @deprecated Use {@link #encodeQueryString(String)}
	 */
	@Deprecated
	public static String encodeComponent(String decodedURLComponent) {
		return encodeQueryString(decodedURLComponent);
	}

	/**
	 * Returns a string where all characters that are not valid for a URL
	 * component have been escaped. The escaping of a character is done by
	 * converting it into its UTF-8 encoding and then encoding each of the
	 * resulting bytes as a %xx hexadecimal escape sequence.
	 *
	 * <p>
	 * The following character sets are <em>not</em> escaped by this method:
	 * <ul>
	 * <li>ASCII digits or letters</li>
	 +   * <li>ASCII punctuation characters:
	 *
	 * <pre>- _ . ! ~ * ' ( )</pre>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Notice that this method <em>does</em> encode the URL component delimiter
	 * characters:<blockquote>
	 *
	 * <pre>
	 * ; / ? : &amp; = + $ , #
	 * </pre>
	 *
	 * </blockquote>
	 * </p>
	 *
	 * @param decodedURLComponent a string containing invalid URL characters
	 * @param queryStringSpaces if <code>true</code>, spaces will be encoded as
	 *          +'s.
	 * @return a string with all invalid URL characters escaped
	 *
	 * @throws NullPointerException if decodedURLComponent is <code>null</code>
	 *
	 * @deprecated Use {@link #encodeQueryString(String)},
	 *             {@link #encodePathSegment(String)}
	 */
	@Deprecated
	public static String encodeComponent(String decodedURLComponent,
										 boolean queryStringSpaces) {
		StringValidator.throwIfNull("decodedURLComponent", decodedURLComponent);
		return queryStringSpaces ? encodeQueryString(decodedURLComponent)
				: encodePathSegment(decodedURLComponent);
	}

	/**
	 * Returns a string where all characters that are not valid for a URL
	 * component have been escaped. The escaping of a character is done by
	 * converting it into its UTF-8 encoding and then encoding each of the
	 * resulting bytes as a %xx hexadecimal escape sequence.
	 *
	 * <p>
	 * The following character sets are <em>not</em> escaped by this method:
	 * <ul>
	 * <li>ASCII digits or letters</li>
	 * <li>ASCII punctuation characters:
	 *
	 * <pre>- _ . ! ~ * ' ( )</pre>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Notice that this method <em>does</em> encode the URL component delimiter
	 * characters:<blockquote>
	 *
	 * <pre>
	 * ; / ? : &amp; = + $ , #
	 * </pre>
	 *
	 * </blockquote>
	 * </p>
	 *
	 * @param decodedURLComponent a string containing invalid URL characters
	 * @return a string with all invalid URL characters escaped
	 *
	 * @throws NullPointerException if decodedURLComponent is <code>null</code>
	 */
	public static String encodePathSegment(String decodedURLComponent) {
		StringValidator.throwIfNull("decodedURLComponent", decodedURLComponent);
		try {
			return URLEncoder.encode(decodedURLComponent, "UTF-8")
					.replaceAll("\\+", "%20")
					.replaceAll("\\%21", "!")
					.replaceAll("\\%27", "'")
					.replaceAll("\\%28", "(")
					.replaceAll("\\%29", ")")
					.replaceAll("\\%7E", "~");
		} catch (UnsupportedEncodingException e) {
			//not possible
			return null;
		}
	}

	/**
	 * Returns a string where all characters that are not valid for a URL
	 * component have been escaped. The escaping of a character is done by
	 * converting it into its UTF-8 encoding and then encoding each of the
	 * resulting bytes as a %xx hexadecimal escape sequence.
	 * <p>
	 * Note: this method will convert any the space character into its escape
	 * short form, '+' rather than %20. It should therefore only be used for
	 * query-string parts.
	 *
	 * <p>
	 * The following character sets are <em>not</em> escaped by this method:
	 * <ul>
	 * <li>ASCII digits or letters</li>
	 * <li>ASCII punctuation characters:
	 *
	 * <pre>- _ . ! ~ * ' ( )</pre>
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * Notice that this method <em>does</em> encode the URL component delimiter
	 * characters:<blockquote>
	 *
	 * <pre>
	 * ; / ? : &amp; = + $ , #
	 * </pre>
	 *
	 * </blockquote>
	 * </p>
	 *
	 * @param decodedURLComponent a string containing invalid URL characters
	 * @return a string with all invalid URL characters escaped
	 *
	 * @throws NullPointerException if decodedURLComponent is <code>null</code>
	 */
	public static String encodeQueryString(String decodedURLComponent) {
		StringValidator.throwIfNull("decodedURLComponent", decodedURLComponent);
		try {
			return URLEncoder.encode(decodedURLComponent, "UTF-8")
//					.replaceAll("\\+", "%20")
					.replaceAll("\\%21", "!")
					.replaceAll("\\%27", "'")
					.replaceAll("\\%28", "(")
					.replaceAll("\\%29", ")")
					.replaceAll("\\%7E", "~");
		} catch (UnsupportedEncodingException e) {
			//not possible
			return null;
		}
	}

	private URL() {
	}

	static class StringValidator {
		/**
		 * Returns true if the string is empty or null.
		 *
		 * @param string to test if null or empty
		 *
		 * @return true if the string is empty or null
		 */
		public static boolean isEmptyOrNullString(String string) {
			return (string == null) || (0 == string.trim().length());
		}

		/**
		 * Throws if <code>value</code> is <code>null</code> or empty. This method
		 * ignores leading and trailing whitespace.
		 *
		 * @param name the name of the value, used in error messages
		 * @param value the string value that needs to be validated
		 *
		 * @throws IllegalArgumentException if the string is empty, or all whitespace
		 * @throws NullPointerException if the string is <code>null</code>
		 */
		public static void throwIfEmptyOrNull(String name, String value) {
			assert (name != null);
			assert (name.trim().length() != 0);

			throwIfNull(name, value);

			if (0 == value.trim().length()) {
				throw new IllegalArgumentException(name + " cannot be empty");
			}
		}

		/**
		 * Throws a {@link NullPointerException} if the value is <code>null</code>.
		 *
		 * @param name the name of the value, used in error messages
		 * @param value the value that needs to be validated
		 *
		 * @throws NullPointerException if the value is <code>null</code>
		 */
		public static void throwIfNull(String name, Object value) {
			if (null == value) {
				throw new NullPointerException(name + " cannot be null");
			}
		}

		private StringValidator() {
		}
	}

}