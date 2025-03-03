package company.tests;

import org.junit.Test;

import company.pages.HomePage;
import company.util.OpenPageFlow;
import company.util.WarmUpFlow;

/**
 * Simple test of the start page. Open home page ('xlt.startUrl' with localizations from
 * {@code TestdataHelper#getStartUrl()}) after browser warm up. Does a small check that expected page is open and
 * measures the performance
 */
public class THomepage extends AbstractBrowserTest
{
    @Test
    public void test()
    {
        WarmUpFlow.warmup();

        // just open the home page and check it's okay
        HomePage homePage = OpenPageFlow.openHomePage();
        homePage.validate();
    }
}
