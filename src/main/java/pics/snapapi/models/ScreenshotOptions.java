package pics.snapapi.models;

import java.util.List;
import java.util.Map;

/**
 * Options for the screenshot endpoint.
 *
 * <p>Build instances via the fluent builder:
 *
 * <pre>{@code
 * ScreenshotOptions opts = ScreenshotOptions.builder()
 *     .url("https://example.com")
 *     .format("webp")
 *     .fullPage(true)
 *     .darkMode(true)
 *     .proxy(Map.of("server", "http://proxy:3128"))
 *     .geolocation(Map.of("latitude", 51.5, "longitude", -0.1))
 *     .timezone("Europe/London")
 *     .build();
 * }</pre>
 */
public final class ScreenshotOptions {

    private final String  url;
    private final String  html;
    private final String  markdown;
    private final String  format;
    private final Integer quality;
    private final String  device;
    private final int     width;
    private final int     height;
    private final double  deviceScaleFactor;
    private final boolean isMobile;
    private final boolean hasTouch;
    private final boolean isLandscape;
    private final boolean fullPage;
    private final Integer fullPageScrollDelay;
    private final Integer fullPageMaxHeight;
    private final boolean scrollToBottom;
    private final String  selector;
    private final int     delay;
    private final Integer timeout;
    private final String  waitUntil;
    private final String  waitForSelector;
    private final boolean darkMode;
    private final boolean reducedMotion;
    private final String  css;
    private final String  javascript;
    private final List<String> hideSelectors;
    private final String  clickSelector;
    private final boolean blockAds;
    private final boolean blockTrackers;
    private final boolean blockCookieBanners;
    private final boolean blockChatWidgets;
    private final String  userAgent;
    private final Map<String, String> extraHeaders;
    private final List<Map<String, Object>> cookies;
    private final Map<String, String> httpAuth;
    private final Map<String, Object> proxy;
    private final Boolean premiumProxy;
    private final Map<String, Object> geolocation;
    private final String  timezone;
    private final String  locale;
    private final boolean cache;
    private final Integer cacheTtl;
    private final String  webhookUrl;
    private final boolean async;
    private final String  jobId;
    private final String  pageSize;
    private final Boolean landscape;
    private final Map<String, String> margins;
    private final Map<String, Object> storage;

    private ScreenshotOptions(Builder b) {
        this.url                = b.url;
        this.html               = b.html;
        this.markdown           = b.markdown;
        this.format             = b.format;
        this.quality            = b.quality;
        this.device             = b.device;
        this.width              = b.width;
        this.height             = b.height;
        this.deviceScaleFactor  = b.deviceScaleFactor;
        this.isMobile           = b.isMobile;
        this.hasTouch           = b.hasTouch;
        this.isLandscape        = b.isLandscape;
        this.fullPage           = b.fullPage;
        this.fullPageScrollDelay = b.fullPageScrollDelay;
        this.fullPageMaxHeight  = b.fullPageMaxHeight;
        this.scrollToBottom     = b.scrollToBottom;
        this.selector           = b.selector;
        this.delay              = b.delay;
        this.timeout            = b.timeout;
        this.waitUntil          = b.waitUntil;
        this.waitForSelector    = b.waitForSelector;
        this.darkMode           = b.darkMode;
        this.reducedMotion      = b.reducedMotion;
        this.css                = b.css;
        this.javascript         = b.javascript;
        this.hideSelectors      = b.hideSelectors;
        this.clickSelector      = b.clickSelector;
        this.blockAds           = b.blockAds;
        this.blockTrackers      = b.blockTrackers;
        this.blockCookieBanners = b.blockCookieBanners;
        this.blockChatWidgets   = b.blockChatWidgets;
        this.userAgent          = b.userAgent;
        this.extraHeaders       = b.extraHeaders;
        this.cookies            = b.cookies;
        this.httpAuth           = b.httpAuth;
        this.proxy              = b.proxy;
        this.premiumProxy       = b.premiumProxy;
        this.geolocation        = b.geolocation;
        this.timezone           = b.timezone;
        this.locale             = b.locale;
        this.cache              = b.cache;
        this.cacheTtl           = b.cacheTtl;
        this.webhookUrl         = b.webhookUrl;
        this.async              = b.async;
        this.jobId              = b.jobId;
        this.pageSize           = b.pageSize;
        this.landscape          = b.landscape;
        this.margins            = b.margins;
        this.storage            = b.storage;
    }

