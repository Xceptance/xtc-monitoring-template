package company.scenario.browsing.oteldemo;

import com.codeborne.selenide.WebDriverRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

import company.util.trace.AutoTraceListener;
import company.util.trace.TraceContext;
import company.util.trace.TraceSetup;

public class OtelDemoTracingTest {

    private AutoCloseable traceCleaner;

    @BeforeClass
    public static void globalSetup() {
        WebDriverRunner.addListener(new AutoTraceListener()); // Auto-rotates spans
    }

    @Before
    public void setupTest() {
        TraceContext.startNewTestTrace();
        open("about:blank");

        // 1. Enable ONLY W3C tracing (true, false)
        // 2. Target "localhost" so it intercepts the OTel demo traffic
        // traceCleaner = TraceSetup.setup(true, false, "localhost");
    }

    @After
    public void teardownTest() {
        TraceContext.clear();
        if (traceCleaner != null) {
            try {
                traceCleaner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testAstronomyShopAddCart() {
        // Extract just the 32-character Trace ID from the W3C traceparent to print it
        String traceparent = TraceContext.getW3cTraceparent();
        String traceId = traceparent.split("-")[1];

        System.out.println("================================================");
        System.out.println("🔍 OPEN JAEGER: http://localhost:8080/jaeger/ui/");
        System.out.println("📋 SEARCH FOR TRACE ID: " + traceId);
        System.out.println("================================================");

        // 1. Open the shop (Listener generates Span 1)
        open("http://localhost:8080");

        // Wait for products to load
        $("[data-cy='product-card']").shouldBe(visible);

        // 2. Click on the first product (Listener generates Span 2)
        $$("[data-cy='product-card']").first().click();

        // 3. Add to cart (Listener generates Span 3)
        $("button[data-cy='product-add-to-cart']").shouldBe(visible).click();

        // Let the network requests finish before closing the browser
        sleep(2000);
    }
}
