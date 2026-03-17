package pics.snapapi;

import pics.snapapi.http.HttpClient;
import pics.snapapi.models.AnalyzeOptions;
import pics.snapapi.models.ExtractOptions;
import pics.snapapi.models.JsonBuilder;
import pics.snapapi.models.PdfOptions;
import pics.snapapi.models.ScrapeOptions;
import pics.snapapi.models.ScreenshotOptions;
import pics.snapapi.models.VideoOptions;
import pics.snapapi.retry.RetryPolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Official SnapAPI Java client.
 *
 * <p>All methods are synchronous and throw {@link SnapAPIException} (or a subclass) on error.
 * Build a client via the fluent {@link Builder}:
 *
 * <pre>{@code
 * SnapAPIClient client = SnapAPIClient.builder()
 *     .apiKey("sk_live_...")
 *     .build();
 *
 * // Take a screenshot
 * byte[] png = client.screenshot(ScreenshotOptions.builder()
 *     .url("https://example.com")
 *     .format("png")
 *     .fullPage(true)
 *     .build());
 *
 * Files.write(Path.of("screenshot.png"), png);
 * }</pre>
 */
public final class SnapAPIClient {

    private static final String DEFAULT_BASE_URL = "https://api.snapapi.pics";
    private static final int    DEFAULT_TIMEOUT  = 60;

    private final HttpClient http;

    private SnapAPIClient(Builder b) {
        RetryPolicy policy = b.retryPolicy != null
            ? b.retryPolicy
            : RetryPolicy.builder()
                .maxRetries(b.maxRetries)
                .initialDelayMs(b.initialDelayMs)
                .build();

        this.http = new HttpClient(b.baseUrl, b.apiKey, b.timeoutSecs, policy);
    }

    // =========================================================================
    // Screenshot   POST /v1/screenshot
    // =========================================================================

    /**
     * Capture a screenshot of a URL, HTML, or Markdown string.
     *
     * @param opts Screenshot options built with {@link ScreenshotOptions#builder()}.
     * @return Raw image bytes (PNG, JPEG, WebP, etc.), or JSON bytes when
     *         {@code storage} or {@code webhookUrl} is set.
     * @throws IllegalStateException if no input source is configured.
     * @throws SnapAPIException      on API errors.
     */
    public byte[] screenshot(ScreenshotOptions opts) {
        String json = buildScreenshotJson(opts);
        return http.post("/v1/screenshot", json);
    }

    /**
     * Capture a screenshot and save it to a local file.
     *
     * @param opts     Screenshot options.
     * @param filepath Destination file path.
     * @throws SnapAPIException if the API call fails or the file cannot be written.
     */
    public void screenshotToFile(ScreenshotOptions opts, Path filepath) {
        byte[] bytes = screenshot(opts);
        try {
            Path parent = filepath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(filepath, bytes);
        } catch (IOException e) {
            throw new NetworkException("Failed to write file: " + filepath, e);
        }
    }

    // =========================================================================
    // PDF   POST /v1/screenshot (format=pdf)
    // =========================================================================

    /**
     * Generate a PDF from a URL or HTML string.
     *
     * @param opts PDF options built with {@link PdfOptions#builder()}.
     * @return Raw PDF bytes.
     * @throws SnapAPIException on API errors.
     */
    public byte[] pdf(PdfOptions opts) {
        String json = buildPdfJson(opts);
        return http.post("/v1/screenshot", json);
    }

    /**
     * Generate a PDF and save it to a local file.
     *
     * @param opts     PDF options.
     * @param filepath Destination file path.
     * @throws SnapAPIException if the API call fails or the file cannot be written.
     */
    public void pdfToFile(PdfOptions opts, Path filepath) {
        byte[] bytes = pdf(opts);
        try {
            Path parent = filepath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(filepath, bytes);
        } catch (IOException e) {
            throw new NetworkException("Failed to write file: " + filepath, e);
        }
    }

    // =========================================================================
    // Scrape   POST /v1/scrape
    // =========================================================================

    /**
     * Scrape text, HTML, or links from one or more pages.
     *
     * @param opts Scrape options built with {@link ScrapeOptions#builder()}.
     * @return JSON response bytes. Parse with your preferred JSON library.
     * @throws SnapAPIException on API errors.
     */
    public byte[] scrape(ScrapeOptions opts) {
        String json = buildScrapeJson(opts);
        return http.post("/v1/scrape", json);
    }

