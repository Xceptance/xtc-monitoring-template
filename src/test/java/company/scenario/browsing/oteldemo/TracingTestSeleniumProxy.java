package company.scenario.browsing.oteldemo;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.startAction;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.stopAction;
import static company.util.trace.TraceSetup.getHeadersToInject;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.events.EventFiringDecorator;

import com.codeborne.selenide.Config;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.proxy.SelenideProxyServer;
import com.xceptance.xlt.api.tests.AbstractWebDriverTestCase;
import com.xceptance.xlt.api.webdriver.XltChromeDriver;

import company.util.trace.AutoTraceListener;
import company.util.trace.TraceContext;

public class TracingTestSeleniumProxy
    extends AbstractWebDriverTestCase
{
    private WebDriver driver;

    @Before
    public void setup()
    {
        TraceContext.startNewTestTrace();

        // This starts the proxy and binds it to Chrome
        driver = createProxyTracedDriver("httpbin");
    }

    @After
    public void teardown()
    {
        TraceContext.clear();
        if (driver != null)
        {
            driver.quit(); // Closes the browser
        }
        // Note: In a robust framework, you'd extract the BrowserUpProxy instance
        // to a class variable so you can explicitly call proxy.stop() here!
    }

    @Test
    public void testHeaders()
    {
        startAction("test");
        // This endpoint returns a JSON payload of all headers it received
        Selenide.open("https://httpbin.org/headers");

        // Grab the raw text of the page (which will be JSON)
        String pageSource = $("body").getText();

        // Print it to your console to visually inspect it
        System.out.println("Headers received by server:\n" + pageSource);

        // Assert that the proxy successfully intercepted and injected the W3C/Datadog headers
        // Note: HTTP headers are often converted to Title-Case or lowercase by servers
        $("body").shouldHave(text("traceparent"));

        // If testing Datadog:
        $("body").shouldHave(text("X-Datadog-Trace-Id"));

        // Assert that the dynamically generated ID actually made it through
        $("body").shouldHave(text(TraceContext.getW3cTraceparent()));

        stopAction();
        // Let the network requests finish before closing the browser
        sleep(2000);
    }

    /*
    public static WebDriver createProxyTracedDriver2(String targetDomain)
    {
        // 1. Start the Embedded Proxy Server

        // Instantiate and start the proxy server MANUALLY
        Config config = new SelenideConfig().proxyEnabled(true);
        SelenideProxyServer selenideProxy = new SelenideProxyServer(config, null);
        selenideProxy.start();

        // 2. Add the Header Injection Filter
        // NOTE: Since TraceContext uses a single shared TraceState (not ThreadLocal),
        // we can safely call the static methods from any thread including the proxy thread.
        selenideProxy.addRequestFilter("trace-interceptor", (request, contents, messageInfo) -> {
            System.out.println("🔗 Intercepted request to " + messageInfo.getUrl() + ", injecting trace headers...");

            System.out.println("domain matching " + targetDomain + ", injecting trace headers...");

            String w3c = TraceContext.getW3cTraceparent();

            System.out.println("Current state:   " + TraceContext.getCurrentState());
            System.out.println("W3C Traceparent: " + TraceContext.getW3cTraceparent());
            System.out.println("Datadog TraceID: " + TraceContext.getDatadogTraceId());

            if (w3c != null)
            {
                request.headers().add("traceparent", w3c);
                System.out.println("🔗 Injected W3C traceparent: " + w3c);
            }

            // Inject Datadog Trace
            String ddTrace = TraceContext.getDatadogTraceId();
            if (ddTrace != null)
            {
                request.headers().add("x-datadog-trace-id", ddTrace);
                request.headers().add("x-datadog-parent-id", TraceContext.getDatadogSpanId());
                request.headers().add("x-datadog-origin", "ciapp-test");
                System.out.println("🔗 Injected Datadog trace: " + ddTrace);
            }

            return null; // Let the request proceed
        });

        // 3. Convert BrowserUp Proxy to a Selenium Proxy configuration
        Proxy seleniumProxy = selenideProxy.getSeleniumProxy();
        // Ensure that localhost is not bypassed by the proxy settings
        if (seleniumProxy != null)
        {
            seleniumProxy.setNoProxy("");
        }

        // 4. Configure the Browser Options to use the proxy and trust its certificates
        ChromeOptions options = new ChromeOptions();
        options.setProxy(seleniumProxy);
        options.setAcceptInsecureCerts(true); // Crucial: Proxies break standard SSL chains!

        // 5. Initialize the raw driver with the proxy options
        WebDriver rawDriver = new ChromeDriver(options);

        // 6. Wrap the driver in our Span Rotator (from the previous step)
        AutoTraceListener spanRotator = new AutoTraceListener();
        WebDriver decoratedDriver = new EventFiringDecorator<>(spanRotator).decorate(rawDriver);

        WebDriverRunner.setWebDriver(decoratedDriver, selenideProxy);

        return decoratedDriver;
    }
     */

    public WebDriver createProxyTracedDriver(String targetDomain)
    {
        // Start the Embedded Proxy Server
        // Instantiate and start the proxy server manually
        Config config = new SelenideConfig().proxyEnabled(true);
        SelenideProxyServer selenideProxy = new SelenideProxyServer(config, null);
        selenideProxy.start();

        // Add the Header Injection Filter
        selenideProxy.addRequestFilter("trace-injector", (request, contents, messageInfo) -> {
            // Capture the current trace state so it can be accessed by the proxy thread

            if (messageInfo.getUrl().contains(targetDomain))
            {
                TraceContext.TraceState state = TraceContext.getCurrentState();

                Map<String, String> headers = getHeadersToInject(state, true, true);
                headers.forEach((key, value) -> request.headers().add(key, value));
            }

            return null; // Let the request proceed
        });

        // Convert BrowserUp Proxy to a Selenium Proxy configuration
        Proxy seleniumProxy = selenideProxy.getSeleniumProxy();
        // Ensure that localhost is not bypassed by the proxy settings
        if (seleniumProxy != null)
        {
            seleniumProxy.setNoProxy("");
        }

        // Configure the Browser Options to use the proxy and trust its certificates
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/chromium");
        options.setProxy(seleniumProxy);
        options.setAcceptInsecureCerts(true); // Crucial: Proxies break standard SSL chains!

        // Initialize the raw driver with the proxy options
        // WebDriver rawDriver = new ChromeDriver(options);
        XltChromeDriver xltDriver = new XltChromeDriver(options);
        // WebDriver originalDriver = ((WebDriverScriptCommands) TestContext.getCurrent().getAdapter()).getUnderlyingWebDriver();

        // Wrap the driver in our Span Rotator (from the previous step)
        AutoTraceListener spanRotator = new AutoTraceListener();
        WebDriver decoratedDriver = new EventFiringDecorator<>(spanRotator).decorate(xltDriver);

        super.setWebDriver(decoratedDriver);

        WebDriverRunner.setWebDriver(decoratedDriver, selenideProxy);

        return decoratedDriver;
    }
}
