package pics.snapapi.models;

import java.util.List;
import java.util.Map;

/**
 * Minimal JSON object serialiser — avoids a runtime dependency on Jackson or Gson.
 *
 * <p>Only supports the types used in SnapAPI payloads:
 * String, Number, Boolean, null, List&lt;String&gt;, Map&lt;String, ?&gt;.
 *
 * <p>Compatible with Java 11+.
 */
public final class JsonBuilder {

    private final StringBuilder sb = new StringBuilder("{");
    private boolean first = true;

    /** Append a field only when value is not null. */
    public JsonBuilder field(String key, Object value) {
        if (value == null) return this;
        if (!first) sb.append(',');
        first = false;
        sb.append('"').append(escape(key)).append("\":");
        appendValue(value);
        return this;
    }

    /** Build and return the JSON string. */
    public String build() {
        return sb.toString() + "}";
    }

    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void appendValue(Object v) {
        if (v instanceof String) {
            sb.append('"').append(escape((String) v)).append('"');
        } else if (v instanceof Boolean) {
            sb.append((Boolean) v ? "true" : "false");
        } else if (v instanceof Number) {
            sb.append(v);
        } else if (v instanceof List) {
            sb.append('[');
            boolean f = true;
            for (Object item : (List<?>) v) {
                if (!f) sb.append(',');
                f = false;
                appendValue(item);
            }
            sb.append(']');
        } else if (v instanceof Map) {
            sb.append('{');
            boolean f = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                if (!f) sb.append(',');
                f = false;
                sb.append('"').append(escape(e.getKey().toString())).append("\":");
                appendValue(e.getValue());
            }
            sb.append('}');
        } else {
            // Fallback: quote it
            sb.append('"').append(escape(v.toString())).append('"');
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
