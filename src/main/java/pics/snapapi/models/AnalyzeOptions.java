package pics.snapapi.models;

/**
 * Options for the analyze endpoint ({@code POST /v1/analyze}).
 *
 * <pre>{@code
 * AnalyzeOptions opts = AnalyzeOptions.builder()
 *     .url("https://example.com")
 *     .prompt("Summarize this page in 3 bullet points.")
 *     .provider("openai")
 *     .apiKey("sk-...")
 *     .build();
 * }</pre>
 */
public final class AnalyzeOptions {

    private final String  url;
    private final String  prompt;
    private final String  provider;
    private final String  apiKey;
    private final String  model;
    private final String  jsonSchema;
    private final Boolean includeScreenshot;
    private final Boolean includeMetadata;
    private final Integer maxContentLength;
    private final Integer timeout;
    private final boolean blockAds;
    private final boolean blockCookieBanners;
    private final String  waitFor;

    private AnalyzeOptions(Builder b) {
        this.url                = b.url;
        this.prompt             = b.prompt;
        this.provider           = b.provider;
        this.apiKey             = b.apiKey;
        this.model              = b.model;
        this.jsonSchema         = b.jsonSchema;
        this.includeScreenshot  = b.includeScreenshot;
        this.includeMetadata    = b.includeMetadata;
        this.maxContentLength   = b.maxContentLength;
        this.timeout            = b.timeout;
        this.blockAds           = b.blockAds;
        this.blockCookieBanners = b.blockCookieBanners;
        this.waitFor            = b.waitFor;
    }

    public String  getUrl()               { return url; }
    public String  getPrompt()            { return prompt; }
    public String  getProvider()          { return provider; }
    public String  getApiKey()            { return apiKey; }
    public String  getModel()             { return model; }
    public String  getJsonSchema()        { return jsonSchema; }
    public Boolean getIncludeScreenshot() { return includeScreenshot; }
    public Boolean getIncludeMetadata()   { return includeMetadata; }
    public Integer getMaxContentLength()  { return maxContentLength; }
    public Integer getTimeout()           { return timeout; }
    public boolean isBlockAds()           { return blockAds; }
    public boolean isBlockCookieBanners() { return blockCookieBanners; }
    public String  getWaitFor()           { return waitFor; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String  url;
        private String  prompt;
        private String  provider;
        private String  apiKey;
        private String  model;
        private String  jsonSchema;
        private Boolean includeScreenshot;
        private Boolean includeMetadata;
        private Integer maxContentLength;
        private Integer timeout;
        private boolean blockAds           = false;
        private boolean blockCookieBanners = false;
        private String  waitFor;

        private Builder() {}

        public Builder url(String url)                    { this.url = url; return this; }
        public Builder prompt(String prompt)              { this.prompt = prompt; return this; }
        public Builder provider(String provider)          { this.provider = provider; return this; }
        public Builder apiKey(String apiKey)              { this.apiKey = apiKey; return this; }
        public Builder model(String model)                { this.model = model; return this; }
        public Builder jsonSchema(String schema)          { this.jsonSchema = schema; return this; }
        public Builder includeScreenshot(boolean v)       { this.includeScreenshot = v; return this; }
        public Builder includeMetadata(boolean v)         { this.includeMetadata = v; return this; }
        public Builder maxContentLength(int n)            { this.maxContentLength = n; return this; }
        public Builder timeout(int ms)                    { this.timeout = ms; return this; }
        public Builder blockAds(boolean v)                { this.blockAds = v; return this; }
        public Builder blockCookieBanners(boolean v)      { this.blockCookieBanners = v; return this; }
        public Builder waitFor(String s)                  { this.waitFor = s; return this; }

        public AnalyzeOptions build() {
            if (url == null)    throw new IllegalStateException("url is required");
            if (prompt == null) throw new IllegalStateException("prompt is required");
            return new AnalyzeOptions(this);
        }
    }
}
