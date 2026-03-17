package pics.snapapi;

/**
 * Thrown when the API returns HTTP 429 Too Many Requests.
 *
 * <p>The SDK automatically retries rate-limited requests (up to {@code maxRetries} times).
 * This exception is only raised after all retries are exhausted.
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

    /** Seconds to wait before retrying, as indicated by the {@code Retry-After} header. */
    public double getRetryAfter() {
        return retryAfter;
    }
}