    /**
     * Scrape a URL with default options (type=text, pages=1).
     *
     * @param url URL to scrape.
     * @return JSON response bytes.
     */
    public byte[] scrape(String url) {
        return scrape(ScrapeOptions.builder().url(url).build());
    }

    // =========================================================================
    // Extract   POST /v1/extract
    // =========================================================================

    /**
     * Extract structured content from a web page.
     *
     * @param opts Extract options built with {@link ExtractOptions#builder()}.
     * @return JSON response bytes containing the extracted content.
     * @throws SnapAPIException on API errors.
     */
    public byte[] extract(ExtractOptions opts) {
        String json = buildExtractJson(opts);
        return http.post("/v1/extract", json);
    }

    /** Extract page content as Markdown. */
    public byte[] extractMarkdown(String url) {
        return extract(ExtractOptions.builder().url(url).type("markdown").build());
    }

    /** Extract main article body. */
    public byte[] extractArticle(String url) {
        return extract(ExtractOptions.builder().url(url).type("article").build());
    }

    /** Extract plain text. */
    public byte[] extractText(String url) {
        return extract(ExtractOptions.builder().url(url).type("text").build());
    }

    /** Extract all hyperlinks. */
    public byte[] extractLinks(String url) {
        return extract(ExtractOptions.builder().url(url).type("links").build());
    }

    /** Extract all image URLs. */
    public byte[] extractImages(String url) {
        return extract(ExtractOptions.builder().url(url).type("images").build());
    }

    /** Extract page metadata (title, description, OG tags, etc.). */
    public byte[] extractMetadata(String url) {
        return extract(ExtractOptions.builder().url(url).type("metadata").build());
    }

    // =========================================================================
    // Video   POST /v1/video
    // =========================================================================

    /**
     * Record a video of a live webpage.
     *
     * @param opts Video options built with {@link VideoOptions#builder()}.
     * @return Raw video bytes (MP4, WebM, or GIF).
     * @throws SnapAPIException on API errors.
     */
    public byte[] video(VideoOptions opts) {
        String json = buildVideoJson(opts);
        return http.post("/v1/video", json);
    }

    // =========================================================================
    // OG Image
    // =========================================================================

    /**
     * Generate a 1200x630 Open Graph social preview image for a URL.
     *
     * @param url URL to generate an OG image for.
     * @return Raw PNG image bytes.
     * @throws SnapAPIException on API errors.
     */
    public byte[] ogImage(String url) {
        return ogImage(url, "png", 1200, 630);
    }

    /**
     * Generate an Open Graph image with custom dimensions.
     *
     * @param url    URL to capture.
     * @param format Output format.
     * @param width  Image width in pixels.
     * @param height Image height in pixels.
     * @return Raw image bytes.
     */
    public byte[] ogImage(String url, String format, int width, int height) {
        String json = new JsonBuilder()
            .field("url",    url)
            .field("format", format)
            .field("width",  width)
            .field("height", height)
            .build();
        return http.post("/v1/screenshot", json);
    }

    // =========================================================================
    // Analyze   POST /v1/analyze
    // =========================================================================

    /**
     * Analyze a web page with an LLM (bring your own API key).
     *
     * @param opts Analyze options built with {@link AnalyzeOptions#builder()}.
     * @return JSON response bytes containing the analysis result.
     * @throws SnapAPIException on API errors.
     */
    public byte[] analyze(AnalyzeOptions opts) {
        String json = buildAnalyzeJson(opts);
        return http.post("/v1/analyze", json);
    }

    // =========================================================================
    // Usage / Quota
    // =========================================================================

    /**
     * Get API usage for the current billing period.
     *
     * @return JSON response bytes with {@code used}, {@code limit}, {@code remaining},
     *         and {@code resetAt} fields.
     * @throws SnapAPIException on API errors.
     */
    public byte[] getUsage() {
        return http.get("/v1/usage");
    }

    /**
     * Alias for {@link #getUsage()}.
     *
     * @return JSON response bytes.
     */
    public byte[] quota() {
        return getUsage();
    }

    // =========================================================================
    // Ping
    // =========================================================================

    /**
     * Check API availability.
     *
     * @return JSON bytes: {@code {"status":"ok","timestamp":...}}.
     * @throws SnapAPIException on API errors.
     */
    public byte[] ping() {
        return http.get("/v1/ping");
    }

