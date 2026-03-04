package company.scenario.browsing;

import static company.util.trace.TraceSetup.setupWithProxy;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import com.xceptance.xlt.api.tests.AbstractWebDriverTestCase;

import company.util.trace.TraceContext;
import company.util.trace.TracedSession;

public abstract class AbstractBrowserScenarioWithTraceProxy extends AbstractWebDriverTestCase
{
    private TracedSession tracedSession;

    @Before
    public void setup()
    {
        TraceContext.startNewTestTrace();

        // This starts the tracing and returns a WebDriver instance that is already set up to append the headers.
        // TODO get the domain from config
        // TODO fix cert error on posters
        // TODO fix browser resolution
        // TODO initialize everything XLT would normally do
        tracedSession = setupWithProxy(true, true,"httpbin"); // Proxy works
        super.setWebDriver(tracedSession.getDriver());
    }

    @After
    public void teardown()
    {
        // Clear the ThreadLocal IDs
        TraceContext.clear();

        if (tracedSession != null)
        {
            // Unified Cleanup (Proxy stops, Interceptor closes, or DevTools clears)
            try
            {
                tracedSession.close();
            }
            catch (Exception e)
            {
                System.err.println("Failed to clean up tracing session: " + e.getMessage());
            }

            // Close the browser
            if (tracedSession.getDriver() != null)
            {
                tracedSession.getDriver().quit(); // Closes the browser
            }
        }
    }
}
