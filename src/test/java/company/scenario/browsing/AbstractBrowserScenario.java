package company.scenario.browsing;

import static company.util.WarmUpFlow.getWarmupUrl;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.webdriver.WebDriverScriptCommands;

import company.util.trace.AutoTraceListener;
import company.util.trace.TraceContext;
import company.util.trace.TraceSetup;

public abstract class AbstractBrowserScenario extends AbstractWebDriverScriptTestCase
{
    private AutoCloseable traceCleaner;

    @BeforeClass
    public static void enableProxy() {
        // When using a proxy, it needs to be set up before the browser is opened, so that it can capture the initial navigation request.
        Configuration.proxyEnabled = false;
    }

    @Before
    public void setup()
    {
        // Get the underlying XLT WebDriver and wrap it with our listener manually
        // This ensures events like 'beforeGet' are intercepted regardless of how Selenide manages drivers
        WebDriver originalDriver = ((WebDriverScriptCommands) TestContext.getCurrent().getAdapter()).getUnderlyingWebDriver();

        AutoTraceListener listener = new AutoTraceListener();
        WebDriver decoratedDriver = new EventFiringDecorator<WebDriver>(listener).decorate(originalDriver);

        WebDriverRunner.setWebDriver(decoratedDriver);

        // Generate the Trace ID for this specific test run
        TraceContext.startNewTestTrace();

        // Ensure browser is "ready" by opening blank page, allowing us to attach interceptor safely
        Selenide.open("about:blank");

        // TODO get the domain from config
        // Setup interceptor, so it catches the very first real request
        // traceCleaner = TraceSetup.setup(true, true, "posters.xceptance.io");

        // W3C testing against open telemetry demo running on localhost
        //traceCleaner = TraceSetup.setup(true, false, "localhost");

        Selenide.open(getWarmupUrl());
    }

    @After
    public void teardownTest()
    {
        // Clean up thread locals
        TraceContext.clear();

        // If using Interceptor, close it to prevent memory leaks across tests
        if (traceCleaner != null)
        {
            try
            {
                traceCleaner.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
