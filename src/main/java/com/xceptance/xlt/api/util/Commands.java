package com.xceptance.xlt.api.util;

import java.util.Set;
import java.util.function.Function;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.xceptance.xlt.api.engine.scripting.StaticScriptCommands;

public class Commands extends StaticScriptCommands
{
    // all from StaticScriptCommands is here

    /**
     * Get us an element nicely aligned for clicking
     *
     * @param locator
     *            which element to deal with
     * @return the locator for fluid magic
     */
    public static String scrollIntoView(final String locator)
    {
        final WebElement element = findElement(locator);

        ((JavascriptExecutor) getWebDriver()).executeScript("arguments[0].scrollIntoView(true);", element);

        try
        {
            // time for scrolling
            Thread.sleep(250);

            return locator;
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get us an element nicely aligned for clicking with an offset to account for sticky headers
     *
     * @param locator
     *            which element to deal with
     * @param offset
     *            offset in pixel to correct for headers and other things, can be negative
     * @param alignToTop
     *            when true, align with the top of the element, false for the bottom
     * @return the locator for fluid magic
     */
    public static String scrollIntoView(final String locator, final int offset, final boolean alignToTop)
    {
        final WebElement element = findElement(locator);

        final String scrollingCode = "var node = arguments[0];" +
                        "var headerHeight = arguments[2];" +
                        "node.scrollIntoView(arguments[1]);" +
                        "var scrolledY = window.scrollY;" +
                        "if( scrolledY) {" +
                        "  window.scroll(0, scrolledY + headerHeight);" +
                        "}";

        ((JavascriptExecutor) getWebDriver()).executeScript(scrollingCode, element, alignToTop, offset);

        try
        {
            // time for scrolling
            Thread.sleep(250);

            return locator;
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close all handles except the one passed
     *
     * @param handleToKeep
     *            the window handle to keep
     */
    public static void closeAllWindowsBut(final String handleToKeep)
    {
        // close the tab that is not that passed handle
        Commands.getWebDriver().getWindowHandles().stream()
                        .filter(h -> h.equals(handleToKeep) == false)
                        .forEach(h ->
                        {
                            Commands.getWebDriver().switchTo().window(h);
                            Commands.getWebDriver().close();
                        });

        // switch to the only remaining one
        switchToWindow(handleToKeep);
    }

    /**
     * Close this window
     *
     * @param handle
     *            the window handle to close
     */
    public static void closeWindow(final String handle)
    {
        // get me the current
        final String currentHandle = switchToWindow(handle);

        Commands.getWebDriver().close();

        // switch back if this was not our starter window
        if (!currentHandle.equals(handle))
        {
            Commands.getWebDriver().switchTo().window(currentHandle);
        }
    }

    /**
     * Go to that window handle
     *
     * @param targetHandle
     *            the window handle to switch to
     * @param the
     *            previous handle that was active
     */
    public static String switchToWindow(final String targetHandle)
    {
        final String old = Commands.getWebDriver().getWindowHandle();
        Commands.getWebDriver().switchTo().window(targetHandle);
        return old;
    }

    /**
     * Wait for the page load and returns to where we came from
     */
    public static void waitForPageLoad()
    {
        waitForPageLoad(Commands.getWebDriver().getWindowHandle());
    }

    /**
     * Switches to target handle if needed, wait for the page load and returns to where we came from
     *
     * @param targetHandle
     *            the handle of the window to wait for load
     */
    public static void waitForPageLoad(final String targetHandle)
    {
        final String oldHandle = switchToWindow(targetHandle);

        final Wait<WebDriver> wait = new WebDriverWait(getWebDriver(), 30);

        wait.until(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(final WebDriver driver)
            {
                return String
                                .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                                .equals("complete");
            }
        });

        switchToWindow(oldHandle);
    }

    /**
     * Returns the handle that we just got new
     *
     * @param knownHandles
     *            all previously known handles
     * @return the new handle
     */
    public static String getNewWindowHandle(final Set<String> knownHandles)
    {
        // get current handles and compare with set, return the one new
        return getWebDriver().getWindowHandles().stream()
                        .filter(h -> !knownHandles.contains(h))
                        .findFirst().get();
    }
}
