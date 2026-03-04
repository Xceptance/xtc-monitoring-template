package company.scenario.browsing.oteldemo;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;

import org.junit.Test;

import company.scenario.browsing.AbstractBrowserScenario;
import company.util.trace.TraceContext;

public class W3cTracingTest extends AbstractBrowserScenario
{
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
}
