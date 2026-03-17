package pics.snapapi;

import java.util.Collections;
import java.util.Map;

/**
 * Thrown when the API returns HTTP 422 Unprocessable Entity.
 *
 * <p>Inspect {@link #getFields()} for per-field error details.
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
     * @return Unmodifiable map of field name → error message. Never {@code null}.
     */
    public Map<String, String> getFields() {
        return fields;
    }
}
