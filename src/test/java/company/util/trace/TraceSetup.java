package company.util.trace;

import static com.codeborne.selenide.Selenide.open;
import static company.util.trace.BrowserSetup.setupWebDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.devtools.v144.network.Network;
import org.openqa.selenium.devtools.v144.network.model.Headers;
import org.openqa.selenium.remote.http.Contents;
import org.openqa.selenium.remote.http.Filter;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.support.events.EventFiringDecorator;

import com.codeborne.selenide.Config;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.proxy.SelenideProxyServer;
import com.xceptance.xlt.api.webdriver.XltChromeDriver;

/**
 * Provides methods to set up trace header injection for tests, supporting both Selenide's built-in proxy and Selenium 4's NetworkInterceptor. The
 * setupWithProxy() method configures the Selenide proxy to inject trace headers into outgoing requests matching a target domain, while the
 * setupWithInterceptor() method creates a NetworkInterceptor that performs the same injection logic. Both methods use the getHeadersToInject() helper to
 * determine which headers to add based on whether W3C Trace Context and/or Datadog formats are enabled. The interceptor returned by setupWithInterceptor() must
 * be closed at the end of the test to clean up resources.
 */
public class TraceSetup
{
    public static NetworkInterceptor networkInterceptor = null;

    /**
     * Determines which trace headers to inject based on the current trace state and configuration flags. If useW3c is true and a W3C traceparent is available,
     * it will be included. If useDatadog is true and Datadog trace and span IDs are available, they will be included along with origin and sampling priority
     * headers to ensure the trace is properly recognized in Datadog APM.
     *
     * @param state
     *     The current trace state containing trace and span IDs
     * @param useW3c
     *     Whether to include W3C Trace Context headers
     * @param useDatadog
     *     Whether to include Datadog trace headers
     * @return A map of headers to inject into outgoing requests
     */
    public static Map<String, String> getHeadersToInject(TraceContext.TraceState state, boolean useW3c, boolean useDatadog)
    {
        Map<String, String> headers = new HashMap<>();

        if (useW3c && state.getW3cTraceparent() != null)
        {
            headers.put("traceparent", state.getW3cTraceparent());
        }

        if (useDatadog && state.getDatadogTraceId() != null)
        {
            headers.put("x-datadog-trace-id", state.getDatadogTraceId());
            headers.put("x-datadog-parent-id", state.getDatadogSpanId());
            headers.put("x-datadog-origin", "ciapp-test"); // Tags trace as an automated test
            headers.put("x-datadog-sampling-priority", "1"); // Force the backend to keep this trace
        }
        return headers;
    }

    /**
     * Use a Selenide Proxy to inject trace headers. Should be possible to use this in all projects. ATTENTION: use {@code super.setWebDriver(decoratedDriver);}
     * in the base class extending {@code AbstractWebDriverTestCase}
     */
    public static TracedSession setupWithProxy(boolean useW3c, boolean useDatadog, String targetDomain)
    {
        System.out.println("================================================");
        System.out.println("Trace Injection: Using Selenide Embedded Proxy Server");

        // Start the Embedded Proxy Server
        // Instantiate and start the proxy server manually
        Config config = new SelenideConfig().proxyEnabled(true);
        SelenideProxyServer selenideProxy = new SelenideProxyServer(config, null);
        selenideProxy.start();

        // Add the Header Injection Filter
        selenideProxy.addRequestFilter("trace-injector", (request, contents, messageInfo) -> {
            // Capture the current trace state so it can be accessed by the proxy thread
            TraceContext.TraceState state = TraceContext.getCurrentState();

            Map<String, String> headers = getHeadersToInject(state, useW3c, useDatadog);
            headers.forEach((key, value) -> request.headers().add(key, value));

            return null; // Let the request proceed
        });

        // Extract the Selenium-compatible Proxy object
        Proxy seleniumProxy = selenideProxy.getSeleniumProxy();
        // Ensure that localhost is not bypassed by the proxy settings
        if (seleniumProxy != null)
        {
            seleniumProxy.setNoProxy("");
        }

        // Configure the Browser Options to use the proxy and trust its certificates
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/chromium"); // set the chromium binary path - TODO use config value for this
        options.setProxy(seleniumProxy);
        options.addArguments(
            "--ignore-certificate-errors",
            "--remote-allow-origins=*",
            "--allow-insecure-localhost", // Helpful if testing against localhost
            "--ignore-ssl-errors=yes"     // An extra layer of SSL bypass for Chromium
        );
        options.setAcceptInsecureCerts(true); // Crucial: Proxies break standard SSL chains!

        // Initialize the driver with the proxy options
        // use an XLT driver to set everything up like the measurement plugin
        XltChromeDriver xltDriver = setupWebDriver(options);

        // Add the listener to rotate spans before key actions
        AutoTraceListener listener = new AutoTraceListener();
        WebDriver decoratedDriver = new EventFiringDecorator<WebDriver>(listener).decorate(xltDriver);

        WebDriverRunner.setWebDriver(decoratedDriver, selenideProxy);

        // Clean up the proxy server after the test
        return new TracedSession(decoratedDriver, selenideProxy, null);
    }

