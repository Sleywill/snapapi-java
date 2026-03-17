package pics.snapapi;

/**
 * Thrown when the API returns HTTP 401 Unauthorized or 403 Forbidden.
 *
 * <p>This usually means the API key is missing, invalid, or revoked.
 */
public class AuthException extends SnapAPIException {

    public AuthException(String message) {
        super(message, "UNAUTHORIZED", 401, null);
    }

    public AuthException(String message, String details) {
        super(message, "UNAUTHORIZED", 401, details);
    }
}
