package company.util.trace;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

/**
 * A WebDriverListener that automatically rotates the span ID in the TraceContext before key WebDriver actions. This ensures that each major interaction (like
 * page loads, clicks, and form submissions) is associated with a unique span ID, allowing for more granular tracing of user interactions in tests. The listener
 * should be registered with the WebDriver instance at the start of each test to enable automatic span rotation.
 */
public class AutoTraceListener implements WebDriverListener
{
    private final Runnable onSpanRotated;

    // Default constructor for Proxy / Interceptor (they don't need updates)
    public AutoTraceListener()
    {
        this.onSpanRotated = () -> {
        }; // Do nothing
    }

    // New constructor for DevTools
    public AutoTraceListener(Runnable onSpanRotated)
    {
        this.onSpanRotated = onSpanRotated;
    }

    // TODO discuss which events to include here.

    @Override
    public void beforeGet(WebDriver driver, String url)
    {
        TraceContext.rotateSpan();
        onSpanRotated.run(); // <-- Push new IDs to the browser
    }

    @Override
    public void beforeRefresh(WebDriver.Navigation navigation)
    {
        TraceContext.rotateSpan();
        onSpanRotated.run(); // <-- Push new IDs to the browser
    }

    @Override
    public void beforeClick(WebElement element)
    {
        TraceContext.rotateSpan();
        onSpanRotated.run(); // <-- Push new IDs to the browser
    }

    @Override
    public void beforeSendKeys(WebElement element, CharSequence... keysToSend)
    {
        if (keysToSend == null)
        {
            return;
        }

        for (CharSequence key : keysToSend)
        {
            if (key.toString().contains(Keys.ENTER))
            {
                TraceContext.rotateSpan();
                onSpanRotated.run(); // <-- Push new IDs to the browser
                break;
            }
        }
    }
}
