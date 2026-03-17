# Changelog

All notable changes to the SnapAPI Java SDK are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [3.1.0] - 2026-03-17

### Added

- Initial public release of the official SnapAPI Java SDK.
- `SnapAPIClient` — main client built with a fluent `SnapAPIClient.builder()`.
- **Screenshot** — `screenshot(ScreenshotOptions)`, `screenshotToFile(ScreenshotOptions, Path)`
- **PDF** — `pdf(PdfOptions)`, `pdfToFile(PdfOptions, Path)`
- **Scrape** — `scrape(ScrapeOptions)`, `scrape(String url)`
- **Extract** — `extract(ExtractOptions)` plus six convenience methods:
  `extractMarkdown`, `extractArticle`, `extractText`,
  `extractLinks`, `extractImages`, `extractMetadata`
- **Video** — `video(VideoOptions)`
- **OG Image** — `ogImage(String url)`, `ogImage(String, String, int, int)`
- **Analyze** — `analyze(AnalyzeOptions)` (BYOK — bring your own LLM API key)
- **Usage** — `getUsage()`, `quota()`
- **Ping** — `ping()`
- Builder-pattern options classes for all endpoints:
  `ScreenshotOptions`, `PdfOptions`, `ScrapeOptions`, `ExtractOptions`,
  `VideoOptions`, `AnalyzeOptions`
- Typed exception hierarchy:
  `SnapAPIException`, `AuthException`, `RateLimitException`,
  `QuotaException`, `ValidationException`, `TimeoutException`, `NetworkException`
- `RetryPolicy` with builder — exponential backoff, configurable max retries and delay.
- `RetryPolicy.builder().maxRetries(5).initialDelayMs(200).build()`
- `HttpClient` backed by `java.net.http.HttpClient` (Java 11+ built-in) — zero runtime dependencies.
- Manual JSON serialiser (`JsonBuilder`) — no Jackson/Gson dependency.
- JUnit 5 test suite with embedded `HttpServer` (27 tests, no network required).
- Both `build.gradle` (Gradle) and `pom.xml` (Maven) build files.
- Java 17+ compatibility (uses switch expressions, pattern matching for instanceof, sealed classes).
- MIT license.
