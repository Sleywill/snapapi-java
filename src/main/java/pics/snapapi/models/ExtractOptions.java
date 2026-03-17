package pics.snapapi.models;

/**
 * Options for the extract endpoint ({@code POST /v1/extract}).
 *
 * <pre>{@code
 * ExtractOptions opts = ExtractOptions.builder()
 *     .url("https://example.com")
 *     .type("markdown")
 *     .cleanOutput(true)
 *     .build();
 * }</pre>
 */
public final class ExtractOptions {

    private final String  url;
    private final String  type;
    private final String  selector;
    private final String  waitFor;
    private final Integer timeout;
    private final boolean darkMode;
    private final boolean blockAds;
    private final boolean blockCookieBanners;
    private final Boolean includeImages;
    private final Integer maxLength;
    private final Boolean cleanOutput;

    private ExtractOptions(Builder b) {
        this.url                = b.url;
        this.type               = b.type;
        this.selector           = b.selector;
        this.waitFor            = b.waitFor;
        this.timeout            = b.timeout;
        this.darkMode           = b.darkMode;
        this.blockAds           = b.blockAds;
        this.blockCookieBanners = b.blockCookieBanners;
        this.includeImages      = b.includeImages;
        this.maxLength          = b.maxLength;
        this.cleanOutput        = b.cleanOutput;
    }

    public String  getUrl()                { return url; }
    public String  getType()               { return type; }
    public String  getSelector()           { return selector; }
    public String  getWaitFor()            { return waitFor; }
    public Integer getTimeout()            { return timeout; }
    public boolean isDarkMode()            { return darkMode; }
    public boolean isBlockAds()            { return blockAds; }
    public boolean isBlockCookieBanners()  { return blockCookieBanners; }
    public Boolean getIncludeImages()      { return includeImages; }
    public Integer getMaxLength()          { return maxLength; }
    public Boolean getCleanOutput()        { return cleanOutput; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String  url;
        private String  type               = "markdown";
        private String  selector;
        private String  waitFor;
        private Integer timeout;
        private boolean darkMode           = false;
        private boolean blockAds           = false;
        private boolean blockCookieBanners = false;
        private Boolean includeImages;
        private Integer maxLength;
        private Boolean cleanOutput;

        private Builder() {}

        public Builder url(String url)                     { this.url = url; return this; }
        public Builder type(String type)                   { this.type = type; return this; }
        public Builder selector(String s)                  { this.selector = s; return this; }
        public Builder waitFor(String s)                   { this.waitFor = s; return this; }
        public Builder timeout(int ms)                     { this.timeout = ms; return this; }
        public Builder darkMode(boolean v)                 { this.darkMode = v; return this; }
        public Builder blockAds(boolean v)                 { this.blockAds = v; return this; }
        public Builder blockCookieBanners(boolean v)       { this.blockCookieBanners = v; return this; }
        public Builder includeImages(boolean v)            { this.includeImages = v; return this; }
        public Builder maxLength(int n)                    { this.maxLength = n; return this; }
        public Builder cleanOutput(boolean v)              { this.cleanOutput = v; return this; }

        public ExtractOptions build() {
            if (url == null) throw new IllegalStateException("url is required");
            return new ExtractOptions(this);
        }
    }
}
