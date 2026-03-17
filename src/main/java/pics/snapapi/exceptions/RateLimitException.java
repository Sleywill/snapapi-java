package pics.snapapi.exceptions;

import pics.snapapi.SnapAPIException;

/**
 * Thrown when the API returns HTTP 429 Too Many Requests (after retries exhausted).
 *
 * <p>This exception mirrors {@link pics.snapapi.RateLimitException} and is provided
 * in the {@code pics.snapapi.exceptions} package for structural completeness.
 *
 * <p>The SDK automatically retries rate-limited requests up to {@code maxRetries} times.
 * This exception is only raised once all retry attempts are exhausted.
 *
 * <pre>{@code
 * try {
 *     byte[] png = client.screenshot(opts);
 * } catch (RateLimitException e) {
 *     System.err.printf("Rate limited. Retry after %.1f seconds.%n", e.getRetryAfter());
 * }
 * }</pre>
 */
public class RateLimitException extends SnapAPIException {

    private final double retryAfter;

    /**
     * @param message    Human-readable error description.
     * @param retryAfter Seconds to wait before the next attempt (from {@code Retry-After} header).
     */
    public RateLimitException(String message, double retryAfter) {
        super(message, "RATE_LIMITED", 429, null);
        this.retryAfter = retryAfter;
    }

    /**
     * Seconds to wait before retrying, as indicated by the {@code Retry-After} header.
     * Defaults to {@code 1.0} when the header is absent.
     */
    public double getRetryAfter() {
        return retryAfter;
    }
}