    // =========================================================================
    // JSON builders (internal)
    // =========================================================================

    private static String buildScreenshotJson(ScreenshotOptions o) {
        return new JsonBuilder()
            .field("url",               o.getUrl())
            .field("html",              o.getHtml())
            .field("markdown",          o.getMarkdown())
            .field("format",            o.getFormat())
            .field("quality",           o.getQuality())
            .field("device",            o.getDevice())
            .field("width",             o.getWidth())
            .field("height",            o.getHeight())
            .field("deviceScaleFactor", o.getDeviceScaleFactor() != 1.0 ? o.getDeviceScaleFactor() : null)
            .field("isMobile",          o.isMobile() ? Boolean.TRUE : null)
            .field("hasTouch",          o.isHasTouch() ? Boolean.TRUE : null)
            .field("fullPage",          o.isFullPage() ? Boolean.TRUE : null)
            .field("fullPageScrollDelay", o.getFullPageScrollDelay())
            .field("fullPageMaxHeight", o.getFullPageMaxHeight())
            .field("selector",          o.getSelector())
            .field("delay",             o.getDelay() > 0 ? o.getDelay() : null)
            .field("timeout",           o.getTimeout())
            .field("waitUntil",         o.getWaitUntil())
            .field("waitForSelector",   o.getWaitForSelector())
            .field("darkMode",          o.isDarkMode() ? Boolean.TRUE : null)
            .field("reducedMotion",     o.isReducedMotion() ? Boolean.TRUE : null)
            .field("css",               o.getCss())
            .field("javascript",        o.getJavascript())
            .field("hideSelectors",     o.getHideSelectors())
            .field("clickSelector",     o.getClickSelector())
            .field("blockAds",          o.isBlockAds() ? Boolean.TRUE : null)
            .field("blockTrackers",     o.isBlockTrackers() ? Boolean.TRUE : null)
            .field("blockCookieBanners", o.isBlockCookieBanners() ? Boolean.TRUE : null)
            .field("blockChatWidgets",  o.isBlockChatWidgets() ? Boolean.TRUE : null)
            .field("userAgent",         o.getUserAgent())
            .field("extraHeaders",      o.getExtraHeaders())
            .field("webhookUrl",        o.getWebhookUrl())
            .field("jobId",             o.getJobId())
            .field("pageSize",          o.getPageSize())
            .field("landscape",         o.getLandscape())
            .field("margins",           o.getMargins())
            .field("storage",           o.getStorage())
            .build();
    }

    private static String buildPdfJson(PdfOptions o) {
        return new JsonBuilder()
            .field("format",              "pdf")
            .field("url",                 o.getUrl())
            .field("html",                o.getHtml())
            .field("pageSize",            o.getPageSize())
            .field("landscape",           o.isLandscape() ? Boolean.TRUE : null)
            .field("margins",             o.getMargins())
            .field("headerTemplate",      o.getHeaderTemplate())
            .field("footerTemplate",      o.getFooterTemplate())
            .field("displayHeaderFooter", o.isDisplayHeaderFooter() ? Boolean.TRUE : null)
            .field("scale",               o.getScale())
            .field("delay",               o.getDelay() > 0 ? o.getDelay() : null)
            .field("waitForSelector",     o.getWaitForSelector())
            .build();
    }

    private static String buildScrapeJson(ScrapeOptions o) {
        return new JsonBuilder()
            .field("url",            o.getUrl())
            .field("type",           o.getType())
            .field("pages",          o.getPages() != 1 ? o.getPages() : null)
            .field("waitMs",         o.getWaitMs())
            .field("proxy",          o.getProxy())
            .field("premiumProxy",   o.getPremiumProxy())
            .field("blockResources", o.isBlockResources() ? Boolean.TRUE : null)
            .field("locale",         o.getLocale())
            .build();
    }

    private static String buildExtractJson(ExtractOptions o) {
        return new JsonBuilder()
            .field("url",                o.getUrl())
            .field("type",               o.getType())
            .field("selector",           o.getSelector())
            .field("waitFor",            o.getWaitFor())
            .field("timeout",            o.getTimeout())
            .field("darkMode",           o.isDarkMode() ? Boolean.TRUE : null)
            .field("blockAds",           o.isBlockAds() ? Boolean.TRUE : null)
            .field("blockCookieBanners", o.isBlockCookieBanners() ? Boolean.TRUE : null)
            .field("includeImages",      o.getIncludeImages())
            .field("maxLength",          o.getMaxLength())
            .field("cleanOutput",        o.getCleanOutput())
            .build();
    }

