package company.scenario.browsing;

import static company.util.WarmUpFlow.getWarmupUrl;
import static company.util.trace.TraceSetup.networkInterceptor;
import static company.util.trace.TraceSetup.setupWithDevTools;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import company.util.trace.TraceContext;
import company.util.trace.TraceSetup;
import company.util.trace.TracedSession;

public abstract class AbstractBrowserScenarioNetworkInterceptor extends AbstractWebDriverScriptTestCase
{
    private TracedSession tracedSession;

    @BeforeClass
    public static void enableProxy() {
        // When using a proxy, it needs to be set up before the browser is opened, so that it can capture the initial navigation request.
        Configuration.proxyEnabled = false;
    }

    @Before
    public void setupTrace()
    {
        // Generate the Trace ID for this specific test run
        TraceContext.startNewTestTrace();

        // This starts the tracing and returns a WebDriver instance that is already set up to append the headers.
        // TODO get the domain from config
        // TODO fix cert error on posters
        // TODO fix browser resolution
        // TODO initialize everything XLT would normally do
        tracedSession = TraceSetup.setupWithInterceptor(true, true, "posters.xceptance.io");
        //super.setWebDriver(tracedSession.getDriver());
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
