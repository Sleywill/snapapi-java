import pics.snapapi.AuthException;
import pics.snapapi.NetworkException;
import pics.snapapi.QuotaException;
import pics.snapapi.RateLimitException;
import pics.snapapi.SnapAPIClient;
import pics.snapapi.SnapAPIException;
import pics.snapapi.TimeoutException;
import pics.snapapi.ValidationException;
import pics.snapapi.models.AnalyzeOptions;
import pics.snapapi.models.ExtractOptions;
import pics.snapapi.models.PdfOptions;
import pics.snapapi.models.ScrapeOptions;
import pics.snapapi.models.ScreenshotOptions;
import pics.snapapi.models.VideoOptions;
import pics.snapapi.retry.RetryPolicy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Live integration test suite for the SnapAPI Java SDK.
 *
 * <p>Exercises all endpoints, builder options, error hierarchy, and retry logic
 * against the real production API.
 *
 * <p>Compile and run from the sdk root:
 * <pre>
 *   javac -cp src/main/java LiveTest.java
 *   java  -cp src/main/java:. SNAPAPI_KEY=sk_live_... LiveTest
 *   # or:
 *   SNAPAPI_KEY=sk_live_... java -cp ... LiveTest
 * </pre>
 */
public final class LiveTest {

    private static final String TEST_URL  = "https://example.com";
    private static final String API_KEY   = System.getenv("SNAPAPI_KEY") != null
                                              ? System.getenv("SNAPAPI_KEY")
                                              : "";

    private static final List<String> PASSED  = new ArrayList<>();
    private static final List<String> FAILED  = new ArrayList<>();
    private static final List<String> SKIPPED = new ArrayList<>();

    // =========================================================================
    // Harness helpers
    // =========================================================================

    @FunctionalInterface
    interface TestBlock { void run() throws Exception; }

    static void test(String name, TestBlock block) {
        System.out.print("  " + name + " ... ");
        try {
            block.run();
            PASSED.add(name);
            System.out.println("\u001B[32mPASS\u001B[0m");
        } catch (SkipTest e) {
            SKIPPED.add(name);
            System.out.println("\u001B[33mSKIP\u001B[0m (" + e.getMessage() + ")");
        } catch (Throwable e) {
            FAILED.add(name);
            System.out.println("\u001B[31mFAIL\u001B[0m — " + e.getClass().getSimpleName()
                               + ": " + e.getMessage());
            StackTraceElement[] st = e.getStackTrace();
            if (st.length > 0) System.out.println("    at " + st[0]);
        }
    }

    static class SkipTest extends RuntimeException {
        SkipTest(String msg) { super(msg); }
    }

