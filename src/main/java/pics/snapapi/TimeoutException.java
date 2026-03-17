package pics.snapapi;

/**
 * Thrown when a request times out before the API responds.
 */
public class TimeoutException extends SnapAPIException {

    public TimeoutException(String message) {
        super(message, "TIMEOUT", 0, null);
    }
}
