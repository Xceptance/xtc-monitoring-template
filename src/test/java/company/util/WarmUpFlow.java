package company.util;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.webdriver.WebDriverScriptCommands;

import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.open;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.pause;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.startAction;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.stopAction;

public class WarmUpFlow
{
    /**
     * Warm up for browserless tests like {@code TPing}</br> Just some warm up code that ensure we measure right but don't break things
     */
    public static void headlessWarmup()
    {
        // I am just here to get the VM warm

        // get warmup start system time
        long startTime = System.currentTimeMillis();

        try
        {
            Session.getCurrent().getDataManager().setLoggingEnabled(false);

            // warm up
            for (int i = 0; i < 4; i++)
            {
                new LoadUrlLightWeightAction("HiddenWarmUp", getWarmupUrl()).run();
            }
        }
        catch (final Throwable e)
        {
            // no protest at all please
        }
        finally
        {
            // bring logging back
            Session.getCurrent().getDataManager().setLoggingEnabled(true);

            // get warmup end system time
            long endtime = System.currentTimeMillis();

            ActionData warmupAction = new ActionData("Warmup - Ping");
            warmupAction.setTime(startTime);
            warmupAction.setRunTime(endtime - startTime);

            // log the warmup time
            Session.getCurrent().getDataManager().logDataRecord(warmupAction);
        }
    }

    /**
     * Warm up for browser tests like {@code THomepage}</br> Just some warm up code that ensure we measure right but don't break things
     */
    public static void warmup()
    {
        // Make sure the browser is objective
        // do something else that is not related to the original job
        try
        {
            // don't log this
            Session.getCurrent().getDataManager().setLoggingEnabled(false);
            startAction("Warmup");
            open(getWarmupUrl());
            pause(2000);
            stopAction();
        }
        finally
        {
            // trigger a before-unload event causing the browser to send client-performance data now while we still have
            // data logging disabled
            ((WebDriverScriptCommands) TestContext.getCurrent().getAdapter()).getUnderlyingWebDriver().get("about:blank");

            // wait some time for client-performance data to arrive
            pause(2000);

            // bring back logging
            Session.getCurrent().getDataManager().setLoggingEnabled(true);
        }
    }

    private static String getWarmupUrl()
    {
        return XltProperties.getInstance().getProperty("xlt.warmup.url", "https://www.google.com/");
    }
}
