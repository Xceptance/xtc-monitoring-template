package company.util.trace;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages trace and span IDs for tests, supporting both W3C Trace Context and Datadog formats. Each test should call startNewTestTrace() once at the beginning
 * to initialize a new trace, and rotateSpan() before any major action to generate a new span ID. The getW3cTraceparent(), getDatadogTraceId(), and
 * getDatadogSpanId() methods provide the current trace and span IDs in the appropriate formats for injection into headers or logs. The clear() method should be
 * called after each test to clean up the state and prevent ID leakage between tests.
 *
 * NOTE: This implementation assumes tests are NOT executed in parallel. If parallel test execution
 * is needed, a more sophisticated approach (e.g., ThreadLocal with explicit state passing) would be required.
 */
public class TraceContext
{
    /**
     * Holds the mutable state for a single trace execution. This class uses AtomicLongs so that it can be safely shared with other threads (like the
     * proxy interceptor thread) while allowing the main test thread to update values (e.g. rotating span IDs).
     */
    public static class TraceState
    {
        private final AtomicLong traceIdHigh = new AtomicLong();

        private final AtomicLong traceIdLow = new AtomicLong();

        private final AtomicLong currentSpanId = new AtomicLong();

        // W3C Format Getter
        public String getW3cTraceparent()
        {
            long high = traceIdHigh.get();
            long low = traceIdLow.get();
            long span = currentSpanId.get();

            // Check if initialized (assuming non-zero means initialized)
            if (high == 0 && low == 0)
                return null;

            String w3cTraceId = String.format("%016x%016x", high, low);
            String w3cSpanId = String.format("%016x", span);
            return "00-" + w3cTraceId + "-" + w3cSpanId + "-01";
        }

        // Datadog Format Getters
        public String getDatadogTraceId()
        {
            long low = traceIdLow.get();
            // Check if initialized (assuming non-zero means initialized)
            if (low == 0)
                return null;
            return Long.toUnsignedString(low);
        }

        public String getDatadogSpanId()
        {
            long span = currentSpanId.get();
            // Check if initialized (assuming non-zero means initialized)
            if (span == 0)
                return null;
            return Long.toUnsignedString(span);
        }

        /**
         * Resets the trace state to uninitialized values.
         */
        void reset()
        {
            traceIdHigh.set(0);
            traceIdLow.set(0);
            currentSpanId.set(0);
        }
    }

    /**
     * Single shared trace state instance. Since tests are not executed in parallel,
     * we use a simple static instance instead of ThreadLocal. This allows all threads
     * (including proxy interceptor threads) to access the same trace state.
     * The AtomicLong fields ensure thread-safe reads and writes.
     */
    private static final TraceState currentTraceState = new TraceState();

    /**
     * Returns the shared trace state. This object can be safely accessed from any thread
     * (e.g., proxy interceptor threads) and will always return the same instance.
     */
    public static TraceState getCurrentState()
    {
        return currentTraceState;
    }

    /**
     * Initializes a new trace with random trace ID values. This should be called once at the start of each test to ensure that traces are not shared between
     * tests. Span IDs can be rotated as needed throughout the test using rotateSpan().
     */
    public static void startNewTestTrace()
    {
        TraceState state = getCurrentState();
        state.traceIdHigh.set(ThreadLocalRandom.current().nextLong());
        state.traceIdLow.set(ThreadLocalRandom.current().nextLong());

        System.out.println("🚀 Starting new test trace");
        System.out.println("   Initial Traceparent: " + getW3cTraceparent());

        rotateSpan(); // Generate the first span ID for the initial page load
    }

    /**
     * Generates a new random span ID and updates the current trace state. This should be called before any major action to ensure that each action is
     * associated with a unique span ID in the trace. If using the AutoTraceListener, this method will be called automatically before key WebDriver actions.
     */
    public static void rotateSpan()
    {
        System.out.println("🔄 Rotating span ID for new action");
        getCurrentState().currentSpanId.set(ThreadLocalRandom.current().nextLong());
        System.out.println("   New Traceparent:     " + getW3cTraceparent());
    }

    /**
     * Resets the trace state to uninitialized values. This should be called after each test
     * to prevent ID leakage between tests.
     */
    public static void clear()
    {
        currentTraceState.reset();
    }

    public static String getW3cTraceparent()
    {
        return getCurrentState().getW3cTraceparent();
    }

    public static String getDatadogTraceId()
    {
        return getCurrentState().getDatadogTraceId();
    }

    public static String getDatadogSpanId()
    {
        return getCurrentState().getDatadogSpanId();
    }
}
