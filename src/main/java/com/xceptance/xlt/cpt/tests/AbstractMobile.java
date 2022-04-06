package com.xceptance.xlt.cpt.tests;

import java.util.HashMap;

import org.junit.After;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltChromeDriver;

/**
 * TODO: Add class description
 */
public abstract class AbstractMobile extends AbstractTestCase
{
    static ChromeOptions chromeOpts;
    static
    {
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,
                        XltProperties.getInstance().getProperty("xlt.webDriver.chrome_clientperformance.pathToDriverServer"));

        final HashMap<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "Pixel 2");

        chromeOpts = new ChromeOptions();
        chromeOpts.setExperimentalOption("mobileEmulation", mobileEmulation);
    }

    public AbstractMobile()
    {
        super(new XltChromeDriver(chromeOpts));
    }

    @After
    public void closeDriver()
    {
        getWebDriver().quit();
    }
}