package company.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import java.time.Duration;

import com.xceptance.xlt.engine.scripting.TestContext;

public class SearchResultsPage extends AbstractCatalogPage
{
    /**
     * Validates this page.
     */
    @Override
    public void validate()
    {
        super.validate();
        $("#title-search-text").shouldBe(visible, Duration.ofMillis(TestContext.getCurrent().getTimeout()));
    }

    /**
     * Checks if page has at least minResultsCount results
     * 
     * @param minResultsCount
     *            minimal result count
     * @return boolean
     */
    public boolean hasMinResults(String minResultsCount)
    {
        return $$(".product-tile").size() >= Integer.parseInt(minResultsCount);
    }
}