    public String  getUrl()                { return url; }
    public String  getHtml()               { return html; }
    public String  getMarkdown()           { return markdown; }
    public String  getFormat()             { return format; }
    public Integer getQuality()            { return quality; }
    public String  getDevice()             { return device; }
    public int     getWidth()              { return width; }
    public int     getHeight()             { return height; }
    public double  getDeviceScaleFactor()  { return deviceScaleFactor; }
    public boolean isMobile()              { return isMobile; }
    public boolean isHasTouch()            { return hasTouch; }
    public boolean isIsLandscape()         { return isLandscape; }
    public boolean isFullPage()            { return fullPage; }
    public Integer getFullPageScrollDelay(){ return fullPageScrollDelay; }
    public Integer getFullPageMaxHeight()  { return fullPageMaxHeight; }
    public boolean isScrollToBottom()      { return scrollToBottom; }
    public String  getSelector()           { return selector; }
    public int     getDelay()              { return delay; }
    public Integer getTimeout()            { return timeout; }
    public String  getWaitUntil()          { return waitUntil; }
    public String  getWaitForSelector()    { return waitForSelector; }
    public boolean isDarkMode()            { return darkMode; }
    public boolean isReducedMotion()       { return reducedMotion; }
    public String  getCss()                { return css; }
    public String  getJavascript()         { return javascript; }
    public List<String> getHideSelectors() { return hideSelectors; }
    public String  getClickSelector()      { return clickSelector; }
    public boolean isBlockAds()            { return blockAds; }
    public boolean isBlockTrackers()       { return blockTrackers; }
    public boolean isBlockCookieBanners()  { return blockCookieBanners; }
    public boolean isBlockChatWidgets()    { return blockChatWidgets; }
    public String  getUserAgent()          { return userAgent; }
    public Map<String, String> getExtraHeaders() { return extraHeaders; }
    public List<Map<String, Object>> getCookies() { return cookies; }
    public Map<String, String> getHttpAuth() { return httpAuth; }
    public Map<String, Object> getProxy()  { return proxy; }
    public Boolean getPremiumProxy()       { return premiumProxy; }
    public Map<String, Object> getGeolocation() { return geolocation; }
    public String  getTimezone()           { return timezone; }
    public String  getLocale()             { return locale; }
    public boolean isCache()               { return cache; }
    public Integer getCacheTtl()           { return cacheTtl; }
    public String  getWebhookUrl()         { return webhookUrl; }
    public boolean isAsync()               { return async; }
    public String  getJobId()              { return jobId; }
    public String  getPageSize()           { return pageSize; }
    public Boolean getLandscape()          { return landscape; }
    public Map<String, String> getMargins(){ return margins; }
    public Map<String, Object> getStorage(){ return storage; }

    /** @return A new {@link Builder}. */
    public static Builder builder() { return new Builder(); }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /** Fluent builder for {@link ScreenshotOptions}. */
    public static final class Builder {
        private String  url;
        private String  html;
        private String  markdown;
        private String  format             = "png";
        private Integer quality;
        private String  device;
        private int     width              = 1280;
        private int     height             = 800;
        private double  deviceScaleFactor  = 1.0;
        private boolean isMobile           = false;
        private boolean hasTouch           = false;
        private boolean isLandscape        = false;
        private boolean fullPage           = false;
        private Integer fullPageScrollDelay;
        private Integer fullPageMaxHeight;
        private boolean scrollToBottom     = false;
        private String  selector;
        private int     delay              = 0;
        private Integer timeout;
        private String  waitUntil;
        private String  waitForSelector;
        private boolean darkMode           = false;
        private boolean reducedMotion      = false;
        private String  css;
        private String  javascript;
        private List<String> hideSelectors;
        private String  clickSelector;
        private boolean blockAds           = false;
        private boolean blockTrackers      = false;
        private boolean blockCookieBanners = false;
        private boolean blockChatWidgets   = false;
        private String  userAgent;
        private Map<String, String> extraHeaders;
        private List<Map<String, Object>> cookies;
        private Map<String, String> httpAuth;
        private Map<String, Object> proxy;
        private Boolean premiumProxy;
        private Map<String, Object> geolocation;
        private String  timezone;
        private String  locale;
        private boolean cache              = false;
        private Integer cacheTtl;
        private String  webhookUrl;
        private boolean async              = false;
        private String  jobId;
        private String  pageSize;
        private Boolean landscape;
        private Map<String, String> margins;
        private Map<String, Object> storage;

        private Builder() {}

