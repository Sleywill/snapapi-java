package pics.snapapi.exceptions;

import pics.snapapi.SnapAPIException;

/**
 * Thrown when the account has exhausted its monthly API quota (HTTP 402).
 *
 * <p>This exception mirrors {@link pics.snapapi.QuotaException} and is provided
 * in the {@code pics.snapapi.exceptions} package for structural completeness.
 *
 * <p>Upgrade your plan at <a href="https://snapapi.pics/dashboard">snapapi.pics/dashboard</a>.
 *
 * <pre>{@code
 * try {
 *     byte[] png = client.screenshot(opts);
 * } catch (QuotaException e) {
 *     System.err.println("Quota exhausted. Upgrade at snapapi.pics/dashboard");
 * }
 * }</pre>
 */
public class QuotaException extends SnapAPIException {

    public QuotaException(String message) {
        super(message, "QUOTA_EXCEEDED", 402, null);
    }

    public QuotaException(String message, String details) {
        super(message, "QUOTA_EXCEEDED", 402, details);
    }
}
