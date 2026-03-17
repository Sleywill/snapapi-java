package pics.snapapi;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pics.snapapi.models.PdfOptions;
import pics.snapapi.models.ScrapeOptions;
import pics.snapapi.models.ScreenshotOptions;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for error mapping — verifies that HTTP status codes and error bodies
 * are correctly converted to typed exception subclasses.
 */
class ErrorMappingTest {

    private MockWebServer server;
    private SnapAPIClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        client = SnapAPIClient.builder()
            .apiKey("sk_test_errors")
            .baseUrl(server.url("/").toString())
            .timeoutSecs(10)
            .maxRetries(0)
            .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    // -------------------------------------------------------------------------
    // HTTP 401
    // -------------------------------------------------------------------------

    @Test
    void http401MapsToAuthException() {
        server.enqueue(new MockResponse()
            .setResponseCode(401)
            .setBody("{\"message\":\"Invalid API key\",\"error\":\"UNAUTHORIZED\"}"));

        AuthException ex = assertThrows(AuthException.class,
            () -> client.ping());

        assertEquals(401, ex.getStatusCode());
        assertEquals("UNAUTHORIZED", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Invalid API key"));
    }

    @Test
    void http403MapsToAuthException() {
        server.enqueue(new MockResponse()
            .setResponseCode(403)
            .setBody("{\"message\":\"Email not verified\",\"error\":\"FORBIDDEN\"}"));

        assertThrows(AuthException.class, () -> client.ping());
    }

    // -------------------------------------------------------------------------
    // HTTP 402
    // -------------------------------------------------------------------------

    @Test
    void http402MapsToQuotaException() {
        server.enqueue(new MockResponse()
            .setResponseCode(402)
            .setBody("{\"message\":\"Monthly quota exceeded\",\"error\":\"QUOTA_EXCEEDED\"}"));

        QuotaException ex = assertThrows(QuotaException.class,
            () -> client.screenshot(
                ScreenshotOptions.builder().url("https://example.com").build()));

        assertEquals(402, ex.getStatusCode());
        assertEquals("QUOTA_EXCEEDED", ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // HTTP 422
    // -------------------------------------------------------------------------

    @Test
    void http422MapsToValidationException() {
        server.enqueue(new MockResponse()
            .setResponseCode(422)
            .setBody("{\"message\":\"url is required\",\"error\":\"VALIDATION_ERROR\"}"));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> client.screenshot(
                ScreenshotOptions.builder().url("https://example.com").build()));

        assertEquals(422, ex.getStatusCode());
        assertEquals("VALIDATION_ERROR", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("url is required"));
    }

    @Test
    void validationExceptionHasEmptyFieldsByDefault() {
        ValidationException ex = new ValidationException("invalid params");
        assertNotNull(ex.getFields());
        assertTrue(ex.getFields().isEmpty());
    }

    @Test
    void validationExceptionWithFields() {
        java.util.Map<String, String> fields = new java.util.HashMap<>();
        fields.put("url", "must be a valid URL");
        fields.put("format", "must be one of: png, jpeg, webp");

        ValidationException ex = new ValidationException("Validation failed", fields);
        assertEquals(2, ex.getFields().size());
        assertEquals("must be a valid URL", ex.getFields().get("url"));
    }

    // -------------------------------------------------------------------------
    // HTTP 429
    // -------------------------------------------------------------------------

    @Test
    void http429MapsToRateLimitException() {
        server.enqueue(new MockResponse()
            .setResponseCode(429)
            .setHeader("Retry-After", "10")
            .setBody("{\"message\":\"Too many requests\",\"error\":\"RATE_LIMITED\"}"));

        RateLimitException ex = assertThrows(RateLimitException.class,
            () -> client.screenshot(
                ScreenshotOptions.builder().url("https://example.com").build()));

        assertEquals(429, ex.getStatusCode());
        assertEquals(10.0, ex.getRetryAfter(), 0.01);
    }

    @Test
    void http429WithNoRetryAfterHeaderUsesDefault() {
        server.enqueue(new MockResponse()
            .setResponseCode(429)
            .setBody("{\"message\":\"Too many requests\"}"));

        RateLimitException ex = assertThrows(RateLimitException.class,
            () -> client.screenshot(
                ScreenshotOptions.builder().url("https://example.com").build()));

        // Should use default of 1.0s when header absent
        assertEquals(1.0, ex.getRetryAfter(), 0.01);
    }

    @Test
    void http429WithFractionalRetryAfter() {
        server.enqueue(new MockResponse()
            .setResponseCode(429)
            .setHeader("Retry-After", "2.5")
            .setBody("{\"message\":\"Too many requests\"}"));

        RateLimitException ex = assertThrows(RateLimitException.class,
            () -> client.screenshot(
                ScreenshotOptions.builder().url("https://example.com").build()));

        assertEquals(2.5, ex.getRetryAfter(), 0.001);
    }

    // -------------------------------------------------------------------------
    // HTTP 5xx
    // -------------------------------------------------------------------------

    @Test
    void http500MapsToSnapAPIException() {
        server.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("{\"message\":\"Internal server error\",\"error\":\"INTERNAL_ERROR\"}"));

        SnapAPIException ex = assertThrows(SnapAPIException.class,
            () -> client.ping());

        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void http503MapsToSnapAPIException() {
        server.enqueue(new MockResponse()
            .setResponseCode(503)
            .setBody("{\"message\":\"Service unavailable\"}"));

        SnapAPIException ex = assertThrows(SnapAPIException.class,
            () -> client.getUsage());

        assertEquals(503, ex.getStatusCode());
    }

    // -------------------------------------------------------------------------
    // Error body parsing
    // -------------------------------------------------------------------------

    @Test
    void parsesMessageFromErrorBody() {
        server.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("{\"message\":\"Custom error message\",\"error\":\"CUSTOM_ERROR\"}"));

        SnapAPIException ex = assertThrows(SnapAPIException.class,
            () -> client.ping());

        assertTrue(ex.getMessage().contains("Custom error message"));
    }

    @Test
    void handlesEmptyErrorBody() {
        server.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody(""));

        SnapAPIException ex = assertThrows(SnapAPIException.class,
            () -> client.ping());

        assertNotNull(ex.getMessage());
        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void handlesNonJsonErrorBody() {
        server.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error"));

        SnapAPIException ex = assertThrows(SnapAPIException.class,
            () -> client.ping());

        assertEquals(500, ex.getStatusCode());
    }

    // -------------------------------------------------------------------------
    // Exception constructors and accessors
    // -------------------------------------------------------------------------

    @Test
    void snapAPIExceptionAccessors() {
        SnapAPIException ex = new SnapAPIException("Something went wrong", "TEST_CODE", 418, "detail info");

        assertTrue(ex.getMessage().contains("Something went wrong"));
        assertEquals("TEST_CODE", ex.getErrorCode());
        assertEquals(418, ex.getStatusCode());
        assertEquals("detail info", ex.getDetails());
    }

    @Test
    void snapAPIExceptionShortConstructor() {
        SnapAPIException ex = new SnapAPIException("Simple error");
        assertNotNull(ex.getMessage());
        assertEquals(500, ex.getStatusCode());
        assertNull(ex.getDetails());
    }

    @Test
    void authExceptionWithDetails() {
        AuthException ex = new AuthException("Invalid key", "Key revoked at 2026-01-01");
        assertEquals(401, ex.getStatusCode());
        assertEquals("UNAUTHORIZED", ex.getErrorCode());
        assertEquals("Key revoked at 2026-01-01", ex.getDetails());
    }

    @Test
    void quotaExceptionWithDetails() {
        QuotaException ex = new QuotaException("Quota exceeded", "Reset on 2026-04-01");
        assertEquals(402, ex.getStatusCode());
        assertEquals("QUOTA_EXCEEDED", ex.getErrorCode());
        assertEquals("Reset on 2026-04-01", ex.getDetails());
    }

    @Test
    void networkExceptionPreservesCause() {
        IOException cause = new IOException("Connection refused");
        NetworkException ex = new NetworkException("Network error", cause);
        assertEquals(cause, ex.getCause());
        assertEquals(0, ex.getStatusCode());
    }

    @Test
    void timeoutExceptionHasZeroStatusCode() {
        TimeoutException ex = new TimeoutException("Timed out after 60s");
        assertEquals(0, ex.getStatusCode());
        assertEquals("TIMEOUT", ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // Multiple endpoints mapping
    // -------------------------------------------------------------------------

    @Test
    void scrapeEndpoint401MapsToAuth() {
        server.enqueue(new MockResponse()
            .setResponseCode(401)
            .setBody("{\"message\":\"Unauthorized\"}"));

        assertThrows(AuthException.class,
            () -> client.scrape(
                ScrapeOptions.builder().url("https://example.com").build()));
    }

    @Test
    void pdfEndpoint402MapsToQuota() {
        server.enqueue(new MockResponse()
            .setResponseCode(402)
            .setBody("{\"message\":\"Quota exceeded\"}"));

        assertThrows(QuotaException.class,
            () -> client.pdf(PdfOptions.builder().url("https://example.com").build()));
    }
}