    /**
     * Use Selenium 4's NetworkInterceptor. NOTE: The browser MUST be open before calling this, so the WebDriver exists. ATTENTION: Known issue with streaming
     * responses (e.g. Server-Sent Events) - the interceptor will block waiting for the stream to close, which may cause timeouts in tests that rely on
     * streaming data. Use the proxy method if your tests interact with streaming endpoints. So not recommended.
     *
     * @return NetworkInterceptor (you must close this at the end of the test)
     */
    public static TracedSession setupWithInterceptor(boolean useW3c, boolean useDatadog, String targetDomain)
    {
        System.out.println("================================================");
        System.out.println("Trace Injection: Using Selenium NetworkInterceptor");

        // Get the underlying XLT WebDriver and wrap it with our listener manually
        // This ensures events like 'beforeGet' are intercepted regardless of how Selenide manages drivers
        // Configure the Browser Options
        ChromeOptions options = getChromeOptions();

        // Initialize the driver
        // use an XLT driver to set everything up like the measurement plugin
        XltChromeDriver xltDriver = setupWebDriver(options);

        AutoTraceListener listener = new AutoTraceListener();
        WebDriver decoratedDriver = new EventFiringDecorator<WebDriver>(listener).decorate(xltDriver);

        WebDriverRunner.setWebDriver(decoratedDriver);

        // Ensure browser is "ready" by opening blank page, allowing us to attach interceptor safely
        open("about:blank");

        // Capture the current trace state so it can be accessed by the interceptor thread
        TraceContext.TraceState state = TraceContext.getCurrentState();

        Filter traceFilter = next -> req -> {
            // Ignore EventStream requests to prevent TimeoutException (waiting for stream close)
            String accept = req.getHeader("Accept");
            String contentType = req.getHeader("Content-Type");

            if ((accept != null && (accept.contains("text/event-stream") || accept.contains("application/connect+json"))) ||
                (contentType != null && contentType.contains("application/connect+json")))
            {
                return new HttpResponse()
                    .setStatus(200)
                    .setContent(Contents.utf8String(""));
            }

            if (req.getUri().contains(targetDomain))
            {
                Map<String, String> headers = getHeadersToInject(state, useW3c, useDatadog);
                headers.forEach((k, v) -> req.addHeader(k, v));
            }
            return next.execute(req);
        };

        networkInterceptor = new NetworkInterceptor(decoratedDriver, traceFilter);

        return new TracedSession(decoratedDriver, null, networkInterceptor);
    }

    public static TracedSession setupWithDevTools(boolean useW3c, boolean useDatadog, String targetDomain)
    {
        System.out.println("================================================");
        System.out.println("Trace Injection: Using Selenium DevTools Protocol (Stream Safe)");

        // Configure the Browser Options
        ChromeOptions options = getChromeOptions();

        // Initialize the driver
        // use an XLT driver to set everything up like the measurement plugin
        XltChromeDriver xltDriver = setupWebDriver(options);

        // We will instantiate the listener depending on whether DevTools is available
        AutoTraceListener listener;

        DevTools devTools = xltDriver.getDevTools();
        devTools.createSessionIfThereIsNotOne();

        // Enable the Network domain (Required for setExtraHTTPHeaders to take effect)
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));

        // 1. Define the updater logic as a reusable Runnable
        Runnable updateDevToolsHeaders = () -> {
            // Grab the FRESHEST state
            TraceContext.TraceState currentState = TraceContext.getCurrentState();

            // Generate the headers and cast safely to Map<String, Object> for CDP
            Map<String, Object> cdpHeaders = new HashMap<>(getHeadersToInject(currentState, useW3c, useDatadog));

            // Push the new headers to Chromium's internal cache
            devTools.send(Network.setExtraHTTPHeaders(new Headers(cdpHeaders)));
        };

        // 2. Call it immediately to set the initial page load headers
        updateDevToolsHeaders.run();

        // 3. Pass the updater to the listener so it fires on every click/navigation!
        listener = new AutoTraceListener(updateDevToolsHeaders);

        System.out.println("✅ DevTools INTERCEPT: Active. Headers will auto-update on UI actions.");

        // Wrap the XLT driver with our listener
        WebDriver decoratedDriver = new EventFiringDecorator<WebDriver>(listener).decorate(xltDriver);

        // Give the decorated driver back to Selenide
        WebDriverRunner.setWebDriver(decoratedDriver);

        // Custom cleanup task to wipe the headers
        AutoCloseable devToolsCleanup = () -> {
            try
            {
                devTools.send(Network.setExtraHTTPHeaders(new Headers(new java.util.HashMap<>())));
            }
            catch (Exception e)
            {
                // DevTools might already be disconnected
            }
        };

        return new TracedSession(decoratedDriver, null, devToolsCleanup);
    }

    /**
     * set the necessary ChromeOptions for pages with certificate errors.
     *
     * @return
     */
    private static @NonNull ChromeOptions getChromeOptions()
    {
        ChromeOptions options = new ChromeOptions();
        //options.setBinary("/usr/bin/chromium"); // set the chromium binary path - TODO use config value for this
        options.addArguments(
            "--ignore-certificate-errors",
            "--remote-allow-origins=*",
            "--allow-insecure-localhost", // Helpful if testing against localhost
            "--ignore-ssl-errors=yes"     // An extra layer of SSL bypass for Chromium
        );
        options.setAcceptInsecureCerts(true); // Crucial: Proxies break standard SSL chains!
        return options;
    }
}

// TODO add headless option from the property to the XltChromeDriver constructor
// TODO add URL filter back?
