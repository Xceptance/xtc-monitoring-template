package com.xceptance.xlt.cpt.tests;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import java.time.Duration;

import org.junit.Assert;
import org.junit.Test;

import com.codeborne.selenide.WebDriverRunner;
import com.xceptance.xlt.api.util.Action;
import com.xceptance.xlt.api.util.ContextAwareUrl;

/**
 * This examples uses the Selenide infrastructure for monitoring test cases. See the parent class for more information. 
 * Just a simple test case to load the homepage and later on reload it via navigation to see the caching effects 
 * in the performance monitoring
 *
 * @author rschwietzke
 */
public class TSelenideHomepage extends AbstractSelenideTestCase
{
    @Test
    public void homepage() throws Throwable
    {
        // ok, let's open the homepage, this is without tracking
        Action.run("Homepage First", () ->
        {
            open(ContextAwareUrl.homepageByTestLocation());

            // make sure the page came up just fine and is fully loaded with its async JS
            // if the privacy stuff is missing, we will already fail here
            $("#brand .shopLogo").shouldBe(visible, Duration.ofSeconds(5));

            // just make sure we have not been redirected
            Assert.assertEquals("Url redirect happened",
                            WebDriverRunner.getWebDriver().getCurrentUrl(), ContextAwareUrl.homepageByTestLocation());

            // ensure that the cart is empty and we are not logged on
            $(".headerCartProductCount").shouldHave(text("0"));
            $(".goToLogout").shouldNot(exist);
        });

        // reload of the homepage to see the caching effects
        Action.run("Homepage Second", () ->
        {
            $(".titleShopText").click();

            // make sure the page came up just fine and is fully loaded with its async JS
            // if the privacy stuff is missing, we will already fail here
            $("#brand .shopLogo").shouldBe(visible, Duration.ofSeconds(5));

            // the url must have changed aka dropped the locale
            Assert.assertNotEquals("Url has not changed", WebDriverRunner.getWebDriver().getCurrentUrl(), ContextAwareUrl.homepageByTestLocation());

            // ensure that the cart is empty and we are not logged on
            $(".headerCartProductCount").shouldHave(text("0"));
            $(".goToLogout").shouldNot(exist);
        });
    }

}