    private static String buildVideoJson(VideoOptions o) {
        return new JsonBuilder()
            .field("url",                o.getUrl())
            .field("format",             o.getFormat())
            .field("width",              o.getWidth())
            .field("height",             o.getHeight())
            .field("duration",           o.getDuration())
            .field("fps",                o.getFps())
            .field("scrolling",          o.isScrolling() ? Boolean.TRUE : null)
            .field("scrollSpeed",        o.getScrollSpeed())
            .field("scrollDelay",        o.getScrollDelay())
            .field("scrollDuration",     o.getScrollDuration())
            .field("scrollBy",           o.getScrollBy())
            .field("scrollEasing",       o.getScrollEasing())
            .field("scrollBack",         o.isScrollBack())
            .field("scrollComplete",     o.isScrollComplete())
            .field("darkMode",           o.isDarkMode() ? Boolean.TRUE : null)
            .field("blockAds",           o.isBlockAds() ? Boolean.TRUE : null)
            .field("blockCookieBanners", o.isBlockCookieBanners() ? Boolean.TRUE : null)
            .field("delay",              o.getDelay() > 0 ? o.getDelay() : null)
            .build();
    }

    private static String buildAnalyzeJson(AnalyzeOptions o) {
        return new JsonBuilder()
            .field("url",                o.getUrl())
            .field("prompt",             o.getPrompt())
            .field("provider",           o.getProvider())
            .field("apiKey",             o.getApiKey())
            .field("model",              o.getModel())
            .field("jsonSchema",         o.getJsonSchema())
            .field("includeScreenshot",  o.getIncludeScreenshot())
            .field("includeMetadata",    o.getIncludeMetadata())
            .field("maxContentLength",   o.getMaxContentLength())
            .field("timeout",            o.getTimeout())
            .field("blockAds",           o.isBlockAds() ? Boolean.TRUE : null)
            .field("blockCookieBanners", o.isBlockCookieBanners() ? Boolean.TRUE : null)
            .field("waitFor",            o.getWaitFor())
            .build();
    }

    // =========================================================================
    // Builder
    // =========================================================================

    /**
     * Create a new {@link Builder} for {@link SnapAPIClient}.
     *
     * @return A new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder for {@link SnapAPIClient}. */
    public static final class Builder {
        private String      apiKey;
        private String      baseUrl        = DEFAULT_BASE_URL;
        private int         timeoutSecs    = DEFAULT_TIMEOUT;
        private int         maxRetries     = RetryPolicy.DEFAULT_MAX_RETRIES;
        private long        initialDelayMs = RetryPolicy.DEFAULT_INITIAL_DELAY;
        private RetryPolicy retryPolicy;

        private Builder() {}

        /**
         * Your SnapAPI key (required).
         *
         * @param apiKey API key beginning with {@code sk_live_} or {@code sk_test_}.
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Override the API base URL (default: {@code https://api.snapapi.pics}).
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Request timeout in seconds (default: 60).
         */
        public Builder timeoutSecs(int secs) {
            this.timeoutSecs = secs;
            return this;
        }

        /**
         * Maximum number of auto-retries on 429 / 5xx (default: 3).
         * Set to {@code 0} to disable retries.
         */
        public Builder maxRetries(int n) {
            this.maxRetries = n;
            return this;
        }

        /**
         * Initial backoff delay in milliseconds (default: 500).
         * Doubles with each retry, capped at 30s.
         */
        public Builder initialDelayMs(long ms) {
            this.initialDelayMs = ms;
            return this;
        }

        /**
         * Supply a fully custom {@link RetryPolicy}.
         * When set, {@code maxRetries} and {@code initialDelayMs} are ignored.
         */
        public Builder retryPolicy(RetryPolicy policy) {
            this.retryPolicy = policy;
            return this;
        }

        /**
         * Build the {@link SnapAPIClient}.
         *
         * @throws IllegalStateException if {@code apiKey} is null or empty.
         */
        public SnapAPIClient build() {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("apiKey is required");
            }
            return new SnapAPIClient(this);
        }
    }
}
