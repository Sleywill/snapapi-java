package pics.snapapi;

/**
 * Thrown on network-level failures (DNS failure, connection refused, etc.).
 */
public class NetworkException extends SnapAPIException {

    public NetworkException(String message, Throwable cause) {
        super(message, "NETWORK_ERROR", 0, null);
        initCause(cause);
    }

    public NetworkException(String message) {
        super(message, "NETWORK_ERROR", 0, null);
    }
}
