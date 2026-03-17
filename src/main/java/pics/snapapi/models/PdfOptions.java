package pics.snapapi.models;

import java.util.Map;

/**
 * Options for PDF generation (uses the screenshot endpoint with {@code format=pdf}).
 *
 * <pre>{@code
 * PdfOptions opts = PdfOptions.builder()
 *     .url("https://example.com")
 *     .pageSize("a4")
 *     .landscape(true)
 *     .build();
 * }</pre>
 */
public final class PdfOptions {

    private final String  url;
    private final String  html;
    private final String  pageSize;
    private final boolean landscape;
    private final Map<String, String> margins;
    private final String  headerTemplate;
    private final String  footerTemplate;
    private final boolean displayHeaderFooter;
    private final Double  scale;
    private final int     delay;
    private final String  waitForSelector;

    private PdfOptions(Builder b) {
        this.url                  = b.url;
        this.html                 = b.html;
        this.pageSize             = b.pageSize;
        this.landscape            = b.landscape;
        this.margins              = b.margins;
        this.headerTemplate       = b.headerTemplate;
        this.footerTemplate       = b.footerTemplate;
        this.displayHeaderFooter  = b.displayHeaderFooter;
        this.scale                = b.scale;
        this.delay                = b.delay;
        this.waitForSelector      = b.waitForSelector;
    }

    public String  getUrl()                  { return url; }
    public String  getHtml()                 { return html; }
    public String  getPageSize()             { return pageSize; }
    public boolean isLandscape()             { return landscape; }
    public Map<String, String> getMargins()  { return margins; }
    public String  getHeaderTemplate()       { return headerTemplate; }
    public String  getFooterTemplate()       { return footerTemplate; }
    public boolean isDisplayHeaderFooter()   { return displayHeaderFooter; }
    public Double  getScale()                { return scale; }
    public int     getDelay()                { return delay; }
    public String  getWaitForSelector()      { return waitForSelector; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String  url;
        private String  html;
        private String  pageSize            = "a4";
        private boolean landscape           = false;
        private Map<String, String> margins;
        private String  headerTemplate;
        private String  footerTemplate;
        private boolean displayHeaderFooter = false;
        private Double  scale;
        private int     delay               = 0;
        private String  waitForSelector;

        private Builder() {}

        public Builder url(String url)                        { this.url = url; return this; }
        public Builder html(String html)                      { this.html = html; return this; }
        public Builder pageSize(String ps)                    { this.pageSize = ps; return this; }
        public Builder landscape(boolean v)                   { this.landscape = v; return this; }
        public Builder margins(Map<String, String> m)         { this.margins = m; return this; }
        public Builder headerTemplate(String t)               { this.headerTemplate = t; return this; }
        public Builder footerTemplate(String t)               { this.footerTemplate = t; return this; }
        public Builder displayHeaderFooter(boolean v)         { this.displayHeaderFooter = v; return this; }
        public Builder scale(double s)                        { this.scale = s; return this; }
        public Builder delay(int ms)                          { this.delay = ms; return this; }
        public Builder waitForSelector(String s)              { this.waitForSelector = s; return this; }

        public PdfOptions build() {
            if (url == null && html == null) throw new IllegalStateException("url or html is required");
            return new PdfOptions(this);
        }
    }
}
