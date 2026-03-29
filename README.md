# SnapAPI Java SDK

Official Java SDK for [SnapAPI](https://snapapi.pics) — the lightning-fast screenshot, scrape, extract, PDF, video, and AI-analyze API.

[![Maven Central](https://img.shields.io/maven-central/v/pics.snapapi/snapapi-java?label=maven-central&color=C71A36)](https://central.sonatype.com/artifact/pics.snapapi/snapapi-java)
[![CI](https://github.com/Sleywill/snapapi-java/actions/workflows/ci.yml/badge.svg)](https://github.com/Sleywill/snapapi-java/actions)
[![Java 11+](https://img.shields.io/badge/Java-11%2B-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

## Requirements

- Java 11+
- Zero runtime dependencies (uses `java.net.http.HttpClient` built into Java 11)

## Installation

### Maven

```xml
<dependency>
    <groupId>pics.snapapi</groupId>
    <artifactId>snapapi-java</artifactId>
    <version>2.1.0</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
implementation("pics.snapapi:snapapi-java:2.1.0")
```

### Gradle (Groovy DSL)

```groovy
implementation 'pics.snapapi:snapapi-java:2.1.0'
```

## Quick Start

```java
import pics.snapapi.SnapAPIClient;
import pics.snapapi.models.ScreenshotOptions;
import java.nio.file.Files;
import java.nio.file.Path;

SnapAPIClient client = SnapAPIClient.builder()
    .apiKey("sk_live_...")
    .build();

// Take a screenshot
byte[] png = client.screenshot(ScreenshotOptions.builder()
    .url("https://example.com")
    .build());

Files.write(Path.of("screenshot.png"), png);
```

## Features

- **Zero runtime dependencies** — uses only `java.net.http.HttpClient` (Java 11+)
- **Builder pattern** for client and all options — `SnapAPIClient.builder()...build()`
- **Automatic retries** with exponential backoff on 429 / 5xx
- **Rate limit handling** with `Retry-After` header support
- **Typed exception hierarchy** — `AuthException`, `RateLimitException`, `QuotaException`, etc.
- **All endpoints** — screenshot, PDF, scrape, extract, video, OG image, analyze, usage, ping
- **Convenience methods** — `screenshotToFile`, `pdfToFile`, `extractMarkdown`, `extractArticle`, etc.

## Configuration

```java
SnapAPIClient client = SnapAPIClient.builder()
    .apiKey("sk_live_...")
    .baseUrl("https://api.snapapi.pics")  // Default
    .timeoutSecs(60)                       // Default: 60s
    .maxRetries(3)                         // Auto-retry on 429/5xx. Default: 3
    .initialDelayMs(500)                   // Initial backoff. Default: 500ms
    .build();
```

### Custom Retry Policy

```java
import pics.snapapi.retry.RetryPolicy;

RetryPolicy policy = RetryPolicy.builder()
    .maxRetries(5)
    .initialDelayMs(200)
    .build();

SnapAPIClient client = SnapAPIClient.builder()
    .apiKey("sk_live_...")
    .retryPolicy(policy)
    .build();
```

---

## API Reference

### Screenshot

Capture a screenshot of any URL, raw HTML, or Markdown.

```java
// Basic PNG screenshot
byte[] png = client.screenshot(ScreenshotOptions.builder()
    .url("https://example.com")
    .build());

// Full-page dark-mode WebP
byte[] webp = client.screenshot(ScreenshotOptions.builder()
    .url("https://github.com")
    .format("webp")
    .fullPage(true)
    .darkMode(true)
    .blockAds(true)
    .width(1440)
    .height(900)
    .build());

// From raw HTML
byte[] png = client.screenshot(ScreenshotOptions.builder()
    .html("<h1 style='color:red'>Hello!</h1>")
    .build());

// Capture a specific element
byte[] png = client.screenshot(ScreenshotOptions.builder()
    .url("https://example.com")
    .selector("#main-content")
    .build());

// Save to file
client.screenshotToFile(
    ScreenshotOptions.builder().url("https://example.com").build(),
    Path.of("./output/screenshot.png")
);

// Mobile emulation
byte[] png = client.screenshot(ScreenshotOptions.builder()
    .url("https://example.com")
    .device("iPhone 14")
    .isMobile(true)
    .hasTouch(true)
    .deviceScaleFactor(2.0)
    .build());
```

**Key options:**

| Option | Type | Description |
|--------|------|-------------|
| `url` | String | URL to capture |
| `html` | String | Raw HTML string to render |
| `markdown` | String | Markdown string to render |
| `format` | String | `"png"`, `"jpeg"`, `"webp"`, `"avif"` (default: `"png"`) |
| `quality` | int | Quality 1–100 (JPEG/WebP) |
| `width` | int | Viewport width (default: 1280) |
| `height` | int | Viewport height (default: 800) |
| `fullPage` | boolean | Capture full scrollable page |
| `selector` | String | CSS selector — capture that element only |
| `delay` | int | Extra delay before capture (ms) |
| `waitForSelector` | String | Wait for this CSS selector to appear |
| `darkMode` | boolean | Emulate dark mode |
| `blockAds` | boolean | Block ad networks |
| `blockTrackers` | boolean | Block trackers |
| `blockCookieBanners` | boolean | Block cookie banners |
| `css` | String | Custom CSS to inject |
| `javascript` | String | JS to execute before capture |

**Returns:** `byte[]` — raw image bytes, or JSON bytes when `storage`/`webhookUrl` is set.

---

### PDF

```java
// Basic A4 PDF
byte[] pdf = client.pdf(PdfOptions.builder()
    .url("https://example.com")
    .build());
Files.write(Path.of("output.pdf"), pdf);

// Save to file directly
client.pdfToFile(
    PdfOptions.builder().url("https://example.com").build(),
    Path.of("output.pdf")
);

// Letter landscape with margins
byte[] pdf = client.pdf(PdfOptions.builder()
    .url("https://example.com")
    .pageSize("letter")
    .landscape(true)
    .margins(Map.of("top", "1cm", "right", "1cm", "bottom", "1cm", "left", "1cm"))
    .build());

// From HTML
byte[] pdf = client.pdf(PdfOptions.builder()
    .html("<h1>Invoice #1234</h1><p>Amount: $99.00</p>")
    .pageSize("a4")
    .build());
```

**Returns:** `byte[]` — raw PDF bytes.

---

### Scrape

```java
// Scrape text
byte[] json = client.scrape(ScrapeOptions.builder()
    .url("https://example.com")
    .type("text")
    .build());

// Scrape links from 3 pages
byte[] json = client.scrape(ScrapeOptions.builder()
    .url("https://example.com")
    .type("links")
    .pages(3)
    .build());

// Quick scrape with shorthand
byte[] json = client.scrape("https://example.com");
```

**Returns:** `byte[]` — JSON response bytes. Parse with your preferred library.

---

### Extract

```java
// Extract as Markdown
byte[] json = client.extractMarkdown("https://example.com");

// Extract main article
byte[] json = client.extractArticle("https://example.com/blog/post");

// Extract plain text
byte[] json = client.extractText("https://example.com");

// Extract all links
byte[] json = client.extractLinks("https://example.com");

// Extract image URLs
byte[] json = client.extractImages("https://example.com");

// Extract page metadata
byte[] json = client.extractMetadata("https://example.com");

// Full options
byte[] json = client.extract(ExtractOptions.builder()
    .url("https://example.com")
    .type("markdown")
    .selector("#article-content")
    .maxLength(5000)
    .cleanOutput(true)
    .build());
```

**Returns:** `byte[]` — JSON response bytes.

---

### Video

```java
// Basic MP4
byte[] video = client.video(VideoOptions.builder()
    .url("https://example.com")
    .build());
Files.write(Path.of("recording.mp4"), video);

// With scroll animation
byte[] video = client.video(VideoOptions.builder()
    .url("https://example.com")
    .format("mp4")
    .duration(10)
    .scrolling(true)
    .scrollSpeed(200)
    .width(1280)
    .height(720)
    .build());

// Dark mode GIF
byte[] gif = client.video(VideoOptions.builder()
    .url("https://example.com")
    .format("gif")
    .duration(5)
    .darkMode(true)
    .build());
```

**Returns:** `byte[]` — raw video bytes.

---

### OG Image

```java
// Default 1200x630 PNG
byte[] og = client.ogImage("https://example.com");
Files.write(Path.of("og.png"), og);

// Custom dimensions
byte[] og = client.ogImage("https://example.com", "jpeg", 1200, 628);
```

**Returns:** `byte[]` — raw image bytes.

---

### Analyze (LLM)

```java
byte[] json = client.analyze(AnalyzeOptions.builder()
    .url("https://example.com")
    .prompt("Summarize this page in 3 bullet points.")
    .provider("openai")
    .apiKey("sk-...")
    .build());
```

**Returns:** `byte[]` — JSON response bytes with the analysis result.

---

### Usage and Quota

```java
byte[] json = client.getUsage();
// {"used":42,"limit":1000,"remaining":958,"resetAt":"..."}

// Alias
byte[] json = client.quota();
```

---

### Health Check

```java
byte[] json = client.ping();
// {"status":"ok","timestamp":1710000000000}
```

---

## Error Handling

All exceptions extend `SnapAPIException` which extends `RuntimeException`.

```java
try {
    byte[] png = client.screenshot(ScreenshotOptions.builder()
        .url("https://example.com")
        .build());
} catch (AuthException e) {
    System.err.println("Bad API key: " + e.getMessage());
} catch (RateLimitException e) {
    System.err.printf("Rate limited. Retry after: %.1fs%n", e.getRetryAfter());
} catch (QuotaException e) {
    System.err.println("Quota exhausted. Upgrade your plan.");
} catch (ValidationException e) {
    System.err.println("Invalid params: " + e.getMessage());
} catch (TimeoutException e) {
    System.err.println("Request timed out.");
} catch (NetworkException e) {
    System.err.println("Network failure: " + e.getMessage());
} catch (SnapAPIException e) {
    System.err.printf("API error %d (%s): %s%n",
        e.getStatusCode(), e.getErrorCode(), e.getMessage());
}
```

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `AuthException` | 401 / 403 | Invalid or missing API key |
| `RateLimitException` | 429 | Too many requests (auto-retried) |
| `QuotaException` | 402 | Monthly quota exhausted |
| `ValidationException` | 422 | Invalid request parameters |
| `TimeoutException` | — | Request timed out |
| `NetworkException` | — | DNS / connection failure |
| `SnapAPIException` | any | Base class for all SnapAPI exceptions |

---

## Parsing Responses

The SDK returns raw `byte[]` for JSON responses to avoid forcing a JSON library dependency. Parse with your preferred library:

### With Jackson

```java
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();
byte[] json = client.getUsage();
Map<?, ?> usage = mapper.readValue(json, Map.class);
System.out.println("Used: " + usage.get("used"));
```

### With Gson

```java
import com.google.gson.Gson;

Gson gson = new Gson();
byte[] json = client.extractMarkdown("https://example.com");
Map<?, ?> result = gson.fromJson(new String(json), Map.class);
System.out.println(result.get("content"));
```

### With org.json

```java
import org.json.JSONObject;

byte[] json = client.ping();
JSONObject obj = new JSONObject(new String(json));
System.out.println(obj.getString("status")); // "ok"
```

---

## Retry Logic

Automatic retries are enabled by default for:
- **HTTP 429** (rate limited) — waits for the `Retry-After` header value.
- **HTTP 5xx** (server errors) — exponential backoff.
- **Network timeouts** — exponential backoff.

Default: 3 retries, initial delay 500ms (doubles each attempt, capped at 30s).

```java
SnapAPIClient client = SnapAPIClient.builder()
    .apiKey("sk_live_...")
    .maxRetries(5)       // Up to 5 retries
    .initialDelayMs(250) // Start at 250ms, then 500ms, 1s, 2s, 4s
    .build();
```

To disable retries:

```java
SnapAPIClient client = SnapAPIClient.builder()
    .apiKey("sk_live_...")
    .maxRetries(0)
    .build();
```

---

## Running Tests

```bash
# Maven
mvn test

# Gradle
./gradlew test
```

Tests use OkHttp MockWebServer — no real network access required.

---

## Development

```bash
git clone https://github.com/Sleywill/snapapi-java
cd snapapi-java

# Compile
mvn compile

# Run tests
mvn test

# Build JAR
mvn package -DskipTests
```

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](./CONTRIBUTING.md) before submitting a PR.

Found a bug? [Open an issue](https://github.com/Sleywill/snapapi-java/issues/new?template=bug_report.md).
Have an idea? [Request a feature](https://github.com/Sleywill/snapapi-java/issues/new?template=feature_request.md).

---

## License

MIT — see [LICENSE](LICENSE).

## Links

- [SnapAPI Website](https://snapapi.pics)
- [API Documentation](https://snapapi.pics/docs)
- [Maven Central](https://central.sonatype.com/artifact/pics.snapapi/snapapi-java)
- [GitHub](https://github.com/Sleywill/snapapi-java)
- [Changelog](./CHANGELOG.md)
- [Report Issues](https://github.com/Sleywill/snapapi-java/issues)
