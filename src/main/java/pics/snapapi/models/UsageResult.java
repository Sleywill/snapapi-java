package pics.snapapi.models;

/**
 * Represents the API usage result returned by {@code GET /v1/usage}.
 *
 * <p>Since the SDK returns raw {@code byte[]} to avoid forcing a JSON library dependency,
 * this class is provided as a convenience POJO that you can populate yourself after
 * parsing the response with your preferred library (Jackson, Gson, etc.).
 *
 * <p>Example with Jackson:
 * <pre>{@code
 * ObjectMapper mapper = new ObjectMapper();
 * byte[] json = client.getUsage();
 * UsageResult usage = mapper.readValue(json, UsageResult.class);
 * System.out.printf("%d / %d calls used%n", usage.getUsed(), usage.getLimit());
 * }</pre>
 */
public final class UsageResult {

    private int    used;
    private int    limit;
    private int    remaining;
    private String resetAt;
    private String plan;

    /** No-arg constructor for JSON deserialisation. */
    public UsageResult() {}

    /**
     * Construct a UsageResult with all fields.
     *
     * @param used      Calls used in the current billing period.
     * @param limit     Total calls allowed per billing period.
     * @param remaining Calls remaining before quota is exhausted.
     * @param resetAt   ISO-8601 timestamp when the quota resets.
     * @param plan      Current subscription plan name.
     */
    public UsageResult(int used, int limit, int remaining, String resetAt, String plan) {
        this.used      = used;
        this.limit     = limit;
        this.remaining = remaining;
        this.resetAt   = resetAt;
        this.plan      = plan;
    }

    /** Number of API calls used in the current billing period. */
    public int getUsed() {
        return used;
    }

    /** Sets {@code used}. Used by JSON deserialisation. */
    public void setUsed(int used) {
        this.used = used;
    }

    /** Total API calls allowed per billing period. */
    public int getLimit() {
        return limit;
    }

    /** Sets {@code limit}. Used by JSON deserialisation. */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /** API calls remaining before quota is exhausted. */
    public int getRemaining() {
        return remaining;
    }

    /** Sets {@code remaining}. Used by JSON deserialisation. */
    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    /**
     * ISO-8601 timestamp when the quota resets (start of next billing period).
     * May be {@code null} for unlimited plans.
     */
    public String getResetAt() {
        return resetAt;
    }

    /** Sets {@code resetAt}. Used by JSON deserialisation. */
    public void setResetAt(String resetAt) {
        this.resetAt = resetAt;
    }

    /**
     * Current subscription plan (e.g. {@code "free"}, {@code "starter"}, {@code "pro"}).
     * May be {@code null} if not returned by the API.
     */
    public String getPlan() {
        return plan;
    }

    /** Sets {@code plan}. Used by JSON deserialisation. */
    public void setPlan(String plan) {
        this.plan = plan;
    }

    @Override
    public String toString() {
        return "UsageResult{used=" + used
            + ", limit=" + limit
            + ", remaining=" + remaining
            + ", resetAt='" + resetAt + '\''
            + ", plan='" + plan + '\''
            + '}';
    }
}
