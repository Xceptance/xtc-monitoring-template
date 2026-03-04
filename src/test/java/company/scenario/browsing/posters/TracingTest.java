package company.scenario.browsing.posters;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.startAction;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.stopAction;

import org.junit.Test;

import com.codeborne.selenide.Selenide;

import company.scenario.browsing.AbstractBrowserScenarioWithTrace;
import company.util.WarmUpFlow;
import company.util.trace.TraceContext;

public class TracingTest extends AbstractBrowserScenarioWithTrace
{
    @Test
    public void testHeaders()
    {
        // standard warmup and first access of the homepage
        WarmUpFlow.warmup();

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
}
