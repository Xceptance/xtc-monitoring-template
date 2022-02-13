package com.xceptance.xlt.cpt.tests;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.xlt.api.actions.AbstractLightWeightPageAction;
import com.xceptance.xlt.api.actions.AbstractWebAction;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.ContextAwareUrl;

/**
 * Runs a simple interaction that is not using a full browser to avoid that piece of the stack
 */
public class TPing extends AbstractTestCase
{
    /**
     * The "ping" action.
     */
    private static class Ping extends AbstractLightWeightPageAction
    {
        private final String url;
        private final boolean contentTypeValidation;

        protected Ping(final String url)
        {
            super(null, "PingStart");
            this.url = url;
            this.contentTypeValidation = true;
        }

        protected Ping(final String timerName, final String url, final boolean validateContentType)
        {
            super(null, timerName);
            this.url = url;
            this.contentTypeValidation = validateContentType;
        }

        protected Ping(final AbstractWebAction previous, final String url)
        {
            super(previous, "Ping");
            this.url = url;
            this.contentTypeValidation = true;
        }

        protected Ping(final AbstractWebAction previous, final String timerName, final String url, final boolean validateContentType)
        {
            super(previous, timerName);
            this.url = url;
            this.contentTypeValidation = validateContentType;
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        @Override
        protected void execute() throws Exception
        {
            loadPage(url);
        }

        @Override
        protected void postValidate() throws Exception
        {
            final LightWeightPage page = getLightWeightPage();

            // basic checks
            final WebResponse webResponse = page.getWebResponse();
            Assert.assertEquals("Unexpected status code:", 200, webResponse.getStatusCode());

            if (this.contentTypeValidation)
            {
                Assert.assertEquals("Unexpected content type:", "text/html", webResponse.getContentType());
            }
        }
    }

    /**
     * The "ping" action.
     */
    private static class HiddenWarmup extends AbstractLightWeightPageAction
    {
        protected HiddenWarmup()
        {
            super(null, "HiddenWarmupStart");
        }

        protected HiddenWarmup(final AbstractWebAction previous)
        {
            super(previous, "HiddenWarmpup");
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        @Override
        protected void execute() throws Exception
        {
            loadPage("https://www.google.com/");
        }

        @Override
        protected void postValidate() throws Exception
        {
        }
    }

    /**
     * Just some warm up code that ensure we measure right but don't break things
     *
     * @throws Throwable
     */
    private void warmup()
    {
        // I am just here to get the VM warm
        try
        {
            Session.getCurrent().getDataManager().setLoggingEnabled(false);

            // initial
            HiddenWarmup hw = new HiddenWarmup();
            hw.run();

            // warm up
            for (int i = 0; i < 3; i++)
            {
                hw = new HiddenWarmup(hw);
                hw.run();
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
        }
    }

    /**
     * Runs the test case.
     *
     * @throws Throwable
     */
    @Test
    public void test() throws Throwable
    {
        // run some warmup
        warmup();

        final Ping ping = new Ping(ContextAwareUrl.homepageByTestLocation());
        ping.run();

        new Ping(ping, ContextAwareUrl.homepageByTestLocation()).run();
    }
}