        public Builder url(String url)                              { this.url = url; return this; }
        public Builder html(String html)                            { this.html = html; return this; }
        public Builder markdown(String markdown)                    { this.markdown = markdown; return this; }
        public Builder format(String format)                        { this.format = format; return this; }
        public Builder quality(int quality)                         { this.quality = quality; return this; }
        public Builder device(String device)                        { this.device = device; return this; }
        public Builder width(int width)                             { this.width = width; return this; }
        public Builder height(int height)                           { this.height = height; return this; }
        public Builder deviceScaleFactor(double dsf)                { this.deviceScaleFactor = dsf; return this; }
        public Builder isMobile(boolean v)                          { this.isMobile = v; return this; }
        public Builder hasTouch(boolean v)                          { this.hasTouch = v; return this; }
        public Builder isLandscape(boolean v)                       { this.isLandscape = v; return this; }
        public Builder fullPage(boolean v)                          { this.fullPage = v; return this; }
        public Builder fullPageScrollDelay(int ms)                  { this.fullPageScrollDelay = ms; return this; }
        public Builder fullPageMaxHeight(int px)                    { this.fullPageMaxHeight = px; return this; }
        public Builder scrollToBottom(boolean v)                    { this.scrollToBottom = v; return this; }
        public Builder selector(String s)                           { this.selector = s; return this; }
        public Builder delay(int ms)                                { this.delay = ms; return this; }
        public Builder timeout(int ms)                              { this.timeout = ms; return this; }
        public Builder waitUntil(String event)                      { this.waitUntil = event; return this; }
        public Builder waitForSelector(String sel)                  { this.waitForSelector = sel; return this; }
        public Builder darkMode(boolean v)                          { this.darkMode = v; return this; }
        public Builder reducedMotion(boolean v)                     { this.reducedMotion = v; return this; }
        public Builder css(String css)                              { this.css = css; return this; }
        public Builder javascript(String js)                        { this.javascript = js; return this; }
        public Builder hideSelectors(List<String> sels)             { this.hideSelectors = sels; return this; }
        public Builder clickSelector(String sel)                    { this.clickSelector = sel; return this; }
        public Builder blockAds(boolean v)                          { this.blockAds = v; return this; }
        public Builder blockTrackers(boolean v)                     { this.blockTrackers = v; return this; }
        public Builder blockCookieBanners(boolean v)                { this.blockCookieBanners = v; return this; }
        public Builder blockChatWidgets(boolean v)                  { this.blockChatWidgets = v; return this; }
        public Builder userAgent(String ua)                         { this.userAgent = ua; return this; }
        public Builder extraHeaders(Map<String, String> h)          { this.extraHeaders = h; return this; }
        /**
         * Set cookies to inject. Each cookie map should contain at minimum
         * {@code name} and {@code value}, plus optionally {@code domain},
         * {@code path}, {@code expires}, {@code httpOnly}, {@code secure},
         * {@code sameSite}.
         */
        public Builder cookies(List<Map<String, Object>> c)         { this.cookies = c; return this; }
        /** HTTP Basic Auth: map with {@code username} and {@code password}. */
        public Builder httpAuth(Map<String, String> auth)           { this.httpAuth = auth; return this; }
        /**
         * Proxy configuration map. Keys: {@code server} (required, e.g.
         * {@code "http://host:port"}), {@code username}, {@code password},
         * {@code bypass} (List of domains).
         */
        public Builder proxy(Map<String, Object> proxy)             { this.proxy = proxy; return this; }
        /** Use SnapAPI managed residential proxy network. */
        public Builder premiumProxy(boolean v)                      { this.premiumProxy = v; return this; }
        /**
         * Geolocation emulation. Map with {@code latitude} (double),
         * {@code longitude} (double), and optionally {@code accuracy} (double).
         */
        public Builder geolocation(Map<String, Object> geo)         { this.geolocation = geo; return this; }
        /** IANA timezone string, e.g. {@code "America/New_York"}. */
        public Builder timezone(String tz)                          { this.timezone = tz; return this; }
        /** Browser locale, e.g. {@code "en-US"}. */
        public Builder locale(String locale)                        { this.locale = locale; return this; }
        /** Enable response caching. */
        public Builder cache(boolean v)                             { this.cache = v; return this; }
        /** Cache TTL in seconds (60–2592000). */
        public Builder cacheTtl(int seconds)                        { this.cacheTtl = seconds; return this; }
        /** Deliver result to this webhook URL asynchronously. */
        public Builder webhookUrl(String url)                       { this.webhookUrl = url; return this; }
        /** Queue the capture and return a job ID immediately. */
        public Builder async(boolean v)                             { this.async = v; return this; }
        public Builder jobId(String id)                             { this.jobId = id; return this; }
        public Builder pageSize(String size)                        { this.pageSize = size; return this; }
        public Builder landscape(boolean v)                         { this.landscape = v; return this; }
        public Builder margins(Map<String, String> margins)         { this.margins = margins; return this; }
        public Builder storage(Map<String, Object> storage)         { this.storage = storage; return this; }

        /** Build the {@link ScreenshotOptions}. */
        public ScreenshotOptions build() {
            if (url == null && html == null && markdown == null) {
                throw new IllegalStateException("One of url, html, or markdown is required");
            }
            return new ScreenshotOptions(this);
        }
    }
}
