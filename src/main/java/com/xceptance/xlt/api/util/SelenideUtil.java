package com.xceptance.xlt.api.util;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;

import org.openqa.selenium.WebDriver;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.xceptance.xlt.api.engine.Session;

public class SelenideUtil
{
    /**
     * Initializes Selenide.
     *
     * @param configuration
     *            The configuration object of the (current) Context.
     * @param webDriver
     *            The WebDriver to use.
     * @param session
     *            The XLT session to use.
     */
    public static void initializeSelenide(final WebDriver webDriver, final Session session)
    {
        // Record screenshots if a test fails
        com.codeborne.selenide.Configuration.screenshots = false;

        // Fetch XLT result directory of this session
        final String resultDirectory = ((com.xceptance.xlt.engine.SessionImpl) session).getResultsDirectory().toString();

        // Location of the directory where the screenshots/reports from Selenide will be saved
        com.codeborne.selenide.Configuration.reportsFolder = resultDirectory + "/output/" + session.getID();

        // // How to load the page after event was fired
        // com.codeborne.selenide.Configuration.pageLoadStrategy = Context.get().configuration.selenidePageLoadStrategy();
        //
        // // In case of an error save the page source
        // com.codeborne.selenide.Configuration.savePageSource = Context.get().configuration.selenideSavePageSource();
        //
        // // Tests will fail after X milliseconds if validation does not match
        // com.codeborne.selenide.Configuration.timeout = Context.get().configuration.selenideTimeout();

        // Activate the XLT webdriver to log requests
        com.codeborne.selenide.WebDriverRunner.setWebDriver(webDriver);
    }

    /**
     * Scrolls to the top of the current page and accounts for the provided offset.
     *
     * The top offset can be used to deal with sticky headers etc.
     *
     * @param topOffset
     *            The top offset to account for in the scrolling position
     */
    public static void scrollToTopOfPage(final int topOffset)
    {
        Selenide.executeJavaScript("document.documentElement.scrollTop = arguments[0];", topOffset);
        Selenide.sleep(300);
    }

    /**
     * Scrolls to the top of the current age and accounts for the height of the provided header element.
     *
     * @param fixedHeader
     *            The header element's size will be accounted for in the scrolling position
     */
    public static void scrollToTopOfPage(final SelenideElement fixedHeader)
    {
        scrollToTopOfPage(fixedHeader.getRect().getHeight());
    }

    /**
     * Scrolls element into view.
     *
     * Adjust the scrolling position by an offset to e.g. account for sticky headers ro similar.
     *
     * @param elementToScrollIntoView
     *            The element to scroll into view
     * @param alignToTopOfElement
     *            True the scrolling position should align with the top of the element or in case of false with the bottom
     * @param topOffset
     *            An offset onto the scrolling position to account for fixed header elements
     */
    public static void scrollElementIntoView(final SelenideElement elementToScrollIntoView, final boolean alignToTopOfElement, final int topOffset)
    {
        final String scrollingCode = "var node = arguments[0];" +
                        "var headerHeight = arguments[2];" +
                        "node.scrollIntoView(arguments[1]);" +
                        "var scrolledY = window.scrollY;" +
                        "if(scrolledY) {" +
                        "  window.scroll(0, scrolledY - headerHeight);" +
                        "}";

        Selenide.executeJavaScript(scrollingCode, elementToScrollIntoView.should(exist).toWebElement(), alignToTopOfElement, topOffset);
        Selenide.sleep(300);
    }

    /**
     * Scrolls element into view.
     *
     * Considers the height of the given fixed header element in the scrolling position.
     *
     * @param elementToScrollIntoView
     *            The element to scroll into view
     * @param alignToTopOfElement
     *            True the scrolling position should align with the top of the element or in case of false with the bottom
     * @param fixedHeader
     *            The fixed header element which height is considered in the scrolling position
     */
    public static void scrollElementIntoView(final SelenideElement elementToScrollIntoView, final boolean alignToTopOfElement, final SelenideElement fixedHeader)
    {
        scrollElementIntoView(elementToScrollIntoView, alignToTopOfElement, fixedHeader.getRect().getHeight());
    }

    /**
     * Validates if the given element is clickable.
     *
     * Will check for existence, then scroll the element into view and later check for visibility. Scrolls to the top of the element.
     *
     * @param element
     *            The element to validate
     * @param topOffset
     *            An offset to the top of the page, to e.g. account for sticky headers
     * @return The element that was validated
     */
    public static SelenideElement validateClickable(final SelenideElement element, final int topOffset)
    {
        element.should(exist);

        scrollElementIntoView(element, true, topOffset);

        return element.shouldBe(visible);
    }

