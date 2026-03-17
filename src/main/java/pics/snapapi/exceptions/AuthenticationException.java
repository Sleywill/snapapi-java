package pics.snapapi.exceptions;

import pics.snapapi.AuthException;

/**
 * Thrown when authentication fails (HTTP 401 or 403).
 *
 * <p>This is an alias for {@link AuthException} in the {@code pics.snapapi} package,
 * provided for consumers who prefer the longer name.
 *
 * <pre>{@code
 * try {
 *     byte[] png = client.screenshot(opts);
 * } catch (AuthenticationException e) {
 *     System.err.println("Check your API key: " + e.getMessage());
 * }
 * }</pre>
 */
public class AuthenticationException extends AuthException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, String details) {
        super(message, details);
    }
}
