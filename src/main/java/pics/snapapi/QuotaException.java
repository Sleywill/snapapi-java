package pics.snapapi;

/**
 * Thrown when the account has exhausted its monthly API quota (HTTP 402).
 *
 * <p>Upgrade your plan at <a href="https://snapapi.pics/dashboard">snapapi.pics/dashboard</a>.
 */
public class QuotaException extends SnapAPIException {

    public QuotaException(String message) {
        super(message, "QUOTA_EXCEEDED", 402, null);
    }

    public QuotaException(String message, String details) {
        super(message, "QUOTA_EXCEEDED", 402, details);
    }
}
