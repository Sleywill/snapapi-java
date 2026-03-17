package pics.snapapi;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pics.snapapi.models.ScreenshotOptions;
import pics.snapapi.retry.RetryPolicy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for retry logic — exponential backoff, retry counts, Retry-After header.
 */
class RetryTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private SnapAPIClient clientWithRetries(int maxRetries) {
        return SnapAPIClient.builder()
            .apiKey("sk_test_retry")
            .baseUrl(server.url("/").toString())
            .timeoutSecs(10)
            .retryPolicy(RetryPolicy.builder()
                .maxRetries(maxRetries)
                .initialDelayMs(0)   // zero delay for fast tests
                .build())
            .build();
    }

    private static byte[] fakePng() {
        return new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};
    }

    private MockResponse error500() {
        return new MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Internal Server Error\"}");
    }

    private MockResponse ok200() {
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "image/png")
            .setBody(new okio.Buffer().write(fakePng()));
    }

    // -------------------------------------------------------------------------

    @Test
    void noRetryOnZeroMaxRetries() {
        server.enqueue(error500());

        SnapAPIClient client = clientWithRetries(0);
        assertThrows(SnapAPIException.class,
            () -> client.screenshot(
                ScreenshotOptions.builder().url("https://example.com").build()));

        assertEquals(1, server.getRequestCount());
    }

    @Test
    void retriesOnce() throws Exception {
        server.enqueue(error500());
        server.enqueue(ok200());

        SnapAPIClient client = clientWithRetries(1);
        byte[] result = client.screenshot(
            ScreenshotOptions.builder().url("https://example.com").build());

        assertArrayEquals(fakePng(), result);
        assertEquals(2, server.getRequestCount());
    }

    @Test
    void retriesTwiceThenSucceeds() throws Exception {
        server.enqueue(error500());
        server.enqueue(error500());
        server.enqueue(ok200());

        SnapAPIClient client = clientWithRetries(3);
        byte[] result = client.screenshot(
            ScreenshotOptions.builder().url("https://example.com").build());

        assertArrayEquals(fakePng(), result);
        assertEquals(3, server.getRequestCount());
    }

    @Test
    void exhaustsRetriesAndThrows() {
        server.enqueue(error500());
        server.enqueue(error500());
        server.enqueue(error500());
        server.enqueue(error500());

        SnapAPIClient client = clientWithRetries(3);
        assertThrows(SnapAPIException.class,
            () -> client.screenshot(
                ScreenshotOptions.builder().url("https://example.com").build()));

        // 1 initial + 3 retries = 4 total requests
        assertEquals(4, server.getRequestCount());
    }

    @Test
    void retriesOn429() throws Exception {
        server.enqueue(new MockResponse()
            .setResponseCode(429)
            .setHeader("Content-Type", "application/json")
            .setHeader("Retry-After", "0")   // zero for fast test
            .setBody("{\"message\":\"Rate limited\",\"error\":\"RATE_LIMITED\"}"));
        server.enqueue(ok200());

        SnapAPIClient client = clientWithRetries(1);
        byte[] result = client.screenshot(
            ScreenshotOptions.builder().url("https://example.com").build());

        assertArrayEquals(fakePng(), result);
        assertEquals(2, server.getRequestCount());
    }

    @Test
    void doesNotRetryOn4xxOtherThan429() {
        int[] codesNotRetried = {400, 401, 402, 403, 404, 422};

        for (int code : codesNotRetried) {
            server.enqueue(new MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"Error " + code + "\"}"));
        }

        SnapAPIClient client = clientWithRetries(3);

        for (int code : codesNotRetried) {
            assertThrows(SnapAPIException.class,
                () -> client.screenshot(
                    ScreenshotOptions.builder().url("https://example.com").build()),
                "Expected exception for status " + code);
        }

        // Each should have made exactly 1 request (no retries)
        assertEquals(codesNotRetried.length, server.getRequestCount());
    }

    @Test
    void retryPolicyDefaultValues() {
        RetryPolicy policy = RetryPolicy.defaults();
        assertEquals(RetryPolicy.DEFAULT_MAX_RETRIES, policy.getMaxRetries());
        assertEquals(RetryPolicy.DEFAULT_INITIAL_DELAY, policy.getInitialDelayMs());
    }

    @Test
    void retryPolicyExponentialBackoff() {
        RetryPolicy policy = RetryPolicy.builder()
            .maxRetries(5)
            .initialDelayMs(100)
            .build();

        // Attempt 1: 100ms, 2: 200ms, 3: 400ms, 4: 800ms, 5: 1600ms
        SnapAPIException genericError = new SnapAPIException("error", "ERROR", 503, null);
        assertEquals(100L,  policy.computeDelayMs(1, genericError));
        assertEquals(200L,  policy.computeDelayMs(2, genericError));
        assertEquals(400L,  policy.computeDelayMs(3, genericError));
        assertEquals(800L,  policy.computeDelayMs(4, genericError));
        assertEquals(1600L, policy.computeDelayMs(5, genericError));
    }

    @Test
    void retryPolicyBackoffCappedAt30s() {
        RetryPolicy policy = RetryPolicy.builder()
            .maxRetries(30)
            .initialDelayMs(500)
            .build();

        SnapAPIException genericError = new SnapAPIException("error", "ERROR", 503, null);
        // At attempt 20, 500 * 2^19 would overflow, but should be capped at 30s
        long delay = policy.computeDelayMs(20, genericError);
        assertEquals(RetryPolicy.MAX_DELAY_MS, delay);
    }

    @Test
    void retryPolicyUsesRetryAfterHeader() {
        RetryPolicy policy = RetryPolicy.defaults();
        RateLimitException rateLimitEx = new RateLimitException("rate limited", 15.5);

        long delay = policy.computeDelayMs(1, rateLimitEx);
        assertEquals(15500L, delay);
    }

    @Test
    void retryPolicyRespects429ShouldRetry() {
        RetryPolicy policy = RetryPolicy.builder().maxRetries(3).build();

        RateLimitException rateLimitEx = new RateLimitException("rate limited", 1.0);
        assertEquals(true, policy.shouldRetry(rateLimitEx, 1));
        assertEquals(true, policy.shouldRetry(rateLimitEx, 2));
        assertEquals(true, policy.shouldRetry(rateLimitEx, 3));
        assertEquals(false, policy.shouldRetry(rateLimitEx, 4));
    }

    @Test
    void retryPolicyShouldNotRetry4xx() {
        RetryPolicy policy = RetryPolicy.builder().maxRetries(3).build();

        SnapAPIException ex400 = new SnapAPIException("bad request", "BAD_REQUEST", 400, null);
        SnapAPIException ex404 = new SnapAPIException("not found",   "NOT_FOUND",   404, null);

        assertEquals(false, policy.shouldRetry(ex400, 1));
        assertEquals(false, policy.shouldRetry(ex404, 1));
    }

    @Test
    void retryPolicyShouldRetry5xx() {
        RetryPolicy policy = RetryPolicy.builder().maxRetries(3).build();

        SnapAPIException ex500 = new SnapAPIException("internal error", "SERVER_ERROR", 500, null);
        SnapAPIException ex503 = new SnapAPIException("unavailable",    "UNAVAILABLE",  503, null);

        assertEquals(true, policy.shouldRetry(ex500, 1));
        assertEquals(true, policy.shouldRetry(ex503, 1));
    }
}
