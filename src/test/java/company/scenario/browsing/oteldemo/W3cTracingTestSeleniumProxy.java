package company.scenario.browsing.oteldemo;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.startAction;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.stopAction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.events.EventFiringDecorator;

import com.codeborne.selenide.Config;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.proxy.SelenideProxyServer;

import company.util.trace.AutoTraceListener;
import company.util.trace.TraceContext;

public class W3cTracingTestSeleniumProxy
{
    private WebDriver driver;

    @Before
    public void setup()
    {
        TraceContext.startNewTestTrace();

        // This starts the proxy and binds it to Chrome
        driver = createProxyTracedDriver("localhost");
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
    public void testAstronomyShopAddCart()
    {
        // Extract just the 32-character Trace ID from the W3C traceparent to print it
        String traceparent = TraceContext.getW3cTraceparent();
        String traceId = traceparent.split("-")[1];

        System.out.println("================================================");
        System.out.println("🔍 OPEN JAEGER: http://localhost:8080/jaeger/ui/");
        System.out.println("📋 SEARCH FOR TRACE ID: " + traceId);
        System.out.println("================================================");

        // 1. Open the shop (Listener generates Span 1)
        startAction("Open shop homepage");
        open("http://localhost:8080");

        // Wait for products to load
        $("[data-cy='product-card']").shouldBe(visible);
        stopAction();

        startAction("Click on first product");
        // 2. Click on the first product (Listener generates Span 2)
        $$("[data-cy='product-card']").first().click();
        stopAction();

        startAction("Add product to cart");
        // 3. Add to cart (Listener generates Span 3)
        $("button[data-cy='product-add-to-cart']").shouldBe(visible).click();
        stopAction();

        // Let the network requests finish before closing the browser
        sleep(2000);
    }

    public static WebDriver createProxyTracedDriver(String targetDomain)
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
}
