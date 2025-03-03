package company.util;

import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.open;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.startAction;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.stopAction;

import org.apache.commons.lang3.StringUtils;

import com.codeborne.selenide.AuthenticationType;
import com.codeborne.selenide.BasicAuthCredentials;
import com.codeborne.selenide.Selenide;
import com.xceptance.xlt.api.util.XltProperties;

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

        if (StringUtils.isNoneBlank(username, password))
        {
            Selenide.open(TestdataHelper.getStartUrl(), AuthenticationType.BASIC, new BasicAuthCredentials(username, password));
        }
        else
        {
            open(TestdataHelper.getStartUrl());
        }

        stopAction();

        return new HomePage();
    }
}
