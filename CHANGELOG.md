# Changelog

All notable changes to the SnapAPI Java SDK are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [2.1.0] - 2026-03-23

### Changed

- **Version aligned to 2.1.0** across pom.xml, build.gradle, HttpClient.SDK_VERSION, and README.
- **Build targets Java 11** consistently in both Maven and Gradle configurations.
- CI now tests Java 11, 17, and 21 (added Java 11 to matrix).

### Fixed

- `HttpClient.SDK_VERSION` was "1.0.0" while pom.xml was "1.0.0" and build.gradle was "3.1.0". All now consistently "2.1.0".
- `build.gradle` toolchain targeted Java 17, while `pom.xml` targeted Java 11. Both now target Java 11 for maximum compatibility.

## [1.0.0] - 2026-03-17

### Added

- Initial public release of the official SnapAPI Java SDK.
- `SnapAPIClient` -- main client built with a fluent `SnapAPIClient.builder()`.
- **Screenshot** -- `screenshot(ScreenshotOptions)`, `screenshotToFile(ScreenshotOptions, Path)`
- **PDF** -- `pdf(PdfOptions)`, `pdfToFile(PdfOptions, Path)`
- **Scrape** -- `scrape(ScrapeOptions)`, `scrape(String url)`
- **Extract** -- `extract(ExtractOptions)` plus six convenience methods:
  `extractMarkdown`, `extractArticle`, `extractText`,
  `extractLinks`, `extractImages`, `extractMetadata`
- **Video** -- `video(VideoOptions)`
- **OG Image** -- `ogImage(String url)`, `ogImage(String, String, int, int)`
- **Analyze** -- `analyze(AnalyzeOptions)` (BYOK -- bring your own LLM API key)
- **Usage** -- `getUsage()`, `quota()`
- **Ping** -- `ping()`
- Builder-pattern options classes for all endpoints:
  `ScreenshotOptions`, `PdfOptions`, `ScrapeOptions`, `ExtractOptions`,
  `VideoOptions`, `AnalyzeOptions`
- Typed exception hierarchy:
  `SnapAPIException`, `AuthException`, `RateLimitException`,
  `QuotaException`, `ValidationException`, `TimeoutException`, `NetworkException`
- `RetryPolicy` with builder -- exponential backoff, configurable max retries and delay.
- `HttpClient` backed by `java.net.http.HttpClient` (Java 11+ built-in) -- zero runtime dependencies.
- Manual JSON serialiser (`JsonBuilder`) -- no Jackson/Gson dependency.
- JUnit 5 test suite with OkHttp MockWebServer (83 tests, no network required).
- Both `build.gradle` (Gradle) and `pom.xml` (Maven) build files.
- Java 11+ compatibility.
- MIT license.
