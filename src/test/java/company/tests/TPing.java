package company.tests;

import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;

import company.util.LoadUrlLightWeightAction;
import company.util.TestdataHelper;
import company.util.WarmUpFlow;

/**
 * After warm up runs a simple interaction that is not using a full browser to avoid that piece of the stack. </br>
 * This test will only pull the initial HTML from the server, to see is the server is healthy and responding as fast as
 * expected.
 */
public class TPing extends AbstractTestCase
{

    @Test
    public void test() throws Throwable
    {
        // run some warmup
        WarmUpFlow.headlessWarmup();

        new LoadUrlLightWeightAction("Ping", TestdataHelper.getStartUrl()).run();

        new LoadUrlLightWeightAction("Ping", TestdataHelper.getStartUrl()).run();
    }

    /**
     * Since the Ping a is an extremely basic test, checking for the server speed without additional influences, the
     * following configs should be added for proper work of the scenario.</br>
     * com.xceptance.xlt.cssEnabled = false</br>
     * com.xceptance.xlt.css.download.images = never</br>
     * com.xceptance.xlt.javaScriptEnabled = false</br>
     * com.xceptance.xlt.socket.collectNetworkData = false</br>
     * com.xceptance.xlt.loadStaticContent = false
     */
    @Before
    public void setUpConfig()
    {
        XltProperties.getInstance().setProperty("com.xceptance.xlt.cssEnabled", "false");
        XltProperties.getInstance().setProperty("com.xceptance.xlt.css.download.images", "never");
        XltProperties.getInstance().setProperty("com.xceptance.xlt.javaScriptEnabled", "false");
        XltProperties.getInstance().setProperty("com.xceptance.xlt.socket.collectNetworkData", "false");
        XltProperties.getInstance().setProperty("com.xceptance.xlt.loadStaticContent", "false");
    }
}
