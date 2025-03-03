package company.tests;

import org.junit.Before;

import com.codeborne.selenide.WebDriverRunner;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.webdriver.WebDriverScriptCommands;

public abstract class AbstractBrowserTest extends AbstractWebDriverScriptTestCase
{
    @Before
    public void initSelenideDriver()
    {
        WebDriverRunner.setWebDriver(((WebDriverScriptCommands) TestContext.getCurrent()
                                                                           .getAdapter()).getUnderlyingWebDriver());
    }
}
