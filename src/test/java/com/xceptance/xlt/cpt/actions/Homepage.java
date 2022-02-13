package com.xceptance.xlt.cpt.actions;

import org.junit.Assert;

import com.xceptance.xlt.api.util.Action;
import com.xceptance.xlt.api.util.Commands;
import com.xceptance.xlt.api.util.ContextAwareUrl;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * @author rschwietzke
 *
 */
public class Homepage
{
    public static void first()
    {
        // Capture the CDN data first in case of problems. This ruins our connect test in the browser because we build a connection up already
        Action.run("CDN Data", () ->
        {
            Commands.open(XltProperties.getInstance().getProperty("cdnTestUrl"));
        });

        // ok, let's open the homepage, this is without tracking
        Action.run("Homepage", () ->
        {
            // the r=1 prevents location based redirects
            Commands.open(ContextAwareUrl.homepageByTestLocation());

            // make sure the page came up just fine and is fully loaded with its async JS
            // if the privacy stuff is missing, we will already fail here
            Commands.waitForVisible("css=#js-data-privacy-save-button");

            // just make sure we have not been redirected
            Assert.assertTrue("Url redirect happened", Commands.getWebDriver().getCurrentUrl().equals(ContextAwareUrl.homepageByTestLocation()));

            // now make sure, this is a valid and consistent homepage

            // cart is empty
            Commands.assertElementPresent("css=.xlt-miniCart[data-numberofitemsincart='0']");

            // we can trigger the search
            Commands.assertVisible("css=button.headerTopBar__iconTrigger--search");

            // we got a menu
            Commands.assertVisible("css=.headerNav__list.headerNav__list--level-1");

            // footer and one or two pieces of it to avoid jumping on any footer
            // make sure the id is within the footer
            Commands.assertElementPresent("css=footer.xlt-footer form.newsletterSignup");
            Commands.assertElementPresent("css=footer.xlt-footer .footerRegionSelector");

        });
    }

    public static void confirmPrivacy()
    {
        Action.run("Confirm Privacy", () ->
        {
            Commands.click("css=#js-data-privacy-save-button");
            Commands.waitForNotVisible("css=#js-data-privacy-save-button");
        });
    }

    public static void cancelNewsletter()
    {
        Action.run("Cancel Newsletter", () ->
        {
            Commands.click("css=.modal__close");
            Commands.waitForNotVisible("css=.modal__container");
        });
    }

    public static void signUpForNewsletter(final String email)
    {
        Action.run("Sign up for Newsletter", () ->
        {
            // this should not be necessary :-(
            Commands.scrollIntoView("css=.newsletterSignup", -80, true);

            // enter email and submit
            Commands.type("css=.newsletterSignup__input", email);
            Commands.click("css=.newsletterSignup__submit");

            // validate confirmation text
            final String locKey = "loc.newsletter.confirmation." + ContextAwareUrl.localeByTestLocation();
            final String locText = XltProperties.getInstance().getProperty(locKey, "TEXT NOT FOUND");

            Commands.waitForText("css=.newsletterSignup__confirmation", locText);
        });
    }

    public static void reload(final String name)
    {
        Action.run(name, () ->
        {
            Commands.clickAndWait("css=.header a.headerTopBar__logo");
        });
    }
}
