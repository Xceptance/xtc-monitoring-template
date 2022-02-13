package com.xceptance.xlt.cpt.tests;

import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.Action;
import com.xceptance.xlt.api.util.Commands;
import com.xceptance.xlt.api.util.ContextAwareUrl;

public class THomepage extends AbstractWebDriverScriptTestCase
{
    @Test
    public void homepage() throws Throwable
    {
        // open homepage
        Action.run("Homepage First", () ->
        {
            Commands.open(ContextAwareUrl.homepageByTestLocation());
            Commands.waitForVisible("css=#brand");
        });
    }
}
