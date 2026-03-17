package pics.snapapi.exceptions;

import pics.snapapi.SnapAPIException;

import java.util.Collections;
import java.util.Map;

/**
 * Thrown when the API returns HTTP 422 Unprocessable Entity.
 *
 * <p>This exception mirrors {@link pics.snapapi.ValidationException} and is provided
 * in the {@code pics.snapapi.exceptions} package for structural completeness.
 *
 * <pre>{@code
 * try {
 *     byte[] png = client.screenshot(opts);
 * } catch (ValidationException e) {
 *     System.err.println("Invalid request: " + e.getMessage());
 * }
 * }</pre>
 */
public class ValidationException extends SnapAPIException {

    private final Map<String, String> fields;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 422, null);
        this.fields = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, String> fields) {
        super(message, "VALIDATION_ERROR", 422, null);
        this.fields = fields != null ? Collections.unmodifiableMap(fields) : Collections.emptyMap();
    }

    /**
     * Per-field validation error messages, if provided by the API.
     *
     * @return Unmodifiable map of field name to error message. Never {@code null}.
     */
    public Map<String, String> getFields() {
        return fields;
    }
}
