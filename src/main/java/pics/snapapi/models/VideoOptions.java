package pics.snapapi.models;

/**
 * Options for the video recording endpoint ({@code POST /v1/video}).
 *
 * <pre>{@code
 * VideoOptions opts = VideoOptions.builder()
 *     .url("https://example.com")
 *     .format("mp4")
 *     .duration(10)
 *     .scrolling(true)
 *     .build();
 * }</pre>
 */
public final class VideoOptions {

    private final String  url;
    private final String  format;
    private final int     width;
    private final int     height;
    private final int     duration;
    private final int     fps;
    private final boolean scrolling;
    private final Integer scrollSpeed;
    private final Integer scrollDelay;
    private final Integer scrollDuration;
    private final Integer scrollBy;
    private final String  scrollEasing;
    private final boolean scrollBack;
    private final boolean scrollComplete;
    private final boolean darkMode;
    private final boolean blockAds;
    private final boolean blockCookieBanners;
    private final int     delay;

    private VideoOptions(Builder b) {
        this.url                = b.url;
        this.format             = b.format;
        this.width              = b.width;
        this.height             = b.height;
        this.duration           = b.duration;
        this.fps                = b.fps;
        this.scrolling          = b.scrolling;
        this.scrollSpeed        = b.scrollSpeed;
        this.scrollDelay        = b.scrollDelay;
        this.scrollDuration     = b.scrollDuration;
        this.scrollBy           = b.scrollBy;
        this.scrollEasing       = b.scrollEasing;
        this.scrollBack         = b.scrollBack;
        this.scrollComplete     = b.scrollComplete;
        this.darkMode           = b.darkMode;
        this.blockAds           = b.blockAds;
        this.blockCookieBanners = b.blockCookieBanners;
        this.delay              = b.delay;
    }

    public String  getUrl()               { return url; }
    public String  getFormat()            { return format; }
    public int     getWidth()             { return width; }
    public int     getHeight()            { return height; }
    public int     getDuration()          { return duration; }
    public int     getFps()               { return fps; }
    public boolean isScrolling()          { return scrolling; }
    public Integer getScrollSpeed()       { return scrollSpeed; }
    public Integer getScrollDelay()       { return scrollDelay; }
    public Integer getScrollDuration()    { return scrollDuration; }
    public Integer getScrollBy()          { return scrollBy; }
    public String  getScrollEasing()      { return scrollEasing; }
    public boolean isScrollBack()         { return scrollBack; }
    public boolean isScrollComplete()     { return scrollComplete; }
    public boolean isDarkMode()           { return darkMode; }
    public boolean isBlockAds()           { return blockAds; }
    public boolean isBlockCookieBanners() { return blockCookieBanners; }
    public int     getDelay()             { return delay; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String  url;
        private String  format             = "mp4";
        private int     width              = 1280;
        private int     height             = 720;
        private int     duration           = 5;
        private int     fps                = 25;
        private boolean scrolling          = false;
        private Integer scrollSpeed;
        private Integer scrollDelay;
        private Integer scrollDuration;
        private Integer scrollBy;
        private String  scrollEasing;
        private boolean scrollBack         = true;
        private boolean scrollComplete     = true;
        private boolean darkMode           = false;
        private boolean blockAds           = false;
        private boolean blockCookieBanners = false;
        private int     delay              = 0;

        private Builder() {}

        public Builder url(String url)                    { this.url = url; return this; }
        public Builder format(String f)                   { this.format = f; return this; }
        public Builder width(int w)                       { this.width = w; return this; }
        public Builder height(int h)                      { this.height = h; return this; }
        public Builder duration(int secs)                 { this.duration = secs; return this; }
        public Builder fps(int fps)                       { this.fps = fps; return this; }
        public Builder scrolling(boolean v)               { this.scrolling = v; return this; }
        public Builder scrollSpeed(int px)                { this.scrollSpeed = px; return this; }
        public Builder scrollDelay(int ms)                { this.scrollDelay = ms; return this; }
        public Builder scrollDuration(int ms)             { this.scrollDuration = ms; return this; }
        public Builder scrollBy(int px)                   { this.scrollBy = px; return this; }
        public Builder scrollEasing(String e)             { this.scrollEasing = e; return this; }
        public Builder scrollBack(boolean v)              { this.scrollBack = v; return this; }
        public Builder scrollComplete(boolean v)          { this.scrollComplete = v; return this; }
        public Builder darkMode(boolean v)                { this.darkMode = v; return this; }
        public Builder blockAds(boolean v)                { this.blockAds = v; return this; }
        public Builder blockCookieBanners(boolean v)      { this.blockCookieBanners = v; return this; }
        public Builder delay(int ms)                      { this.delay = ms; return this; }

        public VideoOptions build() {
            if (url == null) throw new IllegalStateException("url is required");
            return new VideoOptions(this);
        }
    }
}
