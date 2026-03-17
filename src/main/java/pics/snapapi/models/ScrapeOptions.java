package pics.snapapi.models;

/**
 * Options for the scrape endpoint ({@code POST /v1/scrape}).
 *
 * <pre>{@code
 * ScrapeOptions opts = ScrapeOptions.builder()
 *     .url("https://example.com")
 *     .type("links")
 *     .pages(3)
 *     .build();
 * }</pre>
 */
public final class ScrapeOptions {

    private final String  url;
    private final String  type;
    private final int     pages;
    private final Integer waitMs;
    private final String  proxy;
    private final Boolean premiumProxy;
    private final boolean blockResources;
    private final String  locale;

    private ScrapeOptions(Builder b) {
        this.url            = b.url;
        this.type           = b.type;
        this.pages          = b.pages;
        this.waitMs         = b.waitMs;
        this.proxy          = b.proxy;
        this.premiumProxy   = b.premiumProxy;
        this.blockResources = b.blockResources;
        this.locale         = b.locale;
    }

    public String  getUrl()            { return url; }
    public String  getType()           { return type; }
    public int     getPages()          { return pages; }
    public Integer getWaitMs()         { return waitMs; }
    public String  getProxy()          { return proxy; }
    public Boolean getPremiumProxy()   { return premiumProxy; }
    public boolean isBlockResources()  { return blockResources; }
    public String  getLocale()         { return locale; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String  url;
        private String  type           = "text";
        private int     pages          = 1;
        private Integer waitMs;
        private String  proxy;
        private Boolean premiumProxy;
        private boolean blockResources = false;
        private String  locale;

        private Builder() {}

        public Builder url(String url)               { this.url = url; return this; }
        public Builder type(String type)             { this.type = type; return this; }
        public Builder pages(int pages)              { this.pages = pages; return this; }
        public Builder waitMs(int ms)                { this.waitMs = ms; return this; }
        public Builder proxy(String proxy)           { this.proxy = proxy; return this; }
        public Builder premiumProxy(boolean v)       { this.premiumProxy = v; return this; }
        public Builder blockResources(boolean v)     { this.blockResources = v; return this; }
        public Builder locale(String locale)         { this.locale = locale; return this; }

        public ScrapeOptions build() {
            if (url == null) throw new IllegalStateException("url is required");
            return new ScrapeOptions(this);
        }
    }
}
