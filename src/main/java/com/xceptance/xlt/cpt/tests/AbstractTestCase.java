package com.xceptance.xlt.cpt.tests;

import org.openqa.selenium.WebDriver;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

/**
 * TODO: Add class description
 */
public abstract class AbstractTestCase extends AbstractWebDriverScriptTestCase
{
    public AbstractTestCase(final WebDriver driver)
    {
        super(driver);
    }
}