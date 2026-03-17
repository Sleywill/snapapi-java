package pics.snapapi.http;

import pics.snapapi.AuthException;
import pics.snapapi.NetworkException;
import pics.snapapi.QuotaException;
import pics.snapapi.RateLimitException;
import pics.snapapi.SnapAPIException;
import pics.snapapi.TimeoutException;
import pics.snapapi.ValidationException;
import pics.snapapi.retry.RetryPolicy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal HTTP transport layer backed by {@link java.net.http.HttpClient} (Java 11+).
 *
 * <p>Handles:
 * <ul>
 *   <li>JSON serialisation / deserialisation (manual, no external library)</li>
 *   <li>Authentication headers ({@code X-Api-Key}, {@code Authorization: Bearer})</li>
 *   <li>Retry with exponential backoff via {@link RetryPolicy}</li>
 *   <li>Error mapping to typed {@link SnapAPIException} subclasses</li>
 * </ul>
 *
 * <p>This class is an internal implementation detail and not part of the public API.
 */
public final class HttpClient {

    /** SDK version injected into the {@code User-Agent} header. */
    public static final String SDK_VERSION = "1.0.0";

    private final String                  baseUrl;
    private final String                  apiKey;
    private final java.net.http.HttpClient httpClient;
    private final RetryPolicy             retryPolicy;

    /**
     * Construct an {@link HttpClient}.
     *
     * @param baseUrl     API base URL (no trailing slash).
     * @param apiKey      SnapAPI API key.
     * @param timeoutSecs Request timeout in seconds.
     * @param retryPolicy Retry configuration.
     */
    public HttpClient(String baseUrl, String apiKey, int timeoutSecs, RetryPolicy retryPolicy) {
        this.baseUrl     = baseUrl.replaceAll("/+$", "");
        this.apiKey      = apiKey;
        this.retryPolicy = retryPolicy;
        this.httpClient  = java.net.http.HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSecs))
            .build();
    }

    // -------------------------------------------------------------------------
    // Public transport methods
    // -------------------------------------------------------------------------

    /**
     * Execute a GET request.
     *
     * @param path API path, e.g. {@code "/v1/usage"}.
     * @return Response body bytes.
     * @throws SnapAPIException on any API or network error.
     */
    public byte[] get(String path) {
        return executeWithRetry("GET", path, null);
    }

    /**
     * Execute a POST request with a JSON body.
     *
     * @param path    API path.
     * @param payload JSON string body.
     * @return Response body bytes.
     * @throws SnapAPIException on any API or network error.
     */
    public byte[] post(String path, String payload) {
        return executeWithRetry("POST", path, payload);
    }

    /**
     * Execute a DELETE request.
     *
     * @param path API path.
     * @return Response body bytes.
     * @throws SnapAPIException on any API or network error.
     */
    public byte[] delete(String path) {
        return executeWithRetry("DELETE", path, null);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private byte[] executeWithRetry(String method, String path, String body) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return execute(method, path, body);
            } catch (SnapAPIException ex) {
                if (!retryPolicy.shouldRetry(ex, attempt)) throw ex;
                long delayMs = retryPolicy.computeDelayMs(attempt, ex);
                sleep(delayMs);
            }
        }
    }

    private byte[] execute(String method, String path, String body) {
        URI uri = URI.create(baseUrl + path);

        HttpRequest.Builder rb = HttpRequest.newBuilder(uri)
            .header("X-Api-Key",      apiKey)
            .header("Authorization",  "Bearer " + apiKey)
            .header("Content-Type",   "application/json")
            .header("Accept",         "*/*")
            .header("User-Agent",     "snapapi-java/" + SDK_VERSION);

        HttpRequest.BodyPublisher publisher = (body != null)
            ? HttpRequest.BodyPublishers.ofString(body)
            : HttpRequest.BodyPublishers.noBody();

        if ("GET".equals(method)) {
            rb.GET();
        } else if ("DELETE".equals(method)) {
            rb.method("DELETE", publisher);
        } else {
            rb.POST(publisher);
        }

        HttpRequest request = rb.build();

        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (java.net.http.HttpTimeoutException e) {
            throw new TimeoutException("Request timed out: " + e.getMessage());
        } catch (IOException e) {
            throw new NetworkException("Network error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Request interrupted: " + e.getMessage(), e);
        }

        int statusCode = response.statusCode();
        if (statusCode >= 400) {
            throw parseError(statusCode, response);
        }

        return response.body();
    }

    private SnapAPIException parseError(int statusCode, HttpResponse<byte[]> response) {
        String body = new String(response.body());

        String message = "HTTP " + statusCode;
        String code    = "HTTP_ERROR";
        String details = null;

        // Manual JSON parsing — avoids runtime dependency on Jackson/Gson
        String msgParsed     = extractJsonString(body, "message");
        String errorParsed   = extractJsonString(body, "error");
        String detailsParsed = extractJsonString(body, "details");

        if (msgParsed     != null) message = msgParsed;
        if (errorParsed   != null) code    = errorParsed.replace(" ", "_").toUpperCase();
        if (detailsParsed != null) details = detailsParsed;

        if (statusCode == 401 || statusCode == 403) {
            return new AuthException(message, details);
        } else if (statusCode == 402) {
            return new QuotaException(message, details);
        } else if (statusCode == 422) {
            return new ValidationException(message);
        } else if (statusCode == 429) {
            double retryAfter = 1.0;
            String raHeader = response.headers().firstValue("Retry-After")
                .orElse(response.headers().firstValue("retry-after").orElse(null));
            if (raHeader != null) {
                try {
                    retryAfter = Double.parseDouble(raHeader.trim());
                } catch (NumberFormatException ignored) {
                    // use default 1.0
                }
            }
            return new RateLimitException(message, retryAfter);
        } else {
            return new SnapAPIException(message, code, statusCode, details);
        }
    }

    /**
     * Minimal JSON string-value extractor.
     * Handles {@code "key": "value"}.
     */
    private static String extractJsonString(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }

    private static void sleep(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
