package pics.snapapi;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import pics.snapapi.models.AnalyzeOptions;
import pics.snapapi.models.ExtractOptions;
import pics.snapapi.models.PdfOptions;
import pics.snapapi.models.ScrapeOptions;
import pics.snapapi.models.ScreenshotOptions;
import pics.snapapi.models.VideoOptions;
import pics.snapapi.retry.RetryPolicy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SnapAPIClient}.
 *
 * <p>Uses OkHttp MockWebServer to intercept all HTTP calls —
 * no real network access required.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SnapAPIClientTest {

    private MockWebServer server;
    private SnapAPIClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        client = SnapAPIClient.builder()
            .apiKey("sk_test_abc123")
            .baseUrl(server.url("/").toString())
            .timeoutSecs(10)
            .maxRetries(0)   // disable retries for most tests
            .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static byte[] fakePng() {
        return new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};
    }

    private MockResponse jsonResponse(String body) {
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body);
    }

    private MockResponse binaryResponse(byte[] body, String contentType) {
        return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", contentType)
            .setBody(new okio.Buffer().write(body));
    }

    // =========================================================================
    // 1. Client builder
    // =========================================================================

    @Test
    @Order(1)
    void builderRequiresApiKey() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> SnapAPIClient.builder().build());
        assertTrue(ex.getMessage().contains("apiKey"));
    }

    @Test
    @Order(2)
    void builderCreatesClient() {
        assertDoesNotThrow(() -> SnapAPIClient.builder()
            .apiKey("sk_live_test")
            .baseUrl("https://api.snapapi.pics")
            .timeoutSecs(30)
            .maxRetries(3)
            .build());
    }

    @Test
    @Order(3)
    void builderWithRetryPolicyCreatesClient() {
        assertDoesNotThrow(() -> SnapAPIClient.builder()
            .apiKey("sk_live_test")
            .retryPolicy(RetryPolicy.builder().maxRetries(5).initialDelayMs(200).build())
            .build());
    }

    // =========================================================================
    // 2. Screenshot
    // =========================================================================

    @Test
    @Order(4)
    void screenshotReturnsBinaryBytes() throws Exception {
        byte[] png = fakePng();
        server.enqueue(binaryResponse(png, "image/png"));

        byte[] result = client.screenshot(ScreenshotOptions.builder()
            .url("https://example.com")
            .build());

        assertArrayEquals(png, result);
    }

    @Test
    @Order(5)
    void screenshotRequiresInputSource() {
        assertThrows(IllegalStateException.class,
            () -> ScreenshotOptions.builder().format("png").build());
    }

    @Test
    @Order(6)
    void screenshotSendsCorrectHeaders() throws Exception {
        server.enqueue(binaryResponse(fakePng(), "image/png"));

        client.screenshot(ScreenshotOptions.builder().url("https://example.com").build());

        RecordedRequest req = server.takeRequest();
        assertEquals("sk_test_abc123", req.getHeader("X-Api-Key"));
        assertEquals("Bearer sk_test_abc123", req.getHeader("Authorization"));
        assertNotNull(req.getHeader("User-Agent"));
        assertTrue(req.getHeader("User-Agent").startsWith("snapapi-java/"));
    }

    @Test
    @Order(7)
    void screenshotBuilderOptionsAreSerialised() throws Exception {
        server.enqueue(binaryResponse(fakePng(), "image/png"));

        client.screenshot(ScreenshotOptions.builder()
            .url("https://example.com")
            .format("webp")
            .fullPage(true)
            .darkMode(true)
            .width(1440)
            .build());

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"url\":\"https://example.com\""));
        assertTrue(body.contains("\"format\":\"webp\""));
        assertTrue(body.contains("\"fullPage\":true"));
        assertTrue(body.contains("\"darkMode\":true"));
        assertTrue(body.contains("\"width\":1440"));
    }

    @Test
    @Order(8)
    void screenshotWithHtmlInput() throws Exception {
        server.enqueue(binaryResponse(fakePng(), "image/png"));

        byte[] result = client.screenshot(ScreenshotOptions.builder()
            .html("<h1>Hello</h1>")
            .build());

        assertNotNull(result);
        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"html\":"));
    }

    // =========================================================================
    // 3. PDF
    // =========================================================================

    @Test
    @Order(9)
    void pdfReturnsPdfBytes() throws Exception {
        byte[] pdfBytes = "%PDF-1.4 fake".getBytes(StandardCharsets.UTF_8);
        server.enqueue(binaryResponse(pdfBytes, "application/pdf"));

        byte[] result = client.pdf(PdfOptions.builder()
            .url("https://example.com")
            .build());

        assertArrayEquals(pdfBytes, result);
    }

    @Test
    @Order(10)
    void pdfRequiresUrlOrHtml() {
        assertThrows(IllegalStateException.class,
            () -> PdfOptions.builder().pageSize("a4").build());
    }

    @Test
    @Order(11)
    void pdfSendsFormatPdf() throws Exception {
        server.enqueue(binaryResponse("%PDF".getBytes(), "application/pdf"));

        client.pdf(PdfOptions.builder().url("https://example.com").build());

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"format\":\"pdf\""));
    }

    // =========================================================================
    // 4. Scrape
    // =========================================================================

    @Test
    @Order(12)
    void scrapeReturnsJsonBytes() throws Exception {
        String json = "{\"results\":[{\"data\":\"hello\",\"url\":\"https://example.com\"}]}";
        server.enqueue(jsonResponse(json));

        byte[] result = client.scrape(ScrapeOptions.builder()
            .url("https://example.com")
            .type("text")
            .build());

        assertTrue(new String(result, StandardCharsets.UTF_8).contains("hello"));
    }

    @Test
    @Order(13)
    void scrapeShorthandAcceptsUrl() throws Exception {
        server.enqueue(jsonResponse("{\"results\":[]}"));

        client.scrape("https://example.com");

        RecordedRequest req = server.takeRequest();
        assertTrue(req.getBody().readUtf8().contains("\"url\":\"https://example.com\""));
    }

    // =========================================================================
    // 5. Extract
    // =========================================================================

    @Test
    @Order(14)
    void extractMarkdownReturnsJson() throws Exception {
        String json = "{\"content\":\"# Hello\\n\\nWorld\",\"type\":\"markdown\"}";
        server.enqueue(jsonResponse(json));

        byte[] result = client.extractMarkdown("https://example.com");
        String s = new String(result, StandardCharsets.UTF_8);
        assertTrue(s.contains("# Hello"));
    }

    @Test
    @Order(15)
    void extractConvenienceMethodsSendCorrectType() throws Exception {
        server.enqueue(jsonResponse("{\"content\":\"data\",\"type\":\"links\"}"));

        client.extractLinks("https://example.com");

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"type\":\"links\""));
    }

    @Test
    @Order(16)
    void extractWithFullOptions() throws Exception {
        server.enqueue(jsonResponse("{\"content\":\"text\",\"type\":\"article\"}"));

        client.extract(ExtractOptions.builder()
            .url("https://example.com")
            .type("article")
            .selector("#content")
            .maxLength(5000)
            .cleanOutput(true)
            .build());

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"type\":\"article\""));
        assertTrue(body.contains("\"selector\":\"#content\""));
        assertTrue(body.contains("\"maxLength\":5000"));
        assertTrue(body.contains("\"cleanOutput\":true"));
    }

    // =========================================================================
    // 6. Video
    // =========================================================================

    @Test
    @Order(17)
    void videoReturnsBytes() throws Exception {
        byte[] mp4 = new byte[]{0x00, 0x00, 0x00, 0x20, 'f', 't', 'y', 'p'};
        server.enqueue(binaryResponse(mp4, "video/mp4"));

        byte[] result = client.video(VideoOptions.builder()
            .url("https://example.com")
            .duration(5)
            .build());

        assertArrayEquals(mp4, result);
    }

    @Test
    @Order(18)
    void videoSendsScrollingOptions() throws Exception {
        server.enqueue(binaryResponse(new byte[]{1, 2, 3}, "video/mp4"));

        client.video(VideoOptions.builder()
            .url("https://example.com")
            .scrolling(true)
            .scrollSpeed(200)
            .build());

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"scrolling\":true"));
        assertTrue(body.contains("\"scrollSpeed\":200"));
    }

    // =========================================================================
    // 7. OG Image
    // =========================================================================

    @Test
    @Order(19)
    void ogImageSendsDefaultDimensions() throws Exception {
        server.enqueue(binaryResponse(fakePng(), "image/png"));

        client.ogImage("https://example.com");

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"width\":1200"));
        assertTrue(body.contains("\"height\":630"));
    }

    @Test
    @Order(20)
    void ogImageWithCustomDimensions() throws Exception {
        server.enqueue(binaryResponse(fakePng(), "image/png"));

        client.ogImage("https://example.com", "jpeg", 1200, 628);

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"format\":\"jpeg\""));
        assertTrue(body.contains("\"width\":1200"));
        assertTrue(body.contains("\"height\":628"));
    }

    // =========================================================================
    // 8. Analyze
    // =========================================================================

    @Test
    @Order(21)
    void analyzeReturnsJson() throws Exception {
        String json = "{\"result\":\"This is a test page.\",\"provider\":\"openai\"}";
        server.enqueue(jsonResponse(json));

        byte[] result = client.analyze(AnalyzeOptions.builder()
            .url("https://example.com")
            .prompt("Summarize this page.")
            .provider("openai")
            .apiKey("sk-test")
            .build());

        String s = new String(result, StandardCharsets.UTF_8);
        assertTrue(s.contains("result"));
    }

    @Test
    @Order(22)
    void analyzeRequiresUrlAndPrompt() {
        assertThrows(IllegalStateException.class,
            () -> AnalyzeOptions.builder().url("https://example.com").build());
        assertThrows(IllegalStateException.class,
            () -> AnalyzeOptions.builder().prompt("test").build());
    }

    // =========================================================================
    // 9. Usage / Ping
    // =========================================================================

    @Test
    @Order(23)
    void getUsageReturnsJsonBytes() throws Exception {
        String json = "{\"used\":42,\"limit\":1000,\"remaining\":958}";
        server.enqueue(jsonResponse(json));

        byte[] result = client.getUsage();
        assertTrue(new String(result, StandardCharsets.UTF_8).contains("\"used\":42"));
    }

    @Test
    @Order(24)
    void quotaAliasCallsGetUsage() throws Exception {
        server.enqueue(jsonResponse("{\"used\":10,\"limit\":1000,\"remaining\":990}"));

        byte[] result = client.quota();
        assertTrue(new String(result, StandardCharsets.UTF_8).contains("used"));
    }

    @Test
    @Order(25)
    void pingReturnsOkStatus() throws Exception {
        server.enqueue(jsonResponse("{\"status\":\"ok\",\"timestamp\":1710000000000}"));

        byte[] result = client.ping();
        assertTrue(new String(result, StandardCharsets.UTF_8).contains("\"status\":\"ok\""));
    }

    // =========================================================================
    // 10. Error handling
    // =========================================================================

    @Test
    @Order(26)
    void throws401AuthException() {
        server.enqueue(new MockResponse()
            .setResponseCode(401)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Invalid API key\",\"error\":\"UNAUTHORIZED\"}"));

        AuthException ex = assertThrows(AuthException.class,
            () -> client.screenshot(ScreenshotOptions.builder().url("https://example.com").build()));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    @Order(27)
    void throws403AuthException() {
        server.enqueue(new MockResponse()
            .setResponseCode(403)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Forbidden\",\"error\":\"FORBIDDEN\"}"));

        AuthException ex = assertThrows(AuthException.class,
            () -> client.screenshot(ScreenshotOptions.builder().url("https://example.com").build()));
        assertEquals(401, ex.getStatusCode()); // AuthException uses 401 internally
    }

    @Test
    @Order(28)
    void throws402QuotaException() {
        server.enqueue(new MockResponse()
            .setResponseCode(402)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Quota exceeded\",\"error\":\"QUOTA_EXCEEDED\"}"));

        assertThrows(QuotaException.class,
            () -> client.screenshot(ScreenshotOptions.builder().url("https://example.com").build()));
    }

    @Test
    @Order(29)
    void throws422ValidationException() {
        server.enqueue(new MockResponse()
            .setResponseCode(422)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"url is required\",\"error\":\"VALIDATION_ERROR\"}"));

        assertThrows(ValidationException.class,
            () -> client.screenshot(ScreenshotOptions.builder().url("https://example.com").build()));
    }

    @Test
    @Order(30)
    void throws429RateLimitException() {
        server.enqueue(new MockResponse()
            .setResponseCode(429)
            .setHeader("Content-Type", "application/json")
            .setHeader("Retry-After", "5")
            .setBody("{\"message\":\"Too many requests\",\"error\":\"RATE_LIMITED\"}"));

        RateLimitException ex = assertThrows(RateLimitException.class,
            () -> client.screenshot(ScreenshotOptions.builder().url("https://example.com").build()));
        assertEquals(5.0, ex.getRetryAfter(), 0.01);
    }

    @Test
    @Order(31)
    void throws500SnapAPIException() {
        server.enqueue(new MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Internal server error\"}"));

        SnapAPIException ex = assertThrows(SnapAPIException.class,
            () -> client.screenshot(ScreenshotOptions.builder().url("https://example.com").build()));
        assertEquals(500, ex.getStatusCode());
    }

    // =========================================================================
    // 11. Retry logic
    // =========================================================================

    @Test
    @Order(32)
    void retries500ThenSucceeds() throws Exception {
        // First two calls fail with 500, third succeeds
        server.enqueue(new MockResponse().setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Server error\"}"));
        server.enqueue(new MockResponse().setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Server error\"}"));
        server.enqueue(binaryResponse(fakePng(), "image/png"));

        SnapAPIClient retryClient = SnapAPIClient.builder()
            .apiKey("sk_test_abc123")
            .baseUrl(server.url("/").toString())
            .timeoutSecs(10)
            .retryPolicy(RetryPolicy.builder().maxRetries(3).initialDelayMs(0).build())
            .build();

        byte[] result = retryClient.screenshot(
            ScreenshotOptions.builder().url("https://example.com").build());

        assertArrayEquals(fakePng(), result);
        assertEquals(3, server.getRequestCount());
    }

    @Test
    @Order(33)
    void exhaustedRetriesThrowsException() {
        server.enqueue(new MockResponse().setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Server error\"}"));
        server.enqueue(new MockResponse().setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Server error\"}"));
        server.enqueue(new MockResponse().setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Server error\"}"));

        SnapAPIClient retryClient = SnapAPIClient.builder()
            .apiKey("sk_test_abc123")
            .baseUrl(server.url("/").toString())
            .timeoutSecs(10)
            .retryPolicy(RetryPolicy.builder().maxRetries(2).initialDelayMs(0).build())
            .build();

        assertThrows(SnapAPIException.class,
            () -> retryClient.screenshot(
                ScreenshotOptions.builder().url("https://example.com").build()));
    }

    @Test
    @Order(34)
    void doesNotRetry4xxErrors() {
        server.enqueue(new MockResponse().setResponseCode(400)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"Bad request\"}"));

        SnapAPIClient retryClient = SnapAPIClient.builder()
            .apiKey("sk_test_abc123")
            .baseUrl(server.url("/").toString())
            .timeoutSecs(10)
            .retryPolicy(RetryPolicy.builder().maxRetries(3).initialDelayMs(0).build())
            .build();

        assertThrows(SnapAPIException.class,
            () -> retryClient.screenshot(
                ScreenshotOptions.builder().url("https://example.com").build()));

        // Should have only made 1 request (no retry on 4xx)
        assertEquals(1, server.getRequestCount());
    }

    // =========================================================================
    // 12. Exception hierarchy
    // =========================================================================

    @Test
    @Order(35)
    void authExceptionExtendsSnapAPIException() {
        assertTrue(new AuthException("test") instanceof SnapAPIException);
    }

    @Test
    @Order(36)
    void rateLimitExceptionExtendsSnapAPIException() {
        RateLimitException ex = new RateLimitException("limited", 30.0);
        assertTrue(ex instanceof SnapAPIException);
        assertEquals(30.0, ex.getRetryAfter(), 0.01);
    }

    @Test
    @Order(37)
    void quotaExceptionExtendsSnapAPIException() {
        assertTrue(new QuotaException("quota") instanceof SnapAPIException);
    }

    @Test
    @Order(38)
    void validationExceptionExtendsSnapAPIException() {
        assertTrue(new ValidationException("invalid") instanceof SnapAPIException);
    }

    @Test
    @Order(39)
    void networkExceptionExtendsSnapAPIException() {
        assertTrue(new NetworkException("network") instanceof SnapAPIException);
    }

    @Test
    @Order(40)
    void timeoutExceptionExtendsSnapAPIException() {
        assertTrue(new TimeoutException("timeout") instanceof SnapAPIException);
    }

    @Test
    @Order(41)
    void allExceptionsExtendRuntimeException() {
        assertTrue(new SnapAPIException("base") instanceof RuntimeException);
        assertTrue(new AuthException("test") instanceof RuntimeException);
        assertTrue(new QuotaException("test") instanceof RuntimeException);
        assertTrue(new RateLimitException("test", 1.0) instanceof RuntimeException);
        assertTrue(new ValidationException("test") instanceof RuntimeException);
        assertTrue(new NetworkException("test") instanceof RuntimeException);
        assertTrue(new TimeoutException("test") instanceof RuntimeException);
    }

    // =========================================================================
    // 13. Request routing
    // =========================================================================

    @Test
    @Order(42)
    void screenshotCallsCorrectPath() throws Exception {
        server.enqueue(binaryResponse(fakePng(), "image/png"));
        client.screenshot(ScreenshotOptions.builder().url("https://example.com").build());
        RecordedRequest req = server.takeRequest();
        assertEquals("/v1/screenshot", req.getPath());
        assertEquals("POST", req.getMethod());
    }

    @Test
    @Order(43)
    void pdfCallsScreenshotPath() throws Exception {
        server.enqueue(binaryResponse("%PDF".getBytes(), "application/pdf"));
        client.pdf(PdfOptions.builder().url("https://example.com").build());
        RecordedRequest req = server.takeRequest();
        assertEquals("/v1/screenshot", req.getPath());
    }

    @Test
    @Order(44)
    void scrapeCallsCorrectPath() throws Exception {
        server.enqueue(jsonResponse("{\"results\":[]}"));
        client.scrape("https://example.com");
        RecordedRequest req = server.takeRequest();
        assertEquals("/v1/scrape", req.getPath());
        assertEquals("POST", req.getMethod());
    }

    @Test
    @Order(45)
    void extractCallsCorrectPath() throws Exception {
        server.enqueue(jsonResponse("{\"content\":\"x\"}"));
        client.extractMarkdown("https://example.com");
        RecordedRequest req = server.takeRequest();
        assertEquals("/v1/extract", req.getPath());
        assertEquals("POST", req.getMethod());
    }

    @Test
    @Order(46)
    void videoCallsCorrectPath() throws Exception {
        server.enqueue(binaryResponse(new byte[]{1, 2, 3}, "video/mp4"));
        client.video(VideoOptions.builder().url("https://example.com").build());
        RecordedRequest req = server.takeRequest();
        assertEquals("/v1/video", req.getPath());
        assertEquals("POST", req.getMethod());
    }

    @Test
    @Order(47)
    void usageCallsCorrectPath() throws Exception {
        server.enqueue(jsonResponse("{\"used\":0,\"limit\":1000,\"remaining\":1000}"));
        client.getUsage();
        RecordedRequest req = server.takeRequest();
        assertEquals("/v1/usage", req.getPath());
        assertEquals("GET", req.getMethod());
    }

    @Test
    @Order(48)
    void pingCallsCorrectPath() throws Exception {
        server.enqueue(jsonResponse("{\"status\":\"ok\"}"));
        client.ping();
        RecordedRequest req = server.takeRequest();
        assertEquals("/v1/ping", req.getPath());
        assertEquals("GET", req.getMethod());
    }
}