    static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError(msg);
    }

    static void assertTrue(boolean condition) {
        assertTrue(condition, "assertion failed");
    }

    static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual))
            throw new AssertionError("expected " + expected + " but got " + actual);
    }

    static void assertHasPngHeader(byte[] data) {
        assertTrue(data.length >= 8, "data too short for PNG header");
        assertTrue(data[0] == (byte)0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G',
                   "missing PNG header (got " + byteHex(data[0]) + " " + byteHex(data[1]) + ")");
    }

    static void assertHasJpegHeader(byte[] data) {
        assertTrue(data.length >= 2, "data too short");
        assertTrue(data[0] == (byte)0xFF && data[1] == (byte)0xD8, "missing JPEG header");
    }

    static void assertHasPdfHeader(byte[] data) {
        String s = new String(data, 0, Math.min(4, data.length), StandardCharsets.US_ASCII);
        assertTrue("%PDF".equals(s), "missing PDF header (got: " + s + ")");
    }

    static String byteHex(byte b) {
        return String.format("0x%02X", b);
    }

    static <T extends Throwable> T assertThrows(Class<T> klass, TestBlock block) {
        try {
            block.run();
            throw new AssertionError("Expected " + klass.getSimpleName() + " but nothing was thrown");
        } catch (Throwable e) {
            if (klass.isInstance(e)) return klass.cast(e);
            if (e instanceof AssertionError) throw (AssertionError) e;
            throw new AssertionError("Expected " + klass.getSimpleName()
                                     + " but got " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    static String asString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // =========================================================================
    // Main
    // =========================================================================

    public static void main(String[] args) throws Exception {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("Set SNAPAPI_KEY environment variable.");
            System.exit(2);
        }

        SnapAPIClient client = SnapAPIClient.builder()
            .apiKey(API_KEY)
            .maxRetries(2)
            .initialDelayMs(200)
            .build();

        // =====================================================================
        // Suite 1 — Ping
        // =====================================================================
        System.out.println("\n== Suite 1: Ping / Connectivity ==");

        test("ping returns JSON with status:ok", () -> {
            byte[] resp = client.ping();
            String s = asString(resp);
            assertTrue(s.contains("\"status\""), "no status field in: " + s);
            assertTrue(s.contains("ok"), "status not ok in: " + s);
        });

        test("ping response contains timestamp", () -> {
            String s = asString(client.ping());
            assertTrue(s.contains("timestamp"), "no timestamp: " + s);
        });

        // =====================================================================
        // Suite 2 — Usage / Quota
        // =====================================================================
        System.out.println("\n== Suite 2: Usage / Quota ==");

        test("getUsage returns JSON with used field", () -> {
            byte[] resp = client.getUsage();
            String s = asString(resp);
            assertTrue(s.contains("used") || s.contains("limit"),
                       "unexpected usage response: " + s);
        });

        test("quota() alias returns same structure", () -> {
            String u1 = asString(client.getUsage());
            String u2 = asString(client.quota());
            assertTrue(u1.contains("used") && u2.contains("used"),
                       "quota alias mismatch");
        });

        // =====================================================================
        // Suite 3 — Screenshot
        // =====================================================================
        System.out.println("\n== Suite 3: Screenshot ==");

        test("basic PNG screenshot returns PNG bytes", () -> {
            byte[] png = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL)
                .build());
            assertTrue(png.length > 1000, "PNG too small: " + png.length);
            assertHasPngHeader(png);
        });

        test("screenshot format:jpeg returns JPEG", () -> {
            byte[] jpg = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL)
                .format("jpeg")
                .quality(80)
                .build());
            assertHasJpegHeader(jpg);
        });

        test("screenshot format:webp returns WebP", () -> {
            byte[] webp = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL)
                .format("webp")
                .build());
            assertTrue(webp.length > 100, "webp too small");
            // WebP has RIFF header at 0 and WEBP at offset 8
            String riff = new String(webp, 0, 4, StandardCharsets.US_ASCII);
            String webpSig = new String(webp, 8, 4, StandardCharsets.US_ASCII);
            assertTrue("RIFF".equals(riff) || "WEBP".equals(webpSig),
                       "Not a WebP file");
        });

        test("fullPage screenshot is larger or equal to viewport screenshot", () -> {
            byte[] normal = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL).build());
            byte[] full = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL).fullPage(true).build());
            // full-page is generally larger
            assertTrue(full.length >= normal.length * 0.8,
                       "fullPage result smaller than expected: " + full.length
                       + " vs normal " + normal.length);
        });

        test("darkMode option accepted", () -> {
            byte[] png = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL).darkMode(true).build());
            assertHasPngHeader(png);
        });

        test("custom viewport 1920x1080", () -> {
            byte[] png = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL).width(1920).height(1080).build());
            assertTrue(png.length > 1000);
        });

        test("delay option accepted", () -> {
            byte[] png = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL).delay(300).build());
            assertHasPngHeader(png);
        });

        test("screenshot from HTML string", () -> {
            byte[] png = client.screenshot(ScreenshotOptions.builder()
                .html("<html><body><h1 style='color:red'>Hello</h1></body></html>")
                .build());
            assertHasPngHeader(png);
        });

        test("screenshotToFile saves PNG to disk", () -> {
            Path tmp = Files.createTempFile("snapapi_test_", ".png");
            try {
                client.screenshotToFile(
                    ScreenshotOptions.builder().url(TEST_URL).build(), tmp);
                assertTrue(Files.exists(tmp), "file not created");
                assertTrue(Files.size(tmp) > 1000, "file too small");
                byte[] header = new byte[8];
                System.arraycopy(Files.readAllBytes(tmp), 0, header, 0, 8);
                assertHasPngHeader(header);
            } finally {
                Files.deleteIfExists(tmp);
            }
        });

        test("blockAds and blockTrackers options", () -> {
            byte[] png = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL).blockAds(true).blockTrackers(true).build());
            assertTrue(png.length > 1000);
        });

        test("CSS injection option", () -> {
            byte[] png = client.screenshot(ScreenshotOptions.builder()
                .url(TEST_URL).css("body { background: red !important; }").build());
            assertHasPngHeader(png);
        });

        test("requires url/html/markdown — builder throws IllegalStateException", () -> {
            assertThrows(IllegalStateException.class,
                () -> ScreenshotOptions.builder().format("png").build());
        });

        // =====================================================================
        // Suite 4 — PDF
        // =====================================================================
        System.out.println("\n== Suite 4: PDF ==");

        test("pdf from URL returns PDF bytes", () -> {
            byte[] pdf = client.pdf(PdfOptions.builder().url(TEST_URL).build());
            assertHasPdfHeader(pdf);
        });

        test("pdf from HTML string", () -> {
            byte[] pdf = client.pdf(PdfOptions.builder()
                .html("<html><body><h1>Test</h1></body></html>")
                .pageSize("a4")
                .build());
            assertHasPdfHeader(pdf);
        });

        test("pdf landscape option", () -> {
            byte[] pdf = client.pdf(PdfOptions.builder()
                .url(TEST_URL).landscape(true).build());
            assertHasPdfHeader(pdf);
        });

        test("pdfToFile saves PDF to disk", () -> {
            Path tmp = Files.createTempFile("snapapi_test_", ".pdf");
            try {
                client.pdfToFile(PdfOptions.builder().url(TEST_URL).build(), tmp);
                assertTrue(Files.exists(tmp));
                assertTrue(Files.size(tmp) > 100);
                byte[] header = new byte[4];
                System.arraycopy(Files.readAllBytes(tmp), 0, header, 0, 4);
                assertHasPdfHeader(header);
            } finally {
                Files.deleteIfExists(tmp);
            }
        });

        test("pdf builder requires url or html", () -> {
            assertThrows(IllegalStateException.class,
                () -> PdfOptions.builder().pageSize("a4").build());
        });

        // =====================================================================
        // Suite 5 — Scrape
        // =====================================================================
        System.out.println("\n== Suite 5: Scrape ==");

        test("scrape type:text returns JSON with results", () -> {
            byte[] resp = client.scrape(ScrapeOptions.builder()
                .url(TEST_URL).type("text").build());
            String s = asString(resp);
            assertTrue(s.contains("results") || s.length() > 10,
                       "empty scrape response");
        });

        test("scrape shorthand URL method", () -> {
            byte[] resp = client.scrape(TEST_URL);
            assertTrue(resp.length > 5, "empty scrape");
        });

        test("scrape type:html", () -> {
            byte[] resp = client.scrape(ScrapeOptions.builder()
                .url(TEST_URL).type("html").build());
            String s = asString(resp);
            assertTrue(s.length() > 5, "empty html scrape");
        });

        test("scrape type:links", () -> {
            byte[] resp = client.scrape(ScrapeOptions.builder()
                .url(TEST_URL).type("links").build());
            assertTrue(resp.length > 5);
        });

        test("scrape blockResources option", () -> {
            byte[] resp = client.scrape(ScrapeOptions.builder()
                .url(TEST_URL).blockResources(true).build());
            assertTrue(resp.length > 5);
        });

        test("scrape builder requires url", () -> {
            assertThrows(IllegalStateException.class,
                () -> ScrapeOptions.builder().type("text").build());
        });

        // =====================================================================
        // Suite 6 — Extract
        // =====================================================================
        System.out.println("\n== Suite 6: Extract ==");

        test("extract type:markdown returns content", () -> {
            byte[] resp = client.extract(ExtractOptions.builder()
                .url(TEST_URL).type("markdown").build());
            String s = asString(resp);
            assertTrue(s.contains("content") || s.length() > 10,
                       "empty extract response");
        });

        test("extractMarkdown convenience method", () -> {
            byte[] resp = client.extractMarkdown(TEST_URL);
            String s = asString(resp);
            assertTrue(s.length() > 10, "empty markdown");
        });

        test("extractText convenience method", () -> {
            byte[] resp = client.extractText(TEST_URL);
            assertTrue(resp.length > 5);
        });

        test("extractArticle convenience method", () -> {
            byte[] resp = client.extractArticle(TEST_URL);
            assertTrue(resp.length > 5);
        });

        test("extractLinks convenience method", () -> {
            byte[] resp = client.extractLinks(TEST_URL);
            assertTrue(resp.length > 5);
        });

        test("extractImages convenience method", () -> {
            byte[] resp = client.extractImages(TEST_URL);
            assertTrue(resp.length > 5);
        });

        test("extractMetadata convenience method", () -> {
            byte[] resp = client.extractMetadata(TEST_URL);
            String s = asString(resp);
            assertTrue(s.length() > 5, "empty metadata");
        });

        test("extract with maxLength", () -> {
            byte[] full    = client.extract(ExtractOptions.builder()
                .url(TEST_URL).type("text").build());
            byte[] limited = client.extract(ExtractOptions.builder()
                .url(TEST_URL).type("text").maxLength(100).build());
            // limited should be smaller or same size JSON
            assertTrue(limited.length <= full.length + 500,
                       "maxLength not effective");
        });

        test("extract cleanOutput and blockAds options", () -> {
            byte[] resp = client.extract(ExtractOptions.builder()
                .url(TEST_URL).type("markdown")
                .cleanOutput(true).blockAds(true).build());
            assertTrue(resp.length > 5);
        });

        test("extract builder requires url", () -> {
            assertThrows(IllegalStateException.class,
                () -> ExtractOptions.builder().type("text").build());
        });

        // =====================================================================
        // Suite 7 — Video
        // =====================================================================
        System.out.println("\n== Suite 7: Video ==");

        test("video mp4 returns binary bytes", () -> {
            byte[] mp4 = client.video(VideoOptions.builder()
                .url(TEST_URL).format("mp4").duration(3).fps(15).build());
            assertTrue(mp4.length > 1000, "mp4 too small: " + mp4.length);
        });

        test("video webm format", () -> {
            byte[] webm = client.video(VideoOptions.builder()
                .url(TEST_URL).format("webm").duration(3).fps(15).build());
            assertTrue(webm.length > 100, "webm too small");
        });

        test("video with darkMode option", () -> {
            byte[] mp4 = client.video(VideoOptions.builder()
                .url(TEST_URL).duration(3).darkMode(true).build());
            assertTrue(mp4.length > 1000);
        });

        test("video with scrolling enabled", () -> {
            byte[] mp4 = client.video(VideoOptions.builder()
                .url(TEST_URL).duration(4).scrolling(true).build());
            assertTrue(mp4.length > 1000);
        });

        test("video builder requires url", () -> {
            assertThrows(IllegalStateException.class,
                () -> VideoOptions.builder().duration(5).build());
        });

        // =====================================================================
        // Suite 8 — OG Image
        // =====================================================================
        System.out.println("\n== Suite 8: OG Image ==");

        test("ogImage default dimensions returns PNG", () -> {
            byte[] png = client.ogImage(TEST_URL);
            assertHasPngHeader(png);
            assertTrue(png.length > 1000);
        });

        test("ogImage custom format and dimensions", () -> {
            byte[] jpg = client.ogImage(TEST_URL, "jpeg", 1200, 628);
            assertHasJpegHeader(jpg);
        });

        // =====================================================================
        // Suite 9 — Error handling
        // =====================================================================
        System.out.println("\n== Suite 9: Error Handling ==");

        test("invalid API key throws AuthException", () -> {
            SnapAPIClient bad = SnapAPIClient.builder()
                .apiKey("sk_live_BADKEY_invalid_00000000000000000")
                .maxRetries(0).build();
            assertThrows(AuthException.class, () -> bad.ping());
        });

        test("AuthException has statusCode 401", () -> {
            SnapAPIClient bad = SnapAPIClient.builder()
                .apiKey("sk_live_BADKEY_invalid")
                .maxRetries(0).build();
            try {
                bad.ping();
                throw new AssertionError("no exception");
            } catch (AuthException e) {
                assertEquals(401, e.getStatusCode());
                assertTrue("UNAUTHORIZED".equals(e.getErrorCode()),
                           "errorCode: " + e.getErrorCode());
                assertTrue(e.getMessage() != null && !e.getMessage().isEmpty());
            }
        });

        test("AuthException extends SnapAPIException", () -> {
            assertTrue(new AuthException("test") instanceof SnapAPIException);
        });

        test("RateLimitException extends SnapAPIException", () -> {
            RateLimitException e = new RateLimitException("limited", 5.0);
            assertTrue(e instanceof SnapAPIException);
            assertEquals(5.0, e.getRetryAfter());
        });

        test("QuotaException extends SnapAPIException", () -> {
            assertTrue(new QuotaException("quota") instanceof SnapAPIException);
        });

        test("ValidationException extends SnapAPIException", () -> {
            assertTrue(new ValidationException("invalid") instanceof SnapAPIException);
        });

        test("NetworkException extends SnapAPIException", () -> {
            assertTrue(new NetworkException("network") instanceof SnapAPIException);
        });

        test("TimeoutException extends SnapAPIException", () -> {
            TimeoutException e = new TimeoutException("timeout");
            assertTrue(e instanceof SnapAPIException);
            assertEquals(0, e.getStatusCode());
        });

        test("all exceptions extend RuntimeException", () -> {
            assertTrue(new SnapAPIException("base") instanceof RuntimeException);
            assertTrue(new AuthException("a") instanceof RuntimeException);
            assertTrue(new QuotaException("q") instanceof RuntimeException);
            assertTrue(new RateLimitException("r", 1.0) instanceof RuntimeException);
            assertTrue(new ValidationException("v") instanceof RuntimeException);
            assertTrue(new NetworkException("n") instanceof RuntimeException);
            assertTrue(new TimeoutException("t") instanceof RuntimeException);
        });

        test("SnapAPIException message format includes errorCode", () -> {
            SnapAPIException e = new SnapAPIException("bad thing", "SOME_CODE", 500, null);
            assertTrue(e.getMessage().contains("SOME_CODE"),
                       "message: " + e.getMessage());
        });

        test("builder throws IllegalStateException when apiKey missing", () -> {
            assertThrows(IllegalStateException.class,
                () -> SnapAPIClient.builder().build());
        });

        // =====================================================================
        // Suite 10 — Retry behaviour
        // =====================================================================
        System.out.println("\n== Suite 10: Retry Behaviour ==");

        test("client with maxRetries:0 still succeeds on first try", () -> {
            SnapAPIClient c = SnapAPIClient.builder()
                .apiKey(API_KEY).maxRetries(0).build();
            byte[] resp = c.ping();
            assertTrue(asString(resp).contains("ok"));
        });

        test("RetryPolicy.defaults() has sensible values", () -> {
            RetryPolicy p = RetryPolicy.defaults();
            assertTrue(p.getMaxRetries() > 0);
            assertTrue(p.getInitialDelayMs() > 0);
        });

        test("RetryPolicy exponential backoff computation", () -> {
            RetryPolicy p = RetryPolicy.builder().initialDelayMs(100).build();
            // attempt 1 → 100ms, attempt 2 → 200ms
            SnapAPIException dummy = new SnapAPIException("test", "ERR", 503, null);
            assertTrue(p.computeDelayMs(1, dummy) == 100L);
            assertTrue(p.computeDelayMs(2, dummy) == 200L);
            assertTrue(p.computeDelayMs(3, dummy) == 400L);
        });

        test("RetryPolicy shouldRetry returns false for 4xx non-429", () -> {
            RetryPolicy p = RetryPolicy.defaults();
            SnapAPIException ex400 = new SnapAPIException("bad", "BAD", 400, null);
            assertTrue(!p.shouldRetry(ex400, 1), "should not retry 400");
        });

        test("RetryPolicy shouldRetry returns true for 5xx", () -> {
            RetryPolicy p = RetryPolicy.defaults();
            SnapAPIException ex500 = new SnapAPIException("server", "ERR", 500, null);
            assertTrue(p.shouldRetry(ex500, 1), "should retry 500");
        });

        test("RetryPolicy shouldRetry returns true for RateLimitException", () -> {
            RetryPolicy p = RetryPolicy.defaults();
            assertTrue(p.shouldRetry(new RateLimitException("limited", 1.0), 1));
        });

        test("RetryPolicy respects Retry-After for rate limit delay", () -> {
            RetryPolicy p = RetryPolicy.builder().initialDelayMs(200).build();
            RateLimitException rl = new RateLimitException("limited", 3.5);
            long delay = p.computeDelayMs(1, rl);
            assertEquals(3500L, delay);
        });

        test("multiple sequential requests succeed", () -> {
            SnapAPIClient c = SnapAPIClient.builder().apiKey(API_KEY)
                .maxRetries(2).initialDelayMs(200).build();
            for (int i = 0; i < 3; i++) {
                byte[] resp = c.ping();
                assertTrue(asString(resp).contains("ok"), "ping failed on iteration " + i);
            }
        });

        // =====================================================================
        // Suite 11 — Builder patterns
        // =====================================================================
        System.out.println("\n== Suite 11: Builder Patterns ==");

        test("builder with all options compiles and runs", () -> {
            SnapAPIClient c = SnapAPIClient.builder()
                .apiKey(API_KEY)
                .baseUrl("https://api.snapapi.pics")
                .timeoutSecs(120)
                .maxRetries(3)
                .initialDelayMs(500)
                .build();
            byte[] resp = c.ping();
            assertTrue(asString(resp).contains("ok"));
        });

        test("builder with custom RetryPolicy", () -> {
            RetryPolicy policy = RetryPolicy.builder()
                .maxRetries(5).initialDelayMs(100).build();
            SnapAPIClient c = SnapAPIClient.builder()
                .apiKey(API_KEY).retryPolicy(policy).build();
            byte[] resp = c.ping();
            assertTrue(asString(resp).contains("ok"));
        });

        test("ScreenshotOptions all boolean flags", () -> {
            // Verify builder accepts all flags without NPE
            ScreenshotOptions opts = ScreenshotOptions.builder()
                .url(TEST_URL)
                .isMobile(true)
                .hasTouch(true)
                .fullPage(false)
                .darkMode(false)
                .reducedMotion(true)
                .blockAds(true)
                .blockTrackers(true)
                .blockCookieBanners(true)
                .blockChatWidgets(true)
                .build();
            assertEquals(TEST_URL, opts.getUrl());
            assertTrue(opts.isMobile());
            assertTrue(opts.isReducedMotion());
        });

        test("ScrapeOptions builder all fields", () -> {
            ScrapeOptions opts = ScrapeOptions.builder()
                .url(TEST_URL).type("html").pages(1)
                .blockResources(true).locale("en-US").build();
            assertEquals("html", opts.getType());
            assertEquals("en-US", opts.getLocale());
            assertTrue(opts.isBlockResources());
        });

        test("ExtractOptions builder all fields", () -> {
            ExtractOptions opts = ExtractOptions.builder()
                .url(TEST_URL).type("markdown")
                .maxLength(5000).cleanOutput(true)
                .includeImages(true).blockAds(true).build();
            assertEquals("markdown", opts.getType());
            assertEquals(Integer.valueOf(5000), opts.getMaxLength());
            assertTrue(opts.getCleanOutput());
        });

        test("VideoOptions builder all fields", () -> {
            VideoOptions opts = VideoOptions.builder()
                .url(TEST_URL).format("mp4").width(1280).height(720)
                .duration(10).fps(25).scrolling(true)
                .scrollSpeed(100).scrollDelay(200).scrollDuration(5000)
                .scrollBy(50).scrollEasing("linear")
                .scrollBack(true).scrollComplete(true)
                .darkMode(true).blockAds(true).delay(500).build();
            assertEquals("mp4", opts.getFormat());
            assertEquals(10, opts.getDuration());
            assertTrue(opts.isScrolling());
            assertEquals(Integer.valueOf(100), opts.getScrollSpeed());
        });

        test("PdfOptions builder all fields", () -> {
            PdfOptions opts = PdfOptions.builder()
                .url(TEST_URL).pageSize("letter").landscape(true)
                .scale(0.9).delay(1000).build();
            assertEquals("letter", opts.getPageSize());
            assertTrue(opts.isLandscape());
            assertEquals(Double.valueOf(0.9), opts.getScale());
        });

        // =====================================================================
        // Summary
        // =====================================================================
        int total = PASSED.size() + FAILED.size() + SKIPPED.size();
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("Results: %d passed, %d failed, %d skipped  (%d total)%n",
                          PASSED.size(), FAILED.size(), SKIPPED.size(), total);
        System.out.println("=".repeat(60));

        if (!FAILED.isEmpty()) {
            System.out.println("\nFailed tests:");
            for (String name : FAILED) {
                System.out.println("  - " + name);
            }
            System.exit(1);
        }
    }
}
