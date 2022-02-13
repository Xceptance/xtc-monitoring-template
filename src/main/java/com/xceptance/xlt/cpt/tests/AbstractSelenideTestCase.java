package com.xceptance.xlt.cpt.tests;

import org.junit.Before;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.tests.AbstractWebDriverTestCase;
import com.xceptance.xlt.api.util.SelenideUtil;

public abstract class AbstractSelenideTestCase extends AbstractWebDriverTestCase
{
    /**
     * Initializes Selenide.
     */
    @Before
    public void setup()
    {
        // Setup Selenide
        SelenideUtil.initializeSelenide(getWebDriver(), Session.getCurrent());
    }
}
