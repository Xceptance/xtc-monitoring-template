package company.util.trace;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

public class AutoTraceListenerDevTools implements WebDriverListener
{
    private final Runnable onSpanRotated;

    // Default constructor for Proxy / Interceptor (they don't need updates)
    public AutoTraceListenerDevTools()
    {
        this.onSpanRotated = () -> {
        }; // Do nothing
    }

    // New constructor for DevTools
    public AutoTraceListenerDevTools(Runnable onSpanRotated)
    {
        this.onSpanRotated = onSpanRotated;
    }

    @Override
    public void beforeGet(WebDriver driver, String url)
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
    public void beforeRefresh(WebDriver.Navigation navigation)
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
