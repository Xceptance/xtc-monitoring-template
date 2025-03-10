package company.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.startAction;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.stopAction;

import java.time.Duration;

import com.xceptance.xlt.engine.scripting.TestContext;

/**
 * Interactions and validations available on all pages in the catalog part of the shop.
 */
public class AbstractCatalogPage
{
    private final String brandSelector = "#header-brand-logo";

    private final String searchFieldSelector = "#header-search-text";

    private final String searchButtonSelector = "#header-search-button";

    /**
     * Validates this page.
     */
    public void validate()
    {
        $(brandSelector).shouldBe(visible, Duration.ofMillis(TestContext.getCurrent().getTimeout()));
    }

    /**
     * Search for specific term
     */
    public SearchResultsPage searchFor(String searchTerm)
    {

        startAction("Search for a term");
        // validate search input field is visible (typing produces no error if input field is hidden)
        $(searchFieldSelector).shouldBe(visible);

        // enter the phrase
        $(searchFieldSelector).type(searchTerm);

        // validate search button is visible (click produces no error if element to click is hidden)
        $(searchButtonSelector).shouldBe(visible);

        // click the link to search, this is a page load
        $(searchButtonSelector).click();
        stopAction();
        return new SearchResultsPage();
    }
}
