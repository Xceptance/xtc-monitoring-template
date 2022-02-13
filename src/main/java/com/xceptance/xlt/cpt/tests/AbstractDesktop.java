package com.xceptance.xlt.cpt.tests;

import org.junit.After;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltChromeDriver;

/**
 * TODO: Add class description
 */
public abstract class AbstractDesktop extends AbstractTestCase
{
    private static DesiredCapabilities capabilities;
    static
    {
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,
                        XltProperties.getInstance().getProperty("xlt.webDriver.chrome_clientperformance.pathToDriverServer"));

        final ChromeOptions chromeOpts = new ChromeOptions();
        // chromeOpts.addArguments("user-agent="
        // + XltProperties.getInstance().getProperty("xlt.webDriver.chrome_clientperformance.userAgent.desktop"));

        capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOpts);
    }

    @SuppressWarnings("deprecation")
    public AbstractDesktop()
    {
        super(new XltChromeDriver(capabilities,
                        XltProperties.getInstance().getProperty("xlt.webDriver.chrome_clientperformance.screenless", true)));
        final WebDriver c = getWebDriver();
        c.manage().window().setSize(new Dimension(1400, 1000));
    }

    @After
    public void closeDriver()
    {
        getWebDriver().quit();
    }

}