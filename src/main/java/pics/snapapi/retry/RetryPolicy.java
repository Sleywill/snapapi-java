package pics.snapapi.retry;

import pics.snapapi.RateLimitException;
import pics.snapapi.SnapAPIException;

/**
 * Encapsulates retry configuration and the decision of whether to retry a failed request.
 *
 * <p>The default policy retries on HTTP 429 (rate limit) and HTTP 5xx (server errors),
 * using exponential backoff with jitter.
 *
 * <pre>{@code
 * RetryPolicy policy = RetryPolicy.builder()
 *     .maxRetries(5)
 *     .initialDelayMs(500)
 *     .build();
 * }</pre>
 */
public final class RetryPolicy {

    /** Default maximum number of retry attempts. */
    public static final int    DEFAULT_MAX_RETRIES   = 3;
    /** Default initial backoff delay in milliseconds. */
    public static final long   DEFAULT_INITIAL_DELAY = 500L;
    /** Maximum backoff cap in milliseconds. */
    public static final long   MAX_DELAY_MS          = 30_000L;

    private final int  maxRetries;
    private final long initialDelayMs;

    private RetryPolicy(Builder b) {
        this.maxRetries     = b.maxRetries;
        this.initialDelayMs = b.initialDelayMs;
    }

    /** @return Maximum number of retry attempts. */
    public int getMaxRetries() { return maxRetries; }

    /** @return Initial backoff delay in milliseconds. */
    public long getInitialDelayMs() { return initialDelayMs; }

    /**
     * Determine whether a given exception should trigger a retry.
     *
     * @param ex      The exception thrown by the last attempt.
     * @param attempt Current attempt number (1 = first try).
     * @return {@code true} if another attempt should be made.
     */
    public boolean shouldRetry(SnapAPIException ex, int attempt) {
        if (attempt > maxRetries) return false;
        if (ex instanceof RateLimitException) return true;
        int sc = ex.getStatusCode();
        return sc >= 500 || sc == 0;
    }

    /**
     * Compute how long to wait before the next attempt (exponential backoff, capped).
     *
     * <p>For rate-limit errors the {@code Retry-After} value takes precedence.
     *
     * @param attempt The retry attempt number (1 = first retry).
     * @param ex      The exception that triggered the retry.
     * @return Delay in milliseconds.
     */
    public long computeDelayMs(int attempt, SnapAPIException ex) {
        if (ex instanceof RateLimitException) {
            RateLimitException rl = (RateLimitException) ex;
            long raMs = (long) (rl.getRetryAfter() * 1000);
            return Math.min(raMs, MAX_DELAY_MS);
        }
        // exponential backoff: initialDelay * 2^(attempt-1)
        long delay = initialDelayMs * (1L << (attempt - 1));
        return Math.min(delay, MAX_DELAY_MS);
    }

    /** @return A new {@link Builder} with default values. */
    public static Builder builder() {
        return new Builder();
    }

    /** @return A {@link RetryPolicy} with all default values. */
    public static RetryPolicy defaults() {
        return builder().build();
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /** Builder for {@link RetryPolicy}. */
    public static final class Builder {
        private int  maxRetries     = DEFAULT_MAX_RETRIES;
        private long initialDelayMs = DEFAULT_INITIAL_DELAY;

        private Builder() {}

        /**
         * Maximum number of retry attempts (default: 3).
         * Set to {@code 0} to disable retries.
         */
        public Builder maxRetries(int maxRetries) {
            if (maxRetries < 0) throw new IllegalArgumentException("maxRetries must be >= 0");
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Initial backoff delay in milliseconds (default: 500).
         * Doubles with each attempt.
         */
        public Builder initialDelayMs(long initialDelayMs) {
            if (initialDelayMs < 0) throw new IllegalArgumentException("initialDelayMs must be >= 0");
            this.initialDelayMs = initialDelayMs;
            return this;
        }

        /** Build the {@link RetryPolicy}. */
        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