    /**
     * Validates if the given element is clickable.
     *
     * Will check for existence, then scroll the element into view including accounting for the height of the given fixed header and later check for visibility.
     *
     * @param element
     *            The element to validate
     * @param fixedHeader
     *            The fixed header element which height is accounted for during scrolling
     * @return The element that was validated
     */
    public static SelenideElement validateClickable(final SelenideElement element, final SelenideElement fixedHeader)
    {
        return validateClickable(element, fixedHeader.getRect().getHeight());
    }

    /**
     * Validates if the given element is clickable.
     *
     * Will check for existence, then scroll the element into view account for the global sticky header component and check for visibility.
     *
     * @param element
     *            The element to validate
     * @return The element that was validated
     */
    public static SelenideElement validateClickable(final SelenideElement element)
    {
        return validateClickable(element, 0);
    }

    /**
     * Validates if the element matching the given CSS locator is clickable.
     *
     * Will check for existence, then scroll the element into view account for the global sticky header component and check for visibility.
     *
     * @param cssLocator
     *            The CSS locator of the element to validate
     * @return The element that was validated
     */
    public static SelenideElement validateClickable(final String cssLocator)
    {
        return validateClickable($(cssLocator));
    }

    /**
     * Randomly chooses an element from the given element collection.
     *
     * Validates if the collection has at least the given size.
     *
     * @param collection
     *            The elements collection to chose from
     * @param validationSizeGreaterOrEqual
     *            The minimal expected size of the collection
     * @return The chosen element
     */
    public static SelenideElement chooseRandomly(final ElementsCollection collection, final int validationSizeGreaterOrEqual)
    {
        return collection.shouldHave(CollectionCondition.sizeGreaterThanOrEqual(validationSizeGreaterOrEqual))
                        .get(XltRandom.nextInt(0, collection.size() - 1));
    }

    /**
     * Randomly chooses an element from the given element collection.
     *
     * Validates if the collection has at least the given size and provides the option to ignore the first element.
     *
     * @param collection
     *            The elements collection to chose from
     * @param validationSizeGreaterOrEqual
     *            The minimal expected size of the collection
     * @param ignoreFirst
     *            If true the first element of the collection will be ignored
     * @return The chosen element
     */
    public static SelenideElement chooseRandomly(final ElementsCollection collection, final int validationSizeGreaterOrEqual, final boolean ignoreFirst)
    {
        return collection
                        .shouldHave(CollectionCondition.sizeGreaterThanOrEqual(((validationSizeGreaterOrEqual == 1) && ignoreFirst) ? 2 : validationSizeGreaterOrEqual))
                        .get(XltRandom.nextInt(ignoreFirst ? 1 : 0, collection.size() - 1));
    }

    /**
     * Randomly chooses an element from the given element collection.
     *
     * Chooses between the given first and last element indices. Validates the size of the elements collection via the given last element index.
     *
     * @param collection
     *            The elements collection to chose from
     * @param first
     *            The index of the first element to select
     * @param last
     *            The index of the last element to select
     * @return The chosen element
     */
    public static SelenideElement chooseRandomly(final ElementsCollection collection, final int first, final int last)
    {
        return collection.shouldHave(CollectionCondition.sizeGreaterThanOrEqual(last + 1))
                        .get(XltRandom.nextInt(first, last));
    }

    /**
     * Switches to an iframe by the given CSS locator.
     *
     * @param iframeCssLocator
     *            The CSS locator of the iframe
     */
    public static void enterIFrame(final String iframeCssLocator)
    {
        enterIFrame($(iframeCssLocator));
    }

    /**
     * Switches to the given iframe.
     *
     * @param iframe
     *            The iframe to switch to
     */
    public static void enterIFrame(final SelenideElement iframe)
    {
        iframe.should(exist);

        switchTo().frame(iframe.toWebElement());

        iframe.shouldNot(exist);
    }

    /**
     * Leaves an iframe. Does not do any validation.
     */
    public static void leaveIFrame()
    {
        switchTo().defaultContent();
    }

    /**
     * Leaves an iframe of the given CSS locator.
     *
     * @param iframeCssLocator
     *            The CSS locator
     */
    public static void leaveIFrame(final String iframeCssLocator)
    {
        leaveIFrame($(iframeCssLocator));
    }

    /**
     * Leaves the given iframe.
     *
     * @param iframe
     *            The iframe to leave
     */
    public static void leaveIFrame(final SelenideElement iframe)
    {
        iframe.shouldNot(exist);

        switchTo().defaultContent();

        iframe.should(exist);
    }
}
