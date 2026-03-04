package company.util;

import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.startAction;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.stopAction;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;

import com.codeborne.selenide.AuthenticationType;
import com.codeborne.selenide.BasicAuthCredentials;
import com.codeborne.selenide.Selenide;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.webdriver.WebDriverScriptCommands;

import company.pages.HomePage;

public class OpenPageFlow
{
    /**
     * Opens the home page via the configured start URL.
     *
     * @return the home page
     */
    public static HomePage openHomePage()
    {
        startAction("Open Homepage");

        String username = XltProperties.getInstance().getProperty("com.xceptance.xlt.auth.userName");
        String password = XltProperties.getInstance().getProperty("com.xceptance.xlt.auth.password");

        //WebDriver driver = ((WebDriverScriptCommands) TestContext.getCurrent().getAdapter()).getUnderlyingWebDriver();

        if (StringUtils.isNoneBlank(username, password))
        {
            Selenide.open(TestdataHelper.getStartUrl(), AuthenticationType.BASIC, new BasicAuthCredentials(username, password));
        }
        else
        {
            //driver.get(TestdataHelper.getStartUrl());
            Selenide.open(TestdataHelper.getStartUrl());
        }

        stopAction();

        return new HomePage();
    }
}
