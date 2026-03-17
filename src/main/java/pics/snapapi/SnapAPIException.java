package pics.snapapi;

/**
 * Base exception for all SnapAPI SDK errors.
 *
 * <p>All SnapAPI exceptions extend this class, so you can catch it to handle
 * any SnapAPI-specific error:
 *
 * <pre>{@code
 * try {
 *     byte[] png = client.screenshot(ScreenshotOptions.builder()
 *         .url("https://example.com")
 *         .build());
 * } catch (SnapAPIException e) {
 *     System.err.println("SnapAPI error " + e.getStatusCode() + ": " + e.getMessage());
 * }
 * }</pre>
 */
public class SnapAPIException extends RuntimeException {

    private final String errorCode;
    private final int    statusCode;
    private final String details;

    /**
     * Construct a SnapAPIException.
     *
     * @param message    Human-readable description.
     * @param errorCode  Machine-readable API error code (e.g. {@code "UNAUTHORIZED"}).
     * @param statusCode HTTP status code, or 0 for network/timeout errors.
     * @param details    Optional raw detail payload from the API response.
     */
    public SnapAPIException(String message, String errorCode, int statusCode, String details) {
        super("[" + errorCode + "] " + message);
        this.errorCode  = errorCode;
        this.statusCode = statusCode;
        this.details    = details;
    }

    /** @param message Human-readable description. */
    public SnapAPIException(String message) {
        this(message, "UNKNOWN_ERROR", 500, null);
    }

    /**
     * Machine-readable API error code returned by the server,
     * e.g. {@code "UNAUTHORIZED"}, {@code "QUOTA_EXCEEDED"}.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * HTTP status code of the failed response.
     * Returns {@code 0} for network-level errors (timeout, DNS failure).
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Optional raw detail string from the API error response body.
     * May be {@code null}.
     */
    public String getDetails() {
        return details;
    }
}